package me.func.murder

import me.func.murder.user.Role

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
            // Установить счетчик игроков
            activeBar = me.func.murder.bar.WaitingPlayers
        }

        activeBar.updateMessage()

        val players = org.bukkit.Bukkit.getOnlinePlayers()
        var actualTime = it

        // Если время вышло и пора играть
        if (it == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size + 1 < slots)
                actualTime = 0
            else {
                // Телепортация игроком на игровые точки
                val places = app.worldMeta.getLabels("start")
                players.forEachIndexed { index, player ->
                    player.teleport(places[index])
                }
                // Список игроков Murder
                val users = players.map { app.getUser(it) }
                // Выдача активных ролей
                val murder = users.maxByOrNull { it.stat.villagerStreak }!!
                murder.role = Role.MURDER
                murder.stat.villagerStreak = 0
                val detective = users.minus(murder).maxByOrNull { it.stat.villagerStreak }!!
                detective.role = Role.DETECTIVE
                detective.stat.villagerStreak = 0
                // Выдача мирных жителей
                users.minus(murder).minus(detective).forEach {
                    it.role = Role.VILLAGER
                    it.stat.villagerStreak++
                }
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                val manager = org.bukkit.Bukkit.getScoreboardManager()
                val board = manager.newScoreboard
                users.forEach { user ->
                    val team = board.registerNewTeam(user.name)
                    team.nameTagVisibility = org.bukkit.scoreboard.NameTagVisibility.HIDE_FOR_OTHER_TEAMS
                    team.setOption(
                        org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                        org.bukkit.scoreboard.Team.OptionStatus.NEVER
                    )
                    team.addEntry(user.name)
                    user.player!!.scoreboard = board
                    user.player!!.sendTitle("Ваша роль:", user.role.title)
                    // Выполнение ролийных особенностей
                    user.role.start(user)
                }
                // Установить активный счетчик времени и заполнить его игроками
                activeBar = me.func.murder.bar.PlayBar
                players.map { it.uniqueId }.forEach { activeBar.addViewer(it) }
            }
        }

        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && it < STARTING.lastSecond - 10)
            actualTime = STARTING.lastSecond - 10
        actualTime
    }),
    GAME(300, { time ->
        // Обновить индикатор времени
        activeBar.updateMessage()
        // Каждые 3 секунды, генерировать золото в случайном месте
        if ((time / 20) % 3 == 0)
            goldDropper.dropGold()
        // Если выбит лук, то крутить его и проверять, есть ли рядом игрок
        val droppedBow = app.worldMeta.world.livingEntities.firstOrNull {
            it.type == org.bukkit.entity.EntityType.ARMOR_STAND && it.hasMetadata("detective")
        }
        if (droppedBow != null) {
            val asStand = droppedBow as org.bukkit.entity.ArmorStand
            // Вращение
            val pose = asStand.headPose
            pose.y += Math.toRadians(360.0 / (20 * 2)) // Полный оборот за 2 секунды
            asStand.headPose = pose
            // Изменение по высоте
            asStand.teleport(
                asStand.location.clone().add(0.0, (time % 40 - 20.0) / 40, 0.0)
            ) // [0..40] -> [-20..20] / 40, движение в пределах [-0.5, 0.5]
            // Создание частиц возле лука
            val radius = 1.0 // Радиус окружности
            val omega = 1.0 // Скорость вращения
            val amount = 3 // Количество частиц
            for (counter in 0..amount) {
                app.worldMeta.world.spawnParticle(
                    org.bukkit.Particle.CRIT,
                    asStand.location.clone().add(
                        kotlin.math.sin(time / 2 / kotlin.math.PI * omega / counter) * radius,
                        0.0,
                        kotlin.math.cos(time / 2 / kotlin.math.PI * omega / counter) * radius
                    ),
                    1
                )
            }
        }
        time
    }),
    END(310, { time ->
        // Объявление о закрытии сервера
        clepto.bukkit.B.bc(ru.cristalix.core.formatting.Formatting.fine("Перезагрузка сервера..."))
        // Обновление статуса реалма, чтобы нельзя было войти
        val realm = ru.cristalix.core.realm.IRealmService.get().currentRealmInfo
        realm.status = ru.cristalix.core.realm.RealmStatus.GAME_STARTED_RESTRICTED
        ru.cristalix.core.network.ISocketClient.get().write(
            ru.cristalix.core.network.packages.RealmUpdatePackage(
                ru.cristalix.core.network.packages.RealmUpdatePackage.UpdateType.UPDATE,
                realm
            )
        )
        // Кик всех игроков с сервера
        clepto.cristalix.Cristalix.transfer(
            org.bukkit.Bukkit.getOnlinePlayers().map { it.uniqueId },
            ru.cristalix.core.realm.RealmId.of(lobby)
        )
        if (time + 10 < END.lastSecond) END.lastSecond - 10 else time
    }),
    CLOSE(311, {
        activeStatus = STARTING
        0
    }),
}