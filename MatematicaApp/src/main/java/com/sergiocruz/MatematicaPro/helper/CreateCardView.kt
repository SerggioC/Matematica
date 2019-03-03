package com.sergiocruz.MatematicaPro.helper

import android.app.Activity
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
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

    fun create(history: ViewGroup, ssb_result: SpannableStringBuilder, activity: Activity) {

        //criar novo cardview
        val cardview = ClickableCardView(activity)
        cardview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // height
        cardview.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val scale = activity.resources.displayMetrics.density
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardview.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardview.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardview.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardview.useCompatPadding = true

        val cvColor = ContextCompat.getColor(activity, R.color.cardsColor)
        cardview.setCardBackgroundColor(cvColor)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, //largura
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) //altura

        //Adicionar o texto com o resultado
        textView.text = ssb_result
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")

        // add the textview to the cardview
        cardview.addView(textView)

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardview,
                activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?): Boolean = true
                    override fun onDismiss(view: View?) = history.removeView(cardview)
                })
        )
    }

}