package com.example.rsaproj.ui.controls

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.example.rsaproj.R
import com.example.rsaproj.utils.compatColor
import com.example.rsaproj.utils.compatColorsList
import com.example.rsaproj.utils.dp
import com.example.rsaproj.utils.setTextVal

open class CustomInputLayout
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    var error: String?
        get() = errorTextView.text.toString()
        set(value) {
            if (value.isNullOrBlank()) {
                editTextField.backgroundTintList =
                    context.compatColorsList(defaultEditTextFieldBkgColor)
                errorTextView.text = ""
                errorTextView.isVisible = false
            } else {
                editTextField.backgroundTintList = context.compatColorsList(R.color.red_10)
                errorTextView.text = value
                errorTextView.isVisible = true
            }
            if (value == error) return
            if (editText.hasFocus()) editText.requestFocus()
        }

    var key: String?
        get() = keyTextView.text.toString()
        set(value) {
            keyTextView.text = value ?: ""
            val padding = if (keyTextView.text.isBlank()) 0 else 10.dp
            keyTextView.apply {
                if (padding != paddingStart) setPadding(
                    padding,
                    paddingTop,
                    paddingEnd,
                    paddingBottom
                )
            }
        }

    var hint: String?
        get() = editText.hint.toString()
        set(value) {
            editText.hint = value
        }

    var inputType: Int
        get() = editText.inputType
        set(value) {
            editText.inputType = value
        }

    var filters: Array<InputFilter>
        get() = editText.filters
        set(value) {
            editText.filters = value
        }

    @ColorRes
    var defaultEditTextFieldBkgColor: Int = R.color.bkg_grey
        set(value) {
            field = value
            editTextField.backgroundTintList = context.compatColorsList(value)
        }

    lateinit var editText: CustomAppCompatEditText
    protected lateinit var editTextLayout: LinearLayout
    protected lateinit var editTextField: LinearLayout
    private lateinit var keyTextView: TextView
    private lateinit var errorTextView: TextView

    init {
        inflateLayout()
        initTypedArray(attrs)
    }

    private fun inflateLayout() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.edittext_custom, this, true)
        editTextLayout = findViewById(R.id.edit_text_layout)
        editTextField = findViewById(R.id.edit_text_field)
        keyTextView = findViewById(R.id.edit_text_key)
        editText = findViewById(R.id.edit_text)
        errorTextView = findViewById(R.id.edit_text_error_txt)
    }

    private fun initTypedArray(attrs: AttributeSet?) {
        val ta = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomInputLayout,
            0,
            0
        )

        error = ta.getString(R.styleable.CustomInputLayout_error)
        key = ta.getString(R.styleable.CustomInputLayout_key)
        editText.setTextVal(ta.getString(R.styleable.CustomInputLayout_value) ?: "")
        inputType =
            ta.getInteger(R.styleable.CustomInputLayout_inputType, InputType.TYPE_CLASS_TEXT)
        editText.hint = ta.getString(R.styleable.CustomInputLayout_hint)
        editText.setHintTextColor(context.compatColor(R.color.text_secondary))

        ta.recycle()
    }
}