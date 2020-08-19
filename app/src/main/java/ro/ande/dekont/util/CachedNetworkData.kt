package ro.ande.dekont.util

import kotlinx.coroutines.flow.Flow

/** A wrapper class for returning cached data and a network state for the status of the server data loading. */
class CachedNetworkData<T>(
        val data: Flow<T>,
        val networkState: Flow<NetworkState>
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