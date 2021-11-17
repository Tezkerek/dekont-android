package ro.ande.dekont.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapted from https://gist.github.com/nesquena/d09dc68ff07e845cc622
 */
class PagedLoadScrollListener(
    layoutManager: LinearLayoutManager,
    visibleThreshold: Int = DEFAULT_VISIBLE_THRESHOLD,
) {
    var onLoadMore: (PagedLoadState) -> Unit = {}

    private var currentPage = 0
    private var previousTotalItemCount = 0
    private var prevPagedLoadState: PagedLoadState? = null

    val scrollListener =
        BottomScrollListener(layoutManager, visibleThreshold) onReachBottom@{ itemCount ->
            // no-op if previous state is loading, or exhausted
            if (prevPagedLoadState?.isLoading == true || prevPagedLoadState?.isExhausted == true) {
                return@onReachBottom
            }

            // Invalidate state if the item count is smaller
            if (itemCount < previousTotalItemCount) {
                currentPage = 0
                previousTotalItemCount = itemCount
                prevPagedLoadState = null
                return@onReachBottom
            }

            previousTotalItemCount = itemCount
            currentPage++
            prevPagedLoadState = PagedLoadState(currentPage, true).also { onLoadMore(it) }
        }

    class BottomScrollListener(
        private var layoutManager: LinearLayoutManager,
        private val visibleThreshold: Int = DEFAULT_VISIBLE_THRESHOLD,
        private val onReachBottom: (Int) -> Unit
    ) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val totalItemCount = layoutManager.itemCount
            if (totalItemCount > 0) {
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
                    onReachBottom(totalItemCount)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_VISIBLE_THRESHOLD = 5
    }
}

class PagedLoadState(
    val page: Int,
    var isLoading: Boolean = false,
    var isExhausted: Boolean = false,
) {
    fun notifyLoadComplete() {
        isLoading = false
    }

    fun notifyDataExhausted() {
        isExhausted = true
    }
}