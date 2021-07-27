package me.func.murder.donate

import me.func.murder.user.User
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.item.Items

object DonateHelper {

    fun modifiedItem(user: User, donate: DonatePosition): ItemStack {
        return Items.fromStack(donate.getIcon())
            .displayName(when {
                donate.isActive(user) -> "§lВЫБРАНО"
                user.stat.donate.contains(donate) -> "§aВыбрать"
                else -> "§bПосмотреть"
            } + "§7").build()
    }

}