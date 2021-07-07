package me.func.murder.user

import dev.implario.bukkit.item.item
import org.bukkit.Material

enum class Role(val title: String, val start: ((User) -> Unit)?) {
    VILLAGER("§eМирный житель", null),
    DETECTIVE("§bДетектив", {
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
    MURDER("§cМаньяк", { it ->
        it.player!!.inventory.setItem(1, item {
            type = Material.IRON_SWORD
            text("§cОрудие убийства")
        }.build())
        org.bukkit.Bukkit.getOnlinePlayers().forEach { it.sendTitle("§eОсторожно!", "§cМаньяк с оружием") }
    }),
    NONE("Не отпределен", null)
}