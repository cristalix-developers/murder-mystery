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
    HORROR(
        "Маска ужаса",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY4ZWFlYTc1MDYzM2E0MjY5MjU1MzM1NTgzMWJkNzM3ODUxOTMyYTNmNzgxN2I4MDdkNmM3NDRhODUyOWVmYSJ9fX0="
    ),
    TRADEGY(
        "Маска трагедии",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRjZTNkMTAwZGQ5NTNiODk2ZTFjOTQ0MGExMjI5N2YzMDExMGEzODg5YzQ5Y2UxOWNmOGI4YzA2MDRmY2M5ZSJ9fX0="
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
    DEMON(
        "Демон",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI5NzViNjdjMTlmOWJhMjM0NGY4ZWVlOTU2YzUwMTVhZDYzZDllODhhZDQ4ODJhZTc5MzY5Mzc0ZmIzOTc1In19fQ=="
    ),
    CHAINS(
        "Чейнс",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIyOWE5MjhjYzQzZmUwN2YwYzFmNDg2MzIwYTU5YjY2NWE1MWJjYmRjYjY1MGUzYTFhZDk3MjE1Mzg3MjVhZCJ9fX0="
    ),
    DIVER_HELMET(
        "Водолазный шлем",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5YzE2NzFiMDQ1ZTg0MDEyMmMyNzQxMWI3YzhiYTNlZDZjNTUxOGE3NjRjY2Q0M2Q1M2FiOWE1OWU5ODY4YSJ9fX0="
    ),
    SCREAM(
        "Крик",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzIxOTYxNjQyZDk4Y2I4MDFhMTc2MDhiYTRhMjMyOTc3YjQ2MmVmNjY3OWI5NzhjOWJiNjQ5NWQxNTE2MjczIn19fQ=="
    ),
    NINJA(
        "Ниндзя",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQzZmQyNjdiOGVkN2U4Y2JlY2RmZjdlMDdlNTM4MThjMDA5ZjQzYThjNTQ2NGNjMWNkOTI2ZDQxMTc1N2ZhYyJ9fX0="
    ),
    RAPHAEL(
        "Рафаэль",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU5Yzc3NjQzN2U5OTRhYWNmYWI2MWNkYjVkZmQ2ZTdiOTNiZDkyZjUxMzUzMzk4ZjRhYmNmNzU2ZmEifX19"
    ),
    MICHELANGELO(
        "Микеланджело",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJlYjVlNTRiZTU2YzE0NGNkZjQzNGUyOWMxNTdiMTk4Zjk0YTkxNzZiYWU4OGRkNjVmMDgxODA1MzQyIn19fQ=="
    ),
    LEONARDO(
        "Леонардо",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWYzMjJjMzY3Y2JhZjI5OTYxNzlkZWQzOGM0Zjk2MmQ1NjljMmNmYzY3MTkwNjQ0N2NmMzRhMDNiNjQ5ZWM1In19fQ=="
    ),
    DONATELLO(
        "Донателло",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI5Y2NlYWEyNWJhZTdjMWU1YWIyOWM5ZjU4YjJlMTE1NWMxZTJkNTNmZWU5ODVlNzY0MTI5YzA1Njk4In19fQ=="
    ),
    JASON(
        "Джейсон",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmUzOWZhM2FlZmY2NzE2Njc1NzFkNjU0MWY2ODVjY2Q2YzljNDE4NWY1ZDNhNWFmNTg3MmVjOTg3OWEyMDQ0In19fQ=="
    ),
    EVIL_CLOWN(
        "Злой клоун",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY1MTI1YzJkYTAwNmYyYjJkOWZlY2M2NjRmMjgzZjEyNDNiYzhkOGM3NTBmYjk2NDc2ZWJhZjY5ODgyYWMzYyJ9fX0="
    ),
    EMOJI(
        "Эмодзи",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU0ZTk5NGVhY2Y5MGI2MGVlODdiMTBjNTBhY2I4MGRkMWRhZjllZTZmMmM2M2E3OWIwMTE1NGIxNmRjZjBjZiJ9fX0="
    ),
    FOXI(
        "Фокси",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdhN2M5YTM5M2I4YmRmYjQ0YTc5MWU2OTA5M2Y2NGZiNTY4ZThmZTZjZjQ2M2ZlZGQ4ZmVmZDMxN2Q2ZDMyIn19fQ=="
    ),
    CHICA(
        "Чика",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE0YWRmYzNkY2NlNmViMmM0NzE4NjUzZThhYTJjY2Y1ODVhM2MyYzczYmU0YjBhZGE5NjdlZTM2YmE3NTVlIn19fQ=="
    ),
    BONNIE(
        "Бонни",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWU1ZmZiMTEwZmZhNzg4NWNjMDM3MDVhNGQxYzczNGQ3NTM2MDYzY2EwYmZhYTA1Nzc1NDViYmE4ZDA1YzdiMiJ9fX0="
    ),
    FREDDY(
        "Фредди",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWUxNTEwMzE5MDc5MzVmZTc0ODMwYmQwZmZmZjQ2MzhiNjYxYzEwYmM5M2I3ZDJkZWNlNWU1NGY2Zjc0NWZlYiJ9fX0="
    ),
    ONI(
        "Маска Они",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRmYmI4Y2ZkZjJlZWRmZjBkMTRhZDJjZDkxOWQzZDRmYTEyZTNlZDBmOTk2NzAwOGEzZTM5MzliNDMyMzc3MSJ9fX0="
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

        val lore = listOf("","§fРедкость: ${rare.getColored()}", "§fСтоимость: ${MoneyFormatter.texted(price)}")
        meta.lore = lore

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

    fun setMask(user: User) {
        if (this == NONE) {
            user.player!!.inventory.helmet = null
            return
        }
        user.player!!.inventory.helmet = SkullManager.create(url)
    }
}