package me.func.murder

enum class GameType(val string: String, val skin: String, val slots: Int, val queue: String? = null) {
    DBD("§4DeadByDaylight", "https://webdata.c7x.dev/textures/skin/30719b68-2c69-11e8-b5ea-1cb72caa35fd", 6),
    MUR("§dMurder§fMystery", "https://webdata.c7x.dev/textures/skin/6f3f4a2e-7f84-11e9-8374-1cb72caa35fd", 16),
    SQD(
        "§eИгра в кальмара",
        "https://webdata.c7x.dev/textures/skin/3089411e-2c69-11e8-b5ea-1cb72caa35fd",
        30,
        "7188b5b2-43b3-40fc-bcd0-abeea3883490"
    ),
    PILL("§cСтолбы", "https://webdata.c7x.dev/textures/skin/e7c13d3d-ac38-11e8-8374-1cb72caa35fd", 7),
    SHW("§6SheepWars §f§lNEW!", "https://webdata.c7x.dev/textures/skin/64c67d57-a461-11e8-8374-1cb72caa35fd", 16),
    //AMN("§bAmong Us", "https://web.fiwka.xyz/amongus/red.png", 10, "845e92f3-7006-11ea-acca-1cb72caa35fd"),
}