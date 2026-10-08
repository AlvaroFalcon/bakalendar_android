package com.frostfel.animelist.views.utils

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/** Fades items out under the search bar only; the bottom edge stays sharp. */
class TopFadingRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    override fun getBottomFadingEdgeStrength(): Float = 0f
}
