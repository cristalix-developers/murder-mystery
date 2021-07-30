package me.func.commons.donate

object MoneyFormatter {

    fun texted(amount: Int): String {
        val lower = amount % 64
        if (amount > 64)
            return "§e${amount / 64} стаков${if (lower == 0) "" else " $lower"} монет §f($amount всего)"
        return "§e$amount монет"
    }

}