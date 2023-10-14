package com.example.rsaproj.ui.controls

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import com.example.rsaproj.R
import com.example.rsaproj.utils.compatColor
import com.example.rsaproj.utils.compatDrawable
import java.lang.IllegalArgumentException

class SegmentedControlGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var selectedButtonIndex = 0
    private var buttonWidth = 0

    private var activePointerId = INVALID_POINTER_ID
    private var isSliderTouched: Boolean = false
    private val optionPositionsMap = hashMapOf<Int, Float>()

    private var sliderPosition = 0F
    private var lastTouchX = 0F
    private var sliderRect = RectF()
    private var sliderPaint = Paint()
    private var sliderRadius = 0F
    var sliderColor = context.compatColor(R.color.white)
        set(value) {
            field = value
            sliderPaint.color = value
            invalidate()
        }
    private var sliderShadowRect = RectF()
    private var sliderShadowPaintLeft = Paint()
    private var sliderShadowPaintRight = Paint()

    private var dividerPaint = Paint()
    private var dividerColor = context.compatColor(R.color.white)
    private var dividerStrokeWidth = 1 * (Resources.getSystem().displayMetrics.density)
    private var dividerMargin = 0
    var inset: Float = 0F
        set(value) {
            field = value
            setupSliderShadowPaintLeft()
            setupSliderShadowPaintRight()
            invalidate()
        }
    private var padding: Int = 0

    private var shadowColor = context.compatColor(R.color.bkg_grey_dark)

    private var animator: ValueAnimator? = null
    private var cancelledPosition: Float? = null

    private var setSelectedIndexCallback: (() -> Unit)? = null
    private var selectedOptionCallback: ((Int) -> Unit)? = null

    companion object {
        const val INVALID_POINTER_ID = -1
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it, R.styleable.SegmentedControlGroup, 0, 0
            )
            sliderColor = typedArray.getColor(
                R.styleable.SegmentedControlGroup_customSliderColor,
                context.compatColor(R.color.white)
            )
            dividerColor = typedArray.getColor(
                R.styleable.SegmentedControlGroup_customDividerColor,
                context.compatColor(R.color.white)
            )
            shadowColor = typedArray.getColor(
                R.styleable.SegmentedControlGroup_customShadowColor,
                context.compatColor(android.R.color.black)
            )
            typedArray.recycle()
        }
        inset = resources.getDimensionPixelSize(R.dimen.segmented_control_inset).toFloat()
        padding = resources.getDimensionPixelSize(R.dimen.spacing_internal_half)
        dividerMargin = resources.getDimensionPixelSize(R.dimen.spacing_internal_medium)

        orientation = HORIZONTAL
        background = context.compatDrawable(R.drawable.bkg_segmented_default)
        setPadding(0, padding, 0, padding)

        sliderRadius = resources.getDimensionPixelSize(R.dimen.radius_extra_small).toFloat()
        sliderPaint.apply {
            flags = ANTI_ALIAS_FLAG
            color = sliderColor
            style = Paint.Style.FILL
        }
        dividerPaint.apply {
            flags = ANTI_ALIAS_FLAG
            color = dividerColor
            style = Paint.Style.STROKE
            strokeWidth = dividerStrokeWidth
        }

        sliderRect = RectF()
    }

    fun setSelectedIndex(index: Int, shouldAnimate: Boolean = false) {
        setSelectedIndexCallback = callback@{
            when {
                index == selectedButtonIndex -> return@callback
                shouldAnimate -> animateButtonMovement(newPositionIndex = index)
                else -> {
                    selectedButtonIndex = index
                    sliderPosition = selectedButtonIndex * buttonWidth.toFloat()
                    invalidate()
                }
            }
        }
        invalidate()
    }

    fun setOnSelectedOptionChangeCallback(callback: ((Int) -> Unit)) {
        this.selectedOptionCallback = callback
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.getX(actionIndex)
                activePointerId = event.getPointerId(0)
                if (lastTouchX >= sliderPosition + inset && lastTouchX <= sliderPosition + buttonWidth - inset) {
                    isSliderTouched = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isSliderTouched) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    val currentPointerPosX =
                        event.findPointerIndex(activePointerId).let { pointerIndex ->
                            try {
                                event.getX(pointerIndex)
                            } catch (e: IllegalArgumentException) {
                                lastTouchX
                            }
                        }
                    lastTouchX = currentPointerPosX
                    dragSlider(currentPointerPosX)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                isSliderTouched = false
            }

            MotionEvent.ACTION_UP -> {
                if (isSliderTouched) {
                    selectedOptionCallback?.invoke(selectedButtonIndex)
                }
                activePointerId = INVALID_POINTER_ID
                isSliderTouched = false
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // prevent pointerIndex to go out of range
                event.getPointerId(event.actionIndex).takeIf { it == activePointerId }?.run {
                    val newPointerIndex = if (this == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return false
    }

    private fun setupSliderShadowPaintLeft() {
        sliderShadowPaintLeft.apply {
            setShadowLayer(sliderRadius, 1.5F * inset, 1.5F * inset, shadowColor)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun setupSliderShadowPaintRight() {
        sliderShadowPaintRight.apply {
            setShadowLayer(sliderRadius, -1.5F * inset, 1.5F * inset, shadowColor)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun dragSlider(pointerPosX: Float) {
        if (childCount < 1) {
            return
        }
        var newPositionIndex = 0

        if (optionPositionsMap[1] != null && pointerPosX < optionPositionsMap[1]!!) {
            newPositionIndex = 0
        }

        val endPos = childCount - 1
        if (endPos > 0 && optionPositionsMap[endPos] != null && pointerPosX > optionPositionsMap[endPos]!!) {
            newPositionIndex = endPos
        }

        for (i in 1 until childCount - 1) {
            if (pointerPosX in optionPositionsMap[i]!!..optionPositionsMap[i + 1]!!) {
                newPositionIndex = i
            }
        }

        animateButtonMovement(newPositionIndex = newPositionIndex, isDragging = true)
    }

    private fun animateButtonMovement(
        newPositionIndex: Int,
        onAnimationEndCallback: (() -> Unit)? = null,
        isDragging: Boolean = false
    ) {
        if (newPositionIndex == selectedButtonIndex) {
            return
        }

        if (!isDragging) {
            animator?.cancel()
        }

        val newPosition = buttonWidth * newPositionIndex.toFloat()
        val lastPosition = cancelledPosition ?: buttonWidth * selectedButtonIndex.toFloat()

        if (isDragging) {
            animator?.cancel()
        }

        animator = ValueAnimator.ofFloat(lastPosition, newPosition).apply {

            interpolator = DecelerateInterpolator(1f)

            addUpdateListener {
                sliderPosition = it.animatedValue as Float
                invalidate()
            }

            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        selectedButtonIndex = newPositionIndex
                        cancelledPosition = null
                        onAnimationEndCallback?.invoke()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        super.onAnimationCancel(animation)
                        cancelledPosition = sliderPosition
                    }
                })
        }
        animator?.start()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        for (index in 0 until childCount) {
            val optionButton = getChildAt(index)
            optionButton.isClickable = true

            optionButton.setOnClickListener {
                animateButtonMovement(newPositionIndex = index, onAnimationEndCallback = {
                    selectedOptionCallback?.invoke(index)
                })
            }
        }
        buttonWidth = getChildAt(0).width
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setSelectedIndexCallback?.invoke()
        setSelectedIndexCallback = null
        for (i in 0 until childCount) {
            optionPositionsMap[i] = getChildAt(i).left.toFloat()
        }

        drawDivider(canvas, buttonWidth)
        drawShadow(canvas = canvas, onLeft = true, buttonWidth = buttonWidth)
        drawShadow(canvas = canvas, onLeft = false, buttonWidth = buttonWidth)

        sliderRect.apply {
            left = sliderPosition + inset
            top = inset
            right = sliderPosition + buttonWidth - inset
            bottom = height.toFloat() - inset
        }
        canvas?.drawRoundRect(sliderRect, sliderRadius, sliderRadius, sliderPaint)
    }

    private fun drawDivider(canvas: Canvas?, buttonWidth: Int) {
        for (i in 1 until childCount) {
            val lineTranslationX = buttonWidth * i.toFloat()
            if (lineTranslationX < sliderPosition || lineTranslationX > sliderPosition + buttonWidth) {
                canvas?.drawLine(
                    lineTranslationX,
                    dividerMargin.toFloat(),
                    lineTranslationX,
                    height.toFloat() - dividerMargin.toFloat(), dividerPaint
                )
            }
        }
    }

    private fun drawShadow(canvas: Canvas?, onLeft: Boolean, buttonWidth: Int) {
        sliderShadowRect.apply {
            left = if (onLeft) {
                sliderPosition + 2 * inset
            } else {
                sliderPosition + buttonWidth / 2
            }
            top = 2 * inset
            right = if (onLeft) {
                sliderPosition + buttonWidth / 2
            } else {
                sliderPosition + buttonWidth - 2 * inset
            }
            bottom = height.toFloat() - 2 * inset
        }
        canvas?.drawRoundRect(
            sliderShadowRect,
            sliderRadius,
            sliderRadius,
            if (onLeft) {
                sliderShadowPaintLeft
            } else {
                sliderShadowPaintRight
            }
        )
    }
}