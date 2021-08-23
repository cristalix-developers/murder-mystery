import dev.xdark.clientapi.item.ItemStack
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.AbstractElement
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.*

class Day(private val day: Int, private val icon: ItemStack, private val name: String, private val claimed: Boolean) : RectangleElement() {

    init {
        offset.x = -(4 - day) * 50.0
        origin = CENTER
        align = CENTER
        size = V3(40.0, 120.0)
        color = Color(0, 0, 0, if (claimed) 0.4 else 0.62)
        onClick = { element: AbstractElement, b: Boolean, mouseButton: MouseButton ->
            UIEngine.clientApi.chat().printChatMessage("День $day")
            UIEngine.clientApi.minecraft().setIngameFocus()
        }
        addChild(text {
            offset.y -= 15
            origin = TOP
            align = TOP
            color = if (claimed) {
                Color(60, 60, 255, 1.0)
            } else {
                Color(100, 100, 100, 1.0)
            }
            shadow = true
            content = "День $day"
        }, item {
            origin = CENTER
            align = CENTER
            scale = V3(2.0, 2.0)
            stack = icon
        }, text {
            offset.y += 15
            origin = BOTTOM
            align = BOTTOM
            scale = V3(2.0, 2.0)
            color = if (claimed) {
                Color(60, 60, 255, 1.0)
            } else {
                Color(100, 100, 100, 1.0)
            }
            shadow = true
            content = name
        })
    }

}
