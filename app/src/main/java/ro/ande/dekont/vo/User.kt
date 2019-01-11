package ro.ande.dekont.vo

import com.google.gson.annotations.SerializedName

data class User(
        @field:SerializedName("id")
        val id: Int
)