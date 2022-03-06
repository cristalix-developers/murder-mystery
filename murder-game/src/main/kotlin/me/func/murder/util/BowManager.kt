package me.func.murder.util

import me.func.murder.MurderGame
import me.func.murder.getUser
import me.func.murder.user.Role
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

class BowManager(private val game: MurderGame) {

    private var droppedBow: ArmorStand? = null

    fun drop(location: Location) {
        if (droppedBow != null) return
        // Выпадение лука
        droppedBow =
            StandHelper(location.clone().subtract(0.0, 1.0, 0.0)).gravity(false)
                .marker(true)
                .invisible(true)
                .slot(EnumItemSlot.HEAD, ItemStack(Material.BOW))
                .markTrash()
                .build()
        droppedBow!!.isGlowing = true
    }

    fun rotateIfPresent(time: Int) {
        // Если выбит лук, то крутить его и проверять, есть ли рядом игрок
        if (droppedBow != null) {
            // Вращение
            val pose = droppedBow!!.headPose
            pose.y += Math.toRadians(360.0 / (20 * 3)) // Полный оборот за 3 секунды
            droppedBow!!.headPose = pose
            game.particleHelper.acceptTickBowDropped(droppedBow!!.location, time)
            // Сначала вращать, а потом пытаться подобрать
            tryPickUp()
        }
    }

    private fun tryPickUp() {
        // Если есть кто-то рядом, сделать его детективом
        val nearby =
            game.players.filter { game.userManager.getUser(it).role != Role.MURDER }
                .firstOrNull { it.location.distanceSquared(droppedBow!!.location) < 9 }
        if (nearby != null) {
            val first = game.userManager.getUser(nearby.uniqueId)
            if (first.role == Role.VILLAGER) {
                clear()
                first.role = Role.DETECTIVE

                first.role.start(first, game)
                game.modHelper.sendGlobalTitle("§aЛук подобран")
                game.broadcast(Formatting.fine("Лук перехвачен!"))
            }
        }
    }

    fun clear() {
        if (droppedBow != null) droppedBow!!.remove()
        droppedBow = null
    }
}
