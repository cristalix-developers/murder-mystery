package me.func.murder.mod

import me.func.murder.app
import me.func.murder.user.User
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

object ModHelper {

    fun makeCorpse(user: User) {
        val player = user.player!!
        val profile = (player as CraftPlayer).profile
        val skin = profile.properties["skinURL"].first().value
        val digest = profile.properties["skinDigest"].first().value
        val location = user.player!!.location
        Bukkit.getOnlinePlayers().map { app.getUser(it) }
            .forEach { ModTransfer()
                .string(player.uniqueId.toString())
                .string(player.name)
                .string(skin)
                .string(digest)
                .double(location.x)
                .double(location.y + 0.1)
                .double(location.z)
                .boolean(true)
                .send("corpse", it)
            }
    }

}