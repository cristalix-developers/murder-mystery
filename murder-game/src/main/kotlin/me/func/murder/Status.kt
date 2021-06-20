package me.func.murder

import me.func.murder.user.Role

enum class Status(val title: String, val lastSecond: Int, val now: (Int) -> Int) {
    STARTING("Набор игроков", 30, { it ->
        activeBar.updateMessage()

        val players = org.bukkit.Bukkit.getOnlinePlayers()
        var actualTime = it

        // Если время вышло и пора играть
        if (it == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size + 1 < slots)
                actualTime = 0
            else {
                // Сменить режим игры
                activeStatus = GAME
                // Телепортация игроком на игровые точки
                val places = app.worldMeta.getLabels("start")
                players.forEachIndexed { index, player ->
                    player.teleport(places[index])
                }
                // Список игроков Murder
                val users = players.map { app.getUser(it) }
                // Выдача активных ролей
                val murder = users.maxByOrNull { it.stat.villagerStreak }!!
                murder.role = Role.MURDER
                murder.stat.villagerStreak = 0
                val detective = users.minus(murder).maxByOrNull { it.stat.villagerStreak }!!
                detective.role = Role.DETECTIVE
                detective.stat.villagerStreak = 0
                // Выдача мирных жителей
                users.minus(murder).minus(detective).forEach {
                    it.role = Role.VILLAGER
                    it.stat.villagerStreak++
                }
                // Показ на экране роли и создание команд, чтобы игроки не видели чужие ники
                val manager = org.bukkit.Bukkit.getScoreboardManager()
                val board = manager.newScoreboard
                users.forEach { user ->
                    val team = board.registerNewTeam(user.name)
                    team.nameTagVisibility = org.bukkit.scoreboard.NameTagVisibility.HIDE_FOR_OTHER_TEAMS
                    team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER)
                    team.addEntry(user.name)
                    user.player!!.scoreboard = board
                    user.player!!.sendTitle("Ваша роль:", user.role.title)
                }
            }
        }

        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && it < STARTING.lastSecond - 10)
            actualTime = STARTING.lastSecond - 10
        actualTime
    }),
    GAME("Игра", 10000, {

        it
    }),
    END("Перезагрузка", 10010, {
        it
    }),
    CLOSE("Закрытие", 10011, {
        it
    }),
}