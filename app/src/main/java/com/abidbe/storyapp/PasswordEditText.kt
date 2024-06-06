package com.abidbe.storyapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

class PasswordEditText : AppCompatEditText {
    private var showPasswordDrawable: Drawable? = null
    private var hidePasswordDrawable: Drawable? = null
    private var isPasswordVisible = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        val backgroundDrawable = GradientDrawable()
        backgroundDrawable.shape = GradientDrawable.RECTANGLE
        backgroundDrawable.cornerRadius = resources.getDimension(R.dimen.edittext_corner_radius)
        backgroundDrawable.setStroke(
            resources.getDimensionPixelSize(R.dimen.edittext_border_width),
            resources.getColor(R.color.black)
        )
        background = backgroundDrawable
        showPasswordDrawable = ContextCompat.getDrawable(context, R.drawable.eyeclose)
        hidePasswordDrawable = ContextCompat.getDrawable(context, R.drawable.eye)

        hidePasswordDrawable?.let {
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                it,
                null
            )
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text.length < 8) {
                    error = context.getString(R.string.password_characters)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = compoundDrawablesRelative[2]
                if (drawableEnd != null && event.x >= width - compoundPaddingEnd) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val drawable = if (isPasswordVisible) showPasswordDrawable else hidePasswordDrawable
        drawable?.let { setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, it, null) }
        transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
    }
}