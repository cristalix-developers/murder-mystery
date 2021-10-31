package me.func.commons.achievement

import me.func.commons.user.User

enum class Achievement(
    val title: String,
    val lore: String,
    val predicate: (User) -> Boolean,
    val reward: (User) -> Any
) {
    FIRST_JOIN("Награда новичка", "§7Ваша первая награда\n§f + 1 §bЛутбокс", { true }, { it.stat.lootbox++ }),
    FIRST_WIN("Первая победа", "§7Победить 1 раз\n§f + §e10 монет", { it.stat.wins >= 1 }, { it.giveMoney(10) }),
    TEN_WIN("Восходящая звезда", "§7Победить 10 раз\n§f + §e64 монеты\n" +
            "§f + 1 §bЛутбокс", { it.stat.wins >= 10 }, {
        it.giveMoney(64)
        it.stat.lootbox++
    }),
    HUNDRED_WIN("Герой", "§7Победить 100 раз\n" +
            "§f + 3 §bЛутбокса", { it.stat.wins >= 100 }, { user ->
        repeat(3) { user.stat.lootbox++ }
    }),
    THOUSAND_WIN("Легенда", "§7Победить 1`000 раз\n" +
            "§f + 20 §bЛутбоксов", { it.stat.wins >= 1000 }, { user ->
        repeat(20) { user.stat.lootbox++ }
    }),
    FIVE_KILLS(
        "Начинающий маньяк",
        "§7Убить пятерых\n§f + §e20 монет",
        { it.stat.kills >= 5 },
        { it.giveMoney(20) }),
    HUNDRED_KILLS("Маньяк", "§7Убить 100 человек\n§f + §e128 монет\n" +
            "§f + 1 §bЛутбокс", { it.stat.kills >= 100 }, {
        it.giveMoney(128)
        it.stat.lootbox++
    }),
    THOUSAND_KILLS("Маньяк в розыске", "§7Убить 1`000 человек\n§f + §e256 монет\n" +
            "§f + 2 §bЛутбокса", { it.stat.kills >= 1000 }, {
        it.giveMoney(256)
        it.stat.lootbox++
        it.stat.lootbox++
    }),
    HOUR_GAME("Начало пути", "§7Играть 1 час\n§f + §e64 монеты", { it.stat.timePlayedTotal / 1000 / 3600 >= 1 }, {
        it.giveMoney(64)
    }),
    TWENTY_HOUR_GAME(
        "Постоялец",
        "§7Играть 20 часов\n§f + §e256 монет",
        { it.stat.timePlayedTotal / 1000 / 3600 >= 20 },
        {
            it.giveMoney(256)
        }),
    HERE_FOREVER(
        "Ты здесь навсегда",
        "§7Играть 200 часов\n§f + §e1024 монет\n" +
                "§f + 5 §bЛутбоксов",
        { it.stat.timePlayedTotal / 1000 / 3600 >= 200 },
        { user ->
            repeat(5) { user.stat.lootbox++ }
            user.giveMoney(1024)
        }),
    KILLER("Убийца всего живого", "§7Убить 10`000 игроков\n§f + §e4096 монет", { it.stat.kills >= 10000 }, {
        it.giveMoney(4096)
    }),
    TEN_GAMES("Игрок", "§7Сыграть 10 игр\n§f + §e128 монет", { it.stat.games >= 10 }, {
        it.giveMoney(128)
    }),
    TWENTY_FIVE_GAMES("Эксперт", "§7Сыграть 25 игр\n§f + §e192 монеты\n§f + 1 §bЛутбокс", { it.stat.games >= 25 }, {
        it.giveMoney(192)
        it.stat.lootbox++
    }), FIVE_HOURS("Знаток", "§7Наиграть 5 часов\n§f + §e192 монеты\n§f + 1 §bЛутбокс", { it.stat.timePlayedTotal / 1000 / 3600 >= 5 }, {
        it.giveMoney(192)
        it.stat.lootbox++
    }), FIVE_HUNDRED_GAMES("Невозможно", "§7Наиграть 500 часов\n§f + §e19`200 монет\n§f + 100 §bЛутбоксов", { it.stat.timePlayedTotal / 1000 / 3600 >= 500 }, { user ->
        user.giveMoney(19200)
        repeat(100) { user.stat.lootbox++ }
    }), TWENTY_HUNDRED("Серийный маньяк", "§7Убить 250 человек\n§f + §e192 монеты\n§f + 1 §bЛутбокс", { it.stat.kills >= 250 }, {
        it.giveMoney(192)
        it.stat.lootbox++
    }), GAMER_PRO("Про игрок", "§7Сыграть 1000 игр\n§f + §e192 монеты\n§f + 1 §bЛутбокс", { it.stat.games >= 1000 }, {
        it.giveMoney(192)
        it.stat.lootbox++
    }), GAMER("Игрок", "§7Сыграть 100 игр\n§f + §e64 монеты", { it.stat.games >= 100 }, {
        it.giveMoney(64)
    }), LOOTER("Счастливчик", "§7Открыть 10 лутбоксов\n§f + 1 §bЛутбокс", { it.stat.lootboxOpenned >= 10 }, {
        it.stat.lootbox++
    }), RICH("Богач", "§7Накопить 1`000 монет\n§f + 1 §bЛутбокс", { it.stat.money >= 1000 }, {
        it.stat.lootbox++
    }), VERY_RICH("Миллиардер", "§7Накопить 10`000 монет\n§f + 1 §bЛутбокс", { it.stat.money >= 10000 }, {
        it.stat.lootbox++
    }), MUSIC("Погруженный", "§7Включить музыку\n§f + §e10 монет", { it.stat.music }, {
        it.giveMoney(10)
    }), PLAY_DBD("DeathByDaylight 2021", "§7Победите 1 раз\n§f + §e100 монет", { it.stat.eventWins >= 1 }, {
        it.giveMoney(100)
    }), DBD_WINNER("Победитель DeathByDaylight", "§7Победите 100 раз\n§f + §e1`000 монет", { it.stat.eventWins >= 100 }, {
        it.giveMoney(1000)
    }), DBD_KILLER("Убийца DeathByDaylight", "§7Убейте 10 жертв\n§f + §e192 монет", { it.stat.eventKills >= 10 }, {
        it.giveMoney(192)
    })
}