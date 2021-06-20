package me.func.murder.user

import java.util.*

data class Stat(
    val id: UUID,
    var wins: Int,
    var kills: Int,
    var games: Int,
    var villagerStreak: Int,
)
