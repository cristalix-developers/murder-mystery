package me.func.murder.listener

import clepto.bukkit.B
import io.netty.buffer.Unpooled
import me.func.commons.mod.ModHelper
import me.func.commons.mod.ModTransfer
import me.func.commons.worldMeta
import me.func.murder.*
import me.func.murder.music.Music
import me.func.murder.music.MusicHelper
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.cristalix.core.account.IAccountService
import ru.cristalix.core.display.DisplayChannels
import ru.cristalix.core.display.messages.Mod
import ru.cristalix.core.formatting.Formatting
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

class ConnectionHandler : Listener {

    // Получении точки спавна
    private val spawn = worldMeta.getLabel("spawn").toCenterLocation()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        val user = murder.getUser(player)

        // Заполнение имени для топа
        if (user.stat.lastSeenName == null || (user.stat.lastSeenName != null && user.stat.lastSeenName!!.isEmpty()))
            user.stat.lastSeenName =
                IAccountService.get().getNameByUuid(UUID.fromString(user.session.userId)).get(1, TimeUnit.SECONDS)

        if (activeStatus != Status.STARTING)
            return

        // информация на моды, музыка
        B.postpone(5) {
            if (Math.random() < 0.2) {
                ModHelper.sendCorpse(
                    "Незнакомец",
                    UUID.fromString("308380a9-2c69-11e8-b5ea-1cb72caa35fd"),
                    user,
                    spawn.x,
                    spawn.y,
                    spawn.z
                )
            }
            ModTransfer()
                .integer(2 * (1 + user.stat.villagerStreak))
                .integer(3 * (1 + user.stat.villagerStreak))
                .string(map.title)
                .send("murder-join", user)

            Music.LOBBY.play(user)
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val user = murder.getUser(player)
        MusicHelper.stop(user)

        player.scoreboard.teams.forEach { it.unregister() }
    }
}