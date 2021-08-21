package me.func.commons.donate

import com.google.gson.*
import me.func.commons.donate.impl.*
import java.lang.reflect.Type

class DonateAdapter : JsonDeserializer<DonatePosition>, JsonSerializer<DonatePosition> {

    companion object {
        const val CLASSNAME = "CLASSNAME"
        const val DATA = "DATA"
    }

    @Throws(JsonParseException::class)
    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): DonatePosition {
        val json = element.asJsonObject
        val primitive = json.get(CLASSNAME).asString
        val value = json.get(DATA).asString
        return when(primitive) {
            "DeathImage" -> DeathImage.valueOf(value)
            "NameTag" -> NameTag.valueOf(value)
            "StepParticle" -> StepParticle.valueOf(value)
            "Corpse" -> Corpse.valueOf(value)
            "KillMessage" -> KillMessage.valueOf(value)
            "ArrowParticle" -> ArrowParticle.valueOf(value)
            "Mask" -> Mask.valueOf(value)
            else -> throw IllegalArgumentException("Cannot deserialize data CLASS: $primitive, DATA: $value")
        }
    }

    override fun serialize(element: DonatePosition, type: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(CLASSNAME, element.javaClass.simpleName)
        jsonObject.add(DATA, JsonPrimitive(element.getName()))
        return jsonObject
    }
}