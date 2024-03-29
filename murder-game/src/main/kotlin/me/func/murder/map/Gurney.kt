package me.func.murder.map

import dev.implario.bukkit.item.item
import me.func.murder.MurderGame
import me.func.murder.app
import me.func.murder.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Pig
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Gurney(private val game: MurderGame) {
    fun create(original: Location) {
        original.setYaw((Math.random() * 360).toFloat())

        val pig = game.map.world.spawnEntity(original, EntityType.PIG) as Pig

        val stand = StandHelper(original.clone().subtract(0.0, 1.0, 0.0)).gravity(false)
            .invisible(true)
            .slot(EnumItemSlot.HEAD, item {
                type = Material.CLAY_BALL
                nbt("murder", "katalka")
            })
            .markTrash()
            .build()

        pig.run {
            isSilent = true
            isInvulnerable = true

            addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOW, 999999, 99, false, false
                )
            )
            addPotionEffect(
                PotionEffect(
                    PotionEffectType.INVISIBILITY, 999999, 1, false, false
                )
            )
            setMetadata("trash", FixedMetadataValue(app, true))
            setMetadata("friend", FixedMetadataValue(app, stand.uniqueId.toString()))
        }
    }
}
