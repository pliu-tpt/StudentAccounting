package com.example.studentaccounting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.TransactionDayItemBinding
import com.example.studentaccounting.db.entities.relations.TransactionGroup

class StickyHeaderDecoration(private val adapter: TransactionGroupRecyclerViewAdapter, context: Context) : RecyclerView.ItemDecoration() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val binding: TransactionDayItemBinding = TransactionDayItemBinding.inflate(inflater, null, false)
    private val headerViewHolder: TransactionDayViewHolder = TransactionDayViewHolder(binding)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        var previousHeader: String? = null
        val childCount = parent.childCount

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) {
                continue
            }

            val headerGroup = getHeaderGroupForPosition(position)
            if (headerGroup != null &&
                headerGroup.transactions.firstOrNull()?.transaction?.date != previousHeader
            ) {
                drawHeader(c, parent, child, headerGroup)
                previousHeader = headerGroup.transactions.firstOrNull()?.transaction?.date
            }
        }
    }

    private fun getHeaderGroupForPosition(position: Int): TransactionGroup? {
        var count = 0
        for (group in adapter.transactionGroups) {  // Reference to adapter’s transaction groups
            if (position == count) {
                return group
            }
            count += group.transactions.size + 1 // Add 1 for the group item
            if (position < count) {
                return group
            }
        }
        return null
    }

    private fun drawHeader(c: Canvas, parent: RecyclerView, child: View, group: TransactionGroup) {
        headerViewHolder.bind(group, group.transactions.first().preferred_currency)
        measureLayout(parent, binding.root)

        // Calculate the top position for the header
        val top = Math.max(0, child.top - binding.root.height).toFloat()
        c.save()
        c.translate(0f, top)
        binding.root.draw(c)
        c.restore()
    }

    private fun measureLayout(parent: ViewGroup, header: View) {
        if (header !is ViewGroup) {
            Log.e("StickyHeaderDecoration", "Header view is not a ViewGroup")
            return
        }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        header.measure(widthSpec, heightSpec)
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)

        Log.d("StickyHeaderDecoration", "Header measured width: ${header.measuredWidth}, height: ${header.measuredHeight}")
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        val headerPosition = getHeaderGroupForPosition(position)
        if (headerPosition != null && (position == 0 || isNewHeader(position))) {
            outRect.top = binding.root.height
        } else {
            outRect.top = 0
        }
    }

    private fun isNewHeader(position: Int): Boolean {
        val prevHeaderPosition = getHeaderPosition(position - 1)
        val currentHeaderPosition = getHeaderPosition(position)
        return prevHeaderPosition != currentHeaderPosition
    }

    private fun getHeaderPosition(position: Int): TransactionGroup? {
        var count = 0
        for (group in adapter.transactionGroups) {  // Reference to adapter’s transaction groups
            if (position == count) {
                return group
            }
            count += group.transactions.size + 1 // Add 1 for the group item
            if (position < count) {
                return group
            }
        }
        return null
    }
}