package ro.ande.dekont.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapted from https://gist.github.com/nesquena/d09dc68ff07e845cc622
 */
abstract class PagedLoadScrollListener(
        var layoutManager: LinearLayoutManager,
        private val visibleThreshold: Int = DEFAULT_VISIBLE_THRESHOLD,
) : RecyclerView.OnScrollListener() {
    private var currentPage = 0
    private var previousTotalItemCount = 0
    private var loading = false
    private var loadExhausted = false
    var loadCompleteNotifier: LoadCompleteNotifier = object : LoadCompleteNotifier {
        override fun notifyLoadComplete() {
            loading = false
            previousTotalItemCount = layoutManager.itemCount
        }

        override fun notifyLoadExhausted() {
            loadExhausted = true
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        // no-op if we're loading, or exhausted
        if (loading || loadExhausted) {
            return
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        val totalItemCount = layoutManager.itemCount
        if (totalItemCount < previousTotalItemCount) {
            currentPage = 0
            previousTotalItemCount = totalItemCount
        } else if (totalItemCount > 0) {
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
                currentPage++
                loading = true
                onLoadMore(currentPage, loadCompleteNotifier)
            }
        }
    }

    interface LoadCompleteNotifier {
        /**
         * Call to notify that a load has completed, with new items present.
         */
        fun notifyLoadComplete()

        /**
         * Call to notify that a load has completed, but no new items were present, and none will be forthcoming.
         */
        fun notifyLoadExhausted()
    }

    /**
     * Override this to handle loading of new data. Each time new data is pulled in, the page counter will increase by one.
     * When your load is complete, call the appropriate method on the loadComplete callback.
     * @param page the page counter. Increases by one each time onLoadMore is called.
     * @param loadComplete callback to invoke when your load has completed.
     */
    abstract fun onLoadMore(page: Int, loadComplete: LoadCompleteNotifier)

    companion object {
        private const val DEFAULT_VISIBLE_THRESHOLD = 5
    }
}