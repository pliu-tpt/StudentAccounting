package com.example.studentaccounting

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG


class DividerItemDecorator(private val mDivider: Drawable?, private val viewIds: IntArray? = null, private val notViewIds: IntArray? = null) : ItemDecoration() {


    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft + 5
        val dividerRight = parent.width - parent.paddingRight - 5
        val childCount = parent.childCount

        for (i in 0  until childCount) {

            val child: View = parent.getChildAt(i)
            Log.i(MYTAG, child.id.toString())
            if (hasDivider(child.id)) {
                mDivider?.also {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val dividerTop: Int = child.bottom + params.bottomMargin
                    val dividerBottom = dividerTop + it.intrinsicHeight
                    it.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                    it.draw(c)
                }
            }
        }
    }

    private fun hasDivider(viewId: Int): Boolean {
        return when (viewIds != null) {
            true -> viewIds.contains(viewId)
            false -> {
                when (notViewIds != null) {
                    true -> !notViewIds.contains(viewId)
                    false -> true
                }
            }
        }
    }
}
