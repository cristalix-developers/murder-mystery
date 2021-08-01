package me.func.commons.donate.impl

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.MoneyFormatter
import me.func.commons.donate.Rare
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

enum class KillMessage(private val title: String, private val price: Int, private val rare: Rare, private val format: String) : DonatePosition {

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
    KIRA("Я Кира", 8192, Rare.LEGENDARY, "§c§m%s умер от сердечного приступа."),;

    override fun getTitle(): String {
        return title
    }

    override fun getPrice(): Int {
        return price
    }

    override fun getRare(): Rare {
        return rare
    }

    override fun getIcon(): ItemStack {
        return item {
            type = Material.CLAY_BALL
            nbt("other", "pets1")
            text(rare.with(title) + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}\n§fПример: ${texted("func")}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.activeKillMessage = this
        user.stat.donate.add(this)
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeKillMessage == this
    }

    override fun getName(): String {
        return name
    }

    fun texted(nickname: String): String {
        return Formatting.error(format.format(nickname))
    }

}