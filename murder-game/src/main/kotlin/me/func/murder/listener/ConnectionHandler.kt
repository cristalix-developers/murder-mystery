/*
package me.func.murder.listener

import sdrgsclepto.bukkit.B
import me.func.commons.donate.Rare
import me.func.commons.donate.impl.NameTag
import me.func.commons.mod.ModTransfer
import me.func.commons.user.Role
import me.func.commons.util.Music
import me.func.commons.util.MusicHelper
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.app
import me.func.murder.map
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import ru.cristalix.core.account.IAccountService
import ru.cristalix.core.tab.IConstantTabView
import ru.cristalix.core.tab.ITabService
import ru.cristalix.core.tab.TabTextComponent
import ru.cristalix.core.text.TextFormat
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

//var tab: ITabService = ITabService.get()
//val tabView: IConstantTabView = tab.createConstantTabView()

object ConnectionHandler : Listener {

    init {
        // Таб
        tabView.addPrefix(
            TabTextComponent(
                1,
                TextFormat.RBRACKETS,
                { app.getUser(it).stat.activeNameTag != NameTag.NONE },
                { player ->
                    val tag = app.getUser(player).stat.activeNameTag
                    CompletableFuture.completedFuture(
                        ComponentBuilder(
                            if (tag != NameTag.NONE) tag.getRare().with(tag.getTitle()) else ""
                        ).create()
                    )
                },
                { player -> CompletableFuture.completedFuture(Rare.values().size + 1 - app.getUser(player).stat.activeNameTag.getRare().ordinal) },
            )
        )
        tab.enable()
    }


    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        val user = app.getUser(player)

        user.stat.lastEnter = System.currentTimeMillis()

        \\ Заполнение имени для топа
        if (user.stat.lastSeenName == null || (user.stat.lastSeenName != null && user.stat.lastSeenName!!.isEmpty()))
            user.stat.lastSeenName =
                IAccountService.get().getNameByUuid(UUID.fromString(user.session.userId)).get(1, TimeUnit.SECONDS)

        if (activeStatus != Status.STARTING)
            return

        // Информация на моды, музыка
        B.postpone(5) {
            ModTransfer()
                .string("§cМаньяк " + 2 * (1 + user.stat.villagerStreak) + "%")
                .string("§bДетектив " + 3 * (1 + user.stat.villagerStreak) + "%")
                .string(map.title)
                .send("murder-join", user)

            Music.LOBBY.play(user)
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val user = app.getUser(player)

        user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter

        MusicHelper.stop(user)

        player.scoreboard.teams.forEach { it.unregister() }

        if (activeStatus == Status.GAME && user.role == Role.VILLAGER) {
            user.stat.villagerStreak = 0
        }
    }

    @EventHandler
    fun ChunkLoadEvent.handle() {
        // Загрузка декора
        map.loadDetails(chunk.entities)
    }
}
*/
