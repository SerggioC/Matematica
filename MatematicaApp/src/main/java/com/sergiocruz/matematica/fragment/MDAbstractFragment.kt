package com.sergiocruz.matematica.fragment

import android.app.Activity
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.ItemResultMmcBinding
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.InputTags
import com.sergiocruz.matematica.model.MDData
import com.sergiocruz.matematica.model.OperationStatusTags
import com.sergiocruz.matematica.tasks.SimpleFactorizationTask
import kotlinx.android.synthetic.main.fragment_mmc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.DecimalFormat
import kotlin.math.roundToInt

abstract class MDAbstractFragment : BaseFragment() {

    private val percentFormatter by lazy { DecimalFormat("#.###%") }
    private var asyncTaskMap = mutableMapOf<String, AsyncTask<*, *, *>?>()

    private lateinit var arrayOfEditTexts: Array<EditText>

    override fun optionsMenu() = R.menu.menu_main

    override fun getHistoryLayout(): LinearLayout? = history

    override fun getLayoutIdForFragment() = R.layout.fragment_mmc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                val mdData = gson.fromJson(fav.content, MDData::class.java)
                createResultsCardView(startTime = System.nanoTime(),
                        mmcResultString = mdData.resultString ?: "",
                        mmcResult = mdData.result ?: BigInteger.ZERO,
                        numbers = mdData.inputNumbers,
                        mmcData = mdData,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView.setText(title)

        arrayOfEditTexts = arrayOf(mmc_num_1, mmc_num_2, mmc_num_3, mmc_num_4, mmc_num_5, mmc_num_6, mmc_num_7, mmc_num_8)

        calculateButton.setOnClickListener { calculate() }

        button_add_mmc.setOnClickListener { addTextField() }
        button_remove_mmc.setOnClickListener { removeTextField() }

        arrayOfEditTexts.forEach {
            it.addTextChangedListener(NumberFormatterTextWatcher(it, shouldFormatNumbers, onEditor = ::calculate))
            it.error = null
        }

    }

