package me.func.commons.user

import dev.implario.bukkit.item.item
import me.func.commons.light
import org.bukkit.Material

enum class Role(val title: String, val shortTitle: String, val start: ((User) -> Unit)?) {
    VICTIM("§2Жертва", "§2Жертва", {
        it.hearts = 2
        it.player!!.inventory.setItem(1, light)
        clepto.bukkit.B.postpone(100) {
            me.func.commons.mod.ModHelper.sendTitle(it, "§fОткрывайте\n§eсундуки 㫗")
        }
        clepto.bukkit.B.postpone(200) {
            me.func.commons.mod.ModHelper.sendTitle(it, "§fАктивируйте\n§bдвигатели §4⛽")
        }
    }),
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
            nbt("murder", "sherts")
            nbt("Unbreakable", 1)
        }.build())
        me.func.commons.mod.ModHelper.sendGlobalTitle("Маньяк получил оружие")
    }),
    NONE("§7Мертвы", "§7Убиты", null)
}