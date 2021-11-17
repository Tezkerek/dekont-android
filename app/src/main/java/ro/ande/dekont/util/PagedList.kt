package ro.ande.dekont.util

/**
 * A collection of items organized in pages.
 */
class PagedList<T> {
    private val _pages: MutableMap<Int, List<T>> = mutableMapOf()
    val pages: Map<Int, List<T>>
        get() = _pages

    fun getPageContents(page: Int): List<T>? =
        pages[page]

    /**
     * Set the contents of a page, replacing the previous ones.
     * @param page The page number
     * @param contents The new contents of the page
     */
    fun setPageContents(page: Int, contents: List<T>) {
        _pages[page] = contents
    }

    /**
     * Retrieve a list containing all the items in the paged list.
     */
    fun getAll(): List<T> =
        pages.flatMap { it.value }
}