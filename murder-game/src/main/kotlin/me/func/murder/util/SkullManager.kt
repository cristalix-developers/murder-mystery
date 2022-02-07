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
        val gameProfile = Bukkit.createProfile(UUID.randomUUID(), "")

        gameProfile.properties.add(ProfileProperty("textures", url))

        // oh no, Рейдж, https://papermc.io/javadocs/paper/1.17/com/destroystokyo/paper/profile/PlayerProfile.html...
        // val profileField: Field

        skull.itemMeta = skullMeta

        return skull
    }
}