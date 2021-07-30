package me.func.commons.user

import me.func.commons.donate.DonatePosition
import me.func.commons.donate.impl.Corpse
import me.func.commons.donate.impl.DeathImage
import me.func.commons.donate.impl.NameTag
import me.func.commons.donate.impl.StepParticle
import java.util.*

data class Stat(
    val id: UUID,

    var lootbox: Int,

    var money: Int,
    var wins: Int,
    var kills: Int,
    var games: Int,
    var villagerStreak: Int,

    var donate: MutableList<DonatePosition>,
    var activeParticle: StepParticle,
    var activeDeathImage: DeathImage,
    var activeNameTag: NameTag,
    var activeCorpse: Corpse,

    var lastSeenName: String?,
)
