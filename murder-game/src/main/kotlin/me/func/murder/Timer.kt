package me.func.murder

class Timer(private val game: MurderGame) {
    var time = 0

    fun tick() {
        time = game.activeStatus.now(time, game) + 1
    }
}
