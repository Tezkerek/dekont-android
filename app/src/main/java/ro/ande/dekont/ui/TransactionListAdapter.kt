package ro.ande.dekont.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_list_header.view.*
import kotlinx.android.synthetic.main.transaction_list_item.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import ro.ande.dekont.R
import ro.ande.dekont.util.yearMonth
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Transaction

class TransactionListAdapter
    : ListAdapter<AdapterItem, TransactionListAdapter.ViewHolder>(TransactionsDiffUtil()),
        ITransactionListManager {

    private var categoriesById: Map<Int, Category> = mapOf()

    override fun setCategories(categories: List<Category>) {
        categoriesById = categories.associateBy { it.id }
    }

    override fun appendTransactions(newTransactions: List<Transaction>) {
        if (newTransactions.isEmpty()) return

        val newItems: MutableList<AdapterItem> = mutableListOf()

        // Start with the YearMonth of the last item from the current list,
        // which should be a TransactionItem
        val lastItem = currentList.lastOrNull() as AdapterItem.TransactionItem?
        var prevYearMonth: YearMonth? = lastItem?.value?.date?.yearMonth

        for (transaction in newTransactions) {
            // If the previous YearMonth is different or null, add a header
            if (prevYearMonth != transaction.date.yearMonth) {
                newItems.add(AdapterItem.Header(transaction.date.yearMonth))
            }

            newItems.add(AdapterItem.TransactionItem(transaction))
            prevYearMonth = transaction.date.yearMonth
        }

        submitList(currentList + newItems)
    }

    override fun isItemHeader(position: Int): Boolean =
            getItemViewType(position) == TYPE_HEADER

    override var onTransactionClickListener: ITransactionListManager.OnTransactionClickListener? = null
    override var onTransactionLongClickListener: ITransactionListManager.OnTransactionLongClickListener? = null

    override fun getItemViewType(position: Int): Int =
            when (getItem(position)) {
                is AdapterItem.TransactionItem -> TYPE_TRANSACTION
                is AdapterItem.Header -> TYPE_HEADER
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TRANSACTION -> {
                val view = layoutInflater
                        .inflate(R.layout.transaction_list_item, parent, false)
                ViewHolder.TransactionItem(view)
            }
            TYPE_HEADER -> {
                val view = layoutInflater
                        .inflate(R.layout.transaction_list_header, parent, false)
                ViewHolder.Header(view)

            }
            else -> throw IllegalArgumentException("Invalid viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterItem = getItem(position)
        when (adapterItem) {
            is AdapterItem.TransactionItem -> {
                holder as ViewHolder.TransactionItem

                val transaction = adapterItem.value
                val category = categoriesById[adapterItem.value.categoryId]
                holder.bind(adapterItem.value, category)

                holder.view.apply {
                    setOnClickListener { onTransactionClickListener?.onClick(transaction.id) }
                    setOnLongClickListener { onTransactionLongClickListener?.onLongClick(transaction.id); true }
                }
            }
            is AdapterItem.Header -> {
                holder as ViewHolder.Header
                holder.bind(adapterItem.month)
            }
        }
    }

    sealed class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        class TransactionItem(view: View) : ViewHolder(view) {
            val dayView: TextView = view.transaction_day
            val categoryView: TextView = view.transaction_category
            val amountView: TextView = view.transaction_amount
            val currencyView: TextView = view.transaction_currency

            fun bind(transaction: Transaction, category: Category?) {
                dayView.text = transaction.date.dayOfMonth.toString()
                categoryView.text = transaction.categoryId?.let { id ->
                    category?.name ?: "id: $id"
                }
                amountView.text = transaction.formattedAmount
                currencyView.text = transaction.currency.currencyCode
            }
        }

        class Header(view: View) : ViewHolder(view) {
            val dateView: TextView = view.month_view

            fun bind(month: YearMonth) {
                dateView.text = month.let { "${it.month.name} ${it.year}" }
            }

            fun bind(date: LocalDate) = bind(date.yearMonth)
        }
    }

    class TransactionsDiffUtil : DiffUtil.ItemCallback<AdapterItem>() {
        override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean =
                when (oldItem) {
                    is AdapterItem.TransactionItem ->
                        newItem is AdapterItem.TransactionItem
                                && oldItem.value.id == newItem.value.id
                    is AdapterItem.Header ->
                        newItem is AdapterItem.Header
                                && oldItem.month == newItem.month
                }

        override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean =
                when (oldItem) {
                    is AdapterItem.TransactionItem ->
                        newItem is AdapterItem.TransactionItem
                                && oldItem.value == newItem.value
                    is AdapterItem.Header ->
                        newItem is AdapterItem.Header
                                && oldItem.month == newItem.month
                }

    }

    companion object {
        const val TYPE_TRANSACTION = 0
        const val TYPE_HEADER = 1
    }
}

sealed class AdapterItem {
    class TransactionItem(val value: Transaction) : AdapterItem()
    class Header(val month: YearMonth) : AdapterItem()
}
