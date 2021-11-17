package ro.ande.dekont.util

import android.content.Context
import androidx.annotation.StringRes

sealed class StringResource {
    /** Obtain the string, whatever type it may be. */
    fun getString(context: Context): kotlin.String =
        when (this) {
            is Resource -> context.getString(id)
            is String -> string
        }

    class Resource(@StringRes val id: Int) : StringResource()
    class String(val string: kotlin.String) : StringResource()

    companion object {
        /** Creates a [StringResource.String] if [string] is not null, otherwise creates a [StringResource.Resource] containing [default]. */
        fun createWithDefault(string: kotlin.String?, @StringRes default: Int): StringResource =
            string?.let { StringResource(it) } ?: StringResource(default)
    }
}

fun StringResource(string: String): StringResource =
    StringResource.String(string)

fun StringResource(resId: Int): StringResource =
    StringResource.Resource(resId)

fun String.toStringResource(): StringResource = StringResource(this)

fun String?.toStringResource(@StringRes default: Int): StringResource =
    StringResource.createWithDefault(this, default)
