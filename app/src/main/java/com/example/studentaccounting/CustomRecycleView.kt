package com.example.studentaccounting

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class CustomRecycleView(ctx: Context, attrs: AttributeSet) : RecyclerView(ctx, attrs) {

    private var totalHeight: Int = 0

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST))
            totalHeight += child.measuredHeight
        }
        super.getLayoutParams().height = totalHeight
        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY))
    }

}