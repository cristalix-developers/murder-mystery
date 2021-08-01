package me.func.commons.util

import me.func.commons.worldMeta
import org.bukkit.Location

object ParticleHelper {

    fun acceptTickBowDropped(location: Location, tick: Int) {
        // Создание частиц возле лука
        val radius = 1.2 // Радиус окружности
        val omega = 1.0 // Скорость вращения
        val amount = 2 // Количество частиц
        for (counter in 0..amount) {
            worldMeta.world.spawnParticle(
                org.bukkit.Particle.SPELL_WITCH,
                location.clone().add(
                    kotlin.math.sin(tick / 2 / kotlin.math.PI * omega * counter / amount) * radius,
                    1.6 + kotlin.math.sin(tick / kotlin.math.PI / 5),
                    kotlin.math.cos(tick / 2 / kotlin.math.PI * omega * counter / amount) * radius
                ),
                1
            )
        }
    }

    fun happyVillager(location: Location) {
        // Создание частиц возле лука
        worldMeta.world.spawnParticle(
            org.bukkit.Particle.VILLAGER_HAPPY,
            location.clone().add(
                (Math.random() - 0.5) / 0.8,
                (Math.random() + 0.5),
                (Math.random() - 0.5) / 0.8
            ),
            1
        )
    }

}