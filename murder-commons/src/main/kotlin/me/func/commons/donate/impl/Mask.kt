package me.func.commons.donate.impl

import me.func.commons.donate.DonatePosition
import me.func.commons.donate.MoneyFormatter
import me.func.commons.donate.Rare
import me.func.commons.user.User
import me.func.commons.util.SkullManager
import org.bukkit.inventory.ItemStack

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
enum class Mask(private val title: String, private val price: Int, private val rare: Rare, private val url: String) :
    DonatePosition {
    NONE(
        "Без маски",
        0,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ5NGEzMGEyNzk5ZWJmYmM4YWQ3NzZiOTM4N2YzZTlkMTc5OWJiYTg5MDAwMTNhYzk5MmRiMWZiYWQ0MWNlNyJ9fX0="
    ),
    COMEDY_MASK(
        "Комедийная маска",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTljOTEwYTFlMmRhMmI3MzdkNGMwYmUwZWY5OTRlMTAzZTFlZGU0YjA1OTkyODA3NjNiMjg1N2E3OTM5NmNhZiJ9fX0="
    ),
    HAZMAT_SUIT(
        "Защитный комплект",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRjY2VhZDIyZDZhZGJmZjc2NGNhMzE1ZTIxOWJiNWYyZjQ1N2NiNWM0MzAyN2ViY2Y1NzZhN2Y2NzZkNjMifX19"
    ),
    GAS_MASK(
        "Маска с противогазом",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTFkNDNmZTM5MGJjMjA3MjZjNmU5ZGRjNGQ4NDU4YjU4ZGMwNGU4ZmU4OGFmOGJlYTFhMjU2OTY1ODc2MmM5MyJ9fX0="
    ),
    GUY_FAWKES(
        "Маска Гая Фокса",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRiODIxNzJjZjNlMzQ1MmQ3MDg2MWRlZGM1YjZhYjE2ODgxZGRjNGJjMjMzMTNiODljYTA3MTYzNDYyYzc5In19fQ=="
    ),
    SAW(
        "Пила",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ3MDg4MWY3ZmYzODg5ZTI1ODU2NjIxYTU5MmU0MGM5OGRiNjBhODNjOTc2MGFiODAxNjdhNDUxODczMjc1NSJ9fX0="
    ),
    DALLAS(
        "Даллас",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U0ZGI5YzJjMDY3ZWIyODNkZjk4NzU0NzIxNjJkZWY2MGY2N2JlNGY1MzhmNmRhZWU3YWY5ODE4MjZiYzc3MiJ9fX0="
    ),
    HOUSTON(
        "Хьюстон",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgzOTJiODg1MjRjNTdjMjk3MTc1N2QwMTcwYjllMDFjZWNhYThhNmUxZThjYmVkY2VmMTIwOGJlMDQxNzhlNCJ9fX0="
    ),
    CHAINS(
        "Чейнс",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIyOWE5MjhjYzQzZmUwN2YwYzFmNDg2MzIwYTU5YjY2NWE1MWJjYmRjYjY1MGUzYTFhZDk3MjE1Mzg3MjVhZCJ9fX0="
    ),
    ;

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
        val skull = SkullManager.create(url)
        val meta = skull.itemMeta

        meta.displayName =
            rare.with(title) + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}"
        skull.itemMeta = meta

        return skull
    }

    override fun give(user: User) {
        user.stat.mask = this
        user.stat.donate.add(this)
    }

    override fun isActive(user: User): Boolean {
        return user.stat.mask == this
    }

    override fun getName(): String {
        return name
    }

    fun getURL(): String {
        return url
    }

    fun setMask(user: User) {
        if (this == NONE) {
            user.player!!.inventory.helmet = null
            return
        }
        user.player!!.inventory.helmet = SkullManager.create(url)
    }
}