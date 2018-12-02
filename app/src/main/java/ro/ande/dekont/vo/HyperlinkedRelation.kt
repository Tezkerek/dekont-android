package ro.ande.dekont.vo

import java.net.URL

/**
 * Represents a relationship that holds the related resource's id and a hyperlink to the resource.
 */
data class HyperlinkedRelation(
        val id: Int,
        val url: URL
) {
    companion object {
        fun empty() = HyperlinkedRelation(0, URL(""))
    }
}