@file:Suppress("DEPRECATION")

package me.func.murder.map

import clepto.bukkit.Cycle
import me.func.murder.MurderGame
import me.func.murder.user.User
import me.func.murder.util.Music
import me.func.murder.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Door
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import ru.cristalix.core.math.V3
import ru.cristalix.core.util.UtilV3

class StandardsInteract(private val game: MurderGame) {
    fun closeDoor(v3: V3, ticks: Long) {
        val state = game.map.world.getBlockAt(v3.x.toInt(), v3.y.toInt(), v3.z.toInt()).state
        val door = state.data as Door

        door.isOpen = !door.isOpen

        state.update()

        game.context.after(ticks) {
            door.isOpen = !door.isOpen
            state.update()
        }
    }

    fun drop(breakList: List<V3>): MutableList<Pair<Int, Byte>> {
        return breakList.map { UtilV3.toLocation(it, game.map.world) }.map {
            val block = it.block.type
            val type = block.id to it.block.data
            it.block.setTypeAndDataFast(0, 0)
            it.world.spawnFallingBlock(it, block, 1)
            type
        }.toMutableList()
    }

    fun dropAndExplode(
        dropped: Boolean, after: Iterable<V3>, before: Iterable<V3>, replace: Material, was: Material
    ): Boolean {
        if (dropped) return false

        after.map { UtilV3.toLocation(it, game.map.world) }.forEach { location ->
            location.block.type = replace
            location.world.spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, location, 1)
            game.players.forEach { player ->
                player.playSound(
                    location, Sound.BLOCK_ANVIL_FALL, 0.2f, 1f
                )
            }
        }
        before.map { UtilV3.toLocation(it, game.map.world) }.forEach { location -> location.block.type = was }

        return true
    }

    fun movePlayer(user: User, from: V3, to: V3, ticks: Int, outDot: V3) {
        if (user.animationLock) return
        val world = game.map.world
        val list = arrayListOf<ArmorStand>()
        val size = 5
        val platesInBlock = 1.8
        val side = size * platesInBlock.toInt()
        val totalStands = side * side
        repeat(side) { first ->
            repeat(side) { second ->
                val currentV3 = from.clone().add(
                    (-size / platesInBlock + first % (size * platesInBlock)) / platesInBlock,
                    0.0,
                    (-size / platesInBlock + second % (size * platesInBlock)) / platesInBlock
                )
                list.add(
                    StandHelper(UtilV3.toLocation(currentV3, world)).slot(
                        EnumItemSlot.HEAD, ItemStack(Material.IRON_BLOCK)
                    ).gravity(false).invisible(true).markTrash().build()
                )
            }
        }
        user.player.playSound(user.player.location, Sound.BLOCK_ANVIL_STEP, 0.4f, 1f)
        user.animationLock = true
        val centralPlate = list[totalStands / 2]
        centralPlate.addPassenger(user.player)
        val dVector = Vector(to.x - from.x, to.y - from.y, to.z - from.z).multiply(1f / ticks)
        Cycle.run(1, ticks) { tick ->
            if (centralPlate.passenger == null) centralPlate.addPassenger(user.player)
            if (tick == ticks - 1) {
                list.forEach { it.remove() }
                list.clear()
                game.context.after(2) { user.player.teleport(UtilV3.toLocation(outDot, world)) }
                user.player.playSound(user.player.location, Sound.BLOCK_ANVIL_STEP, 0.4f, 1f)
                user.animationLock = false
                Cycle.exit()
                return@run
            }
            user.player.playSound(user.player.location, Sound.BLOCK_ANVIL_STEP, 0.7f, 1f)
            list.forEach { plate ->
                (plate as CraftArmorStand).handle.move(EnumMoveType.SELF, dVector.x, dVector.y, dVector.z)
            }
        }
    }

    fun breakLamps() {
        val lamps = game.map.getLabels("lamp").map { it.clone().set(it.x, it.y - 1, it.z).block }
        var lampOff = false

        game.modHelper.sendGlobalTitle("㟣 §eЭлектро-сбой")

        Music.LIGHT_OFF.playAll(game)

        game.after(12 * 20) { Music.OUTLAST.playAll(game) }
        val blindness = PotionEffect(PotionEffectType.BLINDNESS, 85, 1, true, false)
        game.players.forEach { it.addPotionEffect(blindness) }

        fun changePower(on: Boolean) {
            lamps.filter {
                Math.random() < 0.25
            }.forEach {
                it.setTypeAndDataFast((if (on) Material.REDSTONE_LAMP_ON else Material.REDSTONE_LAMP_OFF).id, 0)
                it.location.getNearbyPlayers(14.0)
                    .forEach { player -> player.playSound(it.location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.05f, 0f) }
            }
        }

        Cycle.run(7, 130) {
            if (game.players.isEmpty()) {
                changePower(true)
                Cycle.exit()
                return@run
            }
            lampOff = !lampOff
            changePower(lampOff)
        }
    }
}
