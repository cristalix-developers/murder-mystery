package me.func.murder.donate

import me.func.murder.user.User
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object DonateHelper {

    fun modifiedItem(user: User, donate: DonatePosition): ItemStack {
        val clone = donate.getIcon().clone()
        val meta = clone.itemMeta
        meta.displayName = when {
            donate.isActive(user) -> {
                meta.addEnchant(Enchantment.LUCK, 1, false)
                "§f§lВЫБРАНО"
            }
            user.stat.donate.contains(donate) -> "§aВыбрать"
            else -> "§bПосмотреть"
        } + " §7${donate.getTitle()}"
        clone.itemMeta = meta
        return clone
    }

}