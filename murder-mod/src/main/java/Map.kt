import com.google.gson.Gson
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.GuiOverlayRender
import dev.xdark.feder.NetUtil
import org.lwjgl.opengl.GL11
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*
import kotlin.math.PI

class Map {

    private val minimapSize = 100.0
    private val gson = Gson()

    init {
        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "murder:map-load") {
                createMinimap(gson.fromJson(NetUtil.readUtf8(data, 65536), MapData::class.java))
            }
        }
    }

    private fun createMinimap(mapData: MapData) {
        val minimap = rectangle {
            size.x = mapData.textureSize
            size.y = mapData.textureSize

            color = WHITE
            scale = V3(2.5, 2.5, 1.0)

            align = Relative.CENTER
            val mapTexture = mapData.mapTexturePath.replace("minecraft:", "")
            textureLocation = UIEngine.clientApi.resourceManager().getLocation(NAMESPACE, mapTexture)

            addChild(rectangle {
                color = WHITE
                size.x = 2.0
                size.y = 2.0
            })
            addChild(rectangle {
                color = WHITE
                color.green = 0
                size.x = 2.0
                size.y = 2.0
                align = Relative.BOTTOM_RIGHT
            })

            beforeRender = {
                GL11.glDepthFunc(GL11.GL_EQUAL)
            }
            afterRender = {
                GL11.glDepthFunc(GL11.GL_LEQUAL)
            }

            mask = true

            for (marker in mapData.markers) {
                addChild(text {
                    offset = V3(marker.x, marker.y)
                    scale.x /= 2.5
                    scale.y /= 2.5
                    color = Color(255, 255, 255, 0.6)
                    origin = Relative.CENTER
                    content = marker.text
                    shadow = true
                })
            }

        }

        val minimapBounds = rectangle {
            size.x = minimapSize
            size.y = minimapSize
            offset.z = 0.01
            origin = Relative.CENTER
            align = Relative.CENTER
            color = Color(18, 18, 18, 0.7)
            addChild(minimap)
            val playerTexture = mapData.playerTexturePath.replace("minecraft:", "")
            val location = UIEngine.clientApi.resourceManager().getLocation("minecraft", playerTexture)
            addChild(rectangle {
                size.x = 8.0
                size.y = 8.0
                textureLocation = location
                textureFrom = V2(8.0 / 8, 8.0 / 8)
                textureSize = V2(8.0 / 8, 8.0 / 8)
                color = WHITE
                align = Relative.CENTER
                origin = Relative.CENTER
            })
        }

        val minimapContainer = rectangle {
            size.x = minimapSize + 4
            size.y = minimapSize + 4
            color = Color(18, 18, 18, 0.7)
            addChild(minimapBounds)
            origin = Relative.BOTTOM_RIGHT
            align = Relative.BOTTOM_RIGHT
            offset.x -= 25
            offset.y -= 25
        }

        UIEngine.overlayContext.addChild(minimapContainer)

        UIEngine.registerHandler(GuiOverlayRender::class.java) {
            val player = UIEngine.clientApi.minecraft().player

            val rotation = -player.rotationYaw * PI / 180
            minimap.rotation.degrees = rotation

            for (child in minimap.children) {
                child.rotation.degrees = -rotation
            }

            val partialTicks = UIEngine.clientApi.minecraft().timer.renderPartialTicks

            minimap.origin.x =
                -(player.lastX + (player.x - player.lastX) * partialTicks - mapData.maxX) / mapData.textureSize
            minimap.origin.y =
                -(player.lastZ + (player.z - player.lastZ) * partialTicks - mapData.maxZ) / mapData.textureSize
        }
    }
}