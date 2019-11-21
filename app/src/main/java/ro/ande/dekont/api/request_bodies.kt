package ro.ande.dekont.api

import com.google.gson.annotations.SerializedName
import java.util.*

class LoginRequest(
        @field:SerializedName("email")
        val email: String,
        @field:SerializedName("password")
        val password: String,
        @field:SerializedName("name")
        val name: String
)

class RegistrationRequest(
        @field:SerializedName("email")
        val email: String,
        @field:SerializedName("password")
        val password: String,
        @field:SerializedName("reporting_currency")
        val reportingCurrency: Currency?
)

class GroupJoinRequest(
        @field:SerializedName("invite_code")
        val inviteCode: String
)