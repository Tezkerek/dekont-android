package ro.ande.dekont.vo

import com.google.gson.annotations.SerializedName

data class User(
        @field:SerializedName("id")
        val id: Int,

        @field:SerializedName("username")
        val username: String,

        @field:SerializedName("group")
        val group: Int?
) {
        fun isInGroup(): Boolean = group != null
}