import dev.xdark.clientapi.entity.EntityArmorStand
import dev.xdark.clientapi.entity.EntityLivingBase
import dev.xdark.clientapi.entity.EntityPlayer
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.NameTemplateRender
import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.opengl.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.UIEngine.registerHandler
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.utility.*


object MobIndicator {

    init {
        val context = Context3D(V3())

        val bar = rectangle {
            origin = Relative.LEFT
            align = Relative.LEFT
            offset.x = 0.5
            size.x = 10.0
            size.y = 3.0
            color = Color(51, 240, 51)
        }

        val body = rectangle {
            size = V3(16.0, 4.0)
            color = Color(0, 0, 0, 0.5)
            origin = Relative.CENTER
            addChild(bar)
        }

        context.addChild(body)

        registerHandler(RenderTickPre::class.java) {
            val player = clientApi.minecraft().player
            val matrix = Matrix4f()
            Matrix4f.setIdentity(matrix)
            Matrix4f.rotate(
                ((player.rotationYaw + 180) / 180 * Math.PI).toFloat(),
                Vector3f(0f, -1f, 0f),
                matrix,
                matrix
            )
            Matrix4f.rotate((player.rotationPitch / 180 * Math.PI).toFloat(), Vector3f(-1f, 0f, 0f), matrix, matrix)
            context.matrices[rotationMatrix] = matrix
        }

        val map = mutableMapOf<Int, Pair<Double, Double>>()

        registerHandler(PluginMessage::class.java) {
            if (channel == "hub:mob") {
                val id = data.readInt()
                val hp = data.readDouble()
                val maxHp = data.readDouble()
                if (hp == 0.0 || maxHp == 0.0)
                    map.remove(id)
                else
                    map[id] = hp to maxHp
            }
        }

        registerHandler(NameTemplateRender::class.java) {
            if (entity !is EntityLivingBase) return@registerHandler
            val entity = entity as EntityLivingBase

            if (entity !is EntityPlayer && entity !is EntityArmorStand) isCancelled = true

            val mob = map[entity.entityId] ?: return@registerHandler

            val part = (mob.first / mob.second).toFloat()
            if (part == 1f) return@registerHandler

            val partialTicks = clientApi.minecraft().timer.renderPartialTicks

            context.offset = V3(
                entity.lastX + (entity.x - entity.lastX) * partialTicks,
                entity.lastY + (entity.y - entity.lastY) * partialTicks + entity.eyeHeight + 1,
                entity.lastZ + (entity.z - entity.lastZ) * partialTicks
            )

            val width = (entity.maxHealth * 2).coerceAtMost(30f).toDouble()
            bar.size.x = width * part
            body.size.x = width + 1.0

            var green = part * 2
            if (green > 1) green = 1f
            var red = (1 - part) * 2
            if (red > 1) red = 1f
            bar.color.green = (green * 255).toInt()
            bar.color.red = (red * 255).toInt()

            GlStateManager.disableLighting()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDepthMask(false)

            context.transformAndRender()
            GlStateManager.enableLighting()
            GL11.glDepthMask(true)

        }
    }
}