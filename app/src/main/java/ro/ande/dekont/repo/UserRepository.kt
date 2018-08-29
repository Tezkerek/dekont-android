package ro.ande.dekont.repo

import android.arch.lifecycle.LiveData
import ro.ande.dekont.DekontService
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.LoginRequest
import ro.ande.dekont.vo.Token
import javax.inject.Inject

class UserRepository @Inject constructor(private val dekontService: DekontService) {

    fun login(email: String, password: String, name: String): LiveData<ApiResponse<Token>> {
        val loginRequest = LoginRequest(email, password, name)

        return dekontService.login(loginRequest)
    }

    fun verifyToken(token: Token) = verifyToken(token.token)
    fun verifyToken(token: String) = dekontService.verifyAuthToken(token)
}