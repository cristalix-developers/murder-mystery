package me.func.murder.util

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
object SkullManager {

    fun create(url: String): ItemStack {
        val skull = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val skullMeta: SkullMeta = skull.itemMeta as SkullMeta

        Bukkit.createProfile(UUID.randomUUID(), "").properties.add(ProfileProperty("textures", url))

        return skull.apply { itemMeta = skullMeta }
    }
}