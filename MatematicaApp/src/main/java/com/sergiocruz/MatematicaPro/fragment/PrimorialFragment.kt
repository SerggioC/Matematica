package com.sergiocruz.MatematicaPro.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutCompat
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
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

class PrimorialFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActionDone,
    OnEditorActionError {
    var BG_Operation: AsyncTask<Long, Double, BigInteger> = BackGroundOperation()
    internal var cv_width: Int = 0
    internal var height_dip: Int = 0
    private var num: Long = 0
    private var startTime: Long = 0

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main, R.menu.menu_help_primorial)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.itemId
        if (id == R.id.action_save_history_images) {
            MenuHelper.saveHistoryImages(activity!!)
        }
        if (id == R.id.action_share_history) {
            MenuHelper.shareHistory(activity!!)
        }
        if (id == R.id.action_share_history_images) {
            MenuHelper.shareHistoryImages(activity!!)
        }
        if (id == R.id.action_clear_all_history) {
            MenuHelper.removeHistory(activity!!)
        }
        if (id == R.id.action_help_primorial) {
            val help_primorial = getString(R.string.help_text_primorial)
            val ssb = SpannableStringBuilder(help_primorial)
            val history = activity!!.findViewById<View>(R.id.history) as LinearLayout
            CreateCardView.create(history, ssb, activity!!)
        }
        if (id == R.id.action_about) {
            startActivity(Intent(activity, AboutActivity::class.java))
        }
        if (id == R.id.action_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val display = activity!!.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        //int height = size.y;
        val lr_dip = (4 * scale + 0.5f).toInt() * 2
        cv_width = width - lr_dip
        hideKeyboard(activity)
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_primorial

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(BG_Operation, context)) resetButtons()
    }

    override fun onActionError() =
        showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
    
    override fun onActionDone() = calculatePrimorial()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_calc_primorial.setOnClickListener { calculatePrimorial() }
        inputEditText.watchThis(this, this)
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
        val cardview = ClickableCardView(activity!!)
        cardview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // height
        cardview.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lr_dip = (6 * scale + 0.5f).toInt()
        val tb_dip = (8 * scale + 0.5f).toInt()
        cardview.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardview.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip)
        cardview.useCompatPadding = true

        val cv_color = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardview.setCardBackgroundColor(cv_color)

        // Add cardview to history layout at the top (index 0)
        val history = activity!!.findViewById<View>(R.id.history) as LinearLayout
        history.addView(cardview, 0)


        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, //largura
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) //altura

        val text = number.toString() + "#=\n" + bigIntegerResult
        val ssb = SpannableStringBuilder(text)
        if (wasCanceled!!) {
            val incomplete_calc = "\n" + getString(R.string._incomplete_calc)
            ssb.append(incomplete_calc)
            ssb.setSpan(
                ForegroundColorSpan(Color.RED),
                ssb.length - incomplete_calc.length,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                RelativeSizeSpan(0.8f),
                ssb.length - incomplete_calc.length,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //Adicionar o texto com o resultado
        textView.text = ssb
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")

        val ll_vertical_root = LinearLayout(activity)
        ll_vertical_root.layoutParams = LinearLayout.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        ll_vertical_root.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardview,
                activity!!,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?): Boolean {
                        return true
                    }

                    override fun onDismiss(view: View?) {
                        history.removeView(cardview)
                    }
                })
        )

        val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", true)
        if (shouldShowPerformance) {
            val gradient_separator = getGradientSeparator(context)
            val decimalFormatter = DecimalFormat("#.###")
            val elapsed =
                getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
            val numAlgarismos = bigIntegerResult.toString().length
            gradient_separator.text = numAlgarismos.toString() + " Algarismos, " + elapsed
            ll_vertical_root.addView(gradient_separator)
        }

        ll_vertical_root.addView(textView)

        // add the root layout to the cardview
        cardview.addView(ll_vertical_root)
    }

    private inner class BackGroundOperation : AsyncTask<Long, Double, BigInteger>() {
        internal var number: Long? = null
        internal var primes = ArrayList<Long>()

        public override fun onPreExecute() {
            button_calc_primorial.isClickable = false
            button_calc_primorial.setText(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity)
            cv_width = card_view_1.width
            height_dip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(10, height_dip)
            progressBar.visibility = View.VISIBLE
        }
        
        override fun doInBackground(vararg num: Long?): BigInteger {
            number = num[0]!!
            if (number == 1L) return BigInteger.ONE
            if (number == 2L) return BigInteger.valueOf(2L)

            primes.add(1L)
            primes.add(2L)

            var progress: Double
            var oldProgress = 0.0

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
                progress = i.toDouble() / number as Double
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

        override fun onProgressUpdate(vararg values: Double?) {
            if (this@PrimorialFragment.isVisible) {
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(
                        Math.round(values[0]!! * cv_width).toInt(),
                        height_dip
                    )
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
