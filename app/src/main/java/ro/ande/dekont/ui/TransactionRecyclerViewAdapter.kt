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
import org.zakariya.stickyheaders.SectioningAdapter
import ro.ande.dekont.R
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Transaction

class TransactionRecyclerViewAdapter : SectioningAdapter() {
    private var transactions: MutableList<Transaction> = mutableListOf()
    private var categories: List<Category> = listOf()
    private var sections: MutableList<Section> = mutableListOf()

    private var onTransactionClickListener: OnTransactionClickListener? = null
    private var onTransactionLongPressListener: OnTransactionLongPressListener? = null

    fun setTransactions(transactions: List<Transaction>) {
        val oldTransactions = this.transactions
        val oldSections = this.sections

        val byYearMonth: Map<YearMonth, List<Transaction>> = transactions
                .asSequence()
                .groupBy { YearMonth.from(it.date) }

        this.transactions = transactions.toMutableList()
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
                                if (oldSection != null) {
                                    // Contents are unchanged if size is the same, and the elements have not changed
                                    val areContentsUnchanged = oldSection.transactions.let {
                                        it.size == contents.size &&
                                        it.foldIndexed(true) { i, acc, transaction -> acc && transaction == contents[i] }
                                    }
                                        if (areContentsUnchanged) oldSection.isCollapsed else false
                                } else {
                                    // New section is collapsed
                                    false
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

    fun addTransactions(transactions: List<Transaction>) {
        // Add transactions
        this.transactions.addAll(transactions)

        // Add sections to end, expanded by default
        val newSections = transactions.groupBy { YearMonth.from(it.date) }.map { TransactionRecyclerViewAdapter.Section(it.key, it.value) }
        val indexOffset = this.sections.size
        this.sections.addAll(newSections)
        // Notify new sections inserted
        newSections.forEachIndexed { index, _ -> notifySectionInserted(indexOffset+index) }
    }

    fun mergeTransactions(transactions: List<Transaction>) {
        addTransactions(transactions.minus(this.transactions))
    }

    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyAllSectionsDataSetChanged()
    }

    interface OnTransactionClickListener {
        fun onClick(transactionId: Int)
    }

    interface OnTransactionLongPressListener {
        fun onLongPress(transactionId: Int)
    }

    /** Sets the listener to be called on a long press */
    fun setOnTransactionLongPressListener(listener: OnTransactionLongPressListener) {
        onTransactionLongPressListener = listener
    }
    fun setOnTransactionLongPressListener(listener: (Int) -> Unit) {
        setOnTransactionLongPressListener(object : OnTransactionLongPressListener {
            override fun onLongPress(transactionId: Int) {
                listener(transactionId)
            }
        })
    }

    /** Sets the listener to be called on a click */
    fun setOnTransactionClickListener(listener: OnTransactionClickListener) {
        onTransactionClickListener = listener
    }
    fun setOnTransactionClickListener(listener: (Int) -> Unit) {
        setOnTransactionClickListener(object : OnTransactionClickListener {
            override fun onClick(transactionId: Int) {
                listener(transactionId)
            }
        })
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
        // Try to find the category of the transaction. If not found, display the category id
        viewHolder.categoryView.text =
                if (transaction.categoryId == null) ""
                else categories.find { it.id == transaction.categoryId }?.name ?: "id: ${transaction.categoryId}"
        viewHolder.amountView.text = transaction.formattedAmount
        viewHolder.currencyView.text = transaction.currency.currencyCode

        // Set click listener
        viewHolder.view.setOnClickListener {
            this.onTransactionClickListener?.onClick(transaction.id)
        }

        // Set long press listener
        viewHolder.view.setOnLongClickListener {
            this.onTransactionLongPressListener?.onLongPress(transaction.id)
            true
        }
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
        val categoryView: TextView = view.transaction_category
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
