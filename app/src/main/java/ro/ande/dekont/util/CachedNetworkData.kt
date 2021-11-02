package ro.ande.dekont.util

import kotlinx.coroutines.flow.Flow

/** A wrapper class for returning cached data and a network state for the status of the server data loading. */
class CachedNetworkData<T>(
        val data: Flow<T>,
        val networkState: Flow<NetworkState>
)

sealed class NetworkState(val isExhausted: Boolean)
class NetworkSuccessState(isExhausted: Boolean) : NetworkState(isExhausted)
class NetworkErrorState(val message: String, isExhausted: Boolean = false) : NetworkState(isExhausted)
class NetworkLoadingState : NetworkState(false)