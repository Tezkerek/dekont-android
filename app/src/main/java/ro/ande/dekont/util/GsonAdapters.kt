package ro.ande.dekont.util

import com.google.gson.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type
import java.util.*

class GsonLocalDateSerializer : JsonSerializer<LocalDate> {
    override fun serialize(date: LocalDate?, type: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(date?.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
}

class GsonLocalDateDeserializer : JsonDeserializer<LocalDate> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return LocalDate.parse(json?.asString, DateTimeFormatter.ISO_LOCAL_DATE)
    }

}

class GsonCurrencySerializer : JsonSerializer<Currency> {
    override fun serialize(currency: Currency?, type: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(currency?.currencyCode)
    }
}

class GsonCurrencyDeserializer : JsonDeserializer<Currency> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Currency {
        return Currency.getInstance(json?.asString)
    }
}
