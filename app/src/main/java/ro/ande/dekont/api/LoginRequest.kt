package ro.ande.dekont.api

import com.google.gson.annotations.SerializedName

class LoginRequest(
        @field:SerializedName("email")
        val email: String,
        @field:SerializedName("password")
        val password: String,
        @field:SerializedName("name")
        val name: String
)