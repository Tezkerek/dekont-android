package ro.ande.dekont.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

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
