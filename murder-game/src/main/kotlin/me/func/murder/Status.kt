package me.func.murder


enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(30, { it ->
        // Если набор игроков начался, обновить статус реалма
        if (it == 0) {
            val realm = ru.cristalix.core.realm.IRealmService.get().currentRealmInfo
            realm.status = ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_JOIN
            ru.cristalix.core.network.ISocketClient.get().write(
                ru.cristalix.core.network.packages.RealmUpdatePackage(
                    ru.cristalix.core.network.packages.RealmUpdatePackage.UpdateType.UPDATE,
                    realm
                )
            )
        }
        val players = org.bukkit.Bukkit.getOnlinePlayers()
        // Обновление шкалы онлайна
        players.forEach {
            me.func.murder.mod.ModTransfer()
                .integer(slots)
                .integer(players.size)
                .boolean(true)
                .send("update-online", app.getUser(it))
        }
        var actualTime = it

        // Если время вышло и пора играть
        if (it / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size + 1 < slots)
                actualTime = 1
            else {
                // Обновление статуса реалма, чтобы нельзя было войти
                val realm = ru.cristalix.core.realm.IRealmService.get().currentRealmInfo
                realm.status = ru.cristalix.core.realm.RealmStatus.GAME_STARTED_RESTRICTED
                ru.cristalix.core.network.ISocketClient.get().write(
                    ru.cristalix.core.network.packages.RealmUpdatePackage(
                        ru.cristalix.core.network.packages.RealmUpdatePackage.UpdateType.UPDATE,
                        realm
                    )
                )
                // Обнуление прошлого героя и добавления количества игр
                heroName = ""
                me.func.murder.games++
                // Телепортация игроком на игровые точки
                val places = app.worldMeta.getLabels("start")
                players.forEachIndexed { index, player ->
                    player.teleport(places[index])
                }
                // Список игроков Murder
                val users = players.map { app.getUser(it) }
                // Выдача активных ролей
                val accountService = ru.cristalix.core.account.IAccountService.get()
                val murder = users.maxByOrNull { it.stat.villagerStreak }!!
                murder.role = me.func.murder.user.Role.MURDER
                murder.stat.villagerStreak = 0
                murderName = accountService.getNameByUuid(murder.stat.id).get()
                val detective = users.minus(murder).maxByOrNull { it.stat.villagerStreak }!!
                detective.role = me.func.murder.user.Role.DETECTIVE
                detective.stat.villagerStreak = 0
                detectiveName = accountService.getNameByUuid(detective.stat.id).get()
                // Выдача мирных жителей
                users.forEach {
                    if (it.role != me.func.murder.user.Role.MURDER && it.role != me.func.murder.user.Role.DETECTIVE) {
                        it.role = me.func.murder.user.Role.VILLAGER
                        it.stat.villagerStreak++
                    }
                }
                // Создание списка игроков для замены скинов
                val playerList = org.bukkit.Bukkit.getBannedPlayers()
                    .map { (it as org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer).entityId }
                    .joinToString { it.toString() + " " }
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                val manager = org.bukkit.Bukkit.getScoreboardManager()
                val board = manager.newScoreboard
                users.forEach { user ->
                    val player = user.player!!
                    val team = board.registerNewTeam(user.id.substring(0, 14))
                    team.nameTagVisibility = org.bukkit.scoreboard.NameTagVisibility.HIDE_FOR_OTHER_TEAMS
                    team.setOption(
                        org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                        org.bukkit.scoreboard.Team.OptionStatus.NEVER
                    )
                    team.addEntry(user.name)
                    player.scoreboard = board
                    player.sendTitle("Ваша роль:", user.role.title)
                    // Выполнение ролийных особенностей
                    clepto.bukkit.B.postpone(10 * 20) {
                        user.role.start?.invoke(user)
                    }
                    // Скорборд
                    val address = java.util.UUID.randomUUID().toString()
                    val objective =
                        clepto.cristalix.Cristalix.scoreboardService().getPlayerObjective(player.uniqueId, address)
                    objective.displayName = "Murder Mystery"
                    objective.startGroup("Игра")
                        .record { "Роль: " + user.role.title }
                        .record { "Детектив: " + if (detective.player != null && detective.player!!.gameMode != org.bukkit.GameMode.SPECTATOR) "§aжив" else "§cмертв" }
                        .record {
                            "Живых: §b" + org.bukkit.Bukkit.getOnlinePlayers()
                                .filter { it.gameMode != org.bukkit.GameMode.SPECTATOR }.size
                        }
                        .record { "Игроков: §7" + org.bukkit.Bukkit.getOnlinePlayers().size }
                    clepto.cristalix.Cristalix.scoreboardService().setCurrentObjective(player.uniqueId, address)
                    // Убрать шансы
                    me.func.murder.mod.ModTransfer().string(playerList).send("murder-start", user)
                    // Замена скинов
                    val profile: com.destroystokyo.paper.profile.PlayerProfile = user.player!!.playerProfile
                    val currentSkin = me.func.murder.util.SkitSet.skins.random()
                    val craftPlayer = user.player!! as org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

                    profile.setProperty(com.destroystokyo.paper.profile.ProfileProperty("skinURL", currentSkin.url))
                    profile.setProperty(
                        com.destroystokyo.paper.profile.ProfileProperty(
                            "skinDigest",
                            currentSkin.digest
                        )
                    )

                    user.player!!.playerProfile = profile

                    org.bukkit.Bukkit.getOnlinePlayers().forEach {
                        it.hidePlayer(app, user.player!!)
                        it.showPlayer(app, user.player!!)
                    }

                    craftPlayer.reregisterPlayer(craftPlayer.handle)
                    craftPlayer.handle.playerConnection.sendPacket(
                        net.minecraft.server.v1_12_R1.PacketPlayOutRespawn(
                            0,
                            app.worldMeta.world.handle.difficulty,
                            net.minecraft.server.v1_12_R1.WorldType.NORMAL,
                            craftPlayer.handle.playerInteractManager.gameMode
                        )
                    )

                    craftPlayer.handle.updateAbilities()
                    craftPlayer.handle.playerConnection.sendPacket(
                        net.minecraft.server.v1_12_R1.PacketPlayOutPosition(
                            craftPlayer.location.getX(),
                            craftPlayer.location.getY(),
                            craftPlayer.location.getZ(),
                            craftPlayer.location.getYaw(),
                            craftPlayer.location.getPitch(),
                            HashSet(),
                            0
                        )
                    )

                    net.minecraft.server.v1_12_R1.MinecraftServer.getServer().playerList.updateClient(craftPlayer.handle)
                }

                activeStatus = GAME
                actualTime + 1
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && it / 20 < STARTING.lastSecond - 10)
            actualTime = (STARTING.lastSecond - 10) * 20
        actualTime
    }),
    GAME(300, { time ->
        // Обновление шкалы времени
        if (time % 20 == 0) {
            org.bukkit.Bukkit.getOnlinePlayers().forEach {
                me.func.murder.mod.ModTransfer()
                    .integer(GAME.lastSecond)
                    .integer(time)
                    .boolean(false)
                    .send("update-online", app.getUser(it))
            }
        }
        // Каждые 10 секунд, генерировать золото в случайном месте
        if ((time / 20) % 10 == 0)
            goldDropper.dropGold()
        // Если выбит лук, то крутить его и проверять, есть ли рядом игрок
        val droppedBow = app.worldMeta.world.livingEntities.firstOrNull {
            it.type == org.bukkit.entity.EntityType.ARMOR_STAND && it.hasMetadata("detective")
        }
        if (droppedBow != null) {
            val asStand = droppedBow as org.bukkit.entity.ArmorStand
            // Если есть кто-то рядом, сделать его детективом
            val nearby = org.bukkit.Bukkit.getOnlinePlayers()
                .firstOrNull { it.location.distanceSquared(droppedBow.location) < 7 }
            if (nearby != null) {
                val first = app.getUser(nearby.uniqueId)
                if (first.role == me.func.murder.user.Role.VILLAGER) {
                    asStand.remove()
                    first.role = me.func.murder.user.Role.DETECTIVE
                    first.role.start?.invoke(first)
                    clepto.bukkit.B.bc(ru.cristalix.core.formatting.Formatting.fine("Лук перехвачен!"))
                }
            }
            // Вращение
            val pose = asStand.headPose
            pose.y += Math.toRadians(360.0 / (20 * 3)) // Полный оборот за 3 секунды
            asStand.headPose = pose
            // Создание частиц возле лука
            val radius = 1.2 // Радиус окружности
            val omega = 1.0 // Скорость вращения
            val amount = 2 // Количество частиц
            for (counter in 0..amount) {
                app.worldMeta.world.spawnParticle(
                    org.bukkit.Particle.SPELL_WITCH,
                    asStand.location.clone().add(
                        kotlin.math.sin(time / 2 / kotlin.math.PI * omega * counter) * radius,
                        1.6 + kotlin.math.sin(time / kotlin.math.PI / 5),
                        kotlin.math.cos(time / 2 / kotlin.math.PI * omega * counter) * radius
                    ),
                    1
                )
            }
        }
        // Если осталось менее минуты, выдать свечение игрокам
        if (time == (GAME.lastSecond - 60) * 20) {
            org.bukkit.Bukkit.getOnlinePlayers().filter { it.gameMode != org.bukkit.GameMode.SPECTATOR }
                .forEach { it.isGlowing = true }
        }
        // Проверка на победу
        if (me.func.murder.util.WinUtil.check4win()) {
            activeStatus = END
        }
        time
    }),
    END(310, { time ->
        if (GAME.lastSecond * 20 + 10 == time) {
            clepto.bukkit.B.bc("")
            clepto.bukkit.B.bc("§c§lКОНЕЦ! $winMessage")
            clepto.bukkit.B.bc("    §cМаньяк $murderName")
            clepto.bukkit.B.bc("    §bДетектив $detectiveName")
            if (heroName.isNotEmpty())
                clepto.bukkit.B.bc("    §aГерой $heroName")
            clepto.bukkit.B.bc("")
            // Объявление о закрытии сервера
            clepto.bukkit.B.bc(ru.cristalix.core.formatting.Formatting.fine("Перезагрузка сервера..."))
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                // Кик всех игроков с сервера
                clepto.cristalix.Cristalix.transfer(
                    org.bukkit.Bukkit.getOnlinePlayers().map { it.uniqueId },
                    ru.cristalix.core.realm.RealmId.of(lobby)
                )
                activeStatus = STARTING
                app.worldMeta.world.entities.forEach { it.remove() }
                if (me.func.murder.games > me.func.murder.GAMES_STREAK_RESTART)
                    org.bukkit.Bukkit.shutdown()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
}