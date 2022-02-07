package me.func.murder.content

import me.func.murder.MurderGame
import me.func.murder.util.TopCreator

class TopManager(game: MurderGame) {
    init {
        // Создание топа
        val topLabel = game.map.getLabel("top")
        val topArgs = topLabel.tag.split(" ")
        topLabel.setYaw(topArgs[1].toFloat())
        topLabel.setPitch(topArgs[2].toFloat())
        topLabel.add(0.0, 4.5, 0.0)
        TopCreator.create(game, topLabel, topArgs[3], "Топ по " + topArgs[4], topArgs[0]) { it.wins.toString() }
    }
}
