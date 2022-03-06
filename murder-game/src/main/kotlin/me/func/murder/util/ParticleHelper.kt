package me.func.murder.util

import me.func.murder.MurderGame
import org.bukkit.Location

class ParticleHelper(private val game: MurderGame) {
    fun acceptTickBowDropped(location: Location, tick: Int) {
        // Создание частиц возле лука
        val radius = 1.2 // Радиус окружности
        val omega = 1.0 // Скорость вращения
        val amount = 2 // Количество частиц
        for (counter in 0..amount) {
            game.map.world.spawnParticle(
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
}
