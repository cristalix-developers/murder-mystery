import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.feder.NetUtil.readUtf8
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.UIEngine.registerHandler
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.utility.Relative
import ru.cristalix.uiengine.utility.Rotation
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.WHITE
import ru.cristalix.uiengine.utility.rectangle
import java.util.UUID
import kotlin.collections.HashMap
import kotlin.collections.MutableMap
import kotlin.collections.forEach
import kotlin.collections.set

object NeedHelp {

    private var holos: MutableMap<UUID, Context3D> = HashMap()

    init {
        registerHandler(PluginMessage::class.java) {
            if (channel == "holo") {
                val uuid = UUID.fromString(readUtf8(data))
                val x = data.readDouble()
                val y = data.readDouble()
                val z = data.readDouble()
                val texture = readUtf8(data)
                addHolo(uuid, x, y, z, texture)
            } else if (channel == "holohide") {
                val uuid = UUID.fromString(readUtf8(data))
                val holo = holos.remove(uuid)
                if (holo != null) {
                    UIEngine.worldContexts.remove(holo)
                }
            }
        }

        val player = clientApi.minecraft().player
        registerHandler(RenderTickPre::class.java) {
            holos.forEach {
                val yaw =
                    (player.rotationYaw - player.prevRotationYaw) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationYaw
                val pitch =
                    (player.rotationPitch - player.prevRotationPitch) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationPitch
                it.value.context?.rotation = Rotation(-yaw * Math.PI / 180 + Math.PI, 0.0, 1.0, 0.0)
                it.value.children[0].rotation = Rotation(-pitch * Math.PI / 180, 1.0, 0.0, 0.0)
            }
        }
    }

    private fun addHolo(uuid: UUID, x: Double, y: Double, z: Double, texture: String) {
        val rect = rectangle {
            textureLocation = clientApi.resourceManager().getLocation("minecraft", texture)
            size = V3(16.0, 16.0)
            origin = Relative.CENTER
            color = WHITE
            beforeRender = {
                GlStateManager.disableDepth()
            }
            afterRender = {
                GlStateManager.enableDepth()
            }
        }
        val context = Context3D(V3(x, y, z))
        context.addChild(rect)
        holos[uuid] = context
        UIEngine.worldContexts.add(context)
    }
}