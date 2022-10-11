package com.frostfel.animelist.views.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import com.frostfel.animelist.R

class FavoriteButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageButton(context, attrs) {
    init {
        setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_star_empty,
                null
            )
        )
    }

    fun setState(enabled: Boolean) {
        setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                getIconDependingOnState(enabled),
                null
            )
        )
    }

    private fun getIconDependingOnState(enabled: Boolean): Int {
        return if (enabled) R.drawable.ic_star else R.drawable.ic_star_empty
    }
}