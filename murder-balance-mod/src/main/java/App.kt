import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.ArmorRender
import dev.xdark.clientapi.event.render.ExpBarRender
import dev.xdark.clientapi.event.render.HealthRender
import dev.xdark.clientapi.event.render.HungerRender
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.BOTTOM_RIGHT
import ru.cristalix.uiengine.utility.text

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        UIEngine.registerHandler(HealthRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(ExpBarRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(HungerRender::class.java) { isCancelled = true }
        UIEngine.registerHandler(ArmorRender::class.java) { isCancelled = true }

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