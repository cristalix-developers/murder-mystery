package me.func.murder.bar

import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.display.enums.EnumUpdateType
import ru.cristalix.core.display.messages.ProgressMessage
import java.util.*


abstract class GameBar {
    private val viewers: MutableCollection<UUID> = HashSet()
    private lateinit var currentMessage: ProgressMessage

    fun updateMessage() {
        currentMessage = message
        IDisplayService.get().sendProgress(viewers, message)
    }

    fun stop() {
        IDisplayService.get().sendProgress(
            viewers,
            ProgressMessage.builder().position(currentMessage.position).updateType(EnumUpdateType.REMOVE).build()
        )
    }

    abstract val message: ProgressMessage

    fun addViewer(viewerUniqueId: UUID) {
        viewers.add(viewerUniqueId)
        IDisplayService.get().sendProgress(
            viewerUniqueId,
            ProgressMessage.builder()
                .updateType(EnumUpdateType.ADD)
                .position(currentMessage.position)
                .color(currentMessage.color)
                .name(currentMessage.name)
                .start(currentMessage.start)
                .percent(currentMessage.percent)
                .build()
        )
    }

    fun removeViewer(viewerUniqueId: UUID) {
        viewers.remove(viewerUniqueId)
        IDisplayService.get().sendProgress(
            viewerUniqueId,
            ProgressMessage.builder()
                .updateType(EnumUpdateType.REMOVE)
                .position(currentMessage.position)
                .build()
        )
    }
}