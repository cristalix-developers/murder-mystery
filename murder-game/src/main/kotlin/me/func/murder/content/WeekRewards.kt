package me.func.murder.content

import dev.implario.bukkit.item.item
import me.func.murder.MurderGame
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.impl.Corpse
import me.func.murder.donate.impl.LootboxUnit
import me.func.murder.donate.impl.Mask
import me.func.murder.donate.impl.NameTag
import me.func.murder.donate.impl.StepParticle
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

enum class WeekRewards(val title: String, val icon: ItemStack, val give: (User) -> Any) {
    ONE("§e32 монеты", MurderGame.gold, { it.giveMoney(32) }),
    TWO("§b1 Лутбокс", LootboxUnit.icon, { it.stat.lootbox++ }),
    THREE(
        Corpse.G2.rare.with(Corpse.G2.title),
        Corpse.G2.icon,
        { giveWithDuplicate(it, Corpse.G2, 64) }
    ),
    FOUR(
        StepParticle.FALLING_DUST.rare.with(StepParticle.FALLING_DUST.title),
        StepParticle.FALLING_DUST.icon,
        { giveWithDuplicate(it, StepParticle.FALLING_DUST, 128) }
    ),
    FIVE(
        NameTag.NINJA.rare.with(NameTag.NINJA.title),
        NameTag.NINJA.icon,
        { giveWithDuplicate(it, NameTag.NINJA, 256) }
    ),
    SIX(
        Mask.ONI.rare.with(Mask.ONI.title),
        Mask.ONI.icon,
        { giveWithDuplicate(it, Mask.ONI, 512) }
    ),
    SEVEN(
        "§b5 Лутбоксов",
        item {
            type = Material.CLAY_BALL
            enchant(Enchantment.DAMAGE_ALL, 1)
            nbt("other", "enderchest1")
        }, { repeat(5) { _ -> it.stat.lootbox++ } }
    );

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
