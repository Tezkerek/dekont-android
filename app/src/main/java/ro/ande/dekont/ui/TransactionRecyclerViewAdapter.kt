package ro.ande.dekont.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.transaction_list_header.view.*
import kotlinx.android.synthetic.main.transaction_list_item.view.*
import org.threeten.bp.YearMonth
import ro.ande.dekont.util.SectioningAdapter
import ro.ande.dekont.vo.Transaction
import ro.ande.dekont.R

class TransactionRecyclerViewAdapter() : SectioningAdapter() {
    private var transactions: List<Transaction> = listOf()
    private var sections: MutableList<Section> = mutableListOf()

    constructor(transactions: List<Transaction>) : this() {
        setTransactions(transactions)
    }

    fun setTransactions(transactions: List<Transaction>) {
        val oldTransactions = this.transactions
        val oldSections = this.sections

        val byYearMonth: Map<YearMonth, List<Transaction>> = transactions
                .asSequence()
                .groupBy { YearMonth.from(it.date) }

        this.transactions = transactions
        this.sections =  byYearMonth
                .map { entry ->
                    val yearMonth = entry.key
                    val contents =  entry.value

                    val oldSectionIndex = oldSections.indexOfFirst { it.yearMonth == yearMonth }
                    val oldSection = if (oldSectionIndex > -1) oldSections[oldSectionIndex] else null


                    // New sections are expanded
                    // Existing sections are expanded if their contents are changed
                    // Otherwise they keep their state
                    val isCollapsed =
                            let {
                                if (oldSection != null) {
                                    val areContentsUnchanged = oldSection.transactions.foldIndexed(true) { i, acc, transaction -> acc && transaction == contents[i] }
                                    if (areContentsUnchanged) oldSection.isCollapsed else false
                                } else {
                                    // New section is collapsed
                                    false
                                }
                            }

                    Section(yearMonth, entry.value, isCollapsed)
                }
                .toMutableList()

        notifyAllSectionsDataSetChanged()

        // Apply collapse states
        this.sections.forEachIndexed { index, section ->
            setSectionIsCollapsed(index, section.isCollapsed)
        }
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
        viewHolder.monthView.text = section.yearMonth.let { "${it.month.name} ${it.year}" }

        viewHolder.instantCollapse(section.isCollapsed)

        viewHolder.view.setOnClickListener { view ->
            section.isCollapsed = !isSectionCollapsed(sectionIndex)
            setSectionIsCollapsed(sectionIndex, section.isCollapsed)

            viewHolder.animateCollapse(section.isCollapsed)
        }
    }

    // Needed, otherwise crashes
    // See https://github.com/ShamylZakariya/StickyHeaders/issues/87#issuecomment-369088743
    override fun onCreateGhostHeaderViewHolder(parent: ViewGroup): GhostHeaderViewHolder {
        val view = View(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        return GhostHeaderViewHolder(view)
    }

    override fun getNumberOfSections(): Int = sections.size

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int = sections[sectionIndex].transactions.size

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean = true

    override fun doesSectionHaveFooter(sectionIndex: Int): Boolean = false

    private class Section(val yearMonth: YearMonth, transactions: List<Transaction>, var isCollapsed: Boolean = false) {
        val transactions: MutableList<Transaction> = transactions.toMutableList()
    }

    inner class TransactionViewHolder(val view: View) : ItemViewHolder(view) {
        val dayView: TextView = view.transaction_day
        val amountView: TextView = view.transaction_amount
        val currencyView: TextView = view.transaction_currency
    }

    inner class MonthHeaderViewHolder(val view: View) : HeaderViewHolder(view) {
        val monthView: TextView = view.month_view
        val collapseIconView: ImageView = view.collapse_icon
        var isCollapsed: Boolean = false

        fun instantCollapse(collapse: Boolean) {
            collapseIconView.rotation = if (collapse) 180.0f else 0.0f
            this.isCollapsed = collapse
        }

        fun animateCollapse(collapse: Boolean) {
            // Set collapsed/expanded icon
            val pivotX = collapseIconView.width / 2.0f
            val pivotY = collapseIconView.height / 2.0f
            val animation =
                    if (collapse)
                        RotateAnimation(0.0f, 180.0f, pivotX, pivotY)
                    else
                        RotateAnimation(180.0f, 0.0f, pivotX, pivotY)

            animation.duration = 500
            animation.repeatCount = 0
            animation.fillAfter = true

            collapseIconView.animation = animation

            this.isCollapsed = collapse
        }
    }
}
