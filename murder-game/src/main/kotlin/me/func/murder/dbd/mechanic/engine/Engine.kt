package me.func.murder.dbd.mechanic.engine

import org.bukkit.Location
import org.bukkit.entity.ArmorStand

data class Engine(val location: Location, var percent: Int, var stand: ArmorStand?)
