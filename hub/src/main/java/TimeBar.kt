import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object TimeBar {
    init {
        val cooldown = rectangle {
            offset.y += 30
            origin = TOP
            align = TOP
            size = V3(180.0, 5.0, 0.0)
            color = Color(0, 0, 0, 0.62)
            addChild(
                rectangle {
                    origin = LEFT
                    align = LEFT
                    size = V3(180.0, 5.0, 0.0)
                    color = Color(42, 102, 189, 1.0)
                },
                text {
                    origin = TOP
                    align = TOP
                    color = WHITE
                    shadow = true
                    content = "Загрузка..."
                    offset.y -= 15
                }
            )
            enabled = false
        }

        var time = 0
        var currentTime = System.currentTimeMillis()
        val textElement = cooldown.children[1] as TextElement

        registerHandler<GameLoop> {
            if (System.currentTimeMillis() - currentTime > 1000) {
                time--
                currentTime = System.currentTimeMillis()
                textElement.content = textElement.content.dropLast(5) + (time / 60).toString()
                    .padStart(2, '0') + ":" + (time % 60).toString().padStart(2, '0')
            }
        }

        registerHandler<PluginMessage> {
            if (channel == "hub:timebar") {
                val text = NetUtil.readUtf8(data) + "00:00"
                time = data.readInt()

                if (time == 0) {
                    cooldown.children[0].size.x = 0.0
                    cooldown.enabled = false
                    return@registerHandler
                }

                cooldown.color.red = data.readInt()
                cooldown.color.green = data.readInt()
                cooldown.color.blue = data.readInt()

                cooldown.enabled = true
                textElement.content = text
                (cooldown.children[0] as RectangleElement).animate(time - 0.1) {
                    size.x = 0.0
                }
                UIEngine.schedule(time) {
                    cooldown.enabled = false
                    (cooldown.children[0] as RectangleElement).size.x = 180.0
                }
            }
        }
        UIEngine.overlayContext.addChild(cooldown)
    }
}