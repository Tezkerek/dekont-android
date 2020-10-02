package ro.ande.dekont.ui

// Source: https://gist.github.com/filipkowicz/1a769001fae407b8813ab4387c42fcbd

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class HeaderItemDecoration(
        parent: RecyclerView,
        private val shouldFadeOutHeader: Boolean = false,
        private val isHeader: (itemPosition: Int) -> Boolean
) : RecyclerView.ItemDecoration() {

    private var currentHeader: Header? = null

    init {
        parent.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                // Clear saved header as it can be outdated now
                currentHeader = null
            }
        })

        parent.doOnEachNextLayout {
            // Clear saved layout as it may need layout update
            currentHeader = null
        }

        // Handle click on sticky header
        parent.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
            ): Boolean {
                return if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    motionEvent.y <= currentHeader?.viewHolder?.itemView?.bottom ?: 0
                } else false
            }
        })
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.findChildViewUnder(
                parent.paddingLeft.toFloat(),
                parent.paddingTop.toFloat()
        ) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerView = getHeaderViewForItem(topChildPosition, parent) ?: return

        val contactPoint = headerView.bottom + parent.paddingTop
        val childInContact = getChildInContact(parent, contactPoint) ?: return

        if (isHeader(parent.getChildAdapterPosition(childInContact))) {
            moveHeader(c, headerView, childInContact, parent.paddingTop)
            return
        }

        drawHeader(c, headerView, parent.paddingTop)
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View? {
        val headerPosition = getHeaderPositionForItem(itemPosition)
        if (headerPosition == RecyclerView.NO_POSITION) return null

        val headerType = parent.adapter?.getItemViewType(headerPosition) ?: return null
        // If match reuse viewHolder
        if (currentHeader?.position == headerPosition && currentHeader?.viewHolder?.itemViewType == headerType) {
            return currentHeader?.viewHolder?.itemView
        }

        val headerHolder = parent.adapter?.createViewHolder(parent, headerType)
        if (headerHolder != null) {
            parent.adapter?.onBindViewHolder(headerHolder, headerPosition)
            fixLayoutSize(parent, headerHolder.itemView)
            // Save for next draw
            currentHeader = Header(headerPosition, headerHolder)
        }
        return headerHolder?.itemView
    }

    private fun drawHeader(c: Canvas, header: View, paddingTop: Int) {
        c.save()
        c.translate(0f, paddingTop.toFloat())
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View, paddingTop: Int) {
        c.save()
        if (!shouldFadeOutHeader) {
            c.clipRect(0, paddingTop, c.width, paddingTop + currentHeader.height)
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                c.saveLayerAlpha(
                        RectF(0f, 0f, c.width.toFloat(), c.height.toFloat()),
                        (((nextHeader.top - paddingTop) / nextHeader.height.toFloat()) * 255).toInt()
                )
            } else {
                c.saveLayerAlpha(
                        0f, 0f, c.width.toFloat(), c.height.toFloat(),
                        (((nextHeader.top - paddingTop) / nextHeader.height.toFloat()) * 255).toInt(),
                        Canvas.ALL_SAVE_FLAG
                )
            }

        }
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat() /*+ paddingTop*/)

        currentHeader.draw(c)
        if (shouldFadeOutHeader) {
            c.restore()
        }
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val mBounds = Rect()
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            if (mBounds.bottom > contactPoint) {
                if (mBounds.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    /**
     * Properly measures and layouts the top sticky header.
     *
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
                View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun getHeaderPositionForItem(itemPosition: Int): Int {
        var headerPosition = RecyclerView.NO_POSITION
        var currentPosition = itemPosition
        do {
            if (isHeader(currentPosition)) {
                headerPosition = currentPosition
                break
            }
            currentPosition -= 1
        } while (currentPosition >= 0)
        return headerPosition
    }

    class Header(var position: Int, var viewHolder: RecyclerView.ViewHolder)
}

inline fun View.doOnEachNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        action(view)
    }
}
