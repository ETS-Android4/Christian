package com.christian.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.christian.util.ChristianUtil

/**
 * author：Administrator on 2017/6/13 22:07
 * email：lanhuaguizha@gmail.com
 */
class ItemDecoration(private val left: Int = ChristianUtil.dpToPx(8), private val top: Int = ChristianUtil.dpToPx(8), private val right: Int = ChristianUtil.dpToPx(8), private val bottom: Int = ChristianUtil.dpToPx(8)) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = left
        outRect.right = right
        outRect.bottom = bottom

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) outRect.top = top
        //        if (parent.getChildAdapterPosition(view) == Objects.requireNonNull(parent.getAdapter()).getItemCount() - 1)
//            outRect.bottom = ChristianUtil.dpToPx(168);
    }
}