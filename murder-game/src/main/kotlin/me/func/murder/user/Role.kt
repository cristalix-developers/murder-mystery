package me.func.murder.user

import dev.implario.bukkit.item.item
import org.bukkit.Material

enum class Role(val title: String, val start: (User) -> Unit) {
    VILLAGER("Мирный житель", {}),
    DETECTIVE("Детектив", {
        it.player!!.inventory.setItem(2, item {
            type = Material.BOW
            text("Лук детектива")
        }.build())
    }),
    MURDER("Убийца", {
        it.player!!.inventory.setItem(2, item {
            type = Material.IRON_SWORD
            text("Орудие убийства")
        }.build())
    }),
    NONE("Не отпределен", {})
}