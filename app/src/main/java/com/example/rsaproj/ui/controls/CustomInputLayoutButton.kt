package com.example.rsaproj.ui.controls

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.ColorRes
import com.example.rsaproj.R
import com.example.rsaproj.utils.compatColorsList
import com.example.rsaproj.utils.compatDrawable
import com.example.rsaproj.utils.dp

open class CustomInputLayoutButton
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): CustomInputLayout(context, attrs, defStyleAttr) {

    var buttonSrc: Drawable?
        get() = button.drawable
        set(value) {
            button.setImageDrawable(value)
        }

    @ColorRes var buttonBkgColorId: Int = R.color.green
        set(value) {
            field = value
            button.backgroundTintList = context.compatColorsList(value)
        }

    private var button = ImageButton(
        ContextThemeWrapper(context, R.style.Theme_RSAProj_EditTextButton),
        null,
        0
    )
    private var buttonLayout = FrameLayout(context)

    init {
        initTypedArray(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        buildView()
    }

    fun setOnButtonClickListener(onClick: (View) -> Unit) {
        button.setOnClickListener { onClick(it) }
    }

    private fun initTypedArray(attrs: AttributeSet?) {
        val ta = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomInputLayoutButton,
            0,
            0
        )

        buttonSrc = ta.getDrawable(R.styleable.CustomInputLayoutButton_button_src)

        ta.recycle()
    }

    protected open fun buildView() {
        button.setupButton()
        buttonLayout.setupButtonLayout()

        buttonLayout.removeView(button)
        buttonLayout.addView(button)

        editTextField.removeView(buttonLayout)
        editTextField.addView(buttonLayout)
    }

    private fun FrameLayout.setupButtonLayout() {
        val frameParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        val margin = resources.getDimension(R.dimen.input_layout_button_margin).toInt()
        frameParams.setMargins(0, margin, margin, margin)
        layoutParams = frameParams
        background = context.compatDrawable(R.drawable.bkg_segmented_default)
    }

    private fun ImageButton.setupButton() {
        val buttonParams = LayoutParams(
            resources.getDimension(R.dimen.input_layout_button_single_width).toInt(),
            LayoutParams.MATCH_PARENT
        )
        setPadding(9.dp, 9.dp, 9.dp, 9.dp)
        buttonParams.gravity = Gravity.CENTER
        layoutParams = buttonParams
    }
}