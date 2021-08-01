package me.func.murder.map

import dev.implario.bukkit.item.item
import me.func.commons.app
import me.func.commons.worldMeta
import me.func.murder.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Pig
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object Gurney {

    fun create(original: Location) {
        original.setYaw((Math.random() * 360).toFloat())
        val pig: Pig = worldMeta.world.spawnEntity(original, EntityType.PIG) as Pig
        val stand: ArmorStand = StandHelper(original.clone().subtract(0.0, 1.0, 0.0))
            .gravity(false)
            .invisible(true)
            .slot(EnumItemSlot.HEAD, item {
                type = Material.CLAY_BALL
                nbt("murder", "katalka")
            }.build())
            .markTrash()
            .build()
        pig.isSilent = true
        pig.isInvulnerable = true
        pig.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 999999, 99, false, false))
        pig.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false))
        pig.setMetadata("trash", FixedMetadataValue(app, true))
        pig.setMetadata("friend", FixedMetadataValue(app, stand.uniqueId.toString()))
    }
}
