package me.func.murder.listener

import clepto.bukkit.B
import io.netty.buffer.Unpooled
import me.func.murder.app
import me.func.murder.mod.ModHelper
import me.func.murder.mod.ModTransfer
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import ru.cristalix.core.display.DisplayChannels
import ru.cristalix.core.display.messages.Mod
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import java.io.File
import java.nio.file.Files
import java.util.*

class ConnectionHandler : Listener {

    // Прогрузка файлов модов
    private var modList = try {
        File("./mods/").listFiles()!!
            .map {
                val buffer = Unpooled.buffer()
                buffer.writeBytes(Mod.serialize(Mod(Files.readAllBytes(it.toPath()))))
                buffer
            }.toList()
    } catch (exception: Exception) {
        Collections.emptyList()
    }

    // Получении точки спавна
    private val spawn = app.worldMeta.getLabel("spawn").toCenterLocation()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        val user = app.getUser(player)

        B.postpone(1) {
            modList.forEach {
                user.sendPacket(
                    PacketPlayOutCustomPayload(
                        DisplayChannels.MOD_CHANNEL,
                        PacketDataSerializer(it.retainedSlice())
                    )
                )
            }
        }

        B.postpone(10) {
            player.teleport(spawn)

            ModHelper.sendCorpse(
                UUID.fromString("308380a9-2c69-11e8-b5ea-1cb72caa35fd"),
                user,
                spawn.x,
                spawn.y,
                spawn.z
            )
            ModTransfer()
                .integer(2 * (1 + user.stat.villagerStreak))
                .integer(3 * (1 + user.stat.villagerStreak))
                .send("murder-join", user)
        }
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        playerProfile.properties.forEach { profileProperty ->
            if (profileProperty.value == "PARTY_WARP") {
                if (IRealmService.get().currentRealmInfo.status != RealmStatus.WAITING_FOR_PLAYERS) {
                    disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Сейчас нельзя зайти на этот сервер")
                    loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                }
            }
        }
    }
}