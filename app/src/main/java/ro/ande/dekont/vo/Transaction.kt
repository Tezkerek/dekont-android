package ro.ande.dekont.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

@Entity
data class Transaction(
    @field:SerializedName("id")
    @field:PrimaryKey(autoGenerate = true)
    val id: Int,

    @field:SerializedName("user")
    val userId: Int,

    @field:SerializedName("date")
    var date: LocalDate,

    @field:SerializedName("amount")
    var amount: BigDecimal,

    @field:SerializedName("currency")
    var currency: Currency,

    @field:SerializedName("category")
    val categoryId: Int?,

    @field:SerializedName("description")
    var description: String,

    @field:SerializedName("supplier")
    var supplier: String,

    @field:SerializedName("document_number")
    @field:ColumnInfo(name = "document_number")
    var documentNumber: String,

    @field:SerializedName("document_type")
    @field:ColumnInfo(name = "document_type")
    var documentType: String,

    @field:SerializedName("status")
    var status: Int
) {
    /** Constructor for to-be-created transactions */
    constructor(
        date: LocalDate,
        amount: BigDecimal,
        currency: Currency,
        categoryId: Int?,
        description: String,
        supplier: String,
        documentNumber: String,
        documentType: String
    ) : this(
        0,
        0,
        date,
        amount,
        currency,
        categoryId,
        description,
        supplier,
        documentNumber,
        documentType,
        PENDING
    )

    /** Returns a String containing the formatted date */
    val formattedDate: String
        get() = this.date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    /** Returns the amount as a String with two decimal places */
    val formattedAmount: String
        get() = DecimalFormat("##0.00").format(this.amount)

    companion object {
        const val PENDING = 0
        const val APPROVED = 1
        const val REJECTED = 2
    }
}
