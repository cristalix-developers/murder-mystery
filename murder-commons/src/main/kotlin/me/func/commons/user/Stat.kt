package me.func.commons.user

import me.func.commons.achievement.Achievement
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.impl.*
import java.util.*

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
    var achievement: MutableList<Achievement>,

    var donate: MutableList<DonatePosition>,
    var activeKillMessage: KillMessage,
    var activeParticle: StepParticle,
    var activeDeathImage: DeathImage,
    var activeNameTag: NameTag,
    var activeCorpse: Corpse,

    var timePlayedTotal: Long,
    var lastEnter: Long,

    var lastSeenName: String?,
)
