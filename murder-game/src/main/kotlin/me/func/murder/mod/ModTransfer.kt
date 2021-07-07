package me.func.murder.mod

import io.netty.buffer.Unpooled
import net.minecraft.server.v1_12_R1.ItemStack
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import ru.cristalix.core.GlobalSerializers
import me.func.murder.user.User


/**
 * @author func 02.01.2021
 * @project forest
 */
class ModTransfer {
    private val serializer = PacketDataSerializer(Unpooled.buffer())

    fun json(`object`: Any?): ModTransfer {
        return string(GlobalSerializers.toJson(`object`))
    }

    fun string(string: String?): ModTransfer {
        serializer.writeString(string)
        return this
    }

    fun item(item: ItemStack?): ModTransfer {
        serializer.writeItem(item)
        return this
    }

    fun integer(integer: Int): ModTransfer {
        serializer.writeInt(integer)
        return this
    }

    fun double(double: Double): ModTransfer {
        serializer.writeDouble(double)
        return this
    }

    fun boolean(boolean: Boolean): ModTransfer {
        serializer.writeBoolean(boolean)
        return this
    }

    fun send(channel: String?, user: User) {
        user.sendPacket(PacketPlayOutCustomPayload(channel, serializer))
    }
}