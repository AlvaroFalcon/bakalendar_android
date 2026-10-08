package com.frostfel.animelist.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.frostfel.animelist.databinding.AnimeSearchViewBinding

/** Pill search field. Reports every change through [submitQueryChange]. */
class AnimeSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = AnimeSearchViewBinding.inflate(LayoutInflater.from(context), this, true)
    var submitQueryChange: (text: String) -> Unit = { }

    init {
        with(binding) {
            searchField.doAfterTextChanged {
                clearView.isVisible = !it.isNullOrEmpty()
                submitQueryChange(it.toString())
            }
            searchField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) hideKeyboard()
                actionId == EditorInfo.IME_ACTION_SEARCH
            }
            clearView.setOnClickListener {
                searchField.text?.clear()
                hideKeyboard()
            }
        }
    }

    fun getQuery(): String = binding.searchField.text.toString()

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.searchField.windowToken, 0)
        binding.searchField.clearFocus()
    }
}
