package com.sergiocruz.MatematicaPro.fragment

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.*
import android.util.TypedValue
import android.view.*
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
import com.sergiocruz.MatematicaPro.Ui.TooltipManager
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.databinding.FatorizarResultBinding
import com.sergiocruz.MatematicaPro.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.expandIt
import com.sergiocruz.MatematicaPro.model.InputTags
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.DecimalFormat
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

        // Check if result exists in DB
        context?.let {
            launchSafeCoroutine {
                val result: HistoryDataClass? = LocalDatabase.getInstance(it).historyDAO()?.getResultForKeyAndOp(num.toString(), operationName)
                if (result != null) {
                    withContext(Dispatchers.Main) {
                        val type = object : TypeToken<ArrayList<ArrayList<Long>>>() {}.type
                        processData(gson.fromJson(result.content, type), wasCanceled = false, limitHistory = true, saveToDB = false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        bgOperation = BackGroundOperation().execute(num)
                    }
                }
            }
        }

    }

    private fun createCardViewLayout(
            number: Long?,
            str_results: String,
            ssb_str_divisores: SpannableStringBuilder,
            ssbFatores: SpannableStringBuilder,
            str_fact_exp: SpannableStringBuilder,
            hasExpoentes: Boolean
    ) {

        val ssbFatoresTop = SpannableStringBuilder(ssbFatores)
        val spans = ssbFatoresTop.getSpans(0, ssbFatoresTop.length, ForegroundColorSpan::class.java)
        for (i in spans.indices) {
            ssbFatoresTop.removeSpan(spans[i])
        }

        // Adicionar o texto com o resultado da fatorização com expoentes
        val strNum = getString(R.string.factorization_of) + " " + number + " = \n"
        val ssbNum = SpannableStringBuilder(strNum)
        ssbNum.append(ssbFatoresTop)

        val layout = FatorizarResultBinding.inflate(LayoutInflater.from(context))
        with(layout) {
            textViewTop.text = ssbNum

            showFavoriteStarForInput(imageStar, number.toString())

            explainLink.setOnClickListener {
                if (explainContainer.visibility == View.VISIBLE) {
                    collapseIt(explainContainer)
                    explainLink.setText(R.string.show_explain)
                } else {
                    expandIt(explainContainer)
                    explainLink.setText(R.string.hide_explain)
                }
            }

            if (shouldShowPerformance) {
                val formatter1 = DecimalFormat("#.###")
                val elapsed = context?.getString(R.string.performance) + " " + formatter1.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                textViewPerformance.text = elapsed
                textViewPerformance.visibility = View.VISIBLE
            } else {
                textViewPerformance.visibility = View.GONE
            }

            if (shouldShowExplanation == "1") {   // 1 = nunca mostrar
                explainLink.visibility = View.GONE
                explainContainer.visibility = View.GONE
                if (shouldShowPerformance.not()) {
                    gradientSeparator.visibility = View.GONE
                }
            }

            // -1 = sempre  0 = quando pedidas
            if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {
                textViewResults.text = str_results
                textViewDivisores.text = ssb_str_divisores
                textViewAllResults.text = str_fact_exp
                if (hasExpoentes) {
                    explainTitle3.visibility = View.VISIBLE
                    textViewFatores.visibility = View.VISIBLE
                    textViewFatores.text = ssbFatores
                } else {
                    explainTitle3.visibility = View.GONE
                    textViewFatores.visibility = View.GONE
                }
                explainContainer.viewTreeObserver.let { vto ->
                    vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (vto.isAlive) {
                                vto.removeOnGlobalLayoutListener(this)
                            } else {
                                explainContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                            val hei = explainContainer.measuredHeight
                            explainContainer.setTag(R.id.initialHeight, hei)
                            if (shouldShowExplanation == "-1") {  //Always show Explanation
                                explainContainer.visibility = View.VISIBLE
                                explainLink.setText(R.string.hide_explain)
                            } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
                                explainContainer.visibility = View.GONE
                                explainLink.setText(R.string.show_explain)
                            }
                        }
                    })
                }
            }

            root.tag = InputTags(input = number.toString(), operation = operationName)
            root.setOnTouchListener(SwipeToDismissTouchListener(root, activity as Activity))
        }

        getHistoryLayout()?.addView(layout.root, 0)
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
            saveCardToDatabase(result.get(0).get(0).toString(), data, operationName)
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
            CreateCardView.viewWithSSB(history, ssbFatores, activity as Activity, input = resultadosDivisao[0].toString(), isResult = true)

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