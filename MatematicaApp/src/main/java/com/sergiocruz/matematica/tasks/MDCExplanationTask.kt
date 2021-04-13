package com.sergiocruz.matematica.tasks

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned.*
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import android.view.View
import android.widget.TextView
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.helper.setSafeSpan
import com.sergiocruz.matematica.model.MDData
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class MDCExplanationTask(
        private val noCommonFactors: String,
        private val inputNumbers: List<BigInteger>,
        private val fColors: List<Int>,
        updateProgress: (percent: List<Float>) -> Unit,
        private val onFinished: (result: MDData) -> Unit,
) : SimpleFactorizationTask(inputNumbers, updateProgress) {

    override fun doInBackground(vararg voids: Void): List<List<BigInteger>> {
        val result = super.doInBackground(*voids)
        val mmcExplData = createExplanations(factorizationData = result)
        onFinished(mmcExplData)
        return result
    }

    private fun createExplanations(factorizationData: List<List<BigInteger>>): MDData {
        val datasets = ArrayList<ArrayList<BigInteger>>(2)
        val ssbFatorization = SpannableStringBuilder()
        factorizationData.forEachIndexed { outerIndex, outerList ->
            val bases = ArrayList<BigInteger>()
            val exps = ArrayList<BigInteger>()

            val strFatores = inputNumbers[outerIndex].toString() + "="
            val ssbFatores = SpannableStringBuilder(strFatores)
            ssbFatores.setSafeSpan(ForegroundColorSpan(fColors[outerIndex]), 0, ssbFatores.length, SPAN_EXCLUSIVE_INCLUSIVE)

            var counter = BigInteger.ONE
            var nextFactor = 0
            var lastItem = outerList[0]

            val dataSet = LinkedHashMap<BigInteger, BigInteger>()
            outerList.forEachIndexed { innerIndex, innerValue ->
                if (innerIndex == 0) {
                    dataSet[outerList[0]] = BigInteger.ONE
                    bases.add(outerList[0])
                    exps.add(BigInteger.ONE)
                } else {
                    if (innerValue == lastItem) {
                        counter++
                        dataSet[innerValue] = counter
                        bases[nextFactor] = innerValue
                        exps[nextFactor] = counter
                    } else {
                        counter = BigInteger.ONE
                        nextFactor++
                        dataSet[innerValue] = counter
                        bases.add(innerValue)
                        exps.add(counter)
                    }
                }
                lastItem = innerValue
            }

            datasets.add(bases)
            datasets.add(exps)

            // Criar os expoentes
            var valueLength: Int
            val dataCount = dataSet.entries.count()
            dataSet.entries.forEachIndexed { index, pair: Map.Entry<BigInteger, BigInteger> ->
                if (pair.value == BigInteger.ONE) {
                    //Expoente 1
                    if (pair.key > BigInteger.ONE) {
                        ssbFatores.append(pair.key.toString())
                    }

                } else if (pair.value > BigInteger.ONE) {
                    //Expoente superior a 1
                    valueLength = pair.value.toString().length
                    ssbFatores.append(pair.key.toString() + pair.value.toString())
                    ssbFatores.setSafeSpan(SuperscriptSpan(), ssbFatores.length - valueLength, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFatores.setSafeSpan(RelativeSizeSpan(0.9f), ssbFatores.length - valueLength, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                if (index in 1 until (dataCount - 1)) {
                    ssbFatores.append("×")
                }
            }
            if (outerIndex < factorizationData.size - 1) {
                ssbFatores.append("\n")
            }

            ssbFatores.setSafeSpan(StyleSpan(Typeface.BOLD), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            ssbFatores.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            ssbFatorization.append(ssbFatores)
        }

        val basesComuns = java.util.ArrayList<BigInteger>()
        val expsComuns = java.util.ArrayList<BigInteger>()
        val colors = java.util.ArrayList<Int>()
        val bases = datasets[0]
        val exps = datasets[1]
        currentBaseLoop@ for (cb in bases.indices) {
            val currentBase = bases[cb]
            val currentExp = exps[cb]
            val tempBases = java.util.ArrayList<BigInteger>()
            val tempExps = java.util.ArrayList<BigInteger>()
            val tempColors = java.util.ArrayList<Long>()
            tempBases.add(currentBase)
            tempExps.add(currentExp)
            tempColors.add(0)
            var j = 2
            nextBasesLoop@ while (j < datasets.size) {
                val nextBases = datasets[j]
                if (!nextBases.contains(currentBase)) {
                    break@nextBasesLoop
                }
                val nextExps = datasets[j + 1]
                innerLoop@ for (nb in nextBases.indices) {
                    val nextBase = nextBases[nb]
                    val nextExp = nextExps[nb]
                    if (nextBase == currentBase) {
                        tempBases.add(nextBase)
                        tempExps.add(nextExp)
                        tempColors.add((j / 2).toLong())
                    }
                }
                j += 2
            }
            var lowerExp = tempExps[0]
            var lowerIndex = 0
            if (Collections.frequency(tempBases, currentBase) == datasets.size / 2) {
                for (i in tempExps.indices) {
                    if (tempExps[i] < lowerExp) {
                        lowerExp = tempExps[i]
                        lowerIndex = i
                    }
                }
                basesComuns.add(tempBases[lowerIndex])
                expsComuns.add(lowerExp)
                colors.add(tempColors[lowerIndex].toInt())
            }
        }

        var ssbMdc = SpannableStringBuilder()

        //Criar os expoentes do MDC com os maiores fatores com cores e a negrito
        for (i in basesComuns.indices) {
            val baseLength = basesComuns[i].toString().length

            if (expsComuns[i] == BigInteger.ONE) {
                //Expoente 1
                if (basesComuns[i] > BigInteger.ONE) {
                    ssbMdc.append(basesComuns[i].toString())
                    ssbMdc.setSafeSpan(ForegroundColorSpan(fColors[colors[i]]), ssbMdc.length - baseLength, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else if (expsComuns[i] > BigInteger.ONE) {
                //Expoente superior a 1
                val expLength = expsComuns[i].toString().length
                ssbMdc.append(basesComuns[i].toString() + expsComuns[i].toString())
                ssbMdc.setSafeSpan(SuperscriptSpan(), ssbMdc.length - expLength, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMdc.setSafeSpan(RelativeSizeSpan(0.8f), ssbMdc.length - expLength, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMdc.setSafeSpan(ForegroundColorSpan(fColors[colors[i]]), ssbMdc.length - expLength - baseLength, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (ssbMdc.isNotEmpty()) {
                ssbMdc.append("×")
            }
        }
        if (ssbMdc.isNotEmpty()) {
            ssbMdc = ssbMdc.replace(ssbMdc.length - 1, ssbMdc.length, "")
        } else {
            ssbMdc.append(noCommonFactors)
        }

        ssbMdc.setSafeSpan(StyleSpan(Typeface.BOLD), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        ssbMdc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)

        datasets.clear()

        return MDData(inputNumbers, ssbFactorization = ssbFatorization, ssbExpanded = ssbMdc)
    }

}





