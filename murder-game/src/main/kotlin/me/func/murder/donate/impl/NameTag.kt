package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class NameTag(private val title: String, private val price: Int, private val rare: Rare) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON),
    FAST("Молния", 128, Rare.COMMON),
    CHAMPION("Чемпион", 4096, Rare.LEGENDARY);

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
            text(rare.with(title) + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.activeNameTag = this
        user.stat.donate.add(this)
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeNameTag == this
    }

    override fun getName(): String {
        return name
    }

}