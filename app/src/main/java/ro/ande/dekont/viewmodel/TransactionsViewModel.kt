package ro.ande.dekont.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.ande.dekont.R
import javax.inject.Inject

class TransactionsViewModel @Inject constructor(val mApplication: Application) : AndroidViewModel(mApplication) {
    val isLoginValid: MutableLiveData<Boolean> = MutableLiveData()

    private val authToken: String? by lazy {
        mApplication.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)
    }

   fun verifyLogin() {
        isLoginValid.value = authToken != null
    }

    private val verifyLoginCallback = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d("login check", response.isSuccessful.toString())
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