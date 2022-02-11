package me.func.murder.user

import org.bukkit.Achievement
import java.util.UUID

data class Stat(
    val id: UUID,

    var lootbox: Int,
    var lootboxOpenned: Int,

    var money: Int,
    var wins: Int,
    var kills: Int,
    var games: Int,
    var villagerStreak: Int,

    var music: Boolean,
    var moneyBooster: Int,

    var timePlayedTotal: Long,
    var lastEnter: Long,

    var eventWins: Int,
    var eventKills: Int,

    var dailyClaimTimestamp: Long,
    var rewardStreak: Int,

    var lastSeenName: String, // nullable??
)
