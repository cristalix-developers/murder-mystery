package me.func.commons.content

import dev.implario.bukkit.item.item
import me.func.commons.donate.impl.*
import me.func.commons.gold
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

enum class WeekRewards(val title: String, val icon: ItemStack, val give: (User) -> Any) {
    ONE("§e32 монеты", gold, { it.giveMoney(32) }),
    TWO("§b1 Лутбокс", LootboxUnit.getIcon(), { it.stat.lootbox++ }),
    THREE(
        Corpse.G2.getRare().with(Corpse.G2.getTitle()),
        Corpse.G2.getIcon(),
        { Corpse.G2.give(it) }
    ),
    FOUR(
        StepParticle.FALLING_DUST.getRare().with(StepParticle.FALLING_DUST.getTitle()),
        StepParticle.FALLING_DUST.getIcon(),
        { StepParticle.FALLING_DUST.give(it) }
    ),
    FIVE(
        NameTag.NINJA.getRare().with(NameTag.NINJA.getTitle()),
        NameTag.NINJA.getIcon(),
        { NameTag.NINJA.give(it) }
    ),
    SIX(
        Mask.ONI.getRare().with(Mask.ONI.getTitle()),
        Mask.ONI.getIcon(),
        { Mask.ONI.give(it) }
    ),
    SEVEN(
        "§b5 Лутбоксов",
        item {
            type = Material.CLAY_BALL
            enchant(Enchantment.DAMAGE_ALL, 1)
            nbt("other", "enderchest1")
        }.build(), { repeat(5) { _ -> it.stat.lootbox++ } }
    )
}