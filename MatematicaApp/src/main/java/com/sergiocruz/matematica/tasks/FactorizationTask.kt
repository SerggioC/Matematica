package com.sergiocruz.matematica.tasks

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.Ui.ClickableCardView
import com.sergiocruz.matematica.Ui.TooltipManager
import com.sergiocruz.matematica.databinding.ItemResultFatorizarBinding
import com.sergiocruz.matematica.fragment.BaseFragment
import com.sergiocruz.matematica.model.FactorizationData
import com.sergiocruz.matematica.model.InputTags
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*

class FactorizationTask(
        private val numberToFactorize: BigInteger,
        private val incompleteCalcText: String,
        private val factorsColors: List<Int>,
        private val updateProgress: (percent: Float) -> Unit,
        private val onFinished: (data: FactorizationData) -> Unit,
) : AsyncTask<Unit, Float, ArrayList<ArrayList<BigInteger>>>() {

    private var timer: Job = Job()

    override fun doInBackground(vararg num: Unit?): ArrayList<ArrayList<BigInteger>> {
        val factoresPrimos = ArrayList<ArrayList<BigInteger>>()
        val results = ArrayList<BigInteger>()
        val divisores = ArrayList<BigInteger>()
        var number: BigInteger = numberToFactorize
        var progress: Float

        results.add(number)

        if (number.isProbablePrime(100)) {
            factoresPrimos.add(results)
            factoresPrimos.add(divisores)
            return factoresPrimos
        }

        while (number % TWO == BigInteger.ZERO) {
            divisores.add(TWO)
            number /= TWO
            results.add(number)
        }

        var i = THREE

        delayedTimerAsync(repeatMillis = 500, job = timer) {
            progress = try {
                (i.toBigDecimal(scale = 2) / (number.toBigDecimal(scale = 2) / i.toBigDecimal(scale = 2))).toFloat()
            } catch (e: Exception) {
                0f
            }
            updateProgress(progress)
        }
        timer?.start()

        while (i <= number / i) {
            while (number % i == BigInteger.ZERO) {
                divisores.add(i)
                number /= i
                results.add(number)
            }

            if (isCancelled) break
            i += TWO
        }
        if (number > BigInteger.ONE) {
            divisores.add(number)
        }

        if (number != BigInteger.ONE) {
            results.add(BigInteger.ONE)
        }

        factoresPrimos.add(results)
        factoresPrimos.add(divisores)

        return factoresPrimos
    }

    override fun onPostExecute(result: ArrayList<ArrayList<BigInteger>>) {
        processFactorizationData(
                numberFactorized = numberToFactorize,
                result = result,
                factorsColors = factorsColors,
                incompleteCalcText = incompleteCalcText,
                onFinished = onFinished,
        )
        timer?.cancel()
    }

    override fun onCancelled(parcial: ArrayList<ArrayList<BigInteger>>?) {
        super.onCancelled(parcial)
        if (parcial != null) {
            processFactorizationData(
                    numberFactorized = numberToFactorize,
                    result = parcial,
                    wasCanceled = true,
                    factorsColors = factorsColors,
                    incompleteCalcText = incompleteCalcText,
                    onFinished = onFinished,
            )
        }
        timer?.cancel()
    }

    companion object {
        fun processFactorizationData(
                numberFactorized: BigInteger,
                result: ArrayList<ArrayList<BigInteger>>,
                wasCanceled: Boolean = false,
                factorsColors: List<Int>,
                incompleteCalcText: String,
                onFinished: (data: FactorizationData) -> Unit,
        ) {
            /** resultadosDivisao|fatoresPrimos
             *            100|2
             *             50|2
             *             25|5
             *              5|5
             *              1|
             **/

            val resultadosDivisao = result[0]
            val fatoresPrimos = result[1]

            // Tamanho da lista de números primos
            val sizeList = fatoresPrimos.size
            val strFatores: String
            var strResults = ""
            val ssbFatores: SpannableStringBuilder

            if (sizeList <= 1) {
                val factorizationData = FactorizationData(
                        numberToFatorize = numberFactorized,
                        rawResult = result,
                        strResults = "",
                        ssbStrDivisores = SpannableStringBuilder(""),
                        ssbFatores = SpannableStringBuilder(""),
                        strFactExp = SpannableStringBuilder(""),
                        hasExpoentes = false,
                        isPrime = true,
                )
                onFinished(factorizationData)

            } else {
                strFatores = ""
                var hasExpoentes = false
                var counter = 1
                var lastItem: BigInteger? = fatoresPrimos[0]

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
                    ssbFactExpanded.setSafeSpan(ForegroundColorSpan(factorsColors[colorIndex]), ssbFactExpanded.length - fi.length, ssbFactExpanded.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFactExpanded.setSafeSpan(StyleSpan(Typeface.BOLD), ssbFactExpanded.length - fi.length, ssbFactExpanded.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                        ssbFatores.setSafeSpan(ForegroundColorSpan(factorsColors[colorIndex]), ssbFatores.length - key.length, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    } else if (value.toInt() > 1) {
                        //Expoente superior a 1 // pair.getkey = fator; pair.getvalue = expoente

                        ssbFatores.append(key)
                        ssbFatores.setSafeSpan(ForegroundColorSpan(factorsColors[colorIndex]), ssbFatores.length - key.length, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        valueLength = value.length
                        ssbFatores.append(value)
                        ssbFatores.setSafeSpan(SuperscriptSpan(), ssbFatores.length - valueLength, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        ssbFatores.setSafeSpan(RelativeSizeSpan(0.85f), ssbFatores.length - valueLength, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        ssbFatores.setSafeSpan(StyleSpan(Typeface.BOLD), ssbFatores.length - valueLength, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (iterator.hasNext()) ssbFatores.append("×")

                    if (lastKey != key) colorIndex++
                    lastKey = key

                    iterator.remove() // avoids a ConcurrentModificationException
                }

                if (wasCanceled) {
                    val incompleteCalc = "\n${incompleteCalcText}"
                    ssbFatores.append(incompleteCalc)
                    ssbFatores.setSafeSpan(ForegroundColorSpan(Color.RED), ssbFatores.length - incompleteCalc.length, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFatores.setSafeSpan(RelativeSizeSpan(0.8f), ssbFatores.length - incompleteCalc.length, ssbFatores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // Todos os números primos divisores
                val ssbDivisores = SpannableStringBuilder()
                colorIndex = 0
                var currentLong: BigInteger? = fatoresPrimos[0]
                for (i in 0 until sizeList - 1) {
                    val fatorI = fatoresPrimos[i]
                    if (currentLong != fatorI) {
                        colorIndex++
                    }
                    currentLong = fatorI

                    val fa = fatorI.toString() + "\n"
                    ssbDivisores.append(fa)
                    ssbDivisores.setSafeSpan(ForegroundColorSpan(factorsColors[colorIndex]), ssbDivisores.length - fa.length, ssbDivisores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val fatorI2 = fatoresPrimos[sizeList - 1]
                if (currentLong != fatorI2) {
                    colorIndex++
                }
                ssbDivisores.append(fatorI2.toString())
                ssbDivisores.setSafeSpan(ForegroundColorSpan(factorsColors[colorIndex]), ssbDivisores.length - fatorI2.toString().length, ssbDivisores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbDivisores.setSafeSpan(StyleSpan(Typeface.BOLD), 0, ssbDivisores.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                for (i in 0 until resultadosDivisao.size - 1) {
                    strResults += resultadosDivisao[i].toString() + "\n"
                }
                strResults += resultadosDivisao[resultadosDivisao.size - 1].toString()


                val factorizationData = FactorizationData(
                        numberToFatorize = numberFactorized,
                        rawResult = result,
                        strResults = strResults,
                        ssbStrDivisores = ssbDivisores,
                        ssbFatores = ssbFatores,
                        strFactExp = ssbFactExpanded,
                        hasExpoentes = hasExpoentes,
                        isPrime = false,
                )

                onFinished(factorizationData)
            }

        }

        fun createCardViewLayout(data: FactorizationData, isFavorite: Boolean, showPerformance: Boolean, explanations: BaseFragment.Explanations, operationName: String, startTime: Long, context: Activity): ClickableCardView {
            val number = data.numberToFatorize
            val strResults = data.strResults
            val ssbStrDivisores = data.ssbStrDivisores
            val ssbFatores = data.ssbFatores
            val strFactExp = data.strFactExp
            val hasExpoentes = data.hasExpoentes
            val isPrime = data.isPrime

            val spans = ssbFatores.getSpans(0, ssbFatores.length, ForegroundColorSpan::class.java)
            for (i in spans.indices) {
                ssbFatores.removeSpan(spans[i])
            }

            val layout = ItemResultFatorizarBinding.inflate(LayoutInflater.from(context))
            with(layout) {
                val strNum: String
                if (isPrime) {
                    strNum = "$number\n${context.getString(R.string.its_a_prime)}"
                    factorizeRootCardViewItem.setBackgroundColor(ContextCompat.getColor(context, R.color.greener))
                    textViewTop.gravity = Gravity.CENTER_HORIZONTAL
                    textViewTop.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                } else {
                    // Adicionar o texto com o resultado da fatorização com expoentes
                    strNum = context.getString(R.string.factorization_of) + " " + number + " = \n"
                }
                val ssbNum = SpannableStringBuilder(strNum)
                ssbNum.append(ssbFatores)
                textViewTop.text = ssbNum

                imageStar.visibility = if (isFavorite) View.VISIBLE else View.GONE
                imageStar.rotateYAnimation()
                imageStar.setOnClickListener {
                    TooltipManager.showTooltipOn(imageStar, context.getString(R.string.result_is_favorite))
                }

                if (showPerformance) {
                    val formatter1 = DecimalFormat("#.###")
                    val elapsed = context.getString(R.string.performance) + " " + formatter1.format((System.nanoTime() - startTime) / 1_000_000_000.0) + "s"
                    textViewPerformance.text = elapsed
                    textViewPerformance.visibility = View.VISIBLE
                } else {
                    textViewPerformance.visibility = View.GONE
                }

                if (explanations == BaseFragment.Explanations.Never || isPrime) {   // 1 = nunca mostrar
                    explainLink.visibility = View.GONE
                    explain.explainContainer.visibility = View.GONE
                    if (showPerformance.not()) {
                        gradientSeparator.visibility = View.GONE
                    }
                } else {
                    explainLink.setOnClickListener {
                        if (explain.explainContainer.visibility == View.VISIBLE) {
                            collapseThis(explain.explainContainer)
                            explainLink.setText(R.string.show_explain)
                        } else {
                            expandThis(explain.explainContainer)
                            explainLink.setText(R.string.hide_explain)
                        }
                    }
                }

                // -1 = sempre  0 = quando pedidas
                if ((explanations == BaseFragment.Explanations.Always || explanations == BaseFragment.Explanations.WhenAsked) && isPrime.not()) {
                    explain.textViewResults.text = strResults
                    explain.textViewDivisores.text = ssbStrDivisores
                    explain.textViewAllResults.text = strFactExp
                    if (hasExpoentes) {
                        explain.explainTitle3.visibility = View.VISIBLE
                        explain.textViewFatores.visibility = View.VISIBLE
                        explain.textViewFatores.text = data.ssbFatores
                    } else {
                        explain.explainTitle3.visibility = View.GONE
                        explain.textViewFatores.visibility = View.GONE
                    }
                    explain.explainContainer.viewTreeObserver.let { vto ->
                        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                if (vto.isAlive) {
                                    vto.removeOnGlobalLayoutListener(this)
                                } else {
                                    explain.explainContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                                val hei = explain.explainContainer.measuredHeight
                                explain.explainContainer.setTag(R.id.initialHeight, hei)
                                if (explanations == BaseFragment.Explanations.Always) {
                                    explain.explainContainer.visibility = View.VISIBLE
                                    explainLink.setText(R.string.hide_explain)
                                } else if (explanations == BaseFragment.Explanations.WhenAsked) {
                                    explain.explainContainer.visibility = View.GONE
                                    explainLink.setText(R.string.show_explain)
                                }
                            }
                        })
                    }
                }

                root.setOnTouchListener(SwipeToDismissTouchListener(root,
                        context,
                        withExplanations = explanations == BaseFragment.Explanations.WhenAsked || explanations == BaseFragment.Explanations.Always,
                        inputTags = InputTags(input = number.toString(), operation = operationName))
                )
            }
            return layout.root
        }

        private val TWO = BigInteger.valueOf(2)
        private val THREE = BigInteger.valueOf(3)


    }

}