    private fun addTextField() {
        val f3Hidden = mmc_num_3.visibility == View.GONE
        val f4Hidden = mmc_num_4.visibility == View.GONE
        val f5Hidden = mmc_num_5.visibility == View.GONE
        val f6Hidden = mmc_num_6.visibility == View.GONE
        val f7Hidden = mmc_num_7.visibility == View.GONE
        val f8Hidden = mmc_num_8.visibility == View.GONE

        when {
            f3Hidden -> {
                linear_layout_34.visibility = View.VISIBLE
                button_remove_mmc.visibility = View.VISIBLE
                mmc_num_3.visibility = View.VISIBLE
                mmc_num_2.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f4Hidden -> {
                linear_layout_34.visibility = View.VISIBLE
                mmc_num_4.visibility = View.VISIBLE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f5Hidden -> {
                linear_layout_56.visibility = View.VISIBLE
                mmc_num_5.visibility = View.VISIBLE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f6Hidden -> {
                linear_layout_56.visibility = View.VISIBLE
                mmc_num_6.visibility = View.VISIBLE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f7Hidden -> {
                linear_layout_78.visibility = View.VISIBLE
                mmc_num_7.visibility = View.VISIBLE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f8Hidden -> {
                linear_layout_78.visibility = View.VISIBLE
                mmc_num_8.visibility = View.VISIBLE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.INVISIBLE
            }
        }


    }

    private fun removeTextField() {
        val f3visible = mmc_num_3.visibility == View.VISIBLE
        val f4visible = mmc_num_4.visibility == View.VISIBLE
        val f5visible = mmc_num_5.visibility == View.VISIBLE
        val f6visible = mmc_num_6.visibility == View.VISIBLE
        val f7visible = mmc_num_7.visibility == View.VISIBLE
        val f8visible = mmc_num_8.visibility == View.VISIBLE

        when {
            f8visible -> {
                mmc_num_8.setText("")
                mmc_num_8.visibility = View.GONE
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.VISIBLE
            }
            f7visible -> {
                mmc_num_7.setText("")
                mmc_num_7.visibility = View.GONE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                linear_layout_78.visibility = View.GONE
            }
            f6visible -> {
                mmc_num_6.setText("")
                mmc_num_6.visibility = View.GONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f5visible -> {
                mmc_num_5.setText("")
                mmc_num_5.visibility = View.GONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                linear_layout_56.visibility = View.GONE
            }
            f4visible -> {
                mmc_num_4.setText("")
                mmc_num_4.visibility = View.GONE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
            }
            f3visible -> {
                mmc_num_3.setText("")
                mmc_num_3.visibility = View.GONE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_2.imeOptions = EditorInfo.IME_ACTION_DONE
                linear_layout_34.visibility = View.GONE
                button_remove_mmc.visibility = View.INVISIBLE
            }
        }

    }

    @StringRes open val resultPrefix: Int = 0

    abstract fun calculator(input: List<BigInteger>) : BigInteger

    private fun calculate() {
        val startTime = System.nanoTime()

        val emptyTextView = ArrayList<TextView>()
        val inputNumbers = ArrayList<BigInteger>()

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
                    else -> inputNumbers.add(number)
                }
            }
        }

        if (inputNumbers.size < 2) {
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

        var mmcResultString = getString(resultPrefix)
        var mmcResult: BigInteger = BigInteger.ZERO

        if (inputNumbers.size > 1) {
            mmcResultString = inputNumbers.joinToString(
                    prefix = getString(resultPrefix),
                    postfix = ")= ") { it.toString() }
            mmcResult = calculator(inputNumbers)
        }

        mmcResultString += mmcResult

        launchSafeCoroutine(Dispatchers.Default) {
            context?.let { ctx ->
                val input = inputNumbers.sorted().joinToString { it.toString() }
                val result = LocalDatabase.getInstance(ctx).historyDAO().getResultForKeyAndOp(input, operationName)
                val data = result?.content?.let {
                    gson.fromJson(it, MDData::class.java)
                }
                withContext(Dispatchers.Main) {
                    createResultsCardView(
                            startTime = startTime,
                            mmcResultString = mmcResultString,
                            mmcResult = mmcResult,
                            numbers = inputNumbers,
                            mmcData = data,
                    )
                }
                if (data == null) {
                    val mdData = MDData(inputNumbers = inputNumbers, resultString = mmcResultString, result = mmcResult)
                    val dataJson = gson.toJson(mdData, MDData::class.java)
                    val hdt = HistoryDataClass(input, operationName, dataJson, favorite = result?.favorite ?: false)
                    LocalDatabase.getInstance(ctx).historyDAO().saveResult(hdt)
                }
            }
        }

    }

    abstract fun getExplanationTask(
            inputNumbers: List<BigInteger>,
            fColors: List<Int>,
            updateProgress: (percent: List<Float>) -> Unit,
            onFinished: (result: MDData) -> Unit,
    ): SimpleFactorizationTask

    @StringRes open val explainTitleOne: Int = 0
    @StringRes open val explainTitleTwo: Int = 0
    @StringRes open val explainTitleThree: Int = 0

    private fun createResultsCardView(
            startTime: Long,
            mmcResultString: String,
            mmcResult: BigInteger,
            numbers: List<BigInteger>,
            mmcData: MDData? = null
    ) {
        val layout = ItemResultMmcBinding.inflate(layoutInflater)
        val input = numbers.sorted().joinToString { it.toString() }
        with(layout) {
            textViewTop.text = mmcResultString

            showFavoriteStarForInput(imageStar, input)

            root.setOnTouchListener(SwipeToDismissTouchListener(
                    root,
                    activity as Activity,
                    object : SwipeToDismissTouchListener.DismissCallbacks {
                        override fun onDismiss(view: View?) {
                            cancelBackgroundOperation(input)
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
                        explainTitle1.setText(explainTitleOne)
                        textViewFactorization.text = mmcExplData.ssbFactorization
                        explainTitle2.setText(explainTitleTwo)
                        textViewFactorsExpanded.text = mmcExplData.ssbExpanded
                        explainTitle3.setText(explainTitleThree)
                        textViewResult.text = mmcResult.conditionalFormat()
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
                        val startTime2 = System.nanoTime()
                        tags.isCalculating = true
                        tags.isCompleted = false
                        view.tag = tags
                        val fColors = getRandomFactorsColors()
                        val task = getExplanationTask(
                                inputNumbers = numbers,
                                fColors = fColors,
                                updateProgress = { percent: List<Float> ->
                                    if (isVisible.not()) return@getExplanationTask
                                    val color = fColors[percent[1].toInt()]
                                    progressBar.setBackgroundColor(color)
                                    val value0 = percent[0].coerceIn(0.0f, 1.0f)
                                    progressParams.width = (value0 * explain.root.width).roundToInt()
                                    progressBar.layoutParams = progressParams
                                    val text = " " + getString(R.string.factorizing) + " " + percentFormatter.format(value0)
                                    val ssb = SpannableStringBuilder(text)
                                    ssb.setSafeSpan(ForegroundColorSpan(color), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    explainLink.text = ssb
                                },
                                onFinished = { mmcExplData: MDData ->
                                    launchSafeCoroutine(Dispatchers.Default) {
                                        mmcExplData.result = mmcResult
                                        mmcExplData.resultString = mmcResultString
                                        context?.let { ctx ->
                                            val data = gson.toJson(mmcExplData, MDData::class.java)
                                            LocalDatabase.getInstance(ctx).historyDAO().updateHistoryData(key = input, operationName, data)
                                        }
                                        withContext(Dispatchers.Main) {
                                            displayExplanationData(mmcExplData)
                                            tags.isCalculating = false
                                            tags.isCompleted = true
                                            view.tag = tags
                                            textViewPerformance.writePerformanceValue(startTime2)
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                                }
                        )
                        asyncTaskMap[input] = task
                        task.execute()
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
            }
            textViewPerformance.writePerformanceValue(startTime)

            getHistoryLayout()?.limit(historyLimit)
            getHistoryLayout()?.addView(root, 0)
        }

    }

    fun cancelBackgroundOperation(input: String) {
        val task = asyncTaskMap.get(input)
        if (task?.status == AsyncTask.Status.RUNNING) {
            task.cancel(true)
            asyncTaskMap.remove(input)
            showCustomToast(context, getString(R.string.canceled_op))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        var hasCanceled = false
        asyncTaskMap.forEach { (key, task) ->
            if (task?.status == AsyncTask.Status.RUNNING) {
                task.cancel(true)
                asyncTaskMap.remove(key)
                hasCanceled = true
            }
        }

        if (hasCanceled) {
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
        }

        arrayOfEditTexts = emptyArray()
    }

}