package ro.ande.dekont.vo

import com.google.gson.annotations.SerializedName

data class Group(
        @field:SerializedName("id")
        val id: Int,

        @field:SerializedName("name")
        val name: String,

        @field:SerializedName("group_admin")
        val group_admin: Int
)
