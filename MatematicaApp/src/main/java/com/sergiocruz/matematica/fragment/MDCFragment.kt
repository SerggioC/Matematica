package com.sergiocruz.matematica.fragment

import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.model.MDData
import com.sergiocruz.matematica.tasks.MDCExplanationTask
import com.sergiocruz.matematica.tasks.SimpleFactorizationTask
import java.math.BigInteger

class MDCFragment : MDAbstractFragment() {

    override var title: Int = R.string.mdc_title

    override var pageIndex: Int = 3

    override fun getHelpTextId(): Int = R.string.help_text_mdc

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_mdc

    override val explainTitleOne = R.string.explainMDC1html
    override val explainTitleTwo = R.string.explainMDC2html
    override val explainTitleThree = R.string.explainMDC3html

    override fun getExplanationTask(inputNumbers: List<BigInteger>, fColors: List<Int>, updateProgress: (percent: List<Float>) -> Unit, onFinished: (result: MDData) -> Unit): SimpleFactorizationTask {
        return MDCExplanationTask(getString(R.string.no_common_factors), inputNumbers, fColors, updateProgress, onFinished)
    }

    override val resultPrefix: Int = R.string.mdc_result_prefix

    override fun calculator(input: List<BigInteger>): BigInteger = mdc(input)

    /******************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
     *******************************************************************/
    private fun mdc(input: List<BigInteger>): BigInteger {
        var result = input[0]
        for (i in 1 until input.size)
            result = result.gcd(input[i])
        return result
    }

}