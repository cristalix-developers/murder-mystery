import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.AbstractElement
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*

class Pack(vararg title: String) {

    var graffiti: MutableMap<String, Pair<RemoteTexture, RectangleElement>>

    init {
        graffiti = title.associateWith {
            val texture = load(it, it)
            texture to rectangle {
                origin = CENTER
                align = CENTER
                color = WHITE
                enabled = false
                textureLocation = texture.location
                size = V3(25.0, 25.0)
                onHover = { element, hovered ->
                    element.animate(0.1) {
                        size.x = if (hovered) 85.0 else 70.0
                        size.y = if (hovered) 85.0 else 70.0
                    }
                }
                onClick = { _: AbstractElement, _: Boolean, _: MouseButton -> click(texture) }
            }
        }.toMutableMap()
    }

    private fun click(remoteTexture: RemoteTexture) {
        background.enabled = true
        background.animate(0.2) { color.alpha = 0.0 }
        UIEngine.postOverlayContext.schedule(0.21) { background.enabled = false }

        graffiti.forEach { (_, value) -> value.second.enabled = false }
        activeGraffiti.enabled = true

        if (activeGraffiti.children.isEmpty()) {
            activeGraffiti.addChild(rectangle {
                size = V3(25.0, 25.0)
                origin = CENTER
                align = CENTER
                color = WHITE
                color.alpha = 0.6
                textureLocation = remoteTexture.location
            })
        } else {
            (activeGraffiti.children[0] as RectangleElement).textureLocation = remoteTexture.location
        }
    }
}

private fun load(path: String, hash: String): RemoteTexture {
    return RemoteTexture(ResourceLocation.of(NAMESPACE, "$path.png"), hash)
}
