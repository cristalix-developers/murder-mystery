package me.func.murder.user

import dev.implario.bukkit.item.item
import me.func.murder.MurderGame
import me.func.murder.mod.ModHelper
import org.bukkit.Material

enum class Role(val title: String, val shortTitle: String, val start: (User, MurderGame) -> Unit) {
    VICTIM(
        "§2Жертва",
        "§2Жертва",
        { user, game ->
            user.hearts = 2
            user.player!!.inventory.setItem(1, MurderGame.light)
            game.context.after(100) {
                ModHelper.sendTitle(user, "§fОткрывайте\n§eсундуки 㫗")
            }
            game.context.after(200) {
                ModHelper.sendTitle(user, "§fАктивируйте\n§bдвигатели §4⛽")
            }
        }
    ),
    VILLAGER(
        "§2Мирный житель",
        "§2Мирный",
        { _, _ -> }
    ),
    DETECTIVE(
        "§bДетектив",
        "§bДетектив",
        { user, _ ->
            user.player!!.inventory.setItem(1, item {
                type = Material.BOW
                nbt("Unbreakable", 1)
                text("§bЛук детектива")
            })
            user.player!!.inventory.setItem(20, item {
                type = Material.ARROW
                text("§bСтрела детектива")
            })
        }
    ),
    MURDER(
        "§cМаньяк",
        "§cМаньяк",
        { it, game ->
            it.player!!.inventory.setItem(1, item {
                type = Material.IRON_SWORD
                text("§cОрудие убийства")
                nbt("murder", "sherts")
                nbt("Unbreakable", 1)
            })

            game.modHelper.sendGlobalTitle("Маньяк получил оружие")
        }
    ),
    NONE(
        "§7Мертвы",
        "§7Убиты",
        { _, _ -> }
    )
}
