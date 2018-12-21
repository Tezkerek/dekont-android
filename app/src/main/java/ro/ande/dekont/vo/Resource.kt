package ro.ande.dekont.vo

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
open class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    fun isSuccess() = status == Status.SUCCESS
    fun isError() = status == Status.ERROR
    fun isLoading() = status == Status.LOADING

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}

class ResourceDeletion(status: Status, message: String?) : Resource<Void>(status, null, message) {
    companion object {
        fun success(): ResourceDeletion {
            return ResourceDeletion(Status.SUCCESS, null)
        }

        fun error(msg: String): ResourceDeletion {
            return ResourceDeletion(Status.ERROR, msg)
        }

        fun loading(): ResourceDeletion {
            return ResourceDeletion(Status.LOADING, null)
        }
    }
}

enum class Status {
    SUCCESS, ERROR, LOADING
}