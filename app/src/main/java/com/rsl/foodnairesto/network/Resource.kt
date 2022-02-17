package com.rsl.foodnairesto.network

class Resource<T> constructor(
    val status: Status,
    val data: T?,
    val headers: Map<String, Any>?,
    val message: String?
) {

    companion object {

        @JvmStatic
        fun <T> success(data: T?, headers: Map<String, Any>? = emptyMap()): Resource<T> {
            return Resource(Status.SUCCESS, data, headers, null)
        }

        @JvmStatic
        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, emptyMap(), msg)
        }

        @JvmStatic
        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, emptyMap(), null)
        }
    }

    enum class Status {
        SUCCESS, ERROR, LOADING
    }
}