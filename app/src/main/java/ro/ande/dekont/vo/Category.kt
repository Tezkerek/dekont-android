package ro.ande.dekont.vo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Category(
    @field:SerializedName("id")
    @field:PrimaryKey
    val id: Int,

    @field:SerializedName("name")
    var name: String
)