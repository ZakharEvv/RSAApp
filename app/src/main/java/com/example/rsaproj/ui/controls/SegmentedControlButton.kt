package com.example.rsaproj.ui.controls

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.example.rsaproj.R

class SegmentedControlButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setTextAppearance(R.style.SegmentedButtonTextAppearance)
    }
}