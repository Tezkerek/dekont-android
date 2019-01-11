package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.api.LoginRequest
import ro.ande.dekont.api.RegistrationRequest
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.User
import javax.inject.Inject

class UserRepository @Inject constructor(private val dekontService: DekontService) {

    fun login(email: String, password: String, name: String): LiveData<ApiResponse<Token>> {
        val loginRequest = LoginRequest(email, password, name)

        return dekontService.login(loginRequest)
    }

    fun register(email: String, password: String): Deferred<ApiResponse<User>> {
        return dekontService.register(RegistrationRequest(email, password, null))
    }

    fun logout(): LiveData<ApiResponse<Void>> = dekontService.logout()

    fun verifyToken(token: Token) = verifyToken(token.token)
    fun verifyToken(token: String) = dekontService.verifyAuthToken(token)
}