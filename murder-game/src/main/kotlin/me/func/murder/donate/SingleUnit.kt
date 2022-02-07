package me.func.murder.donate

import me.func.murder.user.User
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object DonateHelper {

    fun modifiedItem(user: User, donate: DonatePosition): ItemStack {
        val clone = donate.icon.clone()
        val meta = clone.itemMeta

        meta.displayName = when {
            donate.isActive(user) -> {
                meta.addEnchant(Enchantment.LUCK, 1, false)
                "§f§lВЫБРАНО"
            }
            user.stat.donate!!.contains(donate) -> "§aВыбрать" // todo nullable!!?
            else -> "§bПосмотреть"
        } + " §7${donate.title}"

        clone.itemMeta = meta
        return clone
    }
}
