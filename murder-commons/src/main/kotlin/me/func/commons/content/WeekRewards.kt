package me.func.commons.content

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.impl.*
import me.func.commons.gold
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

enum class WeekRewards(val title: String, val icon: ItemStack, val give: (User) -> Any) {
    ONE("§e32 монеты", gold, { it.giveMoney(32) }),
    TWO("§b1 Лутбокс", LootboxUnit.getIcon(), { it.stat.lootbox++ }),
    THREE(
        Corpse.G2.getRare().with(Corpse.G2.getTitle()),
        Corpse.G2.getIcon(),
        { me.func.commons.content.WeekRewards.Companion.giveWithDuplicate(it, Corpse.G2, 64) }
    ),
    FOUR(
        StepParticle.FALLING_DUST.getRare().with(StepParticle.FALLING_DUST.getTitle()),
        StepParticle.FALLING_DUST.getIcon(),
        { me.func.commons.content.WeekRewards.Companion.giveWithDuplicate(it, StepParticle.FALLING_DUST, 128) }
    ),
    FIVE(
        NameTag.NINJA.getRare().with(NameTag.NINJA.getTitle()),
        NameTag.NINJA.getIcon(),
        { me.func.commons.content.WeekRewards.Companion.giveWithDuplicate(it, NameTag.NINJA, 256) }
    ),
    SIX(
        Mask.ONI.getRare().with(Mask.ONI.getTitle()),
        Mask.ONI.getIcon(),
        { me.func.commons.content.WeekRewards.Companion.giveWithDuplicate(it, Mask.ONI, 512) }
    ),
    SEVEN(
        "§b5 Лутбоксов",
        item {
            type = Material.CLAY_BALL
            enchant(Enchantment.DAMAGE_ALL, 1)
            nbt("other", "enderchest1")
        }.build(), { repeat(5) { _ -> it.stat.lootbox++ } }
    )
    ;

    companion object {
        fun giveWithDuplicate(user: User, donate: DonatePosition, reward: Int) {
            if (user.stat.donate.contains(donate)) {
                user.stat.money += reward
                user.player!!.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §e$reward монет§f."))
            } else {
                donate.give(user)
            }
        }
    }
}