package me.func.murder.bar

import me.func.murder.activeStatus
import me.func.murder.app
import org.bukkit.Bukkit
import ru.cristalix.core.display.enums.EnumPosition
import ru.cristalix.core.display.enums.EnumUpdateType
import ru.cristalix.core.display.messages.ProgressMessage
import ru.cristalix.core.formatting.Color

object PlayBar : GameBar() {
    override val message: ProgressMessage
        get() {
            val timeLess = activeStatus.lastSecond - app.timer.time / 20
            return ProgressMessage
                .builder()
                .name("Мирные игроки победят через ${String.format("%02d:%02d", timeLess / 60, timeLess % 60)}")
                .updateType(EnumUpdateType.UPDATE)
                .color(Color.AQUA)
                .position(EnumPosition.TOPTOP)
                .start(0)
                .percent(1f - (app.timer.time / 20).toFloat() / (activeStatus.lastSecond).toFloat())
                .build()
        }
}