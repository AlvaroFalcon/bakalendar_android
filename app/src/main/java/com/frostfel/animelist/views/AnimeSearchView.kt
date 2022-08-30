package com.frostfel.animelist.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.frostfel.animelist.databinding.AnimeSearchViewBinding

class AnimeSearchView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

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
                onClearClicked()
                searchField.text?.clear()
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
            }

        }
    }

    private fun setVisibilityDependingOnQueryCount(count: Int) {
        binding.clearView.isVisible = count > 0
    }
}