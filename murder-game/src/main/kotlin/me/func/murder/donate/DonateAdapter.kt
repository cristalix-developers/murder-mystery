package me.func.murder.donate

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import me.func.murder.donate.impl.ArrowParticle
import me.func.murder.donate.impl.Corpse
import me.func.murder.donate.impl.DeathImage
import me.func.murder.donate.impl.KillMessage
import me.func.murder.donate.impl.Mask
import me.func.murder.donate.impl.NameTag
import me.func.murder.donate.impl.StepParticle
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
        return when (primitive) {
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
        jsonObject.add(DATA, JsonPrimitive(element.name)) // todo its ok (name) ?
        return jsonObject
    }
}