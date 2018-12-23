package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.LoginRequest
import ro.ande.dekont.vo.Token
import javax.inject.Inject

class UserRepository @Inject constructor(private val dekontService: DekontService) {

    fun login(email: String, password: String, name: String): LiveData<ApiResponse<Token>> {
        val loginRequest = LoginRequest(email, password, name)

        return dekontService.login(loginRequest)
    }

    fun logout(): LiveData<ApiResponse<Void>> = dekontService.logout()

    fun verifyToken(token: Token) = verifyToken(token.token)
    fun verifyToken(token: String) = dekontService.verifyAuthToken(token)
}