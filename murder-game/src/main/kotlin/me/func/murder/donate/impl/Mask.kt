package me.func.murder.donate.impl

import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import me.func.murder.util.SkullManager

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
enum class Mask(
    override val title: String,
    override val price: Int,
    override val rare: Rare,
    private val url: String
) : DonatePosition {
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
    GAS_MASK(
        "Маска с противогазом",
        192,
        Rare.COMMON,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTFkNDNmZTM5MGJjMjA3MjZjNmU5ZGRjNGQ4NDU4YjU4ZGMwNGU4ZmU4OGFmOGJlYTFhMjU2OTY1ODc2MmM5MyJ9fX0="
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
    FOXI(
        "Фокси",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdhN2M5YTM5M2I4YmRmYjQ0YTc5MWU2OTA5M2Y2NGZiNTY4ZThmZTZjZjQ2M2ZlZGQ4ZmVmZDMxN2Q2ZDMyIn19fQ=="
    ),
    CHICA(
        "Чика",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE0YWRmYzNkY2NlNmViMmM0NzE4NjUzZThhYTJjY2Y1ODVhM2MyYzczYmU0YjBhZGE5NjdlZTM2YmE3NTVlIn19fQ=="
    ),
    BONNIE(
        "Бонни",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWU1ZmZiMTEwZmZhNzg4NWNjMDM3MDVhNGQxYzczNGQ3NTM2MDYzY2EwYmZhYTA1Nzc1NDViYmE4ZDA1YzdiMiJ9fX0="
    ),
    DIVER_HELMET(
        "Водолазный шлем",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5YzE2NzFiMDQ1ZTg0MDEyMmMyNzQxMWI3YzhiYTNlZDZjNTUxOGE3NjRjY2Q0M2Q1M2FiOWE1OWU5ODY4YSJ9fX0="
    ),
    RAPHAEL(
        "Рафаэль",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU5Yzc3NjQzN2U5OTRhYWNmYWI2MWNkYjVkZmQ2ZTdiOTNiZDkyZjUxMzUzMzk4ZjRhYmNmNzU2ZmEifX19"
    ),
    MICHELANGELO(
        "Микеланджело",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJlYjVlNTRiZTU2YzE0NGNkZjQzNGUyOWMxNTdiMTk4Zjk0YTkxNzZiYWU4OGRkNjVmMDgxODA1MzQyIn19fQ=="
    ),
    LEONARDO(
        "Леонардо",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWYzMjJjMzY3Y2JhZjI5OTYxNzlkZWQzOGM0Zjk2MmQ1NjljMmNmYzY3MTkwNjQ0N2NmMzRhMDNiNjQ5ZWM1In19fQ=="
    ),
    DONATELLO(
        "Донателло",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI5Y2NlYWEyNWJhZTdjMWU1YWIyOWM5ZjU4YjJlMTE1NWMxZTJkNTNmZWU5ODVlNzY0MTI5YzA1Njk4In19fQ=="
    ),
    SCREAM(
        "Крик",
        768,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzIxOTYxNjQyZDk4Y2I4MDFhMTc2MDhiYTRhMjMyOTc3YjQ2MmVmNjY3OWI5NzhjOWJiNjQ5NWQxNTE2MjczIn19fQ=="
    ),
    CREWMATE_ORANGE(
        "Амогус оранжевый",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE5YjE4MzNjMDg2YzQ4ZmI0ZmVmZTIzYjZhY2Q4MWRhMzU5MGI2MWVlZDQwZjM5NmRiZGY3YWE2ODE1OTBhOSJ9fX0="
    ),
    CREWMATE_WHITE(
        "Амогус белый",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI0ZGFhODQ5ZmZkMmVlODc4MjkzNDljNzQzNDY1MzIxNTRhOTU3MmVhZTBkZmVkMmJiMjgzMWYxYmQwOTdiIn19fQ=="
    ),
    CREWMATE_YELLOW(
        "Амогус жёлтый",
        1024,
        Rare.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ0NjczOGU2Y2VkYTY1MmU5ZTdiNzYxNzcwY2ZkMGE4Y2FkZDU1NGNmODdmMjAyZDFlMWU4N2U3YWYyZWRiYyJ9fX0="
    ),
    JASON(
        "Джейсон",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmUzOWZhM2FlZmY2NzE2Njc1NzFkNjU0MWY2ODVjY2Q2YzljNDE4NWY1ZDNhNWFmNTg3MmVjOTg3OWEyMDQ0In19fQ=="
    ),
    EMOJI(
        "Эмодзи",
        2048,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU0ZTk5NGVhY2Y5MGI2MGVlODdiMTBjNTBhY2I4MGRkMWRhZjllZTZmMmM2M2E3OWIwMTE1NGIxNmRjZjBjZiJ9fX0="
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
    CREWMATE_RED(
        "Амогус красный",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM0ZDQ0YTBmZmUwMjM0OWU5OWRhMDYyOTIxMzA2MzExM2U2YmIzYWZjMjU5ZjQ2NjE4YzkwZWRjZTgzMDc4NiJ9fX0="
    ),
    CREWMATE_PURPLE(
        "Амогус фиолетовый",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmE4ZWNjM2IzNmU5OWExNmY3NTgyMjA3ZGRkMzc1NjA4NGMwMmIwZmIwYjBhMTllNGQzYThkMjA2NzQwN2Y3ZSJ9fX0="
    ),
    CREWMATE_PINK(
        "Амогус розовый",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJiMDUwODgwM2MwOTA4OGI3MWQ5NWViZTU3YTM0NWY3NzU0NzMzZDBjOWEyN2Y4YzE0Y2FlY2U1NWY1MjBmZiJ9fX0="
    ),
    CREWMATE_LIME(
        "Амогус лаймовый",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTJlODgzY2QyODdjMTg1NWFmNmE3ZmEyNzZhNzEyNDc3Y2NkMDJiMWRmNGU4NjYzZDFhN2EwYzc1MDM3ZGMxZCJ9fX0="
    ),
    SAW(
        "Пила",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ3MDg4MWY3ZmYzODg5ZTI1ODU2NjIxYTU5MmU0MGM5OGRiNjBhODNjOTc2MGFiODAxNjdhNDUxODczMjc1NSJ9fX0="
    ),
    AHRI(
        "Ари",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjhiYTBlMjQzNzg1MDY2YzYzNWUwYjMzZjdlMmE1NzRiZTE4MGZlMTg5ZDRlZjk5NTM4NWI5MzJkM2NjYTc1OCJ9fX0="
    ),
    TIK_TOK(
        "ТикТок",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNmMjEwNWJiNzM3NjM4ODMzMDMzZGQ4MjQ0MDcxZTc1ODcwZTJlMTFjMjYxN2U1NDJlODkyNGZiMmI5MDE4MCJ9fX0="
    ),
    ANONIMUS(
        "Анонимус",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I4MDlhYzk5ODNjZTJlMjYxOTY1OTQ5NGU5NWE5OTRmYTZjOTEzNGY1OTFiOGE1NmY3NGU4NjZkMjk0YSJ9fX0="
    ),
    ANONIMUS3(
        "Маска Анонимус",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjFjNDc2NDNkZDRmOTU5NWFmMmY5YmY1ODNhN2YyNjgzZmE1MzdhNzIwMmM4M2UzY2Y2NDM5MTYwNzg5Zjc1YSJ9fX0="
    ),
    STAR_PLATINUM(
        "Стар Платинум",
        4096,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZkZDFhNmU1N2YwYjNmMjc4OGViNzRkZTUwMmRmZjYxMTQ2NzQ1MWM4ZjEwYTk0YWViZjczYjAyMjk4ZjE5MyJ9fX0="
    ),
    ANONIMUS2(
        "Элитный Анонимус",
        8192,
        Rare.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjliMGEzZTRjMGRjODFmNTRiNWEyZGI1NWNjNWM0OTFmNjQ1NWNlMWY5NDczZjk3MDU0NmQ1NjkyZWY4ZDFjIn19fQ=="
    ), ;


    override val icon by lazy {
        val skull = SkullManager.create(url)
        val meta = skull.itemMeta

        val lore = listOf("", "§fРедкость: ${rare.colored}", "§fЦена: ${MoneyFormatter.texted(price)}")
        meta.lore = lore

        skull.itemMeta = meta

        skull
    }

    override fun give(user: User) {
        user.stat.mask = this
        user.stat.donate!!.add(this) // todo
    }

    override fun isActive(user: User): Boolean {
        return user.stat.mask == this
    }

    fun setMask(user: User) {
        if (this == NONE) {
            user.player!!.inventory.helmet = null
            return
        }
        user.player!!.inventory.helmet = SkullManager.create(url)
    }
}
