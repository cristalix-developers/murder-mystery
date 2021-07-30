package me.func.commons.user

import dev.implario.bukkit.item.item
import org.bukkit.Material

enum class Role(val title: String, val shortTitle: String, val start: ((User) -> Unit)?) {
    VILLAGER("§2Мирный житель", "§2Мирный", null),
    DETECTIVE("§bДетектив", "§bДетектив", {
        it.player!!.inventory.setItem(1, item {
            type = Material.BOW
            nbt("Unbreakable", 1)
            text("§bЛук детектива")
        }.build())
        it.player!!.inventory.setItem(20, item {
            type = Material.ARROW
            text("§bСтрела детектива")
        }.build())
    }),
    MURDER("§cМаньяк", "§cМаньяк", {
        it.player!!.inventory.setItem(1, item {
            type = Material.IRON_SWORD
            text("§cОрудие убийства")
        }.build())
        me.func.commons.mod.ModHelper.sendGlobalTitle("Маньяк получил оружие")
    }),
    NONE("§7Мертвы", "§7Убиты", null)
}