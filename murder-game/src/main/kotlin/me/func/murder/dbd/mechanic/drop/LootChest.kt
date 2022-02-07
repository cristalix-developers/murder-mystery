package me.func.murder.dbd.mechanic.drop

import org.bukkit.Location
import org.bukkit.entity.ArmorStand

data class LootChest(val location: Location, var open: Int, val stand: ArmorStand)
