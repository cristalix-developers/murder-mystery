import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.PlayerListRender
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        NeedHelp
        TimeBar
        MobIndicator

        val drops = Drop.values().associateWith { it.createLogo() }.toMutableMap()
        var loaded = false

        val context = Context3D(V3())
        val text = text {
            content = "§fЗагрузка...\n§bCristalix"
            scale = V3(3.0, 3.0, 3.0)
            color = WHITE
            origin = CENTER
            align = CENTER
            offset.z -= 0.3
        }
        var seconds = 0

        var message = "Осталось"

        registerHandler<PluginMessage> {
            if (channel == "hub:drop") {
                if (!loaded) {
                    drops.forEach { UIEngine.overlayContext.addChild(it.value) }
                }
                loaded = true
                val currentDrop = Drop.valueOf(NetUtil.readUtf8(data).toUpperCase())
                (drops[currentDrop]!!.children[2] as TextElement).content =
                    "§a${data.readInt()} §7из §f§l${currentDrop.needTotal}"
            } else if (channel == "hub:timer") {
                context.offset = V3(
                    data.readDouble(),
                    data.readDouble(),
                    data.readDouble()
                )

                seconds = data.readInt()
                message = NetUtil.readUtf8(data)

                val banner = rectangle {
                    size = V3(320.0, 100.0, 0.1)
                    color = Color(0, 0, 0, .62)
                    rotation = Rotation(2 * Math.PI, 0.0, 1.0, -0.1)
                    addChild(text)
                }

                context.addChild(banner)
                UIEngine.worldContexts.add(context)
            }
        }

        var open = false

        registerHandler<PlayerListRender> {
            if (drops.isNotEmpty() && loaded)
                isCancelled = true
        }

        var time = 0L
        registerHandler<GameLoop> {
            // Таб
            if (drops.isNotEmpty() && (!open && Keyboard.isKeyDown(Keyboard.KEY_TAB)) || (open && !Keyboard.isKeyDown(Keyboard.KEY_TAB))) {
                open = !open
                drops.forEach { it.value.enabled = open }
            }

            // Таймер
            if (System.currentTimeMillis() - time > 1000 && context.children.isNotEmpty()) {
                time = System.currentTimeMillis()
                seconds--
                text.content =
                    "§f$message\n§b${seconds / 60 / 60 / 24}§7д. §b${seconds / 60 / 60 % 24}§7ч. §b${seconds / 60 % 60}§7мин. §b${seconds % 60}§7с."
            }
        }
    }
}