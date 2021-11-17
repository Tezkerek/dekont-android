package ro.ande.dekont.api

import com.google.gson.annotations.SerializedName

class PaginatedResponse<T>(
    @field:SerializedName("page")
    val page: Int,
    @field:SerializedName("page_count")
    val pageCount: Int,
    @field:SerializedName("results")
    val data: T
)
