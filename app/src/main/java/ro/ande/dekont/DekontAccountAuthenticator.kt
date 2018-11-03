package ro.ande.dekont

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import ro.ande.dekont.ui.LoginActivity

class DekontAccountAuthenticator(val mContext: Context) : AbstractAccountAuthenticator(mContext) {
    override fun getAuthTokenLabel(p0: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun confirmCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Bundle?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        TODO("not implemented")
        val am = AccountManager.get(mContext)

        val authToken = am.peekAuthToken(account, authTokenType)

        if (authToken.isNullOrEmpty()) {

        }
    }

    override fun hasFeatures(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Array<out String>?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle {
        val intent = Intent(mContext, LoginActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }
}