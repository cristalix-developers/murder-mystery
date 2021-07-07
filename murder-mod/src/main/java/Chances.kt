import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.ArmorRender
import dev.xdark.clientapi.event.render.ExpBarRender
import dev.xdark.clientapi.event.render.HealthRender
import dev.xdark.clientapi.event.render.HungerRender
import dev.xdark.feder.NetUtil
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*

class Chances {

    private val murder = rectangle {
        align = LEFT
        origin = LEFT
        color = Color(0, 0, 0, 0.62)
        size = V3(88.0, 20.0, 0.0)
        addChild(text {
            origin = CENTER
            align = CENTER
            color = WHITE
            shadow = true
        })
    }

    private val detective = rectangle {
        align = RIGHT
        origin = RIGHT
        color = Color(0, 0, 0, 0.62)
        size = V3(88.0, 20.0, 0.0)
        addChild(text {
            origin = CENTER
            align = CENTER
            color = WHITE
            shadow = true
        })
    }

    private val box = rectangle {
        offset = V3(0.0, -25.0)
        origin = BOTTOM
        align = BOTTOM
        size = V3(180.0, 20.0, 0.0)
        addChild(murder, detective)
    }

    private val online = rectangle {
        offset = V3(0.0, -50.0)
        origin = BOTTOM
        align = BOTTOM
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        addChild(
            rectangle {
                origin = LEFT
                align = LEFT
                size = V3(0.0, 5.0, 0.0)
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
    }

    init {
        UIEngine.overlayContext.addChild(box, online)
        box.enabled = false

        UIEngine.registerHandler(HealthRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(ExpBarRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(HungerRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(ArmorRender::class.java) { isCancelled = true }

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "murder-join") {
                (murder.children[0] as TextElement).content = "§cМаньяк " + data.readInt() + "%"
                (detective.children[0] as TextElement).content = "§bДетектив " + data.readInt() + "%"
                box.enabled = true
            } else if (channel == "murder-start") {
                box.enabled = false
                online.animate(2, Easings.BACK_BOTH) {
                    offset.y = -25.0
                }
                NetUtil.readUtf8(data).split(' ').forEach {
                    if (it.isEmpty())
                        return@forEach
                    //val player = clientApi.minecraft().world.getEntity(it.toInt()) as AbstractClientPlayer
                    //val randomSkin = skins.random()
                    //player.gameProfile.properties.put("skinURL", Property("skinURL", randomSkin.url))
                    //player.gameProfile.properties.put("skinDigest", Property("skinDigest", randomSkin.digest))
                }
            } else if (channel == "update-online") {
                val max = data.readInt()
                val current = data.readInt()
                val waiting = data.readBoolean()
                if (waiting) {
                    (online.children[0] as RectangleElement).animate(1) {
                        size.x = 180.0 / max * current
                    }
                    (online.children[1] as TextElement).content = "Ожидание игроков... [$current из $max]"
                } else {
                    (online.children[0] as RectangleElement).animate(1, Easings.BACK_BOTH) {
                        size.x = 180.0 - 180.0 / max * (current / 20)
                    }
                    val timeLess = max - current / 20
                    (online.children[1] as TextElement).content = "Победа мирных через ${String.format("%02d:%02d", timeLess / 60, timeLess % 60)}"
                }
            }
        }
    }

}