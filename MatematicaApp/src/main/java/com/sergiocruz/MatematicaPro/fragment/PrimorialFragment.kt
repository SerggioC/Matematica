package com.sergiocruz.MatematicaPro.fragment

import android.content.SharedPreferences
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.helper.*
import kotlinx.android.synthetic.main.fragment_primorial.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 05/02/2017 12:47
 */

class PrimorialFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {
    var BG_Operation: AsyncTask<Long, Float, BigInteger> = BackGroundOperation()
    private var num: Long = 0
    private var startTime: Long = 0

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getHelpTextId() = R.string.help_text_primorial

    override fun getHelpMenuTitleId() = R.string.action_ajuda_primorial

    override fun getHistoryLayout(): LinearLayout = history

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_primorial

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(BG_Operation, context)) resetButtons()
    }

    override fun onActionDone() = calculatePrimorial()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_calc_primorial.setOnClickListener { calculatePrimorial() }
        inputEditText.watchThis(this)
        cancelButton.setOnClickListener { displayCancelDialogBox(context!!, this) }
        clearButton.setOnClickListener { inputEditText.setText("") }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(BG_Operation, context)
    }

    fun calculatePrimorial() {
        startTime = System.nanoTime()
        hideKeyboard(activity)
        val editnumText = inputEditText.text.toString()
        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.add_num_inteiro))
            return
        }

        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
            if (num == 0L) {
                showCustomToast(context, getString(R.string.zeroPrimorial), InfoLevel.WARNING)
                return
            }
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.zeroPrimorial), InfoLevel.WARNING)
            return
        }

        BG_Operation = BackGroundOperation().execute(num)
    }

    private fun resetButtons() {
        progressBar.visibility = View.GONE
        button_calc_primorial.setText(R.string.calculate)
        button_calc_primorial.isClickable = true
        cancelButton.visibility = View.GONE
    }

    fun createCardView(number: Long?, bigIntegerResult: BigInteger, wasCanceled: Boolean?) {
        //criar novo cardview
        val cardView = ClickableCardView(activity!!)
        cardView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // height
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
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, //largura
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) //altura

        val text = number.toString() + "#=\n" + bigIntegerResult
        val ssb = SpannableStringBuilder(text)
        if (wasCanceled!!) {
            val incomplete = "\n" + getString(R.string._incomplete_calc)
            ssb.append(incomplete)
            ssb.setSpan(
                ForegroundColorSpan(Color.RED),
                ssb.length - incomplete.length,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                RelativeSizeSpan(0.8f),
                ssb.length - incomplete.length,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //Adicionar o texto com o resultado
        textView.text = ssb
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")

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
            val numAlgarismos = bigIntegerResult.toString().length
            val algarismos = getString(R.string.algarismos)
            gradientSeparator.text = "$numAlgarismos $algarismos, $elapsed"
            llVerticalRoot.addView(gradientSeparator)
        }

        llVerticalRoot.addView(textView)

        // add the root layout to the cardview
        cardView.addView(llVerticalRoot)
    }

    lateinit var progressParams: ViewGroup.LayoutParams

    private inner class BackGroundOperation : AsyncTask<Long, Float, BigInteger>() {
        internal var number: Long? = null
        internal var primes = ArrayList<Long>()

        public override fun onPreExecute() {
            button_calc_primorial.isClickable = false
            button_calc_primorial.setText(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity)
            progressParams = progressBar.layoutParams
            progressParams.width = 1
            progressBar.layoutParams = progressParams
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): BigInteger {
            number = num[0]!!
            if (number == 1L) return BigInteger.ONE
            if (number == 2L) return BigInteger.valueOf(2L)

            primes.add(1L)
            primes.add(2L)

            var progress: Float
            var oldProgress = 0.0f

            for (i in 3L..number!!) {
                var isPrime = true
                if (i % 2 == 0L) isPrime = false
                if (isPrime) {
                    var j: Long = 3
                    while (j < i) {
                        if (i % j == 0L) {
                            isPrime = false
                            break
                        }
                        j += 2
                    }
                }
                if (isPrime) {
                    primes.add(i)
                }
                progress = i.toFloat() / number!!.toFloat()
                if (progress - oldProgress > 0.05) { // update a cada 5%
                    publishProgress(progress)
                    oldProgress = progress
                }
                if (isCancelled) break
            }

            var primorial = BigInteger.ONE
            for (j in primes.indices) {
                primorial = primorial.multiply(BigInteger.valueOf(primes[j]))
            }

            return primorial

        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@PrimorialFragment.isVisible) {
                progressParams.width = Math.round(values[0]!! * card_view_1.width)
                progressBar.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: BigInteger) {
            if (this@PrimorialFragment.isVisible) {
                createCardView(number, result, false)
                resetButtons()
            }
        }

        override fun onCancelled(parcial: BigInteger) {
            super.onCancelled(parcial)
            if (this@PrimorialFragment.isVisible) {
                createCardView(primes[primes.size - 1], parcial, true)
                resetButtons()
            }
        }
    }

}