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

    override fun setTransactions(transactions: List<Transaction>) =
            submitList(buildSectionedList(transactions))

    override fun appendTransactions(newTransactions: List<Transaction>) {
        if (newTransactions.isEmpty()) return

        val newItems = buildSectionedList(newTransactions, getLastYearMonthInList())
        submitList(currentList + newItems)
    }

    private fun getLastYearMonthInList(): YearMonth? =
            when (val lastItem = currentList.lastOrNull()) {
                is AdapterItem.TransactionItem -> lastItem.value.date.yearMonth
                is AdapterItem.Header -> lastItem.month
                null -> null
            }

    /**
     * Builds a list of [AdapterItem]s by inserting headers between groups of [Transaction]s
     * with the same [YearMonth].
     * @param itemList The initial list
     * @param startFrom The [YearMonth] to compare with the first group. Useful if you want to
     *                  append the result to an existing list. If null, the first item will always
     *                  be a header.
     */
    private fun buildSectionedList(itemList: List<Transaction>, startFrom: YearMonth? = null): List<AdapterItem> {
        val newItems: MutableList<AdapterItem> = mutableListOf()

        var prevYearMonth: YearMonth? = startFrom

        for (transaction in itemList) {
            // If the previous YearMonth is different or null, add a header
            if (prevYearMonth != transaction.date.yearMonth) {
                newItems.add(AdapterItem.Header(transaction.date.yearMonth))
            }

            newItems.add(AdapterItem.TransactionItem(transaction))
            prevYearMonth = transaction.date.yearMonth
        }

        return newItems
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
        when (val adapterItem = getItem(position)) {
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
