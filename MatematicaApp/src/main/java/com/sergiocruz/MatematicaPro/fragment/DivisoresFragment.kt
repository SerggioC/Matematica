package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
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
import kotlinx.android.synthetic.main.fragment_divisores.*
import java.text.DecimalFormat
import java.util.*

class DivisoresFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    private var asyncTask: AsyncTask<Long, Double, ArrayList<Long>> = BackGroundOperation()
    internal var cvWidth: Int = 0
    internal var heightDip: Int = 0
    internal var num: Long = 0
    private var startTime: Long = 0

    override fun getLayoutIdForFragment() = R.layout.fragment_divisores

    override fun onActionDone() = calcDivisors()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancelButton.setOnClickListener { displayCancelDialogBox(context!!, this) }
        calculateButton.setOnClickListener { calcDivisors() }
        clearButton.setOnClickListener { inputEditText.setText("") }
        inputEditText.watchThis(this)
    }

    override fun loadOptionsMenus() =
        listOf(R.menu.menu_main, R.menu.menu_sub_main, R.menu.menu_help_divisores)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_save_history_images -> MenuHelper.saveHistoryImages(activity as Activity)
            R.id.action_share_history_images -> MenuHelper.shareHistoryImages(activity as Activity)
            R.id.action_share_history -> MenuHelper.shareHistory(activity as Activity)
            R.id.action_clear_all_history -> MenuHelper.removeHistory(activity as Activity)
            R.id.action_help_divisores -> {
                val help = getString(R.string.help_text_divisores)
                val ssb = SpannableStringBuilder(help)
                CreateCardView.create(history, ssb, activity as Activity)
            }
            R.id.action_about -> startActivity(Intent(activity, AboutActivity::class.java))
            R.id.action_settings -> startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //         Checks the orientation of the screen

        //        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //            Toast.makeText(activity, "landscape", Toast.LENGTH_SHORT).show();
        //        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        //            Toast.makeText(activity, "portrait", Toast.LENGTH_SHORT).show();
        //        }

        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        val width = size.x
        //int height = size.y;
        val lrDip = (4 * scale + 0.5f).toInt() * 2
        cvWidth = width - lrDip
        hideKeyboard(activity as Activity)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        cancelAsyncTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (asyncTask.status == AsyncTask.Status.RUNNING) {
            asyncTask.cancel(true)
            showCustomToast(context, getString(R.string.canceled_op))
        }
    }

    private fun cancelAsyncTask() {
        if (asyncTask.status == AsyncTask.Status.RUNNING) {
            asyncTask.cancel(true)
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
            resetButtons()
        }
    }

    private fun calcDivisors() {
        startTime = System.nanoTime()
        hideKeyboard(activity as Activity)
        val editnumText = inputEditText.text.toString()
        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.add_num_inteiro))
            return
        }

        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
            return
        }

        if (editnumText == "0" || num == 0L) {
            val ssb = SpannableStringBuilder(getString(R.string.zero_no_divisores))
            CreateCardView.create(history, ssb, activity as Activity)
            return
        }
        asyncTask = BackGroundOperation().execute(num)
    }

    fun getAllDivisoresLong(numero: Long?): ArrayList<Long> {
        val upperlimit = Math.sqrt(numero!!.toDouble()).toLong()
        val divisores = ArrayList<Long>()
        var i = 1
        while (i <= upperlimit) {
            if (numero % i == 0L) {
                divisores.add(i.toLong())
                if (i.toLong() != numero / i) {
                    val elem = numero / i
                    divisores.add(elem)
                }
            }
            i += 1
        }
        divisores.sort()
        return divisores
    }

    inner class BackGroundOperation : AsyncTask<Long, Double, ArrayList<Long>>() {

        public override fun onPreExecute() {
            lockInput()
            cvWidth = card_view_1.width
            heightDip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(10, heightDip)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): ArrayList<Long> {
            /*
            Long numero = num[0];
            long upperlimit = (long) (Math.sqrt(numero));
            long elem;
            ArrayList<Long> divisores = new ArrayList<Long>();
            for (int i = 1; i <= upperlimit; i += 1) {
                if (numero % i == 0) {
                    divisores.add((long) i);
                    if (i != numero / i) {
                        elem = numero / i;
                        divisores.add(elem);
                    }
                }
                publishProgress((float) i / (float) upperlimit);
                if (isCancelled()) break;
            }
            Collections.sort(divisores);
            return divisores;

*/

            /*
            *
            * Performance update
            * Primeiro obtem os fatores primos depois multiplica-os
            *
            * */
            val divisores = ArrayList<Long>()
            var number: Long? = num[0]
            var progress: Double
            var oldProgress = 0.0

            while (number!! % 2L == 0L) {
                divisores.add(2L)
                number /= 2
            }

            run {
                var i: Long = 3
                while (i <= number / i) {
                    while (number % i == 0L) {
                        divisores.add(i)
                        number /= i
                    }
                    progress = i.toDouble() / (number.toDouble() / i.toDouble())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toDouble())
                        oldProgress = progress
                    }
                    if (isCancelled) break
                    i += 2
                }
            }
            if (number > 1) {
                divisores.add(number)
            }

            val allDivisores = ArrayList<Long>()
            var size: Int
            allDivisores.add(1L)
            for (i in divisores.indices) {
                size = allDivisores.size
                for (j in 0 until size) {
                    val `val` = allDivisores[j] * divisores[i]
                    if (!allDivisores.contains(`val`)) {
                        allDivisores.add(`val`)
                    }
                }
            }
            allDivisores.sort()
            return allDivisores
        }

        override fun onProgressUpdate(vararg values: Double?) {
            if (this@DivisoresFragment.isVisible) {
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(
                        Math.round(values[0]!! * cvWidth).toInt(),
                        heightDip
                    )
            }
        }

        override fun onPostExecute(result: ArrayList<Long>) {
            if (this@DivisoresFragment.isVisible) {
                var str = ""
                for (i in result) {
                    str = "$str, $i"
                    if (i == 1L) {
                        str = num.toString() + " " + getString(R.string.has) + " " + result.size +
                                " " + getString(R.string.divisores_) + "\n{" + i
                    }
                }
                val strDivisores = "$str}"
                val ssb = SpannableStringBuilder(strDivisores)
                if (result.size == 2) {
                    val primeNumber = "\n" + getString(R.string._numero_primo)
                    ssb.append(primeNumber)
                    ssb.setSpan(
                        ForegroundColorSpan(Color.parseColor("#29712d")),
                        ssb.length - primeNumber.length,
                        ssb.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ssb.setSpan(
                        RelativeSizeSpan(0.9f),
                        ssb.length - primeNumber.length,
                        ssb.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                createCardView(ssb)
                resetButtons()
            }
        }

        override fun onCancelled(parcial: ArrayList<Long>) {
            super.onCancelled(parcial)
            if (this@DivisoresFragment.isVisible) {
                var str = ""
                for (i in parcial) {
                    str = "$str, $i"
                    if (i == 1L) {
                        str = getString(R.string.divisors_of) + " " + num + ":\n" + "{" + i
                    }
                }
                val strDivisores = "$str}"
                val ssb = SpannableStringBuilder(strDivisores)
                val incompleteCalc = "\n" + getString(R.string._incomplete_calc)
                ssb.append(incompleteCalc)
                ssb.setSpan(
                    ForegroundColorSpan(Color.RED),
                    ssb.length - incompleteCalc.length,
                    ssb.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb.setSpan(
                    RelativeSizeSpan(0.8f),
                    ssb.length - incompleteCalc.length,
                    ssb.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                createCardView(ssb)
                resetButtons()
            }
        }
    }

    private fun lockInput() {
        calculateButton.isClickable = false
        calculateButton.setText(R.string.working)
        cancelButton.visibility = View.VISIBLE
        hideKeyboard(activity)
    }

    private fun resetButtons() {
        calculateButton.setText(R.string.calculate)
        calculateButton.isClickable = true
        cancelButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    fun createCardView(ssb: SpannableStringBuilder) {
        //criar novo cardview
        val cardView = ClickableCardView(activity as Activity)
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

        val color = ContextCompat.getColor(requireContext(), R.color.cardsColor)
        cardView.setCardBackgroundColor(color)

        // Add cardview to history layout at the top (index 0)
        val history = activity!!.findViewById<View>(R.id.history) as LinearLayout
        history.addView(cardView, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, //largura
            ViewGroup.LayoutParams.WRAP_CONTENT //altura
        )

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
                activity as Activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?) = true
                    override fun onDismiss(view: View?) = history.removeView(cardView)
                })
        )

        val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false)
        if (shouldShowPerformance) {

            val gradientSeparator = getGradientSeparator(context)
            val formatter1 = DecimalFormat("#.###")
            val elapsed =
                getString(R.string.performance) + " " + formatter1.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
            gradientSeparator.text = elapsed
            llVerticalRoot.addView(gradientSeparator)
        }
        llVerticalRoot.addView(textView)

        // add the textview to the cardview
        cardView.addView(llVerticalRoot)
    }

}
