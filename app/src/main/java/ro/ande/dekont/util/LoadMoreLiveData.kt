package ro.ande.dekont.util

import androidx.lifecycle.LiveData

/** A wrapper class for returning cached data and a network state for the status of the server data loading. */
class LoadMoreLiveData<T>(
        val data: LiveData<T>,
        val networkState: LiveData<NetworkState>
)

class NetworkState(
        val state: Status,
        val message: String? = null,
        val isExhausted: Boolean = false
) {
    enum class Status {
        SUCCESS,
        LOADING,
        ERROR
    }
}