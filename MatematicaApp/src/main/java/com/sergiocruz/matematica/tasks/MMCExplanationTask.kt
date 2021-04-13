package com.sergiocruz.matematica.tasks

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned.*
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import com.sergiocruz.matematica.helper.setSafeSpan
import com.sergiocruz.matematica.model.MDData
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class MMCExplanationTask(
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
        for (k in factorizationData.indices) {
            val bases = ArrayList<BigInteger>()
            val exps = ArrayList<BigInteger>()

            val ssbFact = SpannableStringBuilder()
            ssbFact.append(inputNumbers[k].toString() + "=")
            ssbFact.setSafeSpan(ForegroundColorSpan(fColors[k]), 0, ssbFact.length, SPAN_EXCLUSIVE_INCLUSIVE)

            var counter = BigInteger.ONE
            var nextFactor = 0
            var lastItem: BigInteger = factorizationData[k][0]

            val dataset = LinkedHashMap<String, BigInteger>()

            //Contar os expoentes
            for (i in factorizationData[k].indices) {
                if (i == 0) {
                    dataset[factorizationData[k][0].toString()] = BigInteger.ONE
                    bases.add(factorizationData[k][0])
                    exps.add(BigInteger.ONE)
                } else {
                    if (factorizationData[k][i] == lastItem) {
                        counter++
                        dataset[factorizationData[k][i].toString()] = counter
                        bases[nextFactor] = factorizationData[k][i]
                        exps[nextFactor] = counter
                    } else if (factorizationData[k][i] != lastItem) {
                        counter = BigInteger.ONE
                        nextFactor++
                        dataset[factorizationData[k][i].toString()] = counter
                        bases.add(factorizationData[k][i])
                        exps.add(counter)
                    }
                }
                lastItem = factorizationData[k][i]
            }

            datasets.add(bases)
            datasets.add(exps)

            //Criar os expoentes
            var valueLength: Int
            val iterator = dataset.entries.iterator()
            while (iterator.hasNext()) {
                val pair = iterator.next()

                if (pair.value == BigInteger.ONE) {
                    //Expoente 1
                    ssbFact.append(pair.key)

                } else if (pair.value > BigInteger.ONE) {
                    //Expoente superior a 1
                    valueLength = pair.value.toString().length
                    ssbFact.append(pair.key + pair.value.toString())
                    ssbFact.setSafeSpan(SuperscriptSpan(), ssbFact.length - valueLength, ssbFact.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFact.setSafeSpan(RelativeSizeSpan(0.9f), ssbFact.length - valueLength, ssbFact.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                if (iterator.hasNext()) {
                    ssbFact.append("×")
                }

                iterator.remove() // avoids a ConcurrentModificationException
            }
            if (k < factorizationData.size - 1) {
                ssbFact.append("\n")
            }

            ssbFact.setSafeSpan(StyleSpan(Typeface.BOLD), 0, ssbFact.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            ssbFact.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbFact.length, SPAN_EXCLUSIVE_EXCLUSIVE)

            ssbFatorization.append(ssbFact)
        }

        val maioresBases = ArrayList<BigInteger>()
        val maioresExps = ArrayList<BigInteger>()
        val colors = ArrayList<Long>()

        var i = 0
        while (i < datasets.size) {
            val bases = datasets[i]
            val exps = datasets[i + 1]

            for (cb in bases.indices) {
                val currentBase = bases[cb]
                val currentExp = exps[cb]

                if (maioresBases.contains(currentBase).not()) {
                    maioresBases.add(currentBase)
                    maioresExps.add(currentExp)
                    colors.add(i.toLong() / 2)
                }

                if (maioresBases.contains(currentBase)
                        && currentExp > maioresExps[maioresBases.indexOf(currentBase)]) {
                    maioresExps[maioresBases.indexOf(currentBase)] = currentExp
                    colors[maioresBases.indexOf(currentBase)] = (i / 2).toLong()
                }

                var j = i + 2
                while (j < datasets.size) {
                    val nextBases = datasets[j]
                    val nextExps = datasets[j + 1]

                    for (nb in nextBases.indices) {
                        val nextBase = nextBases[nb]
                        val nextExp = nextExps[nb]

                        if (nextBase == currentBase
                                && nextExp > maioresExps[maioresBases.indexOf(currentBase)]
                                && maioresBases.contains(nextBase)) {
                            maioresExps[maioresBases.indexOf(nextBase)] = nextExp
                            colors[maioresBases.indexOf(currentBase)] = (j / 2).toLong()
                        }
                    }
                    j += 2
                }
            }
            i += 2
        }

        datasets.clear()

        val ssbMmc = SpannableStringBuilder()

        // Criar os expoentes do MMC com os maiores fatores com cores e a negrito
        for (i in maioresBases.indices) {
            val baseLength = maioresBases[i].toString().length

            if (maioresExps[i] == BigInteger.ONE) {
                //Expoente 1
                ssbMmc.append(maioresBases[i].toString())
                ssbMmc.setSafeSpan(
                        ForegroundColorSpan(fColors[colors[i].toInt()]),
                        ssbMmc.length - baseLength, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE
                )

            } else if (maioresExps[i] > BigInteger.ONE) {
                //Expoente superior a 1
                val expLength = maioresExps[i].toString().length
                ssbMmc.append(maioresBases[i].toString() + maioresExps[i].toString())
                ssbMmc.setSafeSpan(SuperscriptSpan(), ssbMmc.length - expLength, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMmc.setSafeSpan(RelativeSizeSpan(0.8f), ssbMmc.length - expLength, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMmc.setSafeSpan(ForegroundColorSpan(fColors[colors[i].toInt()]), ssbMmc.length - expLength - baseLength, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            ssbMmc.append("×")
        }
        if (ssbMmc.isNotEmpty()) {
            ssbMmc.replace(ssbMmc.length - 1, ssbMmc.length, "")
        } else {
            ssbMmc.append(noCommonFactors)
        }

        ssbMmc.setSafeSpan(StyleSpan(Typeface.BOLD), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        ssbMmc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)

        return MDData(inputNumbers, ssbFactorization = ssbFatorization, ssbExpanded = ssbMmc)
    }

}





