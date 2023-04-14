package com.example.rsaproj.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

private const val IGNORE_INPUT_TAG = "ignore_input_tag"

fun Context.compatColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.compatColorsList(@ColorRes id: Int): ColorStateList {
    return ContextCompat.getColorStateList(this, id)!!
}

fun Context.compatDrawable(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

internal fun EditText.setTextVal(value: String) {
    if (text.toString() == value) return
    else setVal(value)
}

fun EditText.setTextIfChanged(charSequence: CharSequence) {
    if (text.toString() != charSequence.toString()) {
        setVal(charSequence)
    }
}

internal fun EditText.setVal(value: CharSequence) {
    isIgnoreUserInput = true

    val oldStart = selectionStart
    val oldEnd = selectionEnd

    if (value.isBlank()) {
        text.clear()
    } else {
        setText(value)
    }

    var newStart = Math.min(oldStart, text.length)
    var newEnd = Math.min(oldEnd, text.length)
    if(newStart < 0 && newEnd < 0) {
        newStart = text.length
        newEnd = text.length
    }

    setSelection(newStart, newEnd)

    isIgnoreUserInput = false
}

internal var EditText.isIgnoreUserInput
    get() = tag == IGNORE_INPUT_TAG
    set(value) { tag = if(value) IGNORE_INPUT_TAG else null }

val Int.dp get() = (this * Resources.getSystem().displayMetrics.density).toInt()
