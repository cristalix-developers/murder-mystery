package me.func.murder.util

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
class SkullManager {

    fun create(URL: String?): ItemStack {
        val skull = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val skullMeta: SkullMeta = skull.itemMeta as SkullMeta
        val gameProfile = GameProfile(UUID.randomUUID(), "")
        val encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", URL).toByteArray())

        gameProfile.getProperties().put("textures", Property("textures", String(encodedData)))
        val profileField: Field
        try {
            profileField = skullMeta::class.java.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[skullMeta] = gameProfile
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        skull.itemMeta = skullMeta

        return skull
    }
}