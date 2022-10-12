package com.frostfel.animelist.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.AnimeSearchViewBinding


class AnimeSearchView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val inflater = LayoutInflater.from(context)
    private val binding = AnimeSearchViewBinding.inflate(inflater, this, true)
    var submitQueryChange: (text: String) -> Unit = { }
    var onClearClicked: () -> Unit = { }

    init {
        setupEvents()
    }

    private fun setupEvents() {
        with(binding) {
            clearView.setOnClickListener {
                val imm: InputMethodManager? =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(searchField.windowToken, 0)
                onClearClicked()
                searchField.clearFocus()
                searchField.text?.clear()
                clearView.visibility = View.GONE
            }

            searchField.doAfterTextChanged {
                setVisibilityDependingOnQueryCount(it?.count() ?: 0)
                submitQueryChange(it.toString())
            }
            searchField.setOnFocusChangeListener { _, focused ->
                searchField.gravity = when (focused) {
                    true -> {
                        Gravity.START
                    }
                    else -> {
                        Gravity.CENTER
                    }
                }
                val drawableStart = if (focused) ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_search_ic,
                    null
                ) else null
                searchLayout.startIconDrawable = drawableStart
                if (focused) clearView.visibility = View.VISIBLE else clearView.visibility = GONE
            }

        }
    }

    fun getQuery(): String {
        return binding.searchField.text.toString()
    }

    private fun setVisibilityDependingOnQueryCount(count: Int) {
        binding.clearView.isVisible = count > 0 || binding.searchField.hasWindowFocus()
    }
}