import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.feder.NetUtil.readUtf8
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.utility.Relative
import ru.cristalix.uiengine.utility.Rotation
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.WHITE
import ru.cristalix.uiengine.utility.rectangle
import java.util.UUID
import kotlin.collections.set

object NeedHelp {

    private var holos: MutableMap<UUID, Context3D> = HashMap()

    init {
        app.registerChannel("holo") {
            val uuid = UUID.fromString(readUtf8(this))
            val x = readDouble()
            val y = readDouble()
            val z = readDouble()
            val texture = readUtf8(this)
            addHolo(uuid, x, y, z, texture)
        }

        app.registerChannel("holohide") {
            val uuid = UUID.fromString(readUtf8(this))
            val holo = holos.remove(uuid)
            if (holo != null) {
                UIEngine.worldContexts.remove(holo)
            }
        }

        val player = clientApi.minecraft().player
        registerHandler<RenderTickPre> {
            holos.forEach {
                val yaw =
                    (player.rotationYaw - player.prevRotationYaw) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationYaw
                val pitch =
                    (player.rotationPitch - player.prevRotationPitch) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationPitch
                it.value.rotation = Rotation(-yaw * Math.PI / 180 + Math.PI, 0.0, 1.0, 0.0)
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
            beforeRender = { GlStateManager.disableDepth() }
            afterRender = { GlStateManager.enableDepth() }
        }
        val context = Context3D(V3(x, y, z))
        context.addChild(rect)
        holos[uuid] = context
        UIEngine.worldContexts.add(context)
    }
}
