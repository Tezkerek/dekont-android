package ro.ande.dekont.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.ande.dekont.DekontApi
import ro.ande.dekont.R

class TransactionsViewModel(val mApplication: Application) : AndroidViewModel(mApplication) {
    val isLoginValid: MutableLiveData<Boolean> by lazy {
        verifyLogin()
        MutableLiveData<Boolean>()
    }

    private fun verifyLogin() {
        DekontApi(mApplication).verifyLogin().enqueue(verifyLoginCallback)
    }

    private val verifyLoginCallback = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val result = JSONObject(response.body()!!.string()).getBoolean("is_valid")
                isLoginValid.postValue(result)
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Toast.makeText(mApplication, R.string.error_server_unreachable, Toast.LENGTH_SHORT).show()
        }
    }
}