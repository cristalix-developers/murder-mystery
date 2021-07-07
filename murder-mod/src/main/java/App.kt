import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.xdark.clientapi.entity.AbstractClientPlayer
import dev.xdark.clientapi.entity.EntityProvider
import dev.xdark.clientapi.entity.PlayerModelPart
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.math.BlockPos
import dev.xdark.clientapi.util.EnumFacing
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import java.util.*


class App : KotlinMod() {

    private val skins = arrayOf("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd").map { UrlSkin(it) }

    override fun onEnable() {
        UIEngine.initialize(this)
        Chances()

        registerHandler<PluginMessage> {
            if (channel == "corpse") {
                val corpse = clientApi.entityProvider()
                    .newEntity(EntityProvider.PLAYER, clientApi.minecraft().world) as AbstractClientPlayer
                val profile = GameProfile(UUID.fromString(NetUtil.readUtf8(data)), NetUtil.readUtf8(data))
                profile.properties.put("skinURL", Property("skinURL", NetUtil.readUtf8(data)))
                profile.properties.put("skinDigest", Property("skinDigest", NetUtil.readUtf8(data)))
                corpse.gameProfile = profile


                val info = clientApi.clientConnection().newPlayerInfo(profile)
                info.responseTime = -2
                info.skinType = "DEFAULT"

                val x = data.readDouble()
                val y = data.readDouble()
                val z = data.readDouble()
                val pos = BlockPos.of(x.toInt(), y.toInt(), z.toInt())
                corpse.enableSleepAnimation(pos, EnumFacing.SOUTH)
                corpse.teleport(x, y, z)

                clientApi.clientConnection().addPlayerInfo(info)
                clientApi.minecraft().world.spawnEntity(corpse)
            }
        }
    }

    class UrlSkin(uuid: String) {
        val url: String = "https://webdata.c7x.dev/textures/skin/$uuid"
        val digest: String = uuid.substring(16)
    }
}