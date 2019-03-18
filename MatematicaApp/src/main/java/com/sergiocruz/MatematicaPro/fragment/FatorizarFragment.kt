package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.expandIt
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import java.text.DecimalFormat
import java.util.*
import java.util.Map

class FatorizarFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    private var bgOperation: AsyncTask<Long, Float, ArrayList<ArrayList<Long>>> =
        BackGroundOperation()

    internal var cvWidth: Int = 0
    internal var heightDip: Int = 0
    private var startTime: Long? = null

    override fun getLayoutIdForFragment() = R.layout.fragment_fatorizar

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getHelpTextId(): Int? = R.string.help_text_fatores

    override fun getHelpMenuTitleId(): Int? = R.string.action_ajuda_fatorizar

    override fun getHistoryLayout(): LinearLayout? = history

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

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(bgOperation, context)
    }

    private fun resetButtons() {
        calculateButton.setText(R.string.calculate)
        calculateButton.isClickable = true
        cancelButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cancelButton.setOnClickListener { displayCancelDialogBox(context!!, this) }
        calculateButton.setOnClickListener { calculatePrimeFactors() }
        clearButton.setOnClickListener { factorizeTextView.setText("") }
        factorizeTextView.watchThis(this)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(bgOperation, context)) resetButtons()
    }

    override fun onActionDone() {
        calculatePrimeFactors()
    }

    private fun calculatePrimeFactors() {
        startTime = System.nanoTime()
        val editnumText = factorizeTextView.text.toString()
        val num: Long

        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.insert_integer))
            return
        }
        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.numero_alto))
            return
        }

        if (num == 0L || num == 1L) {
            showCustomToast(
                context,
                getString(R.string.the_number) + " " + num + " " + getString(R.string.has_no_factors)
            )
            return
        }

        bgOperation = BackGroundOperation().execute(num)

    }

    private fun createCardViewLayout(
        number: Long?,
        history: ViewGroup,
        str_results: String,
        ssb_str_divisores: SpannableStringBuilder,
        ssbFatores: SpannableStringBuilder,
        str_fact_exp: SpannableStringBuilder,
        hasExpoentes: Boolean?
    ) {

        //criar novo cardview
        val cardview = ClickableCardView(activity as Activity)
        cardview.layoutParams = getMatchWrapParams()
        cardview.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardview.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardview.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardview.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardview.useCompatPadding = true

        val cvColor = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardview.setCardBackgroundColor(cvColor)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0)

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // criar novo Textview para o resultado da fatorização
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()
        textView.setPadding(0, 0, 0, 0)

        val ssbFatoresTop = SpannableStringBuilder(ssbFatores)
        val spans =
            ssbFatoresTop.getSpans(0, ssbFatoresTop.length, ForegroundColorSpan::class.java)
        for (i in spans.indices) {
            ssbFatoresTop.removeSpan(spans[i])
        }

        //Adicionar o texto com o resultado da fatorizaçãoo com expoentes
        val strNum = getString(R.string.factorization_of) + " " + number + " = \n"
        val ssbNum = SpannableStringBuilder(strNum)
        ssbNum.append(ssbFatoresTop)
        textView.text = ssbNum
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        textView.setTag(R.id.texto, "texto")

        // add the textview com os fatores multiplicados to the Linear layout vertical root
        llVerticalRoot.addView(textView)

        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {

            val llVerticalExpl = LinearLayout(activity)
            llVerticalExpl.layoutParams = getMatchWrapParams()
            llVerticalExpl.orientation = LinearLayout.VERTICAL
            llVerticalExpl.tag = "ll_vertical_expl"

            val textViewExplanations = TextView(activity)
            textViewExplanations.layoutParams = getMatchWrapParams()
            textViewExplanations.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            val explain_text_1 = getString(R.string.expl_text_divisores_1)
            val ssb_explain_1 = SpannableStringBuilder(explain_text_1)
            val boldColorSpan =
                ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.boldColor))
            ssb_explain_1.setSpan(boldColorSpan, 0, ssb_explain_1.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewExplanations.text = ssb_explain_1
            textViewExplanations.setTag(R.id.texto, "texto")
            llVerticalExpl.addView(textViewExplanations)

            val llHorizontal = LinearLayout(activity)
            llHorizontal.layoutParams = getMatchWrapParams()
            llHorizontal.orientation = LinearLayout.HORIZONTAL
            llHorizontal.tag = "ll_horizontal_expl"

            val llVerticalResults = LinearLayout(activity)
            llVerticalResults.layoutParams = getWrapWrapParams()
            llVerticalResults.orientation = LinearLayout.VERTICAL
            llVerticalResults.setPadding(0, 0, (4 * scale + 0.5f).toInt(), 0)

            val verticalSeparador = LinearLayout(activity)
            verticalSeparador.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
            verticalSeparador.orientation = LinearLayout.VERTICAL
            verticalSeparador.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.separatorLineColor
                )
            )
            val um_dip = (1.2 * scale + 0.5f).toInt()
            verticalSeparador.setPadding(um_dip, 4, 0, um_dip)

            val llVerticalDivisores = LinearLayout(activity)
            llVerticalDivisores.layoutParams = getWrapWrapParams()
            llVerticalDivisores.orientation = LinearLayout.VERTICAL
            llVerticalDivisores.setPadding(
                (4 * scale + 0.5f).toInt(),
                0,
                (8 * scale + 0.5f).toInt(),
                0
            )

            val textViewResults = TextView(activity)
            textViewResults.layoutParams = getWrapWrapParams()
            textViewResults.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textViewResults.gravity = Gravity.RIGHT
            val ssbStrResults = SpannableStringBuilder(str_results)
            ssbStrResults.setSpan(
                RelativeSizeSpan(0.9f),
                ssbStrResults.length - str_results.length,
                ssbStrResults.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textViewResults.text = ssbStrResults
            textViewResults.setTag(R.id.texto, "texto")

            llVerticalResults.addView(textViewResults)

            val textViewDivisores = TextView(activity)
            textViewDivisores.layoutParams = getWrapWrapParams()
            textViewDivisores.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textViewDivisores.gravity = Gravity.LEFT
            ssb_str_divisores.setSpan(
                RelativeSizeSpan(0.9f),
                ssb_str_divisores.length - ssb_str_divisores.length,
                ssb_str_divisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textViewDivisores.text = ssb_str_divisores
            textViewDivisores.setTag(R.id.texto, "texto")

            llVerticalDivisores.addView(textViewDivisores)

            //Adicionar os LL Verticais ao Horizontal
            llHorizontal.addView(llVerticalResults)

            llHorizontal.addView(verticalSeparador)

            //LinearLayout divisores
            llHorizontal.addView(llVerticalDivisores)

            val ssbHideExpl = SpannableStringBuilder(getString(R.string.hide_explain))
            ssbHideExpl.setSpan(
                UnderlineSpan(),
                0,
                ssbHideExpl.length - 2,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val ssbShowExpl = SpannableStringBuilder(getString(R.string.explain))
            ssbShowExpl.setSpan(
                UnderlineSpan(),
                0,
                ssbShowExpl.length - 2,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val explainLink = TextView(activity)
            explainLink.layoutParams = getWrapWrapParams()
            explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            explainLink.setTextColor(ContextCompat.getColor(activity!!, R.color.linkBlue))

            val isExpanded = arrayOf(false)

            if (shouldShowExplanation == "-1") {  //Always show Explanation
                llVerticalExpl.visibility = View.VISIBLE
                explainLink.text = ssbHideExpl
                isExpanded[0] = true
            } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
                llVerticalExpl.visibility = View.GONE
                explainLink.text = ssbShowExpl
                isExpanded[0] = false
            }

            explainLink.setOnClickListener { view ->
                val explView =
                    (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
                if (!isExpanded[0]) {
                    (view as TextView).text = ssbHideExpl
                    expandIt(explView)
                    isExpanded[0] = true

                } else if (isExpanded[0]) {
                    (view as TextView).text = ssbShowExpl
                    collapseIt(explView)
                    isExpanded[0] = false
                }
            }

            val gradientSeparator = getGradientSeparator(context)

            if (shouldShowPerformance) {
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    "Performance:" + " " + decimalFormatter.format((System.nanoTime() - startTime!!) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
            } else {
                gradientSeparator.text = ""
            }

            //Linearlayout horizontal com o explainlink e gradiente
            val llHorizontalLink = LinearLayout(activity)
            llHorizontalLink.orientation = HORIZONTAL
            llHorizontalLink.layoutParams = getMatchWrapParams()
            llHorizontalLink.addView(explainLink)
            llHorizontalLink.addView(gradientSeparator)

            llVerticalRoot.addView(llHorizontalLink)

            llVerticalExpl.addView(llHorizontal)

            val textViewFactExpanded = TextView(activity)
            textViewFactExpanded.layoutParams = getMatchWrapParams()
            textViewFactExpanded.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                CARD_TEXT_SIZE
            )
            textViewFactExpanded.gravity = Gravity.LEFT
            val explainText2 = getString(R.string.explain_divisores2) + "\n"
            val ssbExplain2 = SpannableStringBuilder(explainText2)
            ssbExplain2.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.boldColor
                    )
                ), 0, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssbExplain2.append(str_fact_exp)
            ssbExplain2.setSpan(
                RelativeSizeSpan(0.9f),
                ssbExplain2.length - str_fact_exp.length,
                ssbExplain2.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (hasExpoentes!!) {
                val textFactRepetidos = "\n" + getString(R.string.explain_divisores3) + "\n"
                ssbExplain2.append(textFactRepetidos)
                ssbExplain2.setSpan(
                    boldColorSpan,
                    ssbExplain2.length - textFactRepetidos.length,
                    ssbExplain2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbExplain2.append(ssbFatores)
                ssbExplain2.setSpan(
                    RelativeSizeSpan(0.9f),
                    ssbExplain2.length - ssbFatores.length,
                    ssbExplain2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbExplain2.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    ssbExplain2.length - ssbFatores.length,
                    ssbExplain2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textViewFactExpanded.text = ssbExplain2
            textViewFactExpanded.setTag(R.id.texto, "texto")

            llVerticalExpl.addView(textViewFactExpanded)

            llVerticalRoot.addView(llVerticalExpl)


        } else if (shouldShowExplanation == "1") { //nunca mostrar explicações

            if (shouldShowPerformance) {
                //View separator with gradient
                val gradientSeparator = getGradientSeparator(context)
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime!!) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
                llVerticalRoot.addView(gradientSeparator, 0)
            }
        }

        cardview.addView(llVerticalRoot)

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardview,
                activity as Activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?) = true
                    override fun onDismiss(view: View?) = history.removeView(cardview)
                })
        )

    }

    inner class BackGroundOperation : AsyncTask<Long, Float, ArrayList<ArrayList<Long>>>() {

        public override fun onPreExecute() {
            calculateButton.isClickable = false
            calculateButton.text = getString(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity as Activity)
            cvWidth = card_view_1.width
            heightDip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(1, heightDip)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): ArrayList<ArrayList<Long>> {
            val factoresPrimos = ArrayList<ArrayList<Long>>()
            val results = ArrayList<Long>()
            val divisores = ArrayList<Long>()
            var number: Long = num[0]!!
            var progress: Float?
            var oldProgress: Float? = 0f

            results.add(number)

            while (number % 2L == 0L) {
                divisores.add(2L)
                number /= 2
                results.add(number)
            }

            var i: Long = 3
            while (i <= number / i) {
                while (number % i == 0L) {
                    divisores.add(i)
                    number /= i
                    results.add(number)
                }
                progress = i.toFloat() / (number / i)
                if (progress - oldProgress!! > 0.1f) {
                    publishProgress(progress, i.toFloat())
                    oldProgress = progress
                }
                if (isCancelled) break
                i += 2
            }
            if (number > 1L) {
                divisores.add(number)
            }

            if (number != 1L) {
                results.add(1L)
            }

            factoresPrimos.add(results)
            factoresPrimos.add(divisores)

            return factoresPrimos
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@FatorizarFragment.isVisible && values[0] != null) {
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(Math.round(values[0]!! * cvWidth), heightDip)
            }
        }

        override fun onPostExecute(result: ArrayList<ArrayList<Long>>) {
            if (this@FatorizarFragment.isVisible) {
                processData(result, false)
            }
        }

        override fun onCancelled(parcial: ArrayList<ArrayList<Long>>) {
            super.onCancelled(parcial)
            if (this@FatorizarFragment.isVisible) {
                processData(parcial, true)
            }
        }
    }

    private fun processData(result: ArrayList<ArrayList<Long>>, wasCanceled: Boolean) {
        /* resultadosDivisao|fatoresPrimos
            *                100|2
            *                 50|2
            *                 25|5
            *                  5|5
            *                  1|
            *
            * */

        val resultadosDivisao = result[0]
        val fatoresPrimos = result[1]

        // Tamanho da lista de números primos
        val sizeList = fatoresPrimos.size
        var strFatores = ""
        var strResults = ""
        val ssbFatores: SpannableStringBuilder

        history.limit(historyLimit)

        if (sizeList == 1) {
            strFatores = resultadosDivisao[0].toString() + " " + getString(R.string.its_a_prime)
            ssbFatores = SpannableStringBuilder(strFatores)
            ssbFatores.setSpan(
                ForegroundColorSpan(Color.parseColor("#29712d")),
                0,
                ssbFatores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            ) //verde
            CreateCardView.createCardViewWithSSB(history, ssbFatores, activity as Activity)

        } else {
            strFatores = ""
            var hasExpoentes: Boolean? = false
            var counter = 1
            var lastItem: Long? = fatoresPrimos[0]

            val xmlColors = resources.getIntArray(R.array.f_colors_xml)
            val fColors: ArrayList<Int> = ArrayList()

            if (shouldShowColors) {
                for (f_color in xmlColors) fColors.add(f_color)
                fColors.shuffle() //randomizar as cores
            } else {
                for (i in xmlColors.indices) fColors.add(xmlColors[xmlColors.size - 1])
            }

            val ssbFactExpanded = SpannableStringBuilder()
            var colorIndex = 0

            //TreeMap
            val dataSet = LinkedHashMap<String, Int>()

            //Contar os expoentes
            for (i in fatoresPrimos.indices) {
                val fatori = fatoresPrimos[i]

                if (lastItem != fatori) colorIndex++

                val fi = fatori.toString()
                ssbFactExpanded.append(fi)
                ssbFactExpanded.setSpan(
                    ForegroundColorSpan(fColors[colorIndex]),
                    ssbFactExpanded.length - fi.length, ssbFactExpanded.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFactExpanded.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    ssbFactExpanded.length - fi.length, ssbFactExpanded.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFactExpanded.append("×")

                if (i == 0) {
                    dataSet[fatoresPrimos[0].toString()] = 1
                } else if (fatori == lastItem && i > 0) {
                    hasExpoentes = true
                    counter++
                    dataSet[fatori.toString()] = counter
                } else if (fatori != lastItem && i > 0) {
                    counter = 1
                    dataSet[fatori.toString()] = counter
                }
                lastItem = fatori
            }
            ssbFactExpanded.delete(ssbFactExpanded.length - 1, ssbFactExpanded.length)

            ssbFatores = SpannableStringBuilder(strFatores)

            var valueLength: Int
            colorIndex = 0

            val mapValues = dataSet.entries
            val test = arrayOfNulls<Map.Entry<*, *>>(mapValues.size)    // (fator primo)

            var lastKey = test[0]?.key.toString()

            val iterator = dataSet.entries.iterator()

            //Criar os expoentes
            while (iterator.hasNext()) {
                val pair = iterator.next() as Map.Entry<*, *>

                val key = pair.key.toString()
                val value = pair.value.toString()

                if (value.toInt() == 1) {
                    //Expoente 1
                    ssbFatores.append(key)
                    ssbFatores.setSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssbFatores.length - key.length, ssbFatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                } else if (value.toInt() > 1) {
                    //Expoente superior a 1 // pair.getkey = fator; pair.getvalue = expoente

                    ssbFatores.append(key)
                    ssbFatores.setSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssbFatores.length - key.length, ssbFatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    valueLength = value.length
                    ssbFatores.append(value)
                    ssbFatores.setSpan(
                        SuperscriptSpan(),
                        ssbFatores.length - valueLength,
                        ssbFatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ssbFatores.setSpan(
                        RelativeSizeSpan(0.8f),
                        ssbFatores.length - valueLength,
                        ssbFatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                if (iterator.hasNext()) ssbFatores.append("×")

                if (lastKey != key) colorIndex++
                lastKey = key

                iterator.remove() // avoids a ConcurrentModificationException
            }

            if (wasCanceled) {
                val incompleteCalc = "\n" + getString(R.string._incomplete_calc)
                ssbFatores.append(incompleteCalc)
                ssbFatores.setSpan(
                    ForegroundColorSpan(Color.RED),
                    ssbFatores.length - incompleteCalc.length,
                    ssbFatores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFatores.setSpan(
                    RelativeSizeSpan(0.8f),
                    ssbFatores.length - incompleteCalc.length,
                    ssbFatores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // Todos os números primos divisores
            val ssbDivisores = SpannableStringBuilder()
            colorIndex = 0
            var currentLong: Long? = fatoresPrimos[0]
            for (i in 0 until sizeList - 1) {
                val fator_i = fatoresPrimos[i]
                if (currentLong != fator_i) {
                    colorIndex++
                }
                currentLong = fator_i

                val fa = fator_i.toString() + "\n"
                ssbDivisores.append(fa)
                ssbDivisores.setSpan(
                    ForegroundColorSpan(fColors[colorIndex]),
                    ssbDivisores.length - fa.length, ssbDivisores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val fator_i = fatoresPrimos[sizeList - 1]
            if (currentLong != fator_i) {
                colorIndex++
            }
            ssbDivisores.append(fator_i.toString())
            ssbDivisores.setSpan(
                ForegroundColorSpan(fColors[colorIndex]),
                ssbDivisores.length - fator_i.toString().length, ssbDivisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssbDivisores.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                ssbDivisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )

            for (i in 0 until resultadosDivisao.size - 1) {
                strResults += resultadosDivisao[i].toString() + "\n"
            }
            strResults += resultadosDivisao[resultadosDivisao.size - 1].toString()

            createCardViewLayout(
                resultadosDivisao[0],
                history,
                strResults,
                ssbDivisores,
                ssbFatores,
                ssbFactExpanded,
                hasExpoentes
            )
        }

        resetButtons()

    }

}