package me.func.murder.util

import clepto.bukkit.B
import me.func.commons.mod.ModHelper
import me.func.commons.user.Role
import me.func.commons.util.ParticleHelper
import me.func.commons.util.StandHelper
import me.func.murder.murder
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack

lateinit var droppedBowManager: BowManager

class BowManager {

    private var droppedBow: ArmorStand? = null

    init {
        droppedBowManager = this
    }

    fun drop(location: Location) {
        if (droppedBow != null)
            return
        // Выпадение лука
        droppedBow = StandHelper(location.clone().subtract(0.0, 1.0, 0.0))
            .gravity(false)
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
            ParticleHelper.acceptTickBowDropped(droppedBow!!.location, time)
            // Сначала вращать, а потом пытаться подобрать
            tryPickUp()
        }
    }

    private fun tryPickUp() {
        // Если есть кто-то рядом, сделать его детективом
        val nearby = Bukkit.getOnlinePlayers()
            .filter { murder.getUser(it).role != Role.MURDER }
            .firstOrNull { it.location.distanceSquared(droppedBow!!.location) < 9 }
        if (nearby != null) {
            val first = murder.getUser(nearby.uniqueId)
            if (first.role == Role.VILLAGER) {
                clear()
                first.role = Role.DETECTIVE
                first.role.start?.invoke(first)
                ModHelper.sendGlobalTitle("§aЛук подобран")
                B.bc(ru.cristalix.core.formatting.Formatting.fine("Лук перехвачен!"))
            }
        }
    }

    fun clear() {
        if (droppedBow != null)
            droppedBow!!.remove()
        droppedBow = null
    }

}