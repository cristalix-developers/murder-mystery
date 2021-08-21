package me.func.commons.donate.impl

import me.func.commons.donate.DonatePosition
import me.func.commons.donate.Rare

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
enum class Mask(private val title: String, private val price: Int, private val rare: Rare, private val url: String) :
    DonatePosition {
    ;

    override fun getTitle(): String {
        return title
    }

    override fun getPrice(): Int {
        return price
    }
    ;

    override fun getRare(): Rare {
        return rare
    }
}