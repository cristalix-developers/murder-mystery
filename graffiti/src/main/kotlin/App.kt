import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.xdark.clientapi.entity.AbstractClientPlayer
import dev.xdark.clientapi.entity.EntityPlayer
import dev.xdark.clientapi.entity.EntityPlayerSP
import dev.xdark.clientapi.entity.EntityProvider
import dev.xdark.clientapi.event.entity.EntityDataChange
import dev.xdark.clientapi.event.entity.EntityLeftClick
import dev.xdark.clientapi.event.entity.EntityRightClick
import dev.xdark.clientapi.event.entity.RotateAround
import dev.xdark.clientapi.event.input.KeyPress
import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*
import java.lang.StrictMath.pow
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val NAMESPACE = "func"
const val FILE_STORE = "http://51.38.128.132/graffiti/"

val activeGraffiti = Context3D(V3())
val background = rectangle {
    origin = CENTER
    align = CENTER
    color = Color(0, 0, 0, 0.0)
    size = V3(1000.0, 1000.0)
    enabled = false
}

class App : KotlinMod() {

    private val drewGraffities = arrayListOf<Pair<Context3D, String>>()
    private val packs = listOf(Pack("1", "2", "3", "4", "5", "6"))
    private var activePack = packs[0]

    private var fixed = false

    override fun onEnable() {
        UIEngine.initialize(this)

        registerHandler<EntityLeftClick> {
            UIEngine.clientApi.chat().sendChatMessage("wow! nice flex")

            val corpse = clientApi.entityProvider()
                .newEntity(EntityProvider.PLAYER, clientApi.minecraft().world) as AbstractClientPlayer

            val uuid = UUID.randomUUID()
            corpse.setUniqueId(uuid)

            val profile = GameProfile(uuid, "name")
            corpse.gameProfile = profile

            val info = clientApi.clientConnection().newPlayerInfo(profile)
            info.responseTime = 5
            info.skinType = "DEFAULT"
            clientApi.clientConnection().addPlayerInfo(info)

            (entity as AbstractClientPlayer).renderingEntity = corpse

            UIEngine.clientApi.p13nProvider().playEmotion(
                entity as AbstractClientPlayer,
                UUID.fromString("1e953467-c105-4603-ae5d-b8114b96933c"),
                "",
                true
            )
        }

        UIEngine.overlayContext.addChild(background)

        packs.forEach { pack ->
            loadTextures(pack.graffiti.values.map { it.first }.toList()).thenAccept {
                activePack.graffiti.values.forEach { UIEngine.overlayContext.addChild(it.second) }
            }
        }

        val player = clientApi.minecraft().player
        val viewDistance = 4

        val hint = text {
            origin = CENTER
            align = CENTER
            offset.y += 20
            color = WHITE
        }
        UIEngine.overlayContext.addChild(hint)

        registerHandler<RenderTickPre> {
            val nearGraffiti = drewGraffities.firstOrNull {
                val location = it.first.offset
                pow(location.x - player.x, 2.0) + pow(location.z - player.z, 2.0) <= 1.2 &&
                        abs(location.y - player.y) < 2.6
            }
            if (nearGraffiti == null)
                hint.content = ""
            else
                hint.content = nearGraffiti.second
        }

        activeGraffiti.size = V3(25.0, 25.0)
        UIEngine.worldContexts.add(activeGraffiti)

        registerHandler<RotateAround> {
            if (fixed || !activeGraffiti.enabled || activeGraffiti.children.isEmpty())
                return@registerHandler

            val look = entity.lookVec

            val x = entity.x
            val y = entity.y + 1.5
            val z = entity.z

            val world = clientApi.minecraft().world

            val yaw = (360 * 10000 + player.rotationYaw) % 360

            for (i in 1..(viewDistance * 100)) {
                val dx = look.x / 100 * i
                val dy = look.y / 100 * i
                val dz = look.z / 100 * i
                val newX = x + dx
                val newY = y + dy
                val newZ = z + dz

                val id = world.getBlockState(newX, newY, newZ).block.id
                if (id != 0 && id != 6 && id != 31 && id != 37 && id != 38 && id != 68 && id != 107 && id != 131 && id != 132 && id != 143 && id != 160) {

                    var moveX = newX - dx / 60
                    var moveY = newY
                    var moveZ = newZ - dz / 60

                    val onGround = player.rotationPitch > 90 / viewDistance

                    if (onGround) {
                        moveY -= 0.47

                        val matrix = Matrix4f()
                        Matrix4f.setIdentity(matrix)
                        Matrix4f.rotate(
                            ((player.rotationYaw + 180) / 180 * Math.PI).toFloat(),
                            Vector3f(0f, -1f, 0f),
                            matrix,
                            matrix
                        )
                        Matrix4f.rotate((-Math.PI / 2).toFloat(), Vector3f(1f, 0f, 0f), matrix, matrix)
                        activeGraffiti.matrices[rotationMatrix] = matrix
                    } else {
                        moveY += 0.3
                    }

                    when (yaw) {
                        in 45.0..135.0 -> {
                            moveY += 0.5
                            moveZ += 0.5
                            if (!onGround)
                                activeGraffiti.rotation = Rotation(Math.PI / 2, 0.0, 1.0, 0.0)
                        }
                        in 225.0..315.0 -> {
                            moveY += 0.5
                            moveZ -= 0.5
                            if (!onGround)
                                activeGraffiti.rotation = Rotation(-Math.PI / 2, 0.0, 1.0, 0.0)
                        }
                        !in 135.0..225.0 -> {
                            moveY += 0.5
                            moveX += 0.5
                            if (!onGround)
                                activeGraffiti.rotation = Rotation(-Math.PI, 0.0, 1.0, 0.0)
                        }
                        else -> {
                            moveY += 0.5
                            moveX -= 0.5
                            if (!onGround)
                                activeGraffiti.rotation = Rotation(Math.PI * 2, 0.0, 1.0, 0.0)
                        }
                    }

                    activeGraffiti.animate(0.03) {
                        offset.x = moveX
                        offset.y = moveY
                        offset.z = moveZ
                    }
                    break
                }
            }
        }

        registerHandler<KeyPress> {
            if (key == Keyboard.KEY_H) {
                if (!background.enabled) {
                    fixed = false
                    clientApi.minecraft().setIngameNotInFocus()

                    background.enabled = true
                    background.animate(0.12) {
                        color.alpha = 0.8
                    }
                    activePack.graffiti.values.forEachIndexed { index, element ->
                        val angle = 2 * Math.PI / activePack.graffiti.values.size * index
                        element.second.offset.x = sin(angle) * 77
                        element.second.offset.y = cos(angle) * 77 * 0.85 - 20
                        element.second.size = V3(70.0, 70.0)
                        element.second.enabled = true
                    }
                } else {
                    UIEngine.clientApi.minecraft().setIngameFocus()
                    background.enabled = false
                    activePack.graffiti.values.forEach { it.second.enabled = false }
                }
            }
            if (key == Keyboard.KEY_F) {
                fixed = !fixed
                if (activeGraffiti.children.isNotEmpty())
                    activeGraffiti.children[0].color.alpha = if (fixed) 1.0 else 0.6
                if (fixed) {
                    createGraffiti(activeGraffiti.copy(), "")
                }
            }
        }
    }

    private fun createGraffiti(copy: Context3D, highlight: String) {
        drewGraffities.add(copy to highlight)
        UIEngine.worldContexts.add(copy)
        UIEngine.schedule(10) { UIEngine.worldContexts.remove(copy) }
    }
}
