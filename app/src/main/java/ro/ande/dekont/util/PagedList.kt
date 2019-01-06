package ro.ande.dekont.util

/**
 * A collection of items organized in pages.
 */
class PagedList<T> {
    val pages = mutableMapOf<Int, List<T>>()

    /**
     * Set the contents of a page, replacing the previous ones.
     * @param page The page number
     * @param contents The new contents of the page
     */
    fun setPageContents(page: Int, contents: List<T>) {
        pages[page] = contents
    }

    /**
     * Retrieve a list containing all the items in the paged list.
     */
    fun getAll(): List<T> =
            pages.values.fold(mutableListOf()) { all, itemsAtPage ->
                all.apply { addAll(itemsAtPage) }
            }
}