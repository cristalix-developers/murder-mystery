package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class NameTag(
    override val title: String,
    override val price: Int,
    override val rare: Rare
) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON),
    NEWBIE("Новичок", 128, Rare.COMMON),
    FAST("Молния", 480, Rare.COMMON),
    CAIN("Каин", 640, Rare.COMMON),
    NINJA("Ниндзя", 1280, Rare.RARE),
    EXECUTIONER("Палач", 1280, Rare.RARE),
    STRATEGIST("Стратег", 1600, Rare.RARE),
    SECRETAGENT("Тайный агент", 1600, Rare.RARE),
    INVESTIGATOR("Следователь", 2048, Rare.EPIC),
    KILLER("Киллер", 2048, Rare.EPIC),
    CHAMPION("Чемпион", 4096, Rare.LEGENDARY),
    LEGEND("Легенда", 4864, Rare.LEGENDARY);

    override val icon: ItemStack = item {
        type = Material.CLAY_BALL
        nbt("other", "pets1")
        text(rare.with(title) + "\n\n§fРедкость: ${rare.colored}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
    }

    override fun give(user: User) {
        user.stat.activeNameTag = this
        user.stat.donate!!.add(this) /// todo
    }

    override fun isActive(user: User) = user.stat.activeNameTag == this
}
