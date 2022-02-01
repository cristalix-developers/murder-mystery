package me.func.commons.donate.impl

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.Rare
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

// @formatter:off
enum class MoneyKit(
    override val title: String,
    override val price: Int,
    override val rare: Rare,
    override val icon: ItemStack,
    private val reward: Int
) : DonatePosition {
    NONE(
        "Отсутсвует",
        0,
        Rare.COMMON,
        item {}.build(),
        0
    ),
    SMALL(
        "Пара монет",
        10,
        Rare.COMMON,
        item {
            type = Material.CLAY_BALL
            text("§eПара монет §7> §f32\n\n§7Получите §e32 монеты\n§7за §b10 кристаликов§7.")
            nbt("other", "coin2")
        }.build(),
        32
    ),
    NORMAL(
        "Мешок монет",
        39,
        Rare.RARE,
        item {
            type = Material.CLAY_BALL
            text("§eПара монет §7> §f128\n\n§7Получите §e256 монет\n§7за §b39 кристаликов§7.\n\n§aСкидка 30%")
            nbt("other", "bag1")
        }.build(),
        256
    ),
    BIG(
        "Коробка монет",
        119,
        Rare.EPIC,
        item {
            type = Material.CLAY_BALL
            text("§eПара монет §7> §f1024\n\n§7Получите §e1024 монеты\n§7за §b119 кристаликов§7.\n\n§aСкидка 30%")
            nbt("other", "new_lvl_rare_close")
        }.build(),
        1024
    ),
    HUGE(
        "Гора монет",
        499,
        Rare.LEGENDARY,
        item {
            type = Material.TOTEM
            text("§eПара монет §7> §f8192\n\n§7Получите §e8192 монеты\n§7за §b499 кристаликов§7.\n\n§aСкидка 70%")
            nbt("other", "knight")
        }.build(),
        8192
    );

    override fun give(user: User) {
        user.giveMoney(reward)
    }

    override fun isActive(user: User): Boolean {
        return false
    }
}
// @formatter:on