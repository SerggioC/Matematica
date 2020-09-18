package com.sergiocruz.MatematicaPro.helper

import android.app.Activity
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 03/11/2016 22:27
 */

object CreateCardView {

    fun withStringRes(history: LinearLayout?, helpStringRes: Int?, activity: Activity) {
        if (history == null || helpStringRes == null) return
        val helpString = activity.getString(helpStringRes)
        val helpSSB = SpannableStringBuilder(helpString)
        viewWithSSB(history, helpSSB, activity)
    }

    fun viewWithSSB(
        history: LinearLayout,
        helpSSB: SpannableStringBuilder,
        activity: Activity
    ) {
        //criar novo cardview
        val cardView = ClickableCardView(activity)
        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val scale = activity.resources.displayMetrics.density
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardView.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        val cvColor = ContextCompat.getColor(activity, R.color.cardsColor)
        cardView.setCardBackgroundColor(cvColor)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()

        //Adicionar o texto com o resultado
        textView.text = helpSSB
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")

        // add the textview to the cardview
        cardView.addView(textView)

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardView,
                activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?): Boolean = true
                    override fun onDismiss(view: View?) = history.removeView(cardView)
                })
        )
    }

}