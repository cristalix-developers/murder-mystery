package me.func.commons.donate

enum class Rare(val title: String, private val color: String) {

    COMMON("Обычный", "§a"),
    RARE("Редкий", "§9"),
    EPIC("Эпический", "§5"),
    LEGENDARY("Легендарный", "§6"),;

    fun with(content: String): String {
        return "предмет ${getColored()} §7$content"
    }

    fun getColored(): String {
        return "$color$title"
    }
}