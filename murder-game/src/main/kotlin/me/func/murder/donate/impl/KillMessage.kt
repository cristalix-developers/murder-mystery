package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

enum class KillMessage(
    override val title: String,
    override val price: Int,
    override val rare: Rare,
    private val format: String
) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON, "§c§m%s§f был убит."),
    GLOBAL("Внезапная смерть", 64, Rare.COMMON, "§c§m%s§f внезапно умер."),
    AHEAD("Гонщик", 128, Rare.COMMON, "§c§m%s§f остался без головы."),
    DEAD("Смерть", 256, Rare.COMMON, "§c§m%s§f встретил смерть."),
    END("Конец", 768, Rare.RARE, "§fДля §c§m%s§f настал конец."),
    SLEEP("Сон", 1024, Rare.RARE, "§c§m%s§f уснул навсегда."),
    HORNY("На кусочки", 1024, Rare.RARE, "§c§m%s§f разорван на кусочки."),
    ROOM("Комната", 2048, Rare.EPIC, "§c§m%s§f обнаружен с ножом в голове."),
    BLACK("Черный кот", 2048, Rare.EPIC, "§c§m%s§f перешел дорогу черному коту."),
    X("Люди в черном", 8192, Rare.LEGENDARY, "§c§m§fНеизвестный умер."),
    KIRA("Я Кира", 8192, Rare.LEGENDARY, "§c§m%s умер от сердечного приступа.");

    override val icon: ItemStack = item {
        type = Material.CLAY_BALL
        nbt("other", "pets1")
        text(
            rare.with(title) + "\n\n§fРедкость: ${rare.colored}\n§fСтоимость: ${MoneyFormatter.texted(price)}\n§fПример: ${
                texted("func")
            }"
        )
    }

    override fun give(user: User) {
        user.stat.activeKillMessage = this
        user.stat.donate!!.add(this) // todo
    }

    override fun isActive(user: User) = user.stat.activeKillMessage == this

    fun texted(nickname: String): String = Formatting.error(format.format(nickname))
}
