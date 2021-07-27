package me.func.murder.donate

interface DonatePosition {

    fun getTitle(): String

    fun getPrice(): Int

    fun getRare(): Rare

    fun give()

}