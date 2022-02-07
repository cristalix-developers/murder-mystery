@file:Suppress("NOTHING_TO_INLINE")

package me.func.murder

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.routine.Routine
import dev.implario.kensuke.IKensukeUser
import dev.implario.kensuke.UserManager
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

inline fun <U : IKensukeUser> UserManager<U>.getUser(player: Player): U = getUser(player.uniqueId)!!

inline fun <reified E : Entity> World.getEntitiesByType(): Collection<E> = entities.filterIsInstance<E>()

inline fun EventContext.everyAfter(after: Long, every: Long, noinline r: (Routine) -> Unit): Routine =
    after(after) { every(every, r) }
