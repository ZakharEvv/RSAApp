package com.example.rsaproj.ui.controls

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import androidx.appcompat.widget.AppCompatEditText

class CustomAppCompatEditText
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val parentRect = Rect()

    // find next focusable widget
    override fun getFocusedRect(rect: Rect?) {
        super.getFocusedRect(rect)
        rect?.let {
            getMyParent().getFocusedRect(parentRect)
            rect.bottom = parentRect.bottom
        }
    }

    // return visible view area
    override fun getGlobalVisibleRect(rect: Rect?, globalOffset: Point?): Boolean {
        val result = super.getGlobalVisibleRect(rect, globalOffset)
        rect?.let {
            getMyParent().getGlobalVisibleRect(parentRect, globalOffset)
            rect.bottom = parentRect.bottom
        }
        return result
    }

    // request for an area to be visible if required by scroll
    override fun requestRectangleOnScreen(rect: Rect?): Boolean {
        val result = super.requestRectangleOnScreen(rect)
        val parent = getMyParent()
        // 10 is a random magic number to define a rectangle height.
        parentRect.set(0, parent.height - 10, parent.right, parent.height)
        parent.requestRectangleOnScreen(parentRect, true)
        return result;
    }

    private fun getMyParent(): View {
        var myParent: ViewParent? = parent;
        while (!(myParent is CustomInputLayout) && myParent != null) {
            myParent = myParent.parent
        }
        return if (myParent == null) this else myParent as View
    }

}