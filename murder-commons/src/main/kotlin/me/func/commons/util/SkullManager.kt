package me.func.commons.util

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
object SkullManager {

    fun create(URL: String?): ItemStack {
        val skull = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val skullMeta: SkullMeta = skull.itemMeta as SkullMeta
        val gameProfile = GameProfile(UUID.randomUUID(), "")

        gameProfile.getProperties().put("textures", Property("textures", URL))
        val profileField: Field

        try {
            profileField = skullMeta::class.java.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(skullMeta, gameProfile)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        skull.itemMeta = skullMeta

        return skull
    }
}