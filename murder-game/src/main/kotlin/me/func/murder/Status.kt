package me.func.murder

import clepto.bukkit.B
import me.func.commons.mod.ModHelper
import me.func.commons.mod.ModTransfer
import me.func.commons.realm
import me.func.commons.slots
import me.func.commons.user.Role
import me.func.commons.worldMeta
import me.func.murder.music.Music
import me.func.murder.util.droppedBowManager
import me.func.murder.util.goldManager
import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.GameMode
import org.bukkit.entity.Firework
import org.bukkit.entity.Spider
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.formatting.Formatting.fine
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_JOIN
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_RESTRICTED

lateinit var murderName: String
lateinit var detectiveName: String
var heroName = ""
lateinit var winMessage: String

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(30, { it ->
        // Если набор игроков начался, обновить статус реалма
        if (it == 40)
            realm.status = GAME_STARTED_CAN_JOIN

        val players = Bukkit.getOnlinePlayers()
        // Обновление шкалы онлайна
        players.forEach {
            ModTransfer()
                .integer(slots)
                .integer(players.size)
                .boolean(true)
                .send("update-online", murder.getUser(it))
        }
        var actualTime = it

        // Если время вышло и пора играть
        if (it / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size + 3 < slots)
                actualTime = 1
            else {
                // Обновление статуса реалма, чтобы нельзя было войти
                realm.status = GAME_STARTED_RESTRICTED
                // Обнуление прошлого героя и добавления количества игр
                heroName = ""
                games++
                // Создание каталок
                worldMeta.getLabels("gurney").map { me.func.murder.map.Gurney.create(it) }
                // Телепортация игроков на игровые точки и очистка инвентаря
                val places = worldMeta.getLabels("start")
                B.postpone(10) {
                    players.forEachIndexed { index, player ->
                        player.teleport(places[index])
                        player.inventory.clear()
                        player.itemOnCursor = null
                        player.openInventory.topInventory.clear()
                    }
                }
                // Список игроков Murder
                val users = players.map { murder.getUser(it) }
                // Выдача активных ролей
                val murder = users.maxByOrNull { it.stat.villagerStreak }!!
                murder.role = Role.MURDER
                murder.stat.villagerStreak = 0
                murderName = murder.player!!.name
                val detective = users.minus(murder).maxByOrNull { it.stat.villagerStreak }!!
                detective.role = Role.DETECTIVE
                detective.stat.villagerStreak = 0
                detectiveName = detective.player!!.name
                // Выдача мирных жителей
                users.forEach {
                    if (it.role != Role.MURDER && it.role != Role.DETECTIVE) {
                        it.role = Role.VILLAGER
                        it.stat.villagerStreak++
                    }
                }
                // Показать карту
                me.func.murder.util.LocalModHelper.loadMap(map)
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                users.forEach { user ->
                    val player = user.player!!
                    val nameTag = user.stat.activeNameTag
                    player.playerListName =
                        if (nameTag == me.func.commons.donate.impl.NameTag.NONE) " " else nameTag.getRare()
                            .with(nameTag.getTitle())
                    me.func.murder.listener.tab.setTabView(player.uniqueId, me.func.murder.listener.tabView)
                    me.func.murder.listener.tab.update(player)
                    ModHelper.sendTitle(user, "Роль: ${user.role.title}")
                    // Выполнение ролийных особенностей
                    B.postpone(10 * 20) {
                        user.role.start?.invoke(user)
                    }
                    // Отправить информацию о начале игры клиенту
                    ModTransfer()
                        .string(user.role.shortTitle)
                        .send("murder-start", user)

                    ModHelper.updateOnline()
                    // Сменить музыку
                    Music.OUTLAST.play(user)
                }
                // Заспавнить перевернутых пауков
                worldMeta.getLabels("spider").forEach {
                    val spider =
                        it.world.spawnEntity(it, org.bukkit.entity.EntityType.SPIDER) as Spider
                    spider.customName = "Grumm"
                    spider.isCustomNameVisible = false
                    spider.setMetadata("trash", org.bukkit.metadata.FixedMetadataValue(me.func.murder.murder, true))
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
                ModTransfer()
                    .integer(GAME.lastSecond)
                    .integer(time)
                    .boolean(false)
                    .send("update-online", murder.getUser(it))
            }
        }
        // Каждые 10 секунд, генерировать золото в случайном месте
        if ((time / 20) % 10 == 0)
            goldManager.dropGoldRandomly()
        droppedBowManager.rotateIfPresent(time)
        // Если осталось менее двух минут, выдать скорость мардеру,
        // и подсветить всех на 5 секунд, если меньше 30 сек. выдать свечение
        val glowing = PotionEffect(PotionEffectType.GLOWING, 100, 1, false, false)
        if (time == (GAME.lastSecond - 120) * 20) {
            Bukkit.getOnlinePlayers().forEach { player ->
                val user = murder.getUser(player)
                player.addPotionEffect(glowing)
                ModHelper.sendTitle(user, "㥏 Скоро рассвет")
                if (user.role == Role.MURDER) {
                    player.walkSpeed = 0.25f
                    return@forEach
                }
            }
        } else if (time == (GAME.lastSecond - 30) * 20) {
            Bukkit.getOnlinePlayers()
                .filter { it.gameMode != GameMode.SPECTATOR }
                .forEach { it.isGlowing = true }
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
                val user = murder.getUser(it)
                if (it.gameMode != GameMode.SPECTATOR) {
                    user.stat.wins++
                    if (Math.random() < 0.11) {
                        user.stat.lootbox++
                        B.bc(fine("§e${user.player!!.name} §fполучил §bлутбокс§f!"))
                    }
                    val firework = it.world!!.spawn(it.location, Firework::class.java)
                    val meta = firework.fireworkMeta
                    meta.addEffect(
                        FireworkEffect.builder()
                            .flicker(true)
                            .trail(true)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .with(FireworkEffect.Type.BALL)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(org.bukkit.Color.YELLOW)
                            .withColor(org.bukkit.Color.GREEN)
                            .withColor(org.bukkit.Color.WHITE)
                            .build()
                    )
                    meta.power = 0
                    firework.fireworkMeta = meta
                }
                user.stat.games++
                Music.VILLAGER_WIN.play(user)
            }

            B.bc("")
            B.bc("§c§lКОНЕЦ! $winMessage")
            B.bc("    §cМаньяк $murderName")
            B.bc("    §bДетектив $detectiveName")
            if (heroName.isNotEmpty() && detectiveName != heroName)
                B.bc("    §aГерой $heroName")
            B.bc("")
            B.postpone(20 * 8) {
                // Кик всех игроков с сервера
                clepto.cristalix.Cristalix.transfer(
                    Bukkit.getOnlinePlayers().map { it.uniqueId },
                    ru.cristalix.core.realm.RealmId.of(LOBBY_SERVER)
                )
            }
            // Очистка мусорных сущностей
            worldMeta.world.entities.filter { it.hasMetadata("trash") }
                .forEach { it.remove() }
            droppedBowManager.clear()
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                murder.restart()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
}