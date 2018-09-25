package ro.ande.dekont


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.transaction_list_header.view.*
import kotlinx.android.synthetic.main.transaction_list_item.view.*
import org.threeten.bp.Month
import org.zakariya.stickyheaders.SectioningAdapter
import ro.ande.dekont.vo.Transaction

class TransactionRecyclerViewAdapter(
        private var transactions: List<Transaction>
) : SectioningAdapter() {
    private var sections: MutableList<Section> = mutableListOf()

    init {
        setTransactions(transactions)
    }

    fun setTransactions(transactions: List<Transaction>) {
        this.transactions = transactions

        sections = transactions
                .asSequence()
                .groupBy { it.date.month }
                .map { entry -> Section(entry.key, entry.value) }
                .toMutableList()

        notifyAllSectionsDataSetChanged()
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemUserType: Int): ItemViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.transaction_list_item, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerUserType: Int): HeaderViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.transaction_list_header, parent, false)

        return MonthHeaderViewHolder(view)
    }

    override fun onBindItemViewHolder(viewHolder: ItemViewHolder, sectionIndex: Int, itemIndex: Int, itemType: Int) {
        viewHolder as TransactionViewHolder
        val transaction = sections[sectionIndex].transactions[itemIndex]
        viewHolder.dayView.text = transaction.date.dayOfMonth.toString()
        viewHolder.amountView.text = transaction.amount.toString()
        viewHolder.currencyView.text = transaction.currency.currencyCode
    }

    override fun onBindHeaderViewHolder(viewHolder: HeaderViewHolder, sectionIndex: Int, headerType: Int) {
        viewHolder as MonthHeaderViewHolder
        val section = sections[sectionIndex]
        viewHolder.monthView.text = section.month.name

        viewHolder.view.setOnClickListener { section.isExpanded = !section.isExpanded; notifyAllSectionsDataSetChanged() }
    }

    // Needed, otherwise crashes
    // See https://github.com/ShamylZakariya/StickyHeaders/issues/87#issuecomment-369088743
    override fun onCreateGhostHeaderViewHolder(parent: ViewGroup): GhostHeaderViewHolder {
        val view = View(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        return GhostHeaderViewHolder(view)
    }

    override fun getNumberOfSections(): Int = sections.size

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int = sections[sectionIndex].let { if (it.isExpanded) it.transactions.size else 0 }

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean = true

    override fun doesSectionHaveFooter(sectionIndex: Int): Boolean = false

    private class Section(val month: Month, val transactions: List<Transaction>) {
        var isExpanded: Boolean = true
    }

    inner class TransactionViewHolder(val view: View) : SectioningAdapter.ItemViewHolder(view) {
        val dayView: TextView = view.transaction_day
        val amountView: TextView = view.transaction_amount
        val currencyView: TextView = view.transaction_currency
    }

    inner class MonthHeaderViewHolder(val view: View) : SectioningAdapter.HeaderViewHolder(view) {
        val monthView: TextView = view.month_view
    }
}
