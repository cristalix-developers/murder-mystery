package me.func.commons.content

import me.func.commons.mod.ModTransfer
import me.func.commons.user.User

object DailyRewardManager {

    fun open(user: User) {
        val transfer = ModTransfer().integer(user.stat.rewardStreak + 1)
        WeekRewards.values().forEach { transfer.item(it.icon).string("§7Награда: " + it.title) }
        transfer.send("murder:weekly-reward", user)
    }
}