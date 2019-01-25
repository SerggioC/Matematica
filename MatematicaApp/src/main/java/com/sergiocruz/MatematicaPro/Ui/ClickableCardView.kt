package com.sergiocruz.MatematicaPro.Ui

import android.content.Context
import android.support.v7.widget.CardView

class ClickableCardView(context: Context) : CardView(context) {
    override fun performClick(): Boolean {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick()

        // Handle the action for the custom click here

        return true
    }
}