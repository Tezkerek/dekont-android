package ro.ande.dekont.util

import kotlinx.coroutines.flow.Flow

/** A wrapper class for returning cached data and a network state for the status of the server data loading. */
class CachedNetworkData<T>(
        val data: Flow<T>,
        val networkState: Flow<NetworkState>
)

class NetworkState(
        val status: Status,
        val message: String? = null,
        val isExhausted: Boolean = false
) {
    val isSuccess
        get() = status == Status.SUCCESS

    val isLoading
        get() = status == Status.LOADING

    val isError
        get() = status == Status.ERROR

    enum class Status {
        SUCCESS,
        LOADING,
        ERROR
    }

    companion object {
        fun success(message: String? = null, isExhausted: Boolean = false) =
                NetworkState(Status.SUCCESS, message, isExhausted)

        fun loading(message: String? = null, isExhausted: Boolean = false) =
                NetworkState(Status.LOADING, message, isExhausted)

        fun error(message: String? = null, isExhausted: Boolean = false) =
                NetworkState(Status.ERROR, message, isExhausted)
    }
}