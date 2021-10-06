package listener

import Status
import activeStatus
import clepto.bukkit.B
import map
import me.func.commons.donate.Rare
import me.func.commons.donate.impl.NameTag
import me.func.commons.mod.ModTransfer
import me.func.commons.util.Music
import murder
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.cristalix.core.account.IAccountService
import ru.cristalix.core.tab.IConstantTabView
import ru.cristalix.core.tab.ITabService
import ru.cristalix.core.tab.TabTextComponent
import ru.cristalix.core.text.TextFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

var tab: ITabService = ITabService.get()
val tabView: IConstantTabView = tab.createConstantTabView()

object JoinListener : Listener {

    init {
        // Таб
        tabView.addPrefix(
            TabTextComponent(
                1,
                TextFormat.RBRACKETS,
                { murder.getUser(it).stat.activeNameTag != NameTag.NONE },
                { player ->
                    val tag = murder.getUser(player).stat.activeNameTag
                    CompletableFuture.completedFuture(
                        ComponentBuilder(
                        if (tag != NameTag.NONE) tag.getRare().with(tag.getTitle()) else "").create())
                },
                { player -> CompletableFuture.completedFuture(Rare.values().size + 1 - murder.getUser(player).stat.activeNameTag.getRare().ordinal) },
            )
        )
        tab.enable()
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        val user = murder.getUser(player)

        user.stat.lastEnter = System.currentTimeMillis()

        // Заполнение имени для топа
        if (user.stat.lastSeenName == null || (user.stat.lastSeenName != null && user.stat.lastSeenName!!.isEmpty()))
            user.stat.lastSeenName =
                IAccountService.get().getNameByUuid(UUID.fromString(user.session.userId)).get(1, TimeUnit.SECONDS)

        if (activeStatus != Status.STARTING)
            return

        // Информация на моды, музыка
        B.postpone(5) {
            ModTransfer()
                .string("§cМаньяк 20%")
                .string("§aЖертва 80%")
                .string(map.title)
                .send("murder-join", user)

            Music.LOBBY.play(user)
        }
    }

}