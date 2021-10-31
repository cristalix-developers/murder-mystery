package me.func.murder

import java.util.*

enum class GameType(val string: String, val skin: UUID) {
    DBD("§4Dead By Daylight", UUID.fromString("30719b68-2c69-11e8-b5ea-1cb72caa35fd")),
    MUR("§dMurder§fMystery", UUID.fromString("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd")),
    SQD("§eСкоро...", UUID.fromString("3089411e-2c69-11e8-b5ea-1cb72caa35fd")),
}