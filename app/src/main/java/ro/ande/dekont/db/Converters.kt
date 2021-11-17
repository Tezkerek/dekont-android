package ro.ande.dekont.db

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.math.BigDecimal
import java.net.URL
import java.util.*

class Converters {
    @TypeConverter
    fun currencyFromString(code: String?): Currency? = Currency.getInstance(code)

    @TypeConverter
    fun currencyToString(currency: Currency?): String? = currency?.currencyCode

    @TypeConverter
    fun urlFromString(url: String?): URL? = URL(url)

    @TypeConverter
    fun urlToString(url: URL?): String? = url.toString()

    @TypeConverter
    fun dateFromLong(timestamp: Long?): LocalDate? =
        timestamp?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    @TypeConverter
    fun dateToLong(date: LocalDate?): Long? =
        date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun amountToLong(amount: BigDecimal?): Long? =
        amount?.let { (amount * BigDecimal(100)).toLong() }

    @TypeConverter
    fun amountFromLong(long: Long?): BigDecimal? = long?.let { BigDecimal(long / 100.toDouble()) }
}