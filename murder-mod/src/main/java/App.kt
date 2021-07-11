import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.xdark.clientapi.entity.AbstractClientPlayer
import dev.xdark.clientapi.entity.EntityProvider
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.EntitySleepingRotate
import dev.xdark.clientapi.math.BlockPos
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.clientapi.util.EnumFacing
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import java.util.*


const val NAMESPACE = "murder"
const val FILE_STORE = "http://51.38.128.132/murder/"

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)
        Chances()

        registerHandler<PluginMessage> {
            if (channel == "corpse") {
                val corpse = clientApi.entityProvider()
                    .newEntity(EntityProvider.PLAYER, clientApi.minecraft().world) as AbstractClientPlayer
                NetUtil.readUtf8(data)

                val uuid = UUID.randomUUID()
                corpse.setUniqueId(uuid)

                val profile = GameProfile(uuid, NetUtil.readUtf8(data))
                profile.properties.put("skinURL", Property("skinURL", NetUtil.readUtf8(data)))
                profile.properties.put("skinDigest", Property("skinDigest", NetUtil.readUtf8(data)))
                corpse.gameProfile = profile

                val info = clientApi.clientConnection().newPlayerInfo(profile)
                info.responseTime = -2
                info.skinType = "DEFAULT"
                clientApi.clientConnection().addPlayerInfo(info)

                val x = data.readDouble()
                var y = data.readDouble()
                val z = data.readDouble()
                var counter = 0
                var id: Int
                do {
                    y -= 0.15
                    counter++
                    id = clientApi.minecraft().world.getBlockState(x, y, z).id
                } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 30)

                corpse.enableSleepAnimation(BlockPos.of(x.toInt(), y.toInt(), z.toInt()), when (Math.random()) {
                    in 0.0..0.2 -> EnumFacing.SOUTH
                    in 0.2..0.4 -> EnumFacing.DOWN
                    in 0.4..0.6 -> EnumFacing.EAST
                    else -> EnumFacing.NORTH
                })
                corpse.teleport(x, y + 0.2, z)
                corpse.setNoGravity(false)
                clientApi.minecraft().world.spawnEntity(corpse)
            }
        }

        //registerHandler<EntitySleepingRotate> {
        //    GlStateManager.rotate(30f, 0f, 1f, 0f)
        //    isCancelled = true
        //}
    }
}