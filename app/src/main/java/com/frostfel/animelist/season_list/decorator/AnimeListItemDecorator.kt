package com.frostfel.animelist.season_list.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.R

class AnimeListItemDecorator: RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val currentPosition = parent.getChildAdapterPosition(view)

        val leftOffsetBasedOnPosition = if (currentPosition == 0) {
            view.resources.getDimension(R.dimen.first_item_left_offset)
        } else {
            view.resources.getDimension(R.dimen.left_offset)
        }

        val rightOffsetBasedOnPosition = view.resources.getDimension(R.dimen.right_offset)

        outRect.apply {
            right = rightOffsetBasedOnPosition.toInt()
            left = leftOffsetBasedOnPosition.toInt()
        }
    }
}