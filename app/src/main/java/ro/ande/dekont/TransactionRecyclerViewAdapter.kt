package ro.ande.dekont


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_list_item.view.*
import ro.ande.dekont.dummy.DummyContent.DummyItem
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Transaction

class TransactionRecyclerViewAdapter(
        private val transactions: Resource<List<Transaction>>
) : RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.transaction_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transactions.data?.get(position)
        holder.dayView.text = item?.date?.dayOfMonth.toString()
        holder.amountView.text = item?.amount.toString()
        holder.currencyView.text = item?.currency?.currencyCode

//        with(holder.view) {
//            tag = item
//            setOnClickListener(mOnClickListener)
//        }
    }

    override fun getItemCount(): Int = transactions.data?.size ?: 0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dayView: TextView = view.transaction_day
        val amountView: TextView = view.transaction_amount
        val currencyView: TextView = view.transaction_currency
    }
}
