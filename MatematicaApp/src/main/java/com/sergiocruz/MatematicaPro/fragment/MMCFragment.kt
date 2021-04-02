package com.sergiocruz.MatematicaPro.fragment

import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGE_APPEARING
import android.animation.LayoutTransition.CHANGE_DISAPPEARING
import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface.BOLD
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.*
import android.text.style.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.databinding.ItemResultMmcBinding
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.model.InputTags
import com.sergiocruz.MatematicaPro.model.MyTags
import kotlinx.android.synthetic.main.fragment_mmc.*
import kotlinx.android.synthetic.main.include_mmc_explanation.*
import kotlinx.coroutines.Deferred
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MMCFragment : BaseFragment(), OnEditorActions {
    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    private var taskIndex = 0
    internal var startTime: Long = 0
    private lateinit var language: String
    private lateinit var arrayOfEditTexts: Array<EditText>

    override var title: Int = R.string.mmc_title
    override var pageIndex: Int = 2

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
     */
    //    private static BigInteger mdc(BigInteger a, BigInteger b) {
    ////        while (b > 0) {
    //        while (b.compareTo(ZERO) == 1) {
    //            BigInteger temp = b;
    ////            b = a % b;
    //            b = a.remainder(b);
    //            a = temp;
    //        }
    //        return a;
    //    }

    //    private static BigInteger mdc(BigInteger[] input) {
    //        BigInteger result = input[0];
    //        for (int i = 1; i < input.length; i++)
    //            result = mdc(result, input[i]);
    //        return result;
    //    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     */
    //    private final static BigInteger mdc2(BigInteger a, BigInteger b) {
    //        return b == 0 ? a : mdc(b, a % b);
    //    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = Locale.getDefault().displayLanguage
    }

    override fun onDestroy() {
        super.onDestroy()

        var hasCanceled = false
        for (i in asyncTaskQueue.indices) {
            asyncTaskQueue[i]?.let {
                if (it.status == AsyncTask.Status.RUNNING) {
                    it.cancel(true)
                    hasCanceled = true
                }
            }
        }

        if (hasCanceled) {
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
        }

        arrayOfEditTexts = emptyArray()

    }

    override fun optionsMenu() = R.menu.menu_main

    override fun getHelpTextId(): Int = R.string.help_text_mmc

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_mmc

    override fun getHistoryLayout(): LinearLayout? = history

    override fun onActionDone() = calculateMMC()

    override fun getLayoutIdForFragment() = R.layout.fragment_mmc

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scale = resources.displayMetrics.density

        ViewCompat.setOnApplyWindowInsetsListener(card_view_1) { view: View?, insets: WindowInsetsCompat? ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                val insetTop: Int = insets?.systemWindowInsetTop ?: 0
                view?.setPadding(
                        ((view.paddingLeft) / scale).toInt(),
                        (view.paddingTop / scale + insetTop).toInt(),
                        (view.paddingRight / scale).toInt(),
                        (view.paddingBottom / scale).toInt()
                )
            }
            return@setOnApplyWindowInsetsListener insets?.consumeSystemWindowInsets()
        }

        arrayOfEditTexts = arrayOf(mmc_num_1, mmc_num_2, mmc_num_3, mmc_num_4, mmc_num_5, mmc_num_6, mmc_num_7, mmc_num_8)

        calculateButton.setOnClickListener { calculateMMC() }

        button_add_mmc.setOnClickListener { addMMC() }
        button_remove_mmc.setOnClickListener { removeMMC() }

        arrayOfEditTexts.forEach {
            it.addTextChangedListener(NumberFormatterTextWatcher(it, shouldFormatNumbers, onEditor = this))
            it.error = null
        }

    }

    private fun addMMC() {
        val ll34visible = linear_layout_34.visibility == View.VISIBLE
        val f3visible = frame_3.visibility == View.VISIBLE
        val f4visible = frame_4.visibility == View.VISIBLE
        val ll56visible = linear_layout_56.visibility == View.VISIBLE
        val f5visible = frame_5.visibility == View.VISIBLE
        val f6visible = frame_6.visibility == View.VISIBLE
        val ll78visible = linear_layout_78.visibility == View.VISIBLE
        val f7visible = frame_7.visibility == View.VISIBLE
        val f8visible = frame_8.visibility == View.VISIBLE


        if (!ll34visible || f3visible || f4visible) {
            linear_layout_34.visibility = View.VISIBLE

            if (!f3visible) {
                frame_3.visibility = View.VISIBLE
                button_remove_mmc.visibility = View.VISIBLE
                mmc_num_2.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f4visible) {
                frame_4.visibility = View.VISIBLE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (!ll56visible || f5visible || f6visible) {
            linear_layout_56.visibility = View.VISIBLE

            if (!f5visible) {
                frame_5.visibility = View.VISIBLE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f6visible) {
                frame_6.visibility = View.VISIBLE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }
        if (!ll78visible || f7visible || f8visible) {
            linear_layout_78.visibility = View.VISIBLE

            if (!f7visible) {
                frame_7.visibility = View.VISIBLE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f8visible) {
                frame_8.visibility = View.VISIBLE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.INVISIBLE
                return
            }
        }

    }

    private fun removeMMC() {
        val ll34Visibe = linear_layout_34.visibility == View.VISIBLE
        val f3visible = frame_3.visibility == View.VISIBLE
        val f4visible = frame_4.visibility == View.VISIBLE
        val ll56Visible = linear_layout_56.visibility == View.VISIBLE
        val f5visible = frame_5.visibility == View.VISIBLE
        val f6visible = frame_6.visibility == View.VISIBLE
        val ll78Visible = linear_layout_78.visibility == View.VISIBLE
        val f7visible = frame_7.visibility == View.VISIBLE
        val f8visible = frame_8.visibility == View.VISIBLE

        if (ll78Visible) {
            if (f8visible) {
                mmc_num_8.setText("")
                frame_8.visibility = View.GONE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.VISIBLE
                return
            }
            if (f7visible) {
                mmc_num_7.setText("")
                linear_layout_78.visibility = View.GONE
                frame_7.visibility = View.GONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll56Visible) {
            if (f6visible) {
                mmc_num_6.setText("")
                frame_6.visibility = View.GONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f5visible) {
                mmc_num_5.setText("")
                linear_layout_56.visibility = View.GONE
                frame_5.visibility = View.GONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll34Visibe) {
            if (f4visible) {
                mmc_num_4.setText("")
                frame_4.visibility = View.GONE
                frame_4.alpha = 0f
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f3visible) {
                mmc_num_3.setText("")
                frame_3.visibility = View.GONE
                frame_3.alpha = 0f
                linear_layout_34.visibility = View.GONE
                button_remove_mmc.visibility = View.INVISIBLE
                mmc_num_2.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

    }

    private fun calculateMMC() {
        startTime = System.nanoTime()

        val emptyTextView = ArrayList<TextView>()
        val bigNumbers = ArrayList<BigInteger>()

        arrayOfEditTexts.forEach {
            val numString = it.text.digitsOnly()
            if (numString.isEmpty()) {
                emptyTextView.add(it)
            } else {
                when (val number = numString.toBigIntegerOrNull()) {
                    null -> {
                        it.apply {
                            requestFocus()
                            error = getString(R.string.invalid_number)
                            postDelayed({ error = null }, clearErrorDelayMillis)
                        }
                        showKeyboard(activity)
                    }
                    BigInteger.ZERO -> {
                        it.apply {
                            requestFocus()
                            error = getString(R.string.maiores_qzero)
                            postDelayed({ error = null }, clearErrorDelayMillis)
                        }
                        showKeyboard(activity)
                    }
                    else -> bigNumbers.add(number)
                }
            }
        }

        if (bigNumbers.size < 2) {
            emptyTextView[0].apply {
                requestFocus()
                error = getString(R.string.add_number_pair)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            showKeyboard(activity)
            return
        } else {
            hideKeyboard(activity)
            emptyTextView.clear()
            arrayOfEditTexts.forEach { it.error = null }
        }

        var mmcResultString = getString(R.string.mmc_result_prefix)
        var mmcResult: BigInteger? = null

        if (bigNumbers.size > 1) {
            mmcResultString = bigNumbers.joinToString(
                    prefix = getString(R.string.mmc_result_prefix),
                    postfix = ")= ") { it.toString() }
            mmcResult = mmc(bigNumbers)
        }

        mmcResultString += mmcResult

        createResultsCardView(
                mmcResultString = mmcResultString,
                mmcResult = mmcResult,
                numbers = bigNumbers,
        )
    }

    private fun createResultsCardView(mmcResultString: String,
                                      mmcResult: BigInteger?,
                                      numbers: ArrayList<BigInteger>) {

        val layout = ItemResultMmcBinding.inflate(layoutInflater)
        val input = numbers.sorted().joinToString { it.toString() }
        with(layout) {
            textViewTop.text = mmcResultString

            showFavoriteStarForInput(imageStar, input)

            // TODO check if temp result is saved

            root.setOnTouchListener(SwipeToDismissTouchListener(root,
                    activity as Activity,
                    object : SwipeToDismissTouchListener.DismissCallbacks {
                        override fun onDismiss(view: View?) {
                            checkBackgroundOperation(myTags)
                        }
                    },
                    withExplanations = withExplanations,
                    inputTags = InputTags(input, operationName),
            ))

            var isExpanded = false
            var isCalculating = false
            explain.explainContainer.visibility = View.GONE

            if (withExplanations) {
                explainLink.visibility = View.VISIBLE
                explainLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                if (explanations == Explanations.Always) {
                    explainLink.setText(R.string.calculating)
                    isCalculating = true
                    // Adicionar os números a fatorizar na tag do cardview
                    val myTags = MyTags(root,
                            numbers,
                            mmcResult,
                            hasExplanation = false,
                            hasBGOperation = false,
                            texto = "",
                            bGfatores = null,
                            taskNumber = taskIndex)
                    val asyncTask = BackGroundOperationMMC(myTags).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    asyncTaskQueue.add(asyncTask)
                    taskIndex++

                } else if (explanations == Explanations.WhenAsked) {
                    explainLink.setText(R.string.show_explain)
                }
                explainLink.setOnClickListener { view -> (view as TextView)
                    if (isCalculating) return@setOnClickListener
                    if (isExpanded.not()) {
                        view.setText(R.string.hide_explain)
                        expandThis(explain.explainContainer)
                        isExpanded = true
                    } else if (isExpanded) {
                        view.setText(R.string.show_explain)
                        collapseThis(explain.explainContainer)
                        isExpanded = false
                    }
                }
            } else {
                explainLink.visibility = View.GONE
                progressBar.visibility = View.GONE
                if (shouldShowPerformance) {
                    val formatter = DecimalFormat("#.###")
                    val elapsed = root.context.getString(R.string.performance) + " " + formatter.format((System.nanoTime() - startTime) / 1_000_000_000.0) + "s"
                    textViewPerformance.text = elapsed
                    textViewPerformance.visibility = View.VISIBLE
                } else {
                    textViewPerformance.visibility = View.GONE
                }
            }
            history.addView(root, 0)
        }

    }

    fun checkBackgroundOperation(theTags: MyTags) {
        if (theTags.hasBGOperation == true) {
            val taskNumber = theTags.taskNumber
            val task = asyncTaskQueue[taskNumber]
            if (task?.status == AsyncTask.Status.RUNNING) {
                task.cancel(true)
                asyncTaskQueue[taskNumber] = null
                theTags.hasBGOperation = false
                showCustomToast(context, getString(R.string.canceled_op))
            }
        }
    }

    private var fColors: List<Int> = mutableListOf()

    // Asynctask <Params, Progress, Result>
    inner class BackGroundOperationMMC(private var cardTags: MyTags) :
            AsyncTask<Void, Float, Void>() {
        private lateinit var theCardViewBG: CardView
        private lateinit var mmcNumbers: ArrayList<BigInteger>
        private lateinit var bgfatores: ArrayList<ArrayList<BigInteger>>

        private lateinit var gradientSeparator: TextView
        lateinit var progressBar: View
        private lateinit var percentFormatter: NumberFormat

        private lateinit var progressParams: ViewGroup.LayoutParams

        private var timerProgressUpdate: Deferred<Unit>? = null

        public override fun onPreExecute() {
            percentFormatter = DecimalFormat("#.###%")
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
            progressParams = progressBar.layoutParams
            gradientSeparator = theCardViewBG.findViewWithTag<View>("gradient_separator") as TextView
            cardTags.hasBGOperation = true

            fColors = getRandomFactorsColors()

            val text = " " + getString(R.string.factorizing) + " 0%"
            val ssb = SpannableStringBuilder(text)
            ssb.setSafeSpan(ForegroundColorSpan(fColors[0]), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            gradientSeparator.text = ssb
        }

        override fun doInBackground(vararg voids: Void): Void? {
            val fatores = ArrayList<ArrayList<BigInteger>>()
            mmcNumbers = cardTags.bigNumbers

            val numbersSize = mmcNumbers.size
            for (i in 0 until numbersSize) { // fatorizar todos os números inseridos em MMC
                var progress: Float
                val fatoresIx = ArrayList<BigInteger>()

                var number = mmcNumbers[i]
                if (number == BigInteger.ONE) {
                    fatoresIx.add(BigInteger.ONE)
                }
                while (number % BigInteger.valueOf(2) == BigInteger.ZERO) {
                    fatoresIx.add(BigInteger.valueOf(2))
                    number /= BigInteger.valueOf(2)
                }

                var j: BigInteger = BigInteger.valueOf(3)

                timerProgressUpdate = delayedTimerAsync(repeatMillis = 1000) {
                    progress = try {
                        (j.toBigDecimal(scale = 2) / (number.toBigDecimal(scale = 2) / j.toBigDecimal(scale = 2))).toFloat()
                    } catch (e: Exception) {
                        0f
                    }
                    publishProgress(progress, i.toFloat())
                }
                timerProgressUpdate?.start()

                while (j <= number / j) {
                    if (isCancelled) break
                    while (number % j == BigInteger.ZERO) {
                        fatoresIx.add(j)
                        number /= j
                    }
                    j += BigInteger.valueOf(2)
                }
                if (number > BigInteger.ONE) {
                    fatoresIx.add(number)
                }

                fatores.add(fatoresIx)
            }
            cardTags.bGfatores = fatores
            return null
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@MMCFragment.isVisible) {
                val color = fColors[(values[1] ?: 0f).roundToInt()]
                progressBar.setBackgroundColor(color)
                var value0 = values[0] ?: 0f
                if (value0 > 1f) value0 = 1.0f
                progressParams.width = (value0 * card_view_1.width).roundToInt()
                progressBar.layoutParams = progressParams

                val text = " " + getString(R.string.factorizing) + " " + percentFormatter.format(value0)
                val ssb = SpannableStringBuilder(text)
                ssb.setSafeSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                gradientSeparator.text = ssb
            }
        }

        override fun onPostExecute(result: Void?) {
            timerProgressUpdate?.cancel()
            if (this@MMCFragment.isVisible) {

                bgfatores = cardTags.bGfatores!!

                val datasets = ArrayList<ArrayList<BigInteger>>()

                for (k in bgfatores.indices) {
                    val bases = ArrayList<BigInteger>()
                    val exps = ArrayList<BigInteger>()

                    val strFatores = mmcNumbers[k].toString() + "="
                    val ssbFatores = SpannableStringBuilder(strFatores)
                    ssbFatores.setSafeSpan(ForegroundColorSpan(fColors[k]), 0, ssbFatores.length, SPAN_EXCLUSIVE_INCLUSIVE)

                    var counter = BigInteger.ONE
                    var nextFactor = 0
                    var lastItem: BigInteger = bgfatores[k][0]

                    //TreeMap
                    val dataset = LinkedHashMap<String, BigInteger>()

                    //Contar os expoentes
                    for (i in 0 until bgfatores[k].size) {
                        if (i == 0) {
                            dataset[bgfatores[k][0].toString()] = BigInteger.ONE
                            bases.add(bgfatores[k][0])
                            exps.add(BigInteger.ONE)
                        } else {
                            if (bgfatores[k][i] == lastItem) {
                                counter++
                                dataset[bgfatores[k][i].toString()] = counter
                                bases[nextFactor] = bgfatores[k][i]
                                exps[nextFactor] = counter
                            } else if (bgfatores[k][i] != lastItem) {
                                counter = BigInteger.ONE
                                nextFactor++
                                dataset[bgfatores[k][i].toString()] = counter
                                bases.add(bgfatores[k][i])
                                exps.add(counter)
                            }
                        }
                        lastItem = bgfatores[k][i]
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
                            ssbFatores.append(pair.key)

                        } else if (pair.value > BigInteger.ONE) {
                            //Expoente superior a 1
                            valueLength = pair.value.toString().length
                            ssbFatores.append(pair.key + pair.value.toString())
                            ssbFatores.setSafeSpan(SuperscriptSpan(), ssbFatores.length - valueLength, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                            ssbFatores.setSafeSpan(RelativeSizeSpan(0.8f), ssbFatores.length - valueLength, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        if (iterator.hasNext()) {
                            ssbFatores.append("×")
                        }

                        iterator.remove() // avoids a ConcurrentModificationException
                    }
                    if (k < bgfatores.size - 1) ssbFatores.append("\n")

                    ssbFatores.setSafeSpan(StyleSpan(BOLD), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFatores.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)

                    //explainTextView_1;
                    (theCardViewBG.findViewWithTag<View>("explainTextView_1") as TextView).append(ssbFatores)

                }

                val maioresBases = ArrayList<BigInteger>()
                val maioresExps = ArrayList<BigInteger>()
                val colors = ArrayList<Long>()

                run {
                    var i = 0
                    while (i < datasets.size) {
                        val bases = datasets[i]
                        val exps = datasets[i + 1]

                        for (cb in bases.indices) {
                            val currentBase = bases[cb]
                            val currentExp = exps[cb]

                            if (!maioresBases.contains(currentBase)) {
                                maioresBases.add(currentBase)
                                maioresExps.add(currentExp)
                                colors.add(i.toLong() / 2)
                            }

                            if (maioresBases.contains(currentBase) && currentExp > maioresExps[maioresBases.indexOf(currentBase)]
                            ) {
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

                                    if (nextBase == currentBase && nextExp > maioresExps[maioresBases.indexOf(
                                                    currentBase
                                            )] && maioresBases.contains(nextBase)
                                    ) {
                                        maioresExps[maioresBases.indexOf(nextBase)] = nextExp
                                        colors[maioresBases.indexOf(currentBase)] = (j / 2).toLong()
                                    }
                                }
                                j += 2
                            }
                        }
                        i += 2
                    }
                }

                val ssbMmc = SpannableStringBuilder()

                //Criar os expoentes do MMC com os maiores fatores com cores e a negrito
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
                    ssbMmc.append(getString(R.string.no_common_factors))
                }

                ssbMmc.setSafeSpan(StyleSpan(BOLD), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMmc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                //explainTextView_2
                (theCardViewBG.findViewWithTag<View>("explainTextView_2") as TextView).append(ssbMmc)

                ssbMmc.delete(0, ssbMmc.length)
                val resultMmc = cardTags.resultMDC ?: BigInteger.ZERO
                ssbMmc.append(resultMmc.toString())

                ssbMmc.setSafeSpan(StyleSpan(BOLD), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMmc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMmc.setSafeSpan(ForegroundColorSpan(fColors.last()), 0, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)

                //explainTextView_3
                (theCardViewBG.findViewWithTag<View>("explainTextView_3") as TextView).append(ssbMmc)

                progressBar.visibility = View.GONE

                if (shouldShowPerformance) {
                    val decimalFormatter = DecimalFormat("#.###")
                    val elapsed =
                            getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                    gradientSeparator.text = elapsed
                } else {
                    gradientSeparator.text = ""
                }

                cardTags.hasBGOperation = false
                cardTags.hasExplanation = true
                asyncTaskQueue.set(cardTags.taskNumber, null)

                datasets.clear()
            }

        }
    }

    companion object {
        const val CARD_TEXT_SIZE = 15f

        /*****************************************************************
         * MMC: Mínimo múltiplo Comum (LCM: Least Common Multiplier)
         */
        private fun mmc(a: BigInteger, b: BigInteger): BigInteger {
            return b.divide(a.gcd(b)).multiply(a)
        }

        private fun mmc(input: ArrayList<BigInteger>): BigInteger {
            var result = input[0]
            for (i in 1 until input.size)
                result = mmc(result, input[i])
            return result
        }
    }
}