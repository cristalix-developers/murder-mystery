package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

object StarterPack : DonatePosition {
    override val name = "StarterPack"
    override val title = "Начальный набор"
    override val price = 89
    override val rare = Rare.LEGENDARY

    override val icon = item {
        type = Material.CLAY_BALL
        enchant(Enchantment.LUCK, 0)
        nbt("other", "unique")
        nbt("HideFlags", 63)
        text("§bСтартовый набор\n\n§7Вы получите §b3 лутбокса\n§7и §e512 монет§7.\n\n§7Купить за §b89 кристаликов")
    }

    override fun give(user: User) {
        user.stat.lootbox += 3
        user.giveMoney(512)
    }

    override fun isActive(user: User) = false
}
