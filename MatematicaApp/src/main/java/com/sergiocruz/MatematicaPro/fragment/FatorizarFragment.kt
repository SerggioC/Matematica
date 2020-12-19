package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.graphics.Color
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.expandIt
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class FatorizarFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    private lateinit var textWatcher: BigNumbersTextWatcher

    private var bgOperation: AsyncTask<Long, Float, ArrayList<ArrayList<Long>>> =
        BackGroundOperation()

    private var startTime: Long = 0L

    override fun getLayoutIdForFragment() = R.layout.fragment_fatorizar

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override var title = R.string.factorize

    override var pageIndex = 4

    override fun getHelpTextId(): Int = R.string.help_text_fatores

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_fatorizar

    override fun getHistoryLayout(): LinearLayout? = history

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                val type = object : TypeToken<ArrayList<ArrayList<Long>>>() {}.type
                processData(gson.fromJson(fav.content, type), wasCanceled = false, limitHistory = false, saveToDB = false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cancelButton.setOnClickListener { displayCancelDialogBox(requireContext(), this) }
        calculateButton.setOnClickListener { calculatePrimeFactors() }
        clearButton.setOnClickListener { factorizeTextView.setText("") }

        textWatcher = BigNumbersTextWatcher(factorizeTextView, shouldFormatNumbers, this)
        factorizeTextView.addTextChangedListener(textWatcher)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(bgOperation, context)) resetButtons()
    }

    override fun onActionDone() {
        calculatePrimeFactors()
    }

    private fun calculatePrimeFactors() {
        startTime = System.nanoTime()
        val editnumText = factorizeTextView.text.digitsOnly()
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
            showCustomToast(context, getString(R.string.the_number) + " " + num + " " + getString(R.string.has_no_factors))
            return
        }

        bgOperation = BackGroundOperation().execute(num)

    }

    private fun createCardViewLayout(
            number: Long?,
            str_results: String,
            ssb_str_divisores: SpannableStringBuilder,
            ssbFatores: SpannableStringBuilder,
            str_fact_exp: SpannableStringBuilder,
            hasExpoentes: Boolean
    ) {

        //criar novo cardview
        val cardView = ClickableCardView(activity as Activity)
        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardView.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        val cvColor = ContextCompat.getColor(requireActivity(), R.color.cardsColor)
        cardView.setCardBackgroundColor(cvColor)

        // Add cardview to history layout at the top (index 0)
        getHistoryLayout()?.addView(cardView, 0)

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
            val explainText1 = getString(R.string.expl_text_divisores_1)
            val ssbExplain1 = SpannableStringBuilder(explainText1)
            val boldColorSpan =
                ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.boldColor))
            ssbExplain1.setSafeSpan(boldColorSpan, 0, ssbExplain1.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewExplanations.text = ssbExplain1
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
                            requireActivity(),
                            R.color.separatorLineColor
                    )
            )
            val oneDp = (1.2 * scale + 0.5f).toInt()
            verticalSeparador.setPadding(oneDp, 4, 0, oneDp)

            val llVerticalDivisores = LinearLayout(activity)
            llVerticalDivisores.layoutParams = getWrapWrapParams()
            llVerticalDivisores.orientation = LinearLayout.VERTICAL
            llVerticalDivisores.setPadding((4 * scale + 0.5f).toInt(), 0, (8 * scale + 0.5f).toInt(), 0)

            val textViewResults = TextView(activity)
            textViewResults.layoutParams = getWrapWrapParams()
            textViewResults.setTag(R.id.texto, "texto")
            textViewResults.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textViewResults.gravity = Gravity.RIGHT
            val ssbStrResults = SpannableStringBuilder(str_results)
            ssbStrResults.setSafeSpan(RelativeSizeSpan(0.9f), ssbStrResults.length - str_results.length, ssbStrResults.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewResults.text = ssbStrResults

            llVerticalResults.addView(textViewResults)

            val textViewDivisores = TextView(activity)
            textViewDivisores.layoutParams = getWrapWrapParams()
            textViewDivisores.setTag(R.id.texto, "texto")
            textViewDivisores.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textViewDivisores.gravity = Gravity.LEFT
            ssb_str_divisores.setSafeSpan(
                    RelativeSizeSpan(0.9f),
                    ssb_str_divisores.length - ssb_str_divisores.length,
                    ssb_str_divisores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textViewDivisores.text = ssb_str_divisores

            llVerticalDivisores.addView(textViewDivisores)

            //Adicionar os LL Verticais ao Horizontal
            llHorizontal.addView(llVerticalResults)

            llHorizontal.addView(verticalSeparador)

            //LinearLayout divisores
            llHorizontal.addView(llVerticalDivisores)

            val ssbHideExpl = SpannableStringBuilder(getString(R.string.hide_explain))
            ssbHideExpl.setSafeSpan(UnderlineSpan(), 0, ssbHideExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)
            val ssbShowExpl = SpannableStringBuilder(getString(R.string.explain))
            ssbShowExpl.setSafeSpan(UnderlineSpan(), 0, ssbShowExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)

            val explainLink = TextView(activity)
            explainLink.layoutParams = getWrapWrapParams()
            explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            explainLink.setTextColor(ContextCompat.getColor(requireActivity(), R.color.linkBlue))

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
                    expandIt(explView, null)
                    isExpanded[0] = true

                } else if (isExpanded[0]) {
                    (view as TextView).text = ssbShowExpl
                    collapseIt(explView)
                    isExpanded[0] = false
                }
            }

            //Linearlayout horizontal com o explainlink e gradiente
            val llHorizontalLink = LinearLayout(activity)
            llHorizontalLink.orientation = HORIZONTAL
            llHorizontalLink.layoutParams = getMatchWrapParams()
            llHorizontalLink.addView(explainLink)

            context?.let {
                val separator = getGradientSeparator(it, shouldShowPerformance, startTime, number.toString(), FatorizarFragment::class.java.simpleName)
                llHorizontalLink.addView(separator)
            }

            llVerticalRoot.addView(llHorizontalLink)
            llVerticalExpl.addView(llHorizontal)

            val textViewFactExpanded = TextView(activity)
            textViewFactExpanded.layoutParams = getMatchWrapParams()
            textViewFactExpanded.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textViewFactExpanded.gravity = Gravity.LEFT
            val explainText2 = getString(R.string.explain_divisores2) + "\n"
            val ssbExplain2 = SpannableStringBuilder(explainText2)
            ssbExplain2.setSafeSpan(boldColorSpan, 0, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            ssbExplain2.append(str_fact_exp)
            ssbExplain2.setSafeSpan(RelativeSizeSpan(0.9f), ssbExplain2.length - str_fact_exp.length, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            if (hasExpoentes) {
                val textFactRepetidos = "\n" + getString(R.string.explain_divisores3) + "\n"
                ssbExplain2.append(textFactRepetidos)
                ssbExplain2.setSafeSpan(boldColorSpan, ssbExplain2.length - textFactRepetidos.length, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbExplain2.append(ssbFatores)
                ssbExplain2.setSafeSpan(RelativeSizeSpan(0.9f), ssbExplain2.length - ssbFatores.length, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbExplain2.setSafeSpan(StyleSpan(android.graphics.Typeface.BOLD), ssbExplain2.length - ssbFatores.length, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            textViewFactExpanded.text = ssbExplain2
            textViewFactExpanded.setTag(R.id.texto, "texto")

            llVerticalExpl.addView(textViewFactExpanded)
            llVerticalRoot.addView(llVerticalExpl)

        } else if (shouldShowExplanation == "1") { //nunca mostrar explicações
            context?.let {
                val separator = getGradientSeparator(it, shouldShowPerformance, startTime, number.toString(), FatorizarFragment::class.java.simpleName)
                llVerticalRoot.addView(separator)
            }
        }

        cardView.addView(llVerticalRoot)

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
                SwipeToDismissTouchListener(cardView, activity as Activity,
                        object : SwipeToDismissTouchListener.DismissCallbacks {
                            override fun onDismiss(view: View?) {
                                getHistoryLayout()?.removeView(cardView) ?: Unit
                                CoroutineScope(Dispatchers.Default).launch {
                                    context?.let {
                                        LocalDatabase.getInstance(it).historyDAO()?.deleteHistoryItem(cardView.getTag(R.id.pk) as String, FatorizarFragment::class.java.simpleName)
                                    }
                                }
                            }
                        })
        )
        cardView.setTag(R.id.pk, number.toString())
        cardView.setTag(R.id.op, FatorizarFragment::class.java.simpleName)
    }



    lateinit var progressParams: ViewGroup.LayoutParams

    inner class BackGroundOperation : AsyncTask<Long, Float, ArrayList<ArrayList<Long>>>() {

        public override fun onPreExecute() {
            calculateButton.isClickable = false
            calculateButton.text = getString(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity as Activity)
            progressParams = progressBar.layoutParams
            progressParams.width = 1
            progressBar.layoutParams = progressParams
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): ArrayList<ArrayList<Long>> {
            val factoresPrimos = ArrayList<ArrayList<Long>>()
            val results = ArrayList<Long>()
            val divisores = ArrayList<Long>()
            var number: Long = num[0] ?: 0L
            var progress: Float?
            var oldProgress = 0f

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
                if (progress - oldProgress > 0.1f) {
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
                progressParams.width = Math.round(values[0]!! * card_view_1.width)
                progressBar.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: ArrayList<ArrayList<Long>>) {
            if (this@FatorizarFragment.isVisible) {
                processData(result, wasCanceled = false)
            }
        }

        override fun onCancelled(parcial: ArrayList<ArrayList<Long>>?) {
            super.onCancelled(parcial)
            if (this@FatorizarFragment.isVisible && parcial != null) {
                processData(parcial, wasCanceled = true)
            }
        }
    }

    private fun processData(result: ArrayList<ArrayList<Long>>, wasCanceled: Boolean, limitHistory: Boolean = true, saveToDB: Boolean = true) {
        /* resultadosDivisao|fatoresPrimos
            *                100|2
            *                 50|2
            *                 25|5
            *                  5|5
            *                  1|
            *
            * */

        if (saveToDB) {
            val type = object : TypeToken<ArrayList<ArrayList<Long>>>() {}.type
            val data = Gson().toJson(result, type)
            saveCardToDatabase(result.get(0).get(0).toString(), data, FatorizarFragment::class.java.simpleName)
        }

        val resultadosDivisao = result[0]
        val fatoresPrimos = result[1]

        // Tamanho da lista de números primos
        val sizeList = fatoresPrimos.size
        var strFatores = ""
        var strResults = ""
        val ssbFatores: SpannableStringBuilder

        if (limitHistory) {
            history.limit(historyLimit)
        }
        if (sizeList == 1) {
            strFatores = resultadosDivisao[0].toString() + " " + getString(R.string.its_a_prime)
            ssbFatores = SpannableStringBuilder(strFatores)
            ssbFatores.setSafeSpan(ForegroundColorSpan(Color.parseColor("#29712d")), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE) //verde
            CreateCardView.viewWithSSB(history, ssbFatores, activity as Activity)

        } else {
            strFatores = ""
            var hasExpoentes = false
            var counter = 1
            var lastItem: Long? = fatoresPrimos[0]

            val fColors = getRandomFactorsColors()

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
                ssbFactExpanded.setSafeSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssbFactExpanded.length - fi.length, ssbFactExpanded.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFactExpanded.setSafeSpan(
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
                    ssbFatores.setSafeSpan(
                            ForegroundColorSpan(fColors[colorIndex]),
                            ssbFatores.length - key.length, ssbFatores.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                } else if (value.toInt() > 1) {
                    //Expoente superior a 1 // pair.getkey = fator; pair.getvalue = expoente

                    ssbFatores.append(key)
                    ssbFatores.setSafeSpan(
                            ForegroundColorSpan(fColors[colorIndex]),
                            ssbFatores.length - key.length, ssbFatores.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    valueLength = value.length
                    ssbFatores.append(value)
                    ssbFatores.setSafeSpan(
                            SuperscriptSpan(),
                            ssbFatores.length - valueLength,
                            ssbFatores.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ssbFatores.setSafeSpan(
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
                ssbFatores.setSafeSpan(
                        ForegroundColorSpan(Color.RED),
                        ssbFatores.length - incompleteCalc.length,
                        ssbFatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFatores.setSafeSpan(
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
                val fatorI = fatoresPrimos[i]
                if (currentLong != fatorI) {
                    colorIndex++
                }
                currentLong = fatorI

                val fa = fatorI.toString() + "\n"
                ssbDivisores.append(fa)
                ssbDivisores.setSafeSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssbDivisores.length - fa.length, ssbDivisores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val fatorI2 = fatoresPrimos[sizeList - 1]
            if (currentLong != fatorI2) {
                colorIndex++
            }
            ssbDivisores.append(fatorI2.toString())
            ssbDivisores.setSafeSpan(
                    ForegroundColorSpan(fColors[colorIndex]),
                    ssbDivisores.length - fatorI2.toString().length, ssbDivisores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssbDivisores.setSafeSpan(
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
                    strResults,
                    ssbDivisores,
                    ssbFatores,
                    ssbFactExpanded,
                    hasExpoentes
            )
        }

        resetButtons()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        factorizeTextView.removeTextChangedListener(textWatcher)
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

}