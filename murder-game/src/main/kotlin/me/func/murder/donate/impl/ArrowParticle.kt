package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
enum class ArrowParticle(
    override val title: String,
    override val price: Int,
    override val rare: Rare,
    val particle: Particle?,
    private val material: Material
) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON, null, Material.BARRIER),
    SLIME("Слизь", 192, Rare.COMMON, Particle.SLIME, Material.SLIME_BALL),
    WATER_DROP("Капли воды", 256, Rare.COMMON, Particle.DRIP_WATER, Material.WATER_BUCKET),
    FALLING_DUST("Падающая пыль", 512, Rare.RARE, Particle.FALLING_DUST, Material.FLINT),
    SPELL_INSTANT("Феерверк", 512, Rare.RARE, Particle.SPELL_INSTANT, Material.FIREWORK),
    REDSTONE("Красный камень", 768, Rare.RARE, Particle.REDSTONE, Material.REDSTONE),
    VILLAGER_ANGRY("Злой житель", 1024, Rare.EPIC, Particle.VILLAGER_ANGRY, Material.FIREWORK_CHARGE),
    SPELL_WITCH("Колдунья", 2048, Rare.EPIC, Particle.SPELL_WITCH, Material.NETHER_STALK),
    VILLAGER_HAPPY("Счастливый житель", 2048, Rare.EPIC, Particle.VILLAGER_HAPPY, Material.LIME_GLAZED_TERRACOTTA),
    FLAME("Огонь", 2048, Rare.EPIC, Particle.FLAME, Material.FLINT_AND_STEEL),
    LAVA("Лава", 4096, Rare.LEGENDARY, Particle.LAVA, Material.LAVA_BUCKET),
    NOTE("Ноты", 4096, Rare.LEGENDARY, Particle.NOTE, Material.BOOK),
    HEAR("Сердечки", 4096, Rare.LEGENDARY, Particle.HEART, Material.DIAMOND);

    override val icon: ItemStack = item {
        type = material
        text(rare.with(title) + "\n\n§fРедкость: ${rare.colored}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
    }

    override fun give(user: User) {
        user.stat.arrowParticle = this
        user.stat.donate!!.add(this) // todo nullable bruh
    }

    override fun isActive(user: User) = user.stat.arrowParticle == this
}
