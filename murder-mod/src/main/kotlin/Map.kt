import com.google.gson.Gson
import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.feder.NetUtil
import org.lwjgl.opengl.GL11
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.Color
import ru.cristalix.uiengine.utility.Relative
import ru.cristalix.uiengine.utility.V2
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.WHITE
import ru.cristalix.uiengine.utility.rectangle
import ru.cristalix.uiengine.utility.text
import kotlin.math.PI

const val MAP_SIZE = 90.0

class Map {

    private val gson = Gson()
    private lateinit var mapData: MapData
    private lateinit var minimap: RectangleElement
    private var started = false

    init {
        app.registerChannel("murder:map-load") {
            mapData = gson.fromJson(NetUtil.readUtf8(this, 65536), MapData::class.java)
            minimap = createMinimap(mapData)
            started = true
        }

        registerHandler<RenderTickPre> {
            if (!started) return@registerHandler
            if (mapData.title == "OUTLAST") {
                val y = clientApi.minecraft().player.y
                if (y > 121) {
                    mapData.mapTexturePath = "2.png"
                    mapData.textureSize = 128.0
                    mapData.maxX = 42.0
                    mapData.maxZ = -18.0
                } else if (y < 112) {
                    mapData.mapTexturePath = "-1.png"
                    mapData.textureSize = 64.0
                    mapData.maxX = 39.0
                    mapData.maxZ = -21.0
                } else {
                    mapData.mapTexturePath = "1.png"
                    mapData.textureSize = 128.0
                    mapData.maxX = 43.0
                    mapData.maxZ = -16.0
                }
            }
        }

        registerHandler<RenderTickPre> {
            if (!started) return@registerHandler
            val player = clientApi.minecraft().player

            val rotation = -player.rotationYaw * PI / 180
            minimap.rotation.degrees = rotation

            for (child in minimap.children) {
                child.rotation.degrees = -rotation
            }

            val partialTicks = clientApi.minecraft().timer.renderPartialTicks

            minimap.origin.x =
                -(player.lastX + (player.x - player.lastX) * partialTicks - mapData.maxX) / mapData.textureSize
            minimap.origin.y =
                -(player.lastZ + (player.z - player.lastZ) * partialTicks - mapData.maxZ) / mapData.textureSize
        }
    }

    private fun createMinimap(mapData: MapData): RectangleElement {
        val minimap = rectangle {
            size.x = mapData.textureSize
            size.y = mapData.textureSize

            color = WHITE
            scale = V3(2.5, 2.5, 1.0)

            align = Relative.CENTER
            val mapTexture = mapData.mapTexturePath.replace("minecraft:", "")
            textureLocation = clientApi.resourceManager().getLocation(NAMESPACE, mapTexture)

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
                    color = Color(0, 0, 0, 0.62)
                    origin = Relative.CENTER
                    content = marker.text
                    shadow = true
                })
            }
        }

        val minimapBounds = rectangle {
            size.x = MAP_SIZE
            size.y = MAP_SIZE
            offset.z = 0.01
            origin = Relative.CENTER
            align = Relative.CENTER
            color = Color(0, 0, 0, 0.62)
            addChild(minimap)
            val playerTexture = mapData.playerTexturePath.replace("minecraft:", "")
            val location = clientApi.resourceManager().getLocation("minecraft", playerTexture)
            addChild(rectangle {
                size.x = 4.0
                size.y = 4.0
                textureLocation = location
                textureFrom = V2(8.0 / 8, 8.0 / 8)
                textureSize = V2(8.0 / 8, 8.0 / 8)
                color = WHITE
                align = Relative.CENTER
                origin = Relative.CENTER
            })
        }

        val minimapContainer = rectangle {
            size.x = MAP_SIZE
            size.y = MAP_SIZE
            color = Color(0, 0, 0, 0.0)
            addChild(minimapBounds)
            origin = Relative.BOTTOM_RIGHT
            align = Relative.BOTTOM_RIGHT
            offset.x -= 25
            offset.y -= 25
        }

        UIEngine.overlayContext.addChild(minimapContainer)

        return minimap
    }
}
