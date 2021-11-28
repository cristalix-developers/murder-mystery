import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.feder.NetUtil
import io.netty.buffer.Unpooled
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

const val MARGIN = 5
const val WIDTH = 140.0

object QueueStatus {

    private var counter = 0
    private var total = 0
    private var need = 80

    private lateinit var icon: RectangleElement
    private lateinit var title: TextElement
    private lateinit var time: TextElement
    private lateinit var timer: RectangleElement
    private lateinit var background: RectangleElement
    private lateinit var cancel: RectangleElement

    private val box = rectangle {
        scale = V3(1.5, 1.5)
        enabled = false

        align = TOP
        origin = TOP
        offset.y += -WIDTH + 15

        size = V3(WIDTH, WIDTH / 4.0)

        icon = +rectangle {
            size = V3(WIDTH / 4.0, WIDTH / 4.0)
            color = WHITE
            align = TOP_LEFT
            origin = TOP_LEFT
        }

        timer = +rectangle {
            size = V3(WIDTH / 4.0, WIDTH / 24.0)
            align = BOTTOM_LEFT
            origin = BOTTOM_LEFT
            color = Color(0, 0, 0, 0.62)

            time = +text {
                align = CENTER
                origin = CENTER
                color = WHITE
                shadow = true
                scale = V3(0.5, 0.5)
            }
        }

        background = +rectangle {
            size = V3(WIDTH - WIDTH / 4.0, WIDTH / 4.0)
            color = Color(43, 116, 223, 0.86)
            align = TOP_RIGHT
            origin = TOP_RIGHT
            title = +text {
                align = TOP_LEFT
                origin = TOP_LEFT
                offset.x += MARGIN
                offset.y += MARGIN + 2
                scale = V3(0.9, 0.9)
                content = "Игра в кальмара\n§b0 из $need"
                color = WHITE
                shadow = true
            }
        }

        cancel = +rectangle {
            align = BOTTOM_RIGHT
            origin = BOTTOM_RIGHT
            size = V3(WIDTH / 4 * 3, WIDTH / 24)
            color = Color(255, 0, 0, 0.62)

            +text {
                align = LEFT
                origin = LEFT
                color = WHITE
                offset.x += MARGIN
                shadow = true
                scale = V3(0.5, 0.5)
                content = "Покинуть очередь"
            }

            onClick {
                UIEngine.clientApi.clientConnection().sendPayload("queue:leave", Unpooled.buffer())
            }
        }
    }

    init {
        UIEngine.overlayContext + box

        var before = System.currentTimeMillis()

        registerHandler<GameLoop> {
            if (!box.enabled)
                return@registerHandler
            val now = System.currentTimeMillis()

            if (now - before > 1000) {
                before = now
                counter++
                time.content = "⏰ ${counter / 60}:${(counter % 60).toString().padStart(2, '0')}"
                title.content = title.content.split("\n")[0] + "\n§b$total из $need"
            }
        }

        App::class.mod.registerChannel("queue:show") {
            if (box.enabled)
                return@registerChannel
            box.enabled = true
            box.animate(0.4, Easings.BACK_OUT) {
                offset.y = 15.0
            }

            before = System.currentTimeMillis()

            val name = NetUtil.readUtf8(this)
            val realIcon = load("$name.png", name.hashCode().toString())
            need = readInt()

            title.content = title.content.split("\n")[0] + "\n§b$total из $need"

            loadTextures(realIcon).thenAccept { icon.textureLocation = realIcon.location }
        }

        App::class.mod.registerChannel("queue:online") {
            total = readInt()

            if (counter > 120) {
                UIEngine.clientApi.clientConnection().sendPayload("queue:leave", Unpooled.buffer())
            }
        }

        App::class.mod.registerChannel("queue:hide") {
            box.animate(0.25, Easings.QUART_IN) {
                offset.y = -WIDTH + 15
            }
            UIEngine.schedule(0.26) {
                counter = 0
                box.enabled = false
            }
        }
    }

}