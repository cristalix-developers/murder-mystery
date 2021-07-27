package me.func.murder

import clepto.bukkit.B
import me.func.murder.user.Role
import org.bukkit.Bukkit

lateinit var murderName: String
lateinit var detectiveName: String
var heroName = ""
lateinit var winMessage: String

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(30, { it ->
        // Если набор игроков начался, обновить статус реалма
        if (it == 0) {
            val realm = ru.cristalix.core.realm.IRealmService.get().currentRealmInfo
            realm.status = ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_JOIN
        }
        val players = Bukkit.getOnlinePlayers()
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
                // Обнуление прошлого героя и добавления количества игр
                heroName = ""
                games++
                // Телепортация игроков на игровые точки и очистка инвентаря
                val places = me.func.murder.worldMeta.getLabels("start")
                players.forEachIndexed { index, player ->
                    player.teleport(places[index])
                    player.inventory.clear()
                    player.itemOnCursor = null
                    player.openInventory.topInventory.clear()
                }
                // Список игроков Murder
                val users = players.map { app.getUser(it) }
                // Выдача активных ролей
                val accountService = ru.cristalix.core.account.IAccountService.get()
                val murder = users.maxByOrNull { it.stat.villagerStreak }!!
                murder.role = Role.MURDER
                murder.stat.villagerStreak = 0
                murderName = accountService.getNameByUuid(murder.stat.id).get()
                val detective = users.minus(murder).maxByOrNull { it.stat.villagerStreak }!!
                detective.role = Role.DETECTIVE
                detective.stat.villagerStreak = 0
                detectiveName = accountService.getNameByUuid(detective.stat.id).get()
                // Выдача мирных жителей
                users.forEach {
                    if (it.role != Role.MURDER && it.role != Role.DETECTIVE) {
                        it.role = Role.VILLAGER
                        it.stat.villagerStreak++
                    }
                }
                // Показать карту
                me.func.murder.mod.ModHelper.loadMap(map)
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                val manager = Bukkit.getScoreboardManager()
                val board = manager.newScoreboard
                users.forEach { user ->
                    val player = user.player!!
                    val team = board.registerNewTeam(user.session.userId.substring(0, 14))
                    team.nameTagVisibility = org.bukkit.scoreboard.NameTagVisibility.HIDE_FOR_OTHER_TEAMS
                    team.setOption(
                        org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                        org.bukkit.scoreboard.Team.OptionStatus.NEVER
                    )
                    team.addEntry(user.name)
                    player.scoreboard = board
                    me.func.murder.mod.ModHelper.sendTitle(user, "Роль: ${user.role.title}")
                    // Выполнение ролийных особенностей
                    B.postpone(10 * 20) {
                        user.role.start?.invoke(user)
                    }
                    // Отправить информацию о начале игры клиенту
                    me.func.murder.mod.ModTransfer()
                        .string(user.role.shortTitle)
                        .send("murder-start", user)

                    me.func.murder.mod.ModHelper.update()
                    // Сменить музыку
                    me.func.murder.music.Music.OUTLAST.play(user)
                }
                // Заспавнить перевернутых пауков
                me.func.murder.worldMeta.getLabels("spider").forEach {
                    val spider =
                        it.world.spawnEntity(it, org.bukkit.entity.EntityType.SPIDER) as org.bukkit.entity.Spider
                    spider.customName = "Grumm"
                    spider.isCustomNameVisible = false
                    spider.setMetadata("trash", org.bukkit.metadata.FixedMetadataValue(app, true))
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
    GAME(330, { time ->
        // Обновление шкалы времени
        if (time % 20 == 0) {
            Bukkit.getOnlinePlayers().forEach {
                me.func.murder.mod.ModTransfer()
                    .integer(GAME.lastSecond)
                    .integer(time)
                    .boolean(false)
                    .send("update-online", app.getUser(it))
            }
        }
        // Каждые 10 секунд, генерировать золото в случайном месте
        if ((time / 20) % 10 == 0)
            me.func.murder.util.goldManager.dropGoldRandomly()
        // Если выбит лук, то крутить его и проверять, есть ли рядом игрок
        val droppedBow = me.func.murder.worldMeta.world.livingEntities.firstOrNull {
            it.type == org.bukkit.entity.EntityType.ARMOR_STAND && it.hasMetadata("detective")
        }
        if (droppedBow != null) {
            val asStand = droppedBow as org.bukkit.entity.ArmorStand
            // Если есть кто-то рядом, сделать его детективом
            val nearby = Bukkit.getOnlinePlayers()
                .filter { app.getUser(it).role != me.func.murder.user.Role.MURDER }
                .firstOrNull { it.location.distanceSquared(droppedBow.location) < 9 }
            if (nearby != null) {
                val first = app.getUser(nearby.uniqueId)
                if (first.role == Role.VILLAGER) {
                    asStand.remove()
                    first.role = Role.DETECTIVE
                    first.role.start?.invoke(first)
                    B.bc(ru.cristalix.core.formatting.Formatting.fine("Лук перехвачен!"))
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
                me.func.murder.worldMeta.world.spawnParticle(
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
        // Если осталось менее двух минут, выдать скорость мардеру, и подсветить всех на 5 секунд
        val glowing = org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 100, 1, false, false)
        if (time == (GAME.lastSecond - 60 * 2) * 20) {
            Bukkit.getOnlinePlayers().forEach { player ->
                val user = app.getUser(player)
                if (user.role == Role.MURDER) {
                    player.walkSpeed = 0.25f
                    return@forEach
                }
                player.addPotionEffect(glowing)
                me.func.murder.mod.ModHelper.sendTitle(user, "㥏 Скоро рассвет")
            }
        }
        // Проверка на победу
        if (me.func.murder.util.WinUtil.check4win()) {
            activeStatus = END
        }
        time
    }),
    END(340, { time ->
        if (GAME.lastSecond * 20 + 10 == time) {
            // Выдача побед выжившим и выдача всем доп. игр
            Bukkit.getOnlinePlayers().forEach {
                val user = app.getUser(it)
                if ( it.gameMode != org.bukkit.GameMode.SPECTATOR)
                    user.stat.wins++
                user.stat.games++
            }

            B.bc("")
            B.bc("§c§lКОНЕЦ! $winMessage")
            B.bc("    §cМаньяк $murderName")
            B.bc("    §bДетектив $detectiveName")
            if (heroName.isNotEmpty())
                B.bc("    §aГерой $heroName")
            B.bc("")
            // Объявление о закрытии сервера
            B.bc(ru.cristalix.core.formatting.Formatting.fine("Перезагрузка сервера..."))
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                app.restart()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
}