package me.func.murder.user

import me.func.murder.achievement.Achievement
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.impl.ArrowParticle
import me.func.murder.donate.impl.Corpse
import me.func.murder.donate.impl.DeathImage
import me.func.murder.donate.impl.KillMessage
import me.func.murder.donate.impl.Mask
import me.func.murder.donate.impl.NameTag
import me.func.murder.donate.impl.StepParticle
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
    var achievement: MutableList<Achievement>,

    var donate: MutableList<DonatePosition>,
    var activeKillMessage: KillMessage,
    var activeParticle: StepParticle,
    var activeDeathImage: DeathImage,
    var activeNameTag: NameTag,
    var activeCorpse: Corpse,
    var arrowParticle: ArrowParticle,
    var mask: Mask,

    var timePlayedTotal: Long,
    var lastEnter: Long,

    var eventWins: Int,
    var eventKills: Int,

    var dailyClaimTimestamp: Long,
    var rewardStreak: Int,

    var lastSeenName: String, // nullable??
)
