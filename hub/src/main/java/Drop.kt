import dev.xdark.clientapi.item.ItemStack
import dev.xdark.clientapi.nbt.NBTPrimitive
import dev.xdark.clientapi.nbt.NBTTagCompound
import dev.xdark.clientapi.nbt.NBTTagString
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.*

enum class Drop(private val title: String, private val texture: String, val needTotal: Int) {
    SKULL("Черепушки", "skull",  50),
    TROLLCLAW("Коготь тролля", "trollclaw",  10),
    AID_CROWN("Корона Аида", "aid_crown",  1),
    SPIT("Слюна Цербера", "spit",  20),
    CROW_FEATHERS("Перья Ворона", "crow_feathers",  30),;

    fun createLogo(): RectangleElement {
        val padding = 6
        val base = 20.0
        val headSize = 2.0

        return rectangle {
            enabled = false
            size = V3(220.0, base)
            origin = TOP
            align = TOP
            color = Color(0, 0, 0, 0.62)
            offset.y += base + (base + padding) * ordinal + padding
            addChild(item {
                origin = LEFT
                align = LEFT
                color = WHITE
                offset.y += base * headSize + padding * (headSize - 0.5)
                size = V3(64.0, 64.0)
                scale = V3(headSize, headSize, headSize)
                stack = ItemStack.of(
                    NBTTagCompound.of(
                        mapOf(
                            "id" to NBTTagString.of("clay_ball"),
                            "Count" to NBTPrimitive.of(1),
                            "Damage" to NBTPrimitive.of(2),
                            "tag" to NBTTagCompound.of(
                                mapOf(
                                    "helloween" to NBTTagString.of(texture)
                                )
                            )
                        )
                    )
                )
            }, text {
                origin = LEFT
                align = LEFT
                offset.x += base * (headSize - 0.3)
                content = "§7${title}"
            }, text {
                origin = RIGHT
                align = RIGHT
                offset.x -= padding
                content = "§bзагрузка... §7из §f§l$needTotal"
            })
        }
    }
}
