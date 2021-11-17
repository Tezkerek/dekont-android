package ro.ande.dekont.util

import android.content.Context
import android.widget.ArrayAdapter

data class IdTextPair(
    val id: Long,
    val text: String
) {
    override fun toString(): String = text
}

class IdTextPairAdapter(context: Context, resource: Int, objects: List<IdTextPair>) :
    ArrayAdapter<IdTextPair>(context, resource, objects) {
    override fun getItemId(position: Int): Long =
        getItem(position)!!.id
}