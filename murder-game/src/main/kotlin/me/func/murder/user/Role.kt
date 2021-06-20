package me.func.murder.user

enum class Role(val title: String) {
    VILLAGER("Мирный житель"),
    DETECTIVE("Детектив"),
    MURDER("Убийца"),
    NONE("Не отпределен")
}