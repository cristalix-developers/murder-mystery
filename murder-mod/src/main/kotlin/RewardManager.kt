import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.feder.NetUtil
import ru.cristalix.uiengine.UIEngine

class RewardManager {

    init {
        val week = mutableListOf<Day>()
        var currentDay: Int?

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "murder:weekly-reward") {
                currentDay = data.readInt()
                repeat(7) {
                    week.add(Day(it + 1, ItemTools.read(data), NetUtil.readUtf8(data), it + 1 > currentDay!!))
                }
                UIEngine.clientApi.minecraft().setIngameNotInFocus()
            }
        }
    }
}