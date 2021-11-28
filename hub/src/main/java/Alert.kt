import implario.humanize.Humanize
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*

object Alert {

    private lateinit var title: TextElement
    private lateinit var lore: TextElement
    private lateinit var loreBox: RectangleElement
    private val titleBox = rectangle {
        align = TOP
        origin = TOP
        color = Color(42, 102, 189, 0.62)
        size = V3(160.0, 32.0)

        title = +text {
            align = CENTER
            origin = CENTER
            scale = V3(1.5, 1.5)
            color = WHITE
            shadow = true
            content = "Название игры"
        }
        loreBox = +rectangle {
            align = BOTTOM
            origin = BOTTOM
            size = V3(180.0, 18.0)
            offset.y += size.y
            color = Color(0, 0, 0, 0.62)

            lore = +text {
                align = CENTER
                origin = CENTER
                scale = V3(0.75, 0.75)
                color = WHITE
                content = "Долгое-долгое описание мини-игры\nухахаа пау-пау"
            }
        }
    }

    init {
        UIEngine.overlayContext + titleBox

        App::class.mod.registerChannel("func:glass-alert") {
            val context = Context3D(V3(readDouble(), readDouble(), readDouble()))

            context.addChild(
                rectangle {
                    rotation = Rotation(Math.PI, 0.0, 1.0, 0.0)
                    size = V3(125.0, 60.0)
                    color = Color(0, 0, 0, 0.62)

                    val count = readInt()

                    addChild(text {
                        align = CENTER
                        origin = CENTER
                        content = "§bЗакаленное стекло\nупадет если\n$count ${Humanize.plurals("игрок", "игрока", "игроков", count)}\nвстанут на него!"
                        offset.z -= 0.1
                    })
                }
            )
            UIEngine.worldContexts.add(context)
        }

//        App::class.mod.registerChannel("func:alert") {
//            title.content = NetUtil.readUtf8(this)
//            lore.content = NetUtil.readUtf8(this)
//
//            titleBox.animate(0.3) {
//                offset.y -= 40
//            }
//
//            UIEngine.schedule(1.2) {
//                titleBox.animate(0.3) {
//                    offset.y += 40
//                }
//            }
//        }
    }
}