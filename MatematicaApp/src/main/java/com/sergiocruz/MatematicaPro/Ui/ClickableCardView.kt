package com.sergiocruz.MatematicaPro.Ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView

class ClickableCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.cardview.widget.CardView(context, attrs, defStyleAttr) {

    override fun performClick(): Boolean {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick()

        // Handle the action for the custom click here

        return true
    }
}