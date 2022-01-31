import zclepto.bukkit.B
import listener.tab
import listener.tabView
import me.func.commons.donate.impl.NameTag
import me.func.commons.getByPlayer
import me.func.commons.mod.ModHelper
import me.func.commons.mod.ModTransfer
import me.func.commons.realm
import me.func.commons.slots
import me.func.commons.user.Role
import me.func.commons.util.LocalModHelper
import me.func.commons.util.Music
import me.func.commons.worldMeta
import mechanic.GadgetMechanic.blindness
import mechanic.engine.EngineManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.util.UtilEntity
import util.winMessage

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(20, { time ->
        var actualTime = time
        val players = Bukkit.getOnlinePlayers()

        if (time % 20 == 0)
            realm.status = RealmStatus.WAITING_FOR_PLAYERS

        // Если время вышло и пора играть
        if (time / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size < slots) {
                actualTime = 1
            } else {
                // Чистка двигателей и реанимация сундуков
                EngineManager.clearAll()

                // Обновление статуса реалма, чтобы нельзя было войти, начинаем игру
                realm.status = RealmStatus.GAME_STARTED_RESTRICTED

                // Эффект прыгучести, чтобы убрать прыжок
                val disableJump =
                    org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP, Int.MAX_VALUE, 250)

                // Телепортация игроков на игровые точки и очистка инвентаря
                val places = worldMeta.getLabels("start")
                B.postpone(5) {
                    players.forEachIndexed { index, player ->
                        player.teleport(places[index])
                        player.inventory.clear()
                        player.itemOnCursor = null
                        player.openInventory.topInventory.clear()
                        val user = murder.getUser(player)
                        user.stat.mask.setMask(user)

                        killer!!.player!!.inventory.setItem(2, dev.implario.bukkit.item.item {
                            type = org.bukkit.Material.FISHING_ROD
                            nbt("Unbreakable", 1)
                            text("§bХук")
                        }.build())
                    }
                }

                B.postpone(35) { ModHelper.sendGlobalTitle("§4Dead By Daylight ⛽\n\n§fⒸⓇⒾⓈⓉⒶⓁⒾⓍ") }
                Music.DBD_GAME.playAll()

                // Список игроков Murder
                val users = players.map { murder.getUser(it) }
                // Выдача роли маньяка и создание голема
                killer = users.random()
                killer!!.player!!.addPotionEffect(
                    org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW,
                        Int.MAX_VALUE,
                        0
                    )
                )
                killer!!.role = Role.MURDER
                UtilEntity.setScale(killer!!.player, 1.6, 1.6, 1.6)

                // Выдача роли жертвы
                users.filter { it.role != Role.MURDER }
                    .forEach {
                        it.player!!.addPotionEffect(disableJump)
                        it.player!!.addPotionEffect(blindness)
                        ModTransfer().integer(1).send("dbd:heart-create", it)
                        it.role = Role.VICTIM
                    }
                // Показать карту
                LocalModHelper.loadMap(map)
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                users.forEach { user ->
                    val player = user.player!!
                    val nameTag = user.stat.activeNameTag
                    player.playerListName =
                        if (nameTag == NameTag.NONE) " " else "${nameTag.getRare().getColored()} §7${nameTag.getTitle()}"
                    tab.setTabView(player.uniqueId, tabView)
                    tab.update(player)
                    ModHelper.sendTitle(user, "Роль: ${user.role.title}")
                    // Выполнение ролийных особенностей
                    B.postpone(10 * 20) { user.role.start?.invoke(user) }
                    // Отправить информацию о начале игры клиенту
                    ModTransfer()
                        .string(user.role.shortTitle)
                        .send("murder-start", user)

                    // Сменить музыку
                    map.music.play(user)

                    activeStatus = GAME
                    actualTime + 1
                }
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 5 секунд
        if (players.size == slots && time / 20 < STARTING.lastSecond - 10)
            actualTime = (STARTING.lastSecond - 5) * 20

        actualTime
    }),
    GAME(
        480,
        { time ->
            // Обновление шкалы времени
            if (time % 20 == 0) {
                val alive =
                    Bukkit.getOnlinePlayers().filter { it.gameMode != GameMode.SPECTATOR }.size
                Bukkit.getOnlinePlayers().map { getByPlayer(it) }.forEach {
                    ModTransfer()
                        .string("осталось ${maxOf(0, ENGINE_NEEDED - EngineManager.enginesDone())} §4⛽")
                        .integer(maxOf(0, alive - 1))
                        .send("dbd:update", it)
                    ModTransfer()
                        .integer(GAME.lastSecond)
                        .integer(time)
                        .boolean(false)
                        .send("update-online", it)
                }
            }
            Bukkit.getOnlinePlayers().map { it to it.location.distanceSquared(killer?.player!!.location) + 1 }
                .filter { time % maxOf(5, minOf((it.second / 10).toInt(), 25)) == 0 && killer!!.player != it.first }
                .map { murder.getUser(it.first) }
                .forEach {
                    it.player!!.playSound(
                        it.player!!.location,
                        org.bukkit.Sound.BLOCK_WOOD_PLACE,
                        org.bukkit.SoundCategory.PLAYERS,
                        1.0f,
                        0.6f
                    )
                    B.postpone(10) {
                        it.player!!.playSound(
                            it.player!!.location,
                            org.bukkit.Sound.BLOCK_WOOD_PLACE,
                            org.bukkit.SoundCategory.PLAYERS,
                            0.7f,
                            2.0f
                        )
                    }
                    ModTransfer().integer(it.hearts).send("dbd:heart-update", it)
                }
            // Проверка на победу
            if (util.WinUtil.check4win()) {
                activeStatus = END
            }
            time
        }),
    END(490, { time ->
        if (GAME.lastSecond * 20 + 10 == time) {
            // Выдача побед выжившим и выдача всем доп. игр
            Bukkit.getOnlinePlayers().forEach {
                val user = murder.getUser(it)
                if (Math.random() < 0.11) {
                    user.stat.lootbox++
                    B.bc(ru.cristalix.core.formatting.Formatting.fine("§e${user.player!!.name} §fполучил §bлутбокс§f!"))
                }
                val firework = it.world!!.spawn(it.location, org.bukkit.entity.Firework::class.java)
                val meta = firework.fireworkMeta
                meta.addEffect(
                    org.bukkit.FireworkEffect.builder()
                        .flicker(true)
                        .trail(true)
                        .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                        .with(org.bukkit.FireworkEffect.Type.BALL)
                        .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                        .withColor(org.bukkit.Color.YELLOW)
                        .withColor(org.bukkit.Color.GREEN)
                        .withColor(org.bukkit.Color.WHITE)
                        .build()
                )
                meta.power = 0
                firework.fireworkMeta = meta
                Music.VILLAGER_WIN.play(user)
            }
            B.bc("")
            B.bc("§c§lКОНЕЦ! $winMessage")
            if (Bukkit.getOnlinePlayers().size > 3) {
                B.bc("  §bТоп залитого топлива:")
                Bukkit.getOnlinePlayers()
                    .filter { it != killer?.player }
                    .sortedBy { -murder.getUser(it).fuel }
                    .take(3)
                    .forEachIndexed {
                            index, it -> B.bc("     §f§l${index + 1}. §e${it.name} §bзалил ${murder.getUser(it).fuel} штук")
                    }
            }
            if (killer != null)
                B.bc("  §eМаньяк сделал ${killer!!.bites} ударов")
            B.bc("")

            ModHelper.sendGlobalTitle("§e§lКОНЕЦ!\n\n\n§4Dead By Daylight")

            B.postpone(20 * 8) {
                // Кик всех игроков с сервера
                clepto.cristalix.Cristalix.transfer(
                    Bukkit.getOnlinePlayers().map { it.uniqueId },
                    LOBBY_SERVER
                )
            }
            // Очистка мусорных сущностей
            worldMeta.world.entities.filter { it.hasMetadata("trash") }.forEach { it.remove() }
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