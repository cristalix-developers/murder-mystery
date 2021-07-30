import dev.xdark.clientapi.event.network.PluginMessage
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.BOTTOM_RIGHT
import ru.cristalix.uiengine.utility.text

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        val balanceText = text {
            content = "§aЗагрузка..."
            origin = BOTTOM_RIGHT
            align = BOTTOM_RIGHT
            shadow = true
            offset.y -= 15
            offset.x -= 3
        }

        UIEngine.overlayContext.addChild(balanceText)

        registerHandler<PluginMessage> {
            if (channel == "murder:balance") {
                balanceText.content = "§e${data.readInt()} монет"
            }
        }
    }
}