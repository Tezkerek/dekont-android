package ro.ande.dekont.vo

import com.google.gson.annotations.SerializedName

data class Token (
    @field:SerializedName("token")
    val token: String
)