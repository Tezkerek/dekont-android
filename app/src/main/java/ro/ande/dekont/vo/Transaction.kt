package ro.ande.dekont.vo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import java.math.BigDecimal
import java.util.*

@Entity
data class Transaction (
        @field:SerializedName("id")
        @field:PrimaryKey
        val id: Int,

        @field:SerializedName("user")
        @field:Embedded(prefix = "user_")
        val userRelation: HyperlinkedRelation,

        @field:SerializedName("date")
        val date: LocalDate,

        @field:SerializedName("amount")
        val amount: BigDecimal,

        @field:SerializedName("currency")
        val currency: Currency,

        @field:SerializedName("description")
        val description: String,

        @field:SerializedName("document_number")
        @field:ColumnInfo(name = "document_number")
        val documentNumber: String,

        @field:SerializedName("document_type")
        @field:ColumnInfo(name = "document_type")
        val documentType: String,

        @field:SerializedName("status")
        val status: Int
) {
    companion object {
        const val PENDING = 0
        const val APPROVED = 1
        const val REJECTED = 2
    }
}
