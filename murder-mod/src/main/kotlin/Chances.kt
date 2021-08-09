
import dev.xdark.clientapi.entity.EntityArmorStand
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.*
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

    private val cooldown = rectangle {
        offset = V3(0.0, -50.0)
        origin = BOTTOM
        align = BOTTOM
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        addChild(
            rectangle {
                origin = LEFT
                align = LEFT
                size = V3(180.0, 5.0, 0.0)
                color = Color(244, 148, 198, 1.0)
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

    private val topMessage = rectangle {
        align = TOP
        origin = TOP
        color = Color(0, 0, 0, 0.0)
        size = V3(90.0, 18.0, 0.0)
        addChild(text {
            origin = TOP
            align = TOP
            offset.y += 2
            content = "§dMurderMystery §bCristalix"
            shadow = true
            scale = V3(0.76, 0.76, 0.76)
            color.alpha = 0.62
        }, text {
            origin = BOTTOM
            align = BOTTOM
            content = "§fНазвание карты"
            shadow = true
            scale = V3(0.82, 0.82, 0.82)
        })
    }

    private lateinit var roleAndOnline: RectangleElement
    private lateinit var detectiveAlive: RectangleElement
    private var role = ""

    private val message = rectangle {
        origin = CENTER
        align = CENTER
        size = V3(400.0, 250.0, 0.0)
        addChild(text {
            origin = CENTER
            align = CENTER
            size = V3(400.0, 100.0, 0.0)
            color = Color(0, 0, 0, 0.2)
            shadow = true
        })
        enabled = false
    }

    init {
        UIEngine.overlayContext.addChild(box, online, cooldown, message, topMessage)
        box.enabled = false

        UIEngine.registerHandler(HotbarRender::class.java) { isCancelled = box.enabled }

        UIEngine.registerHandler(NameTemplateRender::class.java) {
            if (!box.enabled && entity !is EntityArmorStand)
                isCancelled = true
        }

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "murder-join") {
                (murder.children[0] as TextElement).content = "§cМаньяк " + data.readInt() + "%"
                (detective.children[0] as TextElement).content = "§bДетектив " + data.readInt() + "%"
                // Мигание названием карты в начале
                val text = (topMessage.children[1] as TextElement)
                text.content = "Музыка 㗡"
                val signals = 6
                for (i in 1..signals) {
                    UIEngine.overlayContext.schedule(i * 2) {
                        text.animate(0.9, Easings.BACK_IN) {
                            color.alpha = 0.8
                        }
                    }
                    UIEngine.overlayContext.schedule(i * 2 + 1) {
                        text.animate(0.9, Easings.BACK_IN) {
                            color.alpha = 0.3
                        }
                    }
                }
                UIEngine.overlayContext.schedule(signals * 2 + 2) {
                    text.animate(0.4, Easings.BACK_IN) {
                        scale.x = 0.0
                        scale.y = 0.0
                        color.alpha = 0.0
                    }
                }

                box.enabled = true
                // Загрузка фотографий
                loadTextures(
                    load("1.png", "088121088F83D8890128127"),
                    load("-1.png", "081221088F83D8890128127"),
                    load("2.png", "088231088F83D8890128127"),
                    load("field.png", "088231085F83D8890628127"),
                ).thenRun {
                    Map()
                }
            } else if (channel == "murder-start") {
                box.enabled = false
                online.animate(2, Easings.BACK_BOTH) {
                    offset.y = -25.0
                }
                role = NetUtil.readUtf8(data)
                detectiveAlive = rectangle {
                    origin = BOTTOM_RIGHT
                    align = BOTTOM_RIGHT
                    offset.y -= MAP_SIZE + 28
                    offset.x -= 25
                    size = V3(MAP_SIZE, 20.0, 0.0)
                    color = Color(0, 0, 0, 0.62)
                    addChild(text {
                        origin = CENTER
                        align = CENTER
                        content = "§bДетектив жив"
                        shadow = true
                    })
                }
                roleAndOnline = rectangle {
                    origin = BOTTOM_RIGHT
                    align = BOTTOM_RIGHT
                    offset.y -= MAP_SIZE + 51
                    offset.x -= 25
                    size = V3(MAP_SIZE, 36.0, 0.0)
                    color = Color(0, 0, 0, 0.62)
                    addChild(text {
                        origin = TOP
                        align = TOP
                        content = "§fмирных живо §216"
                        offset.y += 6
                        shadow = true
                    }, text {
                        origin = BOTTOM
                        align = BOTTOM
                        offset.y -= 6
                        content = "§fвы $role"
                        shadow = true
                    })
                }
                UIEngine.overlayContext.addChild(roleAndOnline, detectiveAlive)
            } else if (channel == "murder:update") {
                val detective = data.readBoolean()
                val online = data.readInt()
                (roleAndOnline.children[0] as TextElement).content = "§fмирных живо §2$online"
                (roleAndOnline.children[1] as TextElement).content = "§fвы $role"
                (detectiveAlive.children[0] as TextElement).content =
                    if (detective) "§bДетектив жив" else "§cДетектив мертв"
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
                    (online.children[1] as TextElement).content =
                        "Победа мирных через ${String.format("%02d:%02d", timeLess / 60, timeLess % 60)}"
                }
            } else if (channel == "murder:cooldown") {
                val text = NetUtil.readUtf8(data)
                val seconds = data.readInt() / 20.0
                cooldown.enabled = true
                (cooldown.children[1] as TextElement).content = text
                (cooldown.children[0] as RectangleElement).animate(seconds - 0.1) {
                    size.x = 0.0
                }
                UIEngine.overlayContext.schedule(seconds + 0.1) {
                    cooldown.enabled = false
                    (cooldown.children[0] as RectangleElement).size.x = 180.0
                }
            } else if (channel == "murder:title") {
                val text = message.children[0] as TextElement
                text.content = NetUtil.readUtf8(data)
                message.enabled = true
                text.animate(0.3) {
                    color.red = 255
                    color.green = 140
                    color.blue = 185
                    color.alpha = 1.0
                    scale.x = 2.2
                    scale.y = 2.2
                }
                UIEngine.overlayContext.schedule(3.1) {
                    text.animate(3.15) {
                        scale.x = 20.0
                        scale.y = 20.0
                        color.alpha = 0.0
                    }
                }
                UIEngine.overlayContext.schedule(3.3) {
                    message.enabled = false
                    text.color.red = 0
                    text.color.green = 0
                    text.color.blue = 0
                    text.color.alpha = 0.0
                    text.scale.x = 1.0
                    text.scale.y = 1.0
                }
            }
        }
    }
}