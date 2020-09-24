package ro.ande.dekont.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.from(this)


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
                mutableNonFieldErrors.add(it)
            }
        }
    }

    return mutableNonFieldErrors
}


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


/**
 * Iterates through the list invoking [f] for each element,
 * with the list's [MutableListIterator] as its receiver,
 * as long as [f] returns `true`.
 */
fun <T> MutableList<T>.withListIteratorWhile(f: MutableListIterator<T>.(Int, T) -> Boolean) {
    with (this.listIterator()) {
        while (hasNext()) {
            if (!this.f(nextIndex(), next()))
                return
        }
    }
}

/**
 * Removes the first element from this list that satisfies the predicate.
 * @return Whether the element was removed or not
 */
fun <T> MutableIterable<T>.removeFirst(predicate: (T) -> Boolean): Boolean {
    with (iterator()) {
        while (hasNext()) {
            if (predicate(next())) {
                remove()
                return true
            }
        }
    }
    return false
}