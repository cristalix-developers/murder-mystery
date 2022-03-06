import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.TOP
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.text

const val NAMESPACE = "murder"
const val FILE_STORE = "https://implario.dev/murder/"

lateinit var app: App

class App : KotlinMod() {

    lateinit var heart: TextElement

    override fun onEnable() {
        app = this

        UIEngine.initialize(this)
        Chances
        NeedHelp

        registerChannel("dbd:heart-create") {
            heart = text {
                content = "§4❤❤"
                align = TOP
                origin = TOP
                offset.y += 20
                shadow = true
                scale = V3(2.0, 2.0)
            }
            UIEngine.overlayContext.addChild(heart)
        }

        registerChannel("dbd:heart-update") {
            heart.animate(0.05) {
                heart.scale.y = 4.0
                heart.scale.x = 4.0
            }

            val hearts = readInt()
            GlowEffect.show(0.1, 255, 0, 0, if (hearts == 1) 0.4 else 0.07)

            UIEngine.schedule(0.05) {
                heart.animate(0.05) {
                    heart.scale.y = 1.0
                    heart.scale.x = 1.0
                }
                if (hearts == 1) GlowEffect.show(0.1, 255, 0, 0, 0.0)
            }
            UIEngine.schedule(0.1) {
                heart.animate(0.1) {
                    heart.scale.y = 3.3
                    heart.scale.x = 3.3
                }
                if (hearts == 1) GlowEffect.show(0.1, 255, 0, 0, 0.3)
            }
            UIEngine.schedule(0.2) {
                heart.animate(0.1) {
                    heart.scale.y = 2.0
                    heart.scale.x = 2.0
                }
                if (hearts == 1) GlowEffect.show(0.1, 255, 0, 0, 0.6)
            }
            heart.content = "§4"
            repeat(hearts) { heart.content += "❤" }
        }
    }
}
