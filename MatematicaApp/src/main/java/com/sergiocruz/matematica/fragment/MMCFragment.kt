package com.sergiocruz.matematica.fragment

import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.model.MDData
import com.sergiocruz.matematica.tasks.MMCExplanationTask
import com.sergiocruz.matematica.tasks.SimpleFactorizationTask
import java.math.BigInteger

class MMCFragment : MDAbstractFragment() {

    override var title: Int = R.string.mmc_title

    override var pageIndex: Int = 2

    override fun getHelpTextId(): Int = R.string.help_text_mdc

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_mmc

    override val explainTitleOne = R.string.explainMMC1html
    override val explainTitleTwo = R.string.explainMMC2html
    override val explainTitleThree = R.string.explainMMC3html

    override fun getExplanationTask(inputNumbers: List<BigInteger>, fColors: List<Int>, updateProgress: (percent: List<Float>) -> Unit, onFinished: (result: MDData) -> Unit): SimpleFactorizationTask {
        return MMCExplanationTask(getString(R.string.no_common_factors), inputNumbers, fColors, updateProgress, onFinished)
    }

    override val resultPrefix: Int = R.string.mdc_result_prefix

    override fun calculator(input: List<BigInteger>): BigInteger = mmc(input)

    /*****************************************************************
     * MMC: Mínimo múltiplo Comum (LCM: Least Common Multiplier)
     */
    private fun mmc(a: BigInteger, b: BigInteger): BigInteger {
        return b.divide(a.gcd(b)).multiply(a)
    }

    private fun mmc(input: List<BigInteger>): BigInteger {
        var result = input[0]
        for (i in 1 until input.size)
            result = mmc(result, input[i])
        return result
    }

}