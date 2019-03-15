package com.sergiocruz.MatematicaPro.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.helper.*
import kotlinx.android.synthetic.main.fragment_primality.*
import java.math.BigInteger

class PrimalityFragment : BaseFragment() {

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getLayoutIdForFragment() = R.layout.fragment_primality

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateButton.setOnClickListener { checkNumberFromInput() }
        clearButton.setOnClickListener { inputEditText.setText("") }

        val imeOptions: Int = inputEditText.imeOptions
        inputEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == imeOptions) {
                checkNumberFromInput()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun checkNumberFromInput() {
        val number = inputEditText.text.toString()
        val bigNumber = number.toBigIntegerOrNull(10)
        if (bigNumber != null) {
            checkIfProbablePrime(bigNumber)
        } else {
            showCustomToast(context, getString(R.string.invalid_number), InfoLevel.WARNING)
        }
    }

    private fun checkIfProbablePrime(bigNumber: BigInteger) {
        val isPrime = bigNumber.isProbablePrime(100)
        hideKeyboard(activity)
        createCardView(bigNumber, isPrime)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_history_images -> MenuHelper.saveHistoryImages(activity as Activity)
            R.id.action_share_history_images -> MenuHelper.shareHistoryImages(activity as Activity)
            R.id.action_share_history -> MenuHelper.shareHistory(activity as Activity)
            R.id.action_clear_all_history -> MenuHelper.removeHistory(activity as Activity)
            R.id.action_about -> startActivity(Intent(activity, AboutActivity::class.java))
            R.id.action_settings -> startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createCardView(bigNumber: BigInteger, isPrime: Boolean) {
        //criar novo cardview
        val cardView = ClickableCardView(activity as Activity)
        cardView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT // height
        )
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardView.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        if (isPrime) {
            val color = ContextCompat.getColor(context!!, R.color.greener)
            cardView.setCardBackgroundColor(color)
        } else {
            val color = ContextCompat.getColor(requireContext(), R.color.cardsColor)
            cardView.setCardBackgroundColor(color)
        }

        history.limit(historySize)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, //largura
            ViewGroup.LayoutParams.WRAP_CONTENT //altura
        )

        //Adicionar o texto com o resultado
        textView.text = "$bigNumber \n ${if (isPrime) "Prime Number!" else "Not a prime number"}"
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")
        textView.gravity = Gravity.CENTER_HORIZONTAL

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = LinearLayout.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardView,
                activity as Activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?) = true
                    override fun onDismiss(view: View?) = history.removeView(cardView)
                })
        )

        llVerticalRoot.addView(textView)

        // add the textview to the cardview
        cardView.addView(llVerticalRoot)
    }



}


