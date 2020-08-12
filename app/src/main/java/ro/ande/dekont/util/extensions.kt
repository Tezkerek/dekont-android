package ro.ande.dekont.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.json.JSONArray
import org.json.JSONObject

/** Similar to the zip function in RxJava. Combines the two LiveData instances into one
 * that emits Pair<A, B> values.
 */
fun <A, B> zipLiveData(first: LiveData<A>, second: LiveData<B>): MediatorLiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localA = lastA
            val localB = lastB
            if (localA != null && localB != null) {
                this.value = Pair(localA, localB)
            }
        }

        addSource(first) { lastA = it; update() }
        addSource(second) { lastB = it; update() }
    }
}

fun JSONObject.getStringOrNull(name: String): String? =
        opt(name)?.toString()

/**
 * Collects all of the [JSONArray]'s elements of type [T] into a list.
 * @param T The type of the elements to collect
 */
inline fun <reified T> JSONArray.toList(): List<T> {
    val mutableNonFieldErrors = mutableListOf<T>()

    for (i in 0 until length()) {
        get(i).also {
            if (it is T) {
                mutableNonFieldErrors.add(it as T)
            }
        }
    }

    return mutableNonFieldErrors
}
