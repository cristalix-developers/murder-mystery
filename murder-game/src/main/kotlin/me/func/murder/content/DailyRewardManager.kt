package me.func.murder.content

import me.func.murder.mod.ModTransfer
import me.func.murder.user.User

object DailyRewardManager {

    fun open(user: User) {
        val transfer = ModTransfer().integer(user.stat.rewardStreak + 1)
        WeekRewards.values().forEach { transfer.item(it.icon).string("§7Награда: " + it.title) }
        transfer.send("murder:weekly-reward", user)
    }
}
