package com.sergiocruz.MatematicaPro.fragment

/**
 * Created by Sergio on 13/05/2017.
 */

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.helper.*
import kotlinx.android.synthetic.main.fragment_multiplos.*
import java.math.BigInteger
import java.text.DecimalFormat

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 13/05/2017 14:00
 */

class MultiplosFragment : BaseFragment(), OnEditorActions {
    private var asyncTask: AsyncTask<Long, Double, String> = BackGroundOperation(null, null)
    private var num: Long = 0
    internal var startTime: Long = 0

    override fun getHelpTextId() = R.string.help_text_multiplos

    override fun getHelpMenuTitleId() = R.string.action_ajuda_multiplos

    override fun getHistoryLayout(): LinearLayout = history

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hideKeyboard(activity)
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_multiplos

    override fun onActionDone() = calculateMultiples()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editNumMultiplos.watchThis(this)
        clearButton.setOnClickListener { editNumMultiplos.setText("") }
        button_calc_multiplos.setOnClickListener { calculateMultiples() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (asyncTask.status == AsyncTask.Status.RUNNING) {
            asyncTask.cancel(true)
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
        }
    }

    private fun calculateMultiples() {
        startTime = System.nanoTime()
        hideKeyboard(activity)
        val editnumText = editNumMultiplos.text.toString()
        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.add_num_inteiro), InfoLevel.WARNING)
            editNumMultiplos.apply {
                requestFocus()
                error = getString(R.string.add_num_inteiro)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            return
        }
        if (editnumText == "0") {
            createCardView(0L, "{0}", 0L, false)
            return
        }
        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
            return
        }

        val spinnerMaxMultiplos = spinner_multiplos.selectedItem.toString().toLongOrNull()
        asyncTask = BackGroundOperation(false, null).execute(num, 0L, spinnerMaxMultiplos)
    }

    fun createCardView(number: Long?, multiplos: String, min_multiplos: Long?, showMore: Boolean) {
        //criar novo cardview
        val cardView = ClickableCardView(activity!!)
        cardView.tag = min_multiplos

        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardView.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        val cvColor = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardView.setCardBackgroundColor(cvColor)

        history.limit(historyLimit)
        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()

        val text = getString(R.string.multiplosde) + " " + number + "=\n" + multiplos
        val ssb = SpannableStringBuilder(text)

        //Adicionar o texto com o resultado
        textView.text = ssb
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardView,
                activity!!,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?): Boolean {
                        return true
                    }

                    override fun onDismiss(view: View?) {
                        history.removeView(cardView)
                    }
                })
        )

        if (shouldShowPerformance) {
            val gradientSeparator = getGradientSeparator(context)
            val decimalFormatter = DecimalFormat("#.###")
            val elapsed =
                getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
            gradientSeparator.text = elapsed
            llVerticalRoot.addView(gradientSeparator)
        }

        llVerticalRoot.addView(textView)

        if (showMore) {
            // criar novo Textview com link para mostrar mais números múltiplos
            val showMoreTextView = TextView(activity)
            showMoreTextView.layoutParams = getMatchWrapParams()
            showMoreTextView.gravity = Gravity.RIGHT
            showMoreTextView.setText(R.string.show_more)
            showMoreTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            showMoreTextView.setTypeface(null, Typeface.BOLD)
            showMoreTextView.setTextColor(ContextCompat.getColor(activity!!, R.color.bgCardColor))
            showMoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            showMoreTextView.setOnClickListener {
                startTime = System.nanoTime()
                val spinner =
                    java.lang.Long.parseLong(spinner_multiplos.selectedItem.toString())
                asyncTask = BackGroundOperation(true, cardView).execute(
                    num,
                    cardView.tag as Long,
                    spinner
                )
            }

            llVerticalRoot.addView(showMoreTextView)
        }

        // add the root layout to the cardview
        cardView.addView(llVerticalRoot)
    }

    inner class BackGroundOperation internal constructor(
        private var expandResult: Boolean?,
        private var theCardView: ClickableCardView?
    ) : AsyncTask<Long, Double, String>() {
        internal var number: Long? = null
        private var maxValue: Long? = null

        public override fun onPreExecute() {
            if (!expandResult!!) {
                button_calc_multiplos.isClickable = false
                button_calc_multiplos.setText(R.string.working)
                hideKeyboard(activity)
            }
        }

        override fun doInBackground(vararg num: Long?): String {
            number = num[0]
            val minValue = num[1]
            maxValue = num[2]!! + num[1]!!

            var stringMultiples = ""

            for (i in minValue!! until maxValue!!) {
                val bigNumber = BigInteger.valueOf(number!!).multiply(BigInteger.valueOf(i))
                stringMultiples += "$bigNumber, "
            }
            stringMultiples += "...}"
            return stringMultiples
        }

        override fun onPostExecute(result: String) {
            if (this@MultiplosFragment.isVisible) {
                if (!expandResult!!) {
                    createCardView(number, "{$result", maxValue, true)
                    button_calc_multiplos.setText(R.string.calculate)
                    button_calc_multiplos.isClickable = true
                } else {
                    theCardView?.tag = maxValue
                    val textViewPreResult: TextView
                    if (shouldShowPerformance) {
                        textViewPreResult =
                            (theCardView?.getChildAt(0) as LinearLayout).getChildAt(
                                1
                            ) as TextView
                        val gradientSeparator =
                            (theCardView?.getChildAt(0) as LinearLayout).getChildAt(0) as TextView
                        val decimalFormatter = DecimalFormat("#.###")
                        val elapsed =
                            getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                        gradientSeparator.text = elapsed
                    } else {
                        textViewPreResult =
                            (theCardView?.getChildAt(0) as LinearLayout).getChildAt(0) as TextView
                    }
                    var preResult = textViewPreResult.text.toString()
                    preResult = preResult.substring(0, preResult.length - 4) + result
                    textViewPreResult.text = preResult
                }
            }
            theCardView = null
        }

    }

}
