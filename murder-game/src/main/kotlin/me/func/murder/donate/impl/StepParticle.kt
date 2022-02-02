package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack

enum class StepParticle(
    override val title: String,
    override val price: Int,
    override val rare: Rare,
    private val mat: Material,
    val type: Particle
) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON, Material.BARRIER, Particle.SPELL_INSTANT),
    SLIME("Слизь", 192, Rare.COMMON, Material.SLIME_BALL, Particle.SLIME),
    WATER_DROP("Капли воды", 256, Rare.COMMON, Material.WATER_BUCKET, Particle.DRIP_WATER),
    FALLING_DUST("Падающая пыль", 512, Rare.RARE, Material.FLINT, Particle.FALLING_DUST),
    SPELL_INSTANT("Феерверк", 512, Rare.RARE, Material.FIREWORK, Particle.SPELL_INSTANT),
    REDSTONE("Красный камень", 768, Rare.RARE, Material.REDSTONE, Particle.REDSTONE),
    VILLAGER_ANGRY("Злой житель", 1024, Rare.EPIC, Material.FIREWORK_CHARGE, Particle.VILLAGER_ANGRY),
    SPELL_WITCH("Колдунья", 2048, Rare.EPIC, Material.NETHER_STALK, Particle.SPELL_WITCH),
    VILLAGER_HAPPY("Счастливый житель", 2048, Rare.EPIC, Material.LIME_GLAZED_TERRACOTTA, Particle.VILLAGER_HAPPY),
    FLAME("Огонь", 2048, Rare.EPIC, Material.FLINT_AND_STEEL, Particle.FLAME),
    LAVA("Лава", 4096, Rare.LEGENDARY, Material.LAVA_BUCKET, Particle.LAVA),
    NOTE("Ноты", 4096, Rare.LEGENDARY, Material.BOOK, Particle.NOTE),
    HEAR("Сердечки", 4096, Rare.LEGENDARY, Material.DIAMOND, Particle.HEART);

    override val icon: ItemStack = item {
        type = mat
        text(rare.with(title) + "\n\n§fРедкость: ${rare.colored}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
    }

    override fun give(user: User) {
        user.stat.activeParticle = this
        user.stat.donate!!.add(this) // todo
    }

    override fun isActive(user: User) = user.stat.activeParticle == this
}
