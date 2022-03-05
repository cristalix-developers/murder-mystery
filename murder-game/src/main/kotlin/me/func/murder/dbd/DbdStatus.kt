package me.func.murder.dbd

import dev.implario.bukkit.item.item
import me.func.battlepass.BattlePassUtil
import me.func.battlepass.quest.QuestType
import me.func.murder.MurderGame
import me.func.murder.dbd.mechanic.GadgetMechanic
import me.func.murder.getUser
import me.func.murder.mod.ModHelper
import me.func.murder.mod.ModTransfer
import me.func.murder.user.Role
import me.func.murder.util.Music
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.util.UtilEntity

enum class DbdStatus(val lastSecond: Int, val now: (Int, MurderGame) -> Int) {
    STARTING(20, { time, game ->
        var actualTime = time
        val players = game.players

        if (time % 20 == 0) game.status = RealmStatus.WAITING_FOR_PLAYERS

        // Если время вышло и пора играть
        if (time / 20 >= STARTING.lastSecond && game.players.size >= game.minPlayers) {
            // Начать отсчет заново, так как мало игроков
            if (players.size < game.minPlayers) {
                actualTime = 1
            } else {
                // Чистка двигателей и реанимация сундуков
                game.engineManager!!.clearAll()

                // Обновление статуса реалма, чтобы нельзя было войти, начинаем игру
                game.status = RealmStatus.GAME_STARTED_RESTRICTED

                // Эффект прыгучести, чтобы убрать прыжок
                val disableJump = PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 250)

                // Телепортация игроков на игровые точки и очистка инвентаря
                val places = game.map.getLabels("start")
                game.context.after(5) {
                    players.forEachIndexed { index, player ->
                        player.teleport(places[index])
                        player.inventory.clear()
                        player.itemOnCursor = null
                        player.openInventory.topInventory.clear()
                        val user = game.userManager.getUser(player)
                        me.func.Arcade.getArcadeData(player).mask.setMask(player)

                        game.killer!!.player!!.inventory.setItem(2, item {
                            type = Material.FISHING_ROD
                            nbt("Unbreakable", 1)
                            text("§bХук")
                        })
                    }
                }

                game.after(35) { game.modHelper.sendGlobalTitle("§4Dead By Daylight ⛽\n\n§fⒸⓇⒾⓈⓉⒶⓁⒾⓍ") }
                Music.DBD_GAME.playAll(game)

                // Список игроков Murder
                val users = players.map { game.userManager.getUser(it) }
                // Выдача роли маньяка и создание голема
                game.killer = users.random()
                game.killer!!.player!!.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SLOW, Int.MAX_VALUE, 0
                    )
                )
                game.killer!!.role = Role.MURDER
                UtilEntity.setScale(game.killer!!.player, 1.6, 1.6, 1.6)

                // Выдача роли жертвы
                users.filter { it.role != Role.MURDER }.forEach {
                    it.player!!.addPotionEffect(disableJump)
                    it.player!!.addPotionEffect(GadgetMechanic.blindness)
                    ModTransfer().integer(1).send("dbd:heart-create", it)
                    it.role = Role.VICTIM
                }
                // Показать карту
                game.modHelper.loadMap(game.mapType)
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                users.forEach { user ->
                    // tab.setTabView(player.uniqueId, tabView)
                    // tab.update(player)
                    me.func.mod.Anime.title(user.player!!, "Роль: ${user.role.title}")
                    // Выполнение ролийных особенностей
                    game.context.after(10 * 20) { user.role.start(user, game) }
                    // Отправить информацию о начале игры клиенту
                    ModTransfer().string(user.role.shortTitle).send("murder-start", user)

                    // Сменить музыку
                    game.mapType.music.play(user)

                    game.activeDbdStatus = GAME
                    actualTime + 1
                }
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 5 секунд
        if (players.size == game.slots && time / 20 < STARTING.lastSecond - 10) actualTime =
            (STARTING.lastSecond - 5) * 20

        actualTime
    }),
    GAME(480, { time, game ->
        // Обновление шкалы времени
        if (time % 20 == 0) {
            val alive = game.players.filter { it.gameMode != GameMode.SPECTATOR }.size
            game.players.map { game.userManager.getUser(it) }.forEach {
                ModTransfer().string(
                    "осталось ${
                        maxOf(
                            0, MurderGame.ENGINE_NEEDED - game.engineManager!!.enginesDone()
                        )
                    } §4⛽"
                ).integer(maxOf(0, alive - 1)).send("dbd:update", it)
                ModTransfer().integer(GAME.lastSecond).integer(time).boolean(false).send("update-online", it)
            }
        }
        game.players.map { it to it.location.distanceSquared(game.killer?.player!!.location) + 1 }.filter {
            time % maxOf(5, minOf((it.second / 10).toInt(), 25)) == 0 && game.killer!!.player != it.first
        }.map { game.userManager.getUser(it.first) }.forEach {
            it.player!!.playSound(
                it.player!!.location, Sound.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1.0f, 0.6f
            )
            game.context.after(10) { _ ->
                it.player!!.playSound(
                    it.player!!.location,
                    Sound.BLOCK_WOOD_PLACE,
                    SoundCategory.PLAYERS,
                    0.7f,
                    2.0f
                )
            }
            ModTransfer().integer(it.hearts).send("dbd:heart-update", it)
        }
        // Проверка на победу
        if (game.dbdWinUtil!!.check4win()) {
            game.activeDbdStatus = END
        }
        time
    }),
    END(490, { time, game ->
        if (GAME.lastSecond * 20 + 10 == time) {
            // Выдача побед выжившим и выдача всем доп. игр
            game.players.forEach {
                val user = game.userManager.getUser(it)
                if (Math.random() < 0.11) {
                    me.func.Arcade.giveLootbox(it.uniqueId)
                    game.broadcast(ru.cristalix.core.formatting.Formatting.fine("§e${user.player!!.name} §fполучил §bлутбокс§f!"))
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
            game.broadcast("")
            game.broadcast("§c§lКОНЕЦ! ${game.winMessage}")
            if (game.players.size > 3) {
                game.broadcast("  §bТоп залитого топлива:")
                game.players.filter { it != game.killer!!.player }
                    .sortedBy { -game.userManager.getUser(it).fuel }
                    .take(3)
                    .forEachIndexed { index, it ->
                        game.broadcast("     §f§l${index + 1}. §e${it.name} §bзалил ${game.userManager.getUser(it).fuel} штук")
                    }
            }
            if (game.killer != null) game.broadcast("  §eМаньяк сделал ${game.killer!!.bites} ударов")
            game.broadcast("")

            game.modHelper.sendGlobalTitle("§e§lКОНЕЦ!\n\n\n§4Dead By Daylight")
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                game.players.forEach {
                    BattlePassUtil.update(it, QuestType.PLAY, 1, false)
                }

                game.stopGame()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
}
