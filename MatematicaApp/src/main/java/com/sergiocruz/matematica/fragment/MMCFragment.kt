package com.sergiocruz.matematica.fragment

import android.app.Activity
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.*
import android.text.style.*
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.ItemResultMmcBinding
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.*
import com.sergiocruz.matematica.tasks.MMCExplanationTask
import kotlinx.android.synthetic.main.fragment_mmc.*
import kotlinx.android.synthetic.main.fragment_mmc.calculateButton
import kotlinx.android.synthetic.main.fragment_mmc.history
import kotlinx.android.synthetic.main.fragment_primality.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MMCFragment : BaseFragment(), OnEditorActions {
    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    private var taskIndex = 0

    private lateinit var arrayOfEditTexts: Array<EditText>

    override var title: Int = R.string.mmc_title
    override var pageIndex: Int = 2
    private val percentFormatter by lazy { DecimalFormat("#.###%") }

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
        val startTime = System.nanoTime()

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
        var mmcResult: BigInteger = BigInteger.ZERO

        if (bigNumbers.size > 1) {
            mmcResultString = bigNumbers.joinToString(
                    prefix = getString(R.string.mmc_result_prefix),
                    postfix = ")= ") { it.toString() }
            mmcResult = mmc(bigNumbers)
        }

        mmcResultString += mmcResult


        launchSafeCoroutine(Dispatchers.Default) {
            context?.let { ctx ->
                val input = bigNumbers.sorted().joinToString { it.toString() }
                val result = LocalDatabase.getInstance(ctx).historyDAO().getResultForKeyAndOp(input, operationName)
                val data = result?.content?.let {
                    gson.fromJson(it, MDData::class.java)
                }
                withContext(Dispatchers.Main) {
                    createResultsCardView(
                            startTime = startTime,
                            mmcResultString = mmcResultString,
                            mmcResult = mmcResult,
                            numbers = bigNumbers,
                            mmcData = data,
                    )
                }
                val mdData = MDData(resultString = mmcResultString, result = mmcResult)
                val dataJson = gson.toJson(mdData, MDData::class.java)
                val hdt = HistoryDataClass(input, operationName, dataJson, favorite = result?.favorite ?: false)
                LocalDatabase.getInstance(ctx).historyDAO().saveResult(hdt)
            }
        }

    }

    private fun createResultsCardView(
            startTime: Long,
            mmcResultString: String,
            mmcResult: BigInteger,
            numbers: List<BigInteger>,
            mmcData: MDData? = null) {

        val layout = ItemResultMmcBinding.inflate(layoutInflater)
        val input = numbers.sorted().joinToString { it.toString() }
        with(layout) {
            textViewTop.text = mmcResultString

            showFavoriteStarForInput(imageStar, input)

            root.setOnTouchListener(SwipeToDismissTouchListener(root,
                    activity as Activity,
                    object : SwipeToDismissTouchListener.DismissCallbacks {
                        override fun onDismiss(view: View?) {
//                            checkBackgroundOperation(null)
                        }
                    },
                    withExplanations = withExplanations,
                    inputTags = InputTags(input, operationName),
            ))

            explain.explainContainer.visibility = View.GONE

            if (withExplanations) {
                explainLink.visibility = View.VISIBLE
                explainLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                val progressParams: ViewGroup.LayoutParams = progressBar.layoutParams
                progressParams.width = 1
                progressBar.layoutParams = progressParams
                progressBar.visibility = View.VISIBLE


                fun displayExplanationData(mmcExplData: MDData) {
                    with(explain) {
                        explainContainer.visibility = View.VISIBLE
                        textViewFactorization.text = mmcExplData.ssbFactorization
                        textViewFactorsExpanded.text = mmcExplData.ssbExpanded
                        textViewResult.text = mmcResult.conditionalFormat()
                        writePerformanceValue(startTime)
                    }

                    val ost = explainLink.tag as OperationStatusTags
                    if (!ost.vtoIsCompleted) {
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
                                    explainLink.setText(R.string.hide_explain)
                                    explain.explainContainer.visibility = View.GONE

                                    if (ost.isExpanded.not()) {
                                        expandThis(explain.explainContainer)
                                        ost.isExpanded = true
                                    }
                                    ost.vtoIsCompleted = true
                                    explainLink.tag = ost
                                }
                            })
                        }
                    }
                }

                val ost = OperationStatusTags()
                explainLink.tag = ost
                explainLink.setOnClickListener { view -> (view as TextView)
                    val tags = view.tag as OperationStatusTags

                    if (mmcData != null) {
                        tags.isCalculating = false
                        tags.isCompleted = true
                        view.tag = tags
                        displayExplanationData(mmcData)

                    } else if (tags.isCalculating.not() && tags.isCompleted.not()) {
                        tags.isCalculating = true
                        tags.isCompleted = false
                        view.tag = tags
                        val fColors = getRandomFactorsColors()
                        val task = MMCExplanationTask(
                                inputNumbers = numbers,
                                fColors = fColors,
                                updateProgress = { percent: List<Float> ->
                                    if (isVisible.not()) return@MMCExplanationTask
                                    val color = fColors[percent[1].toInt()]
                                    progressBar.setBackgroundColor(color)
                                    val value0 = percent[0].coerceIn(0.0f, 1.0f)
                                    progressParams.width = (value0 * explain.root.width).roundToInt()
                                    progressBar.layoutParams = progressParams
                                    val text = " " + getString(R.string.factorizing) + " " + percentFormatter.format(value0)
                                    val ssb = SpannableStringBuilder(text)
                                    ssb.setSafeSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                                    explainLink.text = ssb
                                },
                                onFinished = { mmcExplData: MDData ->
                                    launchSafeCoroutine(Dispatchers.Default) {
                                        mmcExplData.result = mmcResult
                                        mmcExplData.resultString = mmcResultString
                                        context?.let { ctx ->
                                            val data = gson.toJson(mmcExplData, MDData::class.java)
                                            LocalDatabase.getInstance(ctx).historyDAO()?.updateHistoryData(key = input, operationName, data)
                                        }
                                        withContext(Dispatchers.Main) {
                                            displayExplanationData(mmcExplData)
                                            tags.isCalculating = false
                                            tags.isCompleted = true
                                            view.tag = tags
                                        }
                                    }
                                }
                        )
                        asyncTaskQueue.add(task)
                        task.execute()
                        taskIndex++
                    }

                    if (tags.isCompleted) {
                        if (tags.isExpanded) {
                            collapseThis(explain.explainContainer)
                            tags.isExpanded = false
                            explainLink.tag = tags
                            explainLink.setText(R.string.show_explain)
                        } else if (tags.vtoIsCompleted) {
                            expandThis(explain.explainContainer)
                            tags.isExpanded = true
                            explainLink.tag = tags
                            explainLink.setText(R.string.hide_explain)
                        }
                    }

                }
                if (explanations == Explanations.Always) {
                    explainLink.callOnClick()
                } else if (explanations == Explanations.WhenAsked) {
                    explainLink.setText(R.string.show_explain)
                }

            } else {
                explainLink.visibility = View.GONE
                progressBar.visibility = View.GONE
                writePerformanceValue(startTime)
            }

            getHistoryLayout()?.limit(historyLimit)
            getHistoryLayout()?.addView(root, 0)
        }

    }

    private fun ItemResultMmcBinding.writePerformanceValue(startTime: Long) {
        if (shouldShowPerformance) {
            val formatter = DecimalFormat("#.###")
            val elapsed = root.context.getString(R.string.performance) + " " + formatter.format((System.nanoTime() - startTime) / 1_000_000_000.0) + "s"
            textViewPerformance.text = elapsed
            textViewPerformance.visibility = View.VISIBLE
        } else {
            textViewPerformance.visibility = View.GONE
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