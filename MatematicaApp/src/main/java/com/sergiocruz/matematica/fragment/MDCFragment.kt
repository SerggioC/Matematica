package com.sergiocruz.matematica.fragment

import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGE_APPEARING
import android.animation.LayoutTransition.CHANGE_DISAPPEARING
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE
import android.text.TextUtils
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.sergiocruz.matematica.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.MyTags
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.Ui.ClickableCardView
import com.sergiocruz.matematica.helper.BigNumbersTextWatcher
import com.sergiocruz.matematica.helper.OnEditorActions
import kotlinx.android.synthetic.main.fragment_mdc.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class MDCFragment : BaseFragment(), OnEditorActions,
        SharedPreferences.OnSharedPreferenceChangeListener {

    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    private var fColors: List<Int> = mutableListOf()
    internal var heightDip: Int = 0
    internal var cvWidth: Int = 0
    private var taskNumber = 0
    internal var startTime: Long = 0
    private lateinit var language: String
    private lateinit var arrayOfEditTexts: Array<EditText>

    override var title: Int = R.string.mdc_title
    override var pageIndex: Int = 3

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     */
    //private final static BigInteger mdc2(BigInteger a, BigInteger b) {
    //    return b.compareTo(ZERO) == 1 ? a : mdc(b, a.remainder(b));
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = Locale.getDefault().displayLanguage
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_mdc

    override fun getHelpTextId(): Int = R.string.help_text_mdc

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_mdc

    override fun getHistoryLayout(): LinearLayout? = history

    override fun optionsMenu() = R.menu.menu_main

    override fun onActionDone() = calculateMDC()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayOfEditTexts = arrayOf(mdc_num_1, mdc_num_2, mdc_num_3, mdc_num_4, mdc_num_5, mdc_num_6, mdc_num_7, mdc_num_8)

        calculateButton.setOnClickListener { calculateMDC() }

        clearButton1.setOnClickListener { mdc_num_1.setText("") }
        clearButton2.setOnClickListener { mdc_num_2.setText("") }
        clearButton3.setOnClickListener { mdc_num_3.setText("") }
        clearButton4.setOnClickListener { mdc_num_4.setText("") }
        clearButton5.setOnClickListener { mdc_num_5.setText("") }
        clearButton6.setOnClickListener { mdc_num_6.setText("") }
        clearButton7.setOnClickListener { mdc_num_7.setText("") }
        clearButton8.setOnClickListener { mdc_num_8.setText("") }

        button_add_mdc.setOnClickListener { addMdcField() }
        button_remove_mdc.setOnClickListener { removeMdcField() }

        arrayOfEditTexts.forEach {
            it.addTextChangedListener(BigNumbersTextWatcher(it, shouldFormatNumbers, onEditor = this))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        var hasCanceled = false
        for (i in asyncTaskQueue.indices) {
            if (asyncTaskQueue[i] != null) {
                asyncTaskQueue[i]?.cancel(true)
                hasCanceled = true
            }
        }
        if (hasCanceled) {
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
        }
        arrayOfEditTexts = emptyArray()
    }

    private fun addMdcField() {
        val ll34Visible = linear_layout_34.visibility == View.VISIBLE
        val f3Visible = frame_3.visibility == View.VISIBLE
        val f4Visible = frame_4.visibility == View.VISIBLE
        val linearLayout56Visibe = linear_layout_56.visibility == View.VISIBLE
        val f5Visible = frame_5.visibility == View.VISIBLE
        val f6Visible = frame_6.visibility == View.VISIBLE
        val linearLayout78Visibe = linear_layout_78.visibility == View.VISIBLE
        val f7Visible = frame_7.visibility == View.VISIBLE
        val f8Visible = frame_8.visibility == View.VISIBLE

        if (!ll34Visible || f3Visible || f4Visible) {
            linear_layout_34.visibility = View.VISIBLE

            if (!f3Visible) {
                frame_3.visibility = View.VISIBLE
                button_remove_mdc.visibility = View.VISIBLE
                mdc_num_2.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f4Visible) {
                frame_4.visibility = View.VISIBLE
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (!linearLayout56Visibe || f5Visible || f6Visible) {
            linear_layout_56.visibility = View.VISIBLE

            if (!f5Visible) {
                frame_5.visibility = View.VISIBLE
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f6Visible) {
                frame_6.visibility = View.VISIBLE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }
        if (!linearLayout78Visibe || f7Visible || f8Visible) {
            linear_layout_78.visibility = View.VISIBLE

            if (!f7Visible) {
                frame_7.visibility = View.VISIBLE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f8Visible) {
                frame_8.visibility = View.VISIBLE
                button_add_mdc.visibility = View.INVISIBLE
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

    }

    private fun removeMdcField() {
        val ll34Visibe = linear_layout_34.visibility == View.VISIBLE
        val f3Visible = frame_3.visibility == View.VISIBLE
        val f4Visible = frame_4.visibility == View.VISIBLE
        val linearLayout56Visibe = linear_layout_56.visibility == View.VISIBLE
        val f5Visible = frame_5.visibility == View.VISIBLE
        val f6Visible = frame_6.visibility == View.VISIBLE
        val linearLayout78Visibe = linear_layout_78.visibility == View.VISIBLE
        val f7Visible = frame_7.visibility == View.VISIBLE
        val f8Visible = frame_8.visibility == View.VISIBLE

        if (linearLayout78Visibe) {
            if (f8Visible) {
                mdc_num_8.setText("")
                frame_8.visibility = View.GONE
                button_add_mdc.visibility = View.VISIBLE
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f7Visible) {
                mdc_num_7.setText("")
                frame_7.visibility = View.GONE
                linear_layout_78.visibility = View.GONE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (linearLayout56Visibe) {
            if (f6Visible) {
                mdc_num_6.setText("")
                frame_6.visibility = View.GONE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f5Visible) {
                mdc_num_5.setText("")
                frame_5.visibility = View.GONE
                linear_layout_56.visibility = View.GONE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll34Visibe) {
            if (f4Visible) {
                mdc_num_4.setText("")
                frame_4.visibility = View.GONE
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f3Visible) {
                mdc_num_3.setText("")
                frame_3.visibility = View.GONE
                linear_layout_34.visibility = View.GONE
                button_remove_mdc.visibility = View.INVISIBLE
                mdc_num_2.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

    }

    private fun calculateMDC() {
        startTime = System.nanoTime()

        val emptyTextView = ArrayList<TextView>()
        val bigNumbers = ArrayList<BigInteger>()

        arrayOfEditTexts.forEach {
            val numString = it.text.digitsOnly()
            if (TextUtils.isEmpty(numString)) {
                emptyTextView.add(it)
            } else {
                when (val number = numString.toLongOrNull()) {
                    null -> {
                        it.apply {
                            requestFocus()
                            error = getString(R.string.numero_alto)
                            postDelayed({ error = null }, clearErrorDelayMillis)
                        }
                        showKeyboard(activity)
                    }
                    0L -> {
                        it.apply {
                            requestFocus()
                            error = getString(R.string.maiores_qzero)
                            postDelayed({ error = null }, clearErrorDelayMillis)
                        }
                        showKeyboard(activity)
                    }
                    else -> bigNumbers.add(number.toBigInteger())
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
        }

        var mdcString: String = getString(R.string.mdc_result_prefix)
        var resultMDC: BigInteger? = null

        if (bigNumbers.size > 1) {
            for (i in 0 until bigNumbers.size - 1) {
                mdcString += bigNumbers[i].toString() + ", "
            }
            mdcString += bigNumbers[bigNumbers.size - 1].toString() + ")= "
            resultMDC = mdc(bigNumbers)
        }
        mdcString += resultMDC

        createResultsCard(mdcString, resultMDC, bigNumbers)
    }

    private fun createResultsCard(mdcString: String, resultMDC: BigInteger?, bigNumbers: ArrayList<BigInteger>) {
        val ssb = SpannableStringBuilder(mdcString)
        if (resultMDC.toString() == "1") {
            ssb.append("\n" + getString(R.string.primos_si))
            ssb.setSafeSpan(
                    ForegroundColorSpan(Color.parseColor("#29712d")),
                    ssb.length - 24,
                    ssb.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSafeSpan(
                    RelativeSizeSpan(0.9f),
                    ssb.length - 24,
                    ssb.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // criar novo cardview
        val cardView = ClickableCardView(requireContext())
        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true
        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f)
        cardView.cardElevation = (2 * scale + 0.5f)
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true
        cardView.layoutTransition = LayoutTransition()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
        }

        val cvColor = ContextCompat.getColor(requireActivity(), R.color.cardsColor)
        cardView.setCardBackgroundColor(cvColor)

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
                SwipeToDismissTouchListener(
                        cardView,
                        activity as Activity,
                        object : SwipeToDismissTouchListener.DismissCallbacks {
                            override fun onDismiss(view: View?) {
                                checkBackgroundOperation(view)
                            }
                        },
                        false)
        )

        val myTags = MyTags(cardView, bigNumbers, resultMDC, hasExplanation = false, hasBGOperation = false, texto = "", bGfatores = null, taskNumber = taskNumber)

        history.limit(historyLimit)
        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()

        //Adicionar o texto com o resultado ao TextView
        textView.text = ssb
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        textView.setTag(R.id.texto, "texto")

        // add the textview to the cardview
        llVerticalRoot.addView(textView)

        if (withExplanations) {
            createExplanations(mdcString, cardView, llVerticalRoot, explanations, myTags)
        } else {
            context?.let {
                val separator = getGradientSeparator(it, shouldShowPerformance, startTime, mdcString, MDCFragment::class.java.simpleName)
                llVerticalRoot.addView(separator, 0)
            }
            cardView.addView(llVerticalRoot) // Só o resultado sem explicações
        }
    }

    private fun createExplanations(
            input: String,
            cardView: CardView,
            llVerticalRoot: LinearLayout,
            explanation: Explanations,
            tags: MyTags,
    ) {
        val ssbHideExpl = SpannableStringBuilder(getString(R.string.hide_explain))
        ssbHideExpl.setSafeSpan(UnderlineSpan(), 0, ssbHideExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)
        val ssbShowExpl = SpannableStringBuilder(getString(R.string.show_explain))
        ssbShowExpl.setSafeSpan(UnderlineSpan(), 0, ssbShowExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)

        // Linearlayout
        val llHorizontal = LinearLayout(activity)
        llHorizontal.orientation = HORIZONTAL
        llHorizontal.layoutParams = getMatchWrapParams()

        val explainLink = TextView(activity)
        explainLink.tag = "explainLink"
        explainLink.layoutParams = getWrapWrapParams()
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainLink.setTextColor(ContextCompat.getColor(requireActivity(), R.color.linkBlue))
        //explainLink.setGravity(Gravity.CENTER_VERTICAL);

        var isExpanded = false
        explainLink.setOnClickListener { view ->
            val explView = (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
            if (isExpanded.not()) {
                (view as TextView).text = ssbHideExpl
                expandThis(explView, null)
                isExpanded = true

            } else if (isExpanded) {
                (view as TextView).text = ssbShowExpl
                collapseThis(explView)
                isExpanded = false
            }
        }

        llHorizontal.addView(explainLink)
        context?.let {
            val separator = getGradientSeparator(it, shouldShowPerformance, startTime, input, DivisoresFragment::class.java.simpleName)
            llHorizontal.addView(separator, 0)
        }
        //LL vertical das explicações
        val llVerticalExpl = LinearLayout(activity)
        llVerticalExpl.tag = "ll_vertical_expl"
        llVerticalExpl.layoutParams = getMatchWrapParams()
        llVerticalExpl.orientation = LinearLayout.VERTICAL

        //ProgressBar
        cvWidth = card_view_1.width
        heightDip = (3 * scale + 0.5f).toInt()
        val progressBar = View(activity)
        progressBar.tag = "progressBar"
        val layoutParams = LinearLayout.LayoutParams(1, heightDip) //Largura, Altura
        progressBar.layoutParams = layoutParams

        // Ponto 1
        val explaintextview1 = TextView(activity)
        explaintextview1.tag = "explainTextView_1"
        explaintextview1.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explaintextview1.setTextColor(ContextCompat.getColor(requireActivity(), R.color.boldColor))
        explaintextview1.text = HtmlCompat.fromHtml(getString(R.string.explainMDC1html), 0)
        explaintextview1.setTag(R.id.texto, "texto")

        // Ponto 2
        val explaintextview2 = TextView(activity)
        explaintextview2.tag = "explainTextView_2"
        explaintextview2.setTag(R.id.texto, "texto")
        explaintextview2.text = HtmlCompat.fromHtml(getString(R.string.explainMDC2html), 0)
        explaintextview2.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explaintextview2.setTextColor(ContextCompat.getColor(requireActivity(), R.color.boldColor))

        // Ponto 3
        val explaintextview3 = TextView(activity)
        explaintextview3.tag = "explainTextView_3"
        explaintextview3.setTag(R.id.texto, "texto")
        explaintextview3.text = HtmlCompat.fromHtml(getString(R.string.explainMDC3html), 0)
        explaintextview3.setTextColor(ContextCompat.getColor(requireActivity(), R.color.boldColor))
        explaintextview3.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)

        llVerticalExpl.addView(explaintextview1)
        llVerticalExpl.addView(explaintextview2)
        llVerticalExpl.addView(explaintextview3)
        llVerticalRoot.addView(llHorizontal)
        llVerticalRoot.addView(progressBar)
        llVerticalRoot.addView(llVerticalExpl)

        if (explanation == Explanations.Always) {  // Always show Explanation
            llVerticalExpl.visibility = View.VISIBLE
            explainLink.text = ssbHideExpl
            isExpanded = true
        } else if (explanation == Explanations.WhenAsked) { // Show Explanation on demand on click
            llVerticalExpl.visibility = View.GONE
            explainLink.text = ssbShowExpl
            isExpanded = false
        }

        cardView.addView(llVerticalRoot)

        val thisCardTags = cardView.tag as MyTags

        thisCardTags.taskNumber = taskNumber
        val asyncTask = BackGroundOperation_MDC(thisCardTags).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        asyncTaskQueue.add(asyncTask)
        taskNumber++
    }

    fun checkBackgroundOperation(view: View?) {
        val theTags = view?.tag as MyTags
        if (theTags.hasBGOperation == true) {
            val taskNumber = theTags.taskNumber
            val task = asyncTaskQueue[taskNumber]
            if (task?.status == AsyncTask.Status.RUNNING) {
                task.cancel(true)
                asyncTaskQueue[taskNumber] = null
                theTags.hasBGOperation = false
                showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
            }
        }
    }

    // Asynctask <Params, Progress, Result>
    inner class BackGroundOperation_MDC internal constructor(private var cardTags: MyTags) :
            AsyncTask<Void, Double, Void>() {
        private lateinit var theCardViewBG: CardView
        private lateinit var mdcNumbers: ArrayList<BigInteger>
        private var bgFactors: ArrayList<ArrayList<BigInteger>>? = null

        private lateinit var gradientSeparator: TextView
        private lateinit var progressBar: View
        private var percentFormatter: NumberFormat = DecimalFormat("#.###%")

        public override fun onPreExecute() {
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
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
            mdcNumbers = cardTags.bigNumbers
            val numbersSize = mdcNumbers.size
            for (i in 0 until numbersSize) { // fatorizar todos os números inseridos em MMC
                var oldProgress = 0.0
                var progress: Double
                val fatoresIx = ArrayList<BigInteger>()
                var numberI: BigInteger = mdcNumbers[i]

                fatoresIx.add(BigInteger.ONE)
                while (numberI % BigInteger.valueOf(2) == BigInteger.ZERO) {
                    fatoresIx.add(BigInteger.valueOf(2))
                    numberI /= BigInteger.valueOf(2)
                }
                var j = BigInteger.valueOf(3)
                while (j <= numberI / j) {
                    while (numberI % j == BigInteger.ZERO) {
                        fatoresIx.add(j)
                        numberI /= j
                    }
                    progress = j.toDouble() / (numberI.toDouble() / j.toDouble())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toDouble())
                        oldProgress = progress
                    }
                    if (isCancelled) break
                    j += BigInteger.valueOf(2)
                }
                if (numberI > BigInteger.ONE) {
                    fatoresIx.add(numberI)
                }
                fatores.add(fatoresIx)
            }
            cardTags.bGfatores = fatores
            return null
        }

        override fun onProgressUpdate(vararg values: Double?) {
            if (this@MDCFragment.isVisible) {
                val color = fColors[(values.getOrNull(1) ?: 0.0).roundToInt()]
                progressBar.setBackgroundColor(color)
                val value0 = (values[0] ?: 0.0).coerceAtMost(1.0)
                progressBar.layoutParams = LinearLayout.LayoutParams((value0 * cvWidth).roundToInt(), heightDip)
                val text = " " + getString(R.string.factorizing) + " " + percentFormatter.format(value0)
                val ssb = SpannableStringBuilder(text)
                ssb.setSafeSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                gradientSeparator.text = ssb
            }
        }

        override fun onPostExecute(result: Void?) {
            if (this@MDCFragment.isVisible) {
                bgFactors = cardTags.bGfatores
                val datasets = ArrayList<ArrayList<BigInteger>>()
                bgFactors?.forEachIndexed { outerIndex, outerList ->
                    val bases = ArrayList<BigInteger>()
                    val exps = ArrayList<BigInteger>()

                    val strFatores = mdcNumbers[outerIndex].toString() + "="
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
                        } else if (innerValue == lastItem && innerIndex > 0L) {
                            counter++
                            dataSet[innerValue] = counter
                            bases[nextFactor] = innerValue
                            exps[nextFactor] = counter
                        } else if (innerValue != lastItem && innerIndex > 0L) {
                            counter = BigInteger.ONE
                            nextFactor++
                            dataSet[innerValue] = counter
                            bases.add(innerValue)
                            exps.add(counter)
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

                        if (index in 1 until (dataCount - 1)) {
                            ssbFatores.append("×")
                        }
                    }
                    if (outerIndex < (bgFactors?.size ?: 0) - 1) ssbFatores.append("\n")

                    ssbFatores.setSafeSpan(StyleSpan(BOLD), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssbFatores.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbFatores.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    (theCardViewBG.findViewWithTag<View>("explainTextView_1") as TextView).append(ssbFatores)
                }

                val basesComuns = ArrayList<BigInteger>()
                val expsComuns = ArrayList<BigInteger>()
                val colors = ArrayList<Int>()
                val bases = datasets[0]
                val exps = datasets[1]
                currentBaseLoop@ for (cb in bases.indices) {
                    val currentBase = bases[cb]
                    val currentExp = exps[cb]
                    val tempBases = ArrayList<BigInteger>()
                    val tempExps = ArrayList<BigInteger>()
                    val tempColors = ArrayList<Long>()
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
                    ssbMdc.append(getString(R.string.no_common_factors))
                }

                ssbMdc.setSafeSpan(StyleSpan(BOLD), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMdc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                //explainTextView_2
                (theCardViewBG.findViewWithTag<View>("explainTextView_2") as TextView).append(ssbMdc)

                ssbMdc.delete(0, ssbMdc.length)
                val resultMdc = cardTags.resultMDC ?: return
                ssbMdc.append(resultMdc.toString())

                ssbMdc.setSafeSpan(StyleSpan(BOLD), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMdc.setSafeSpan(RelativeSizeSpan(0.9f), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssbMdc.setSafeSpan(ForegroundColorSpan(fColors.last()), 0, ssbMdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)

                //explainTextView_3
                (theCardViewBG.findViewWithTag<View>("explainTextView_3") as TextView).append(ssbMdc)

                progressBar.visibility = View.GONE

                if (shouldShowPerformance) {
                    val decimalFormatter = DecimalFormat("#.###")
                    val elapsed = getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                    gradientSeparator.text = elapsed
                } else {
                    gradientSeparator.text = ""
                }

                cardTags.hasBGOperation = false
                cardTags.hasExplanation = true
                asyncTaskQueue[cardTags.taskNumber] = null

                datasets.clear()
                bgFactors?.clear()
            }
        }
    }

    /*****************************************************************
     * MMC: Mínimo múltiplo comum (lcm: least common multiplier)
     */
    //    private static long mmc(long a, long b) {
    //        return (b / mdc(a, b)) * a;
    //    }
    //    private static BigInteger mmc(BigInteger a, BigInteger b) {
    //        return b.divide(a.gcd(b)).multiply(a);
    //    }
    //
    //    private static BigInteger mmc(ArrayList<BigInteger> input) {
    //        BigInteger result = input.get(0);
    //        for (int i = 1; i < input.size(); i++)
    //            result = mmc(result, input.get(i));
    //        return result;
    //    }
    //
    //    /****************************************************************
    //     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
    //     *****************************************************************/
    //    private static BigInteger mdc(BigInteger a, BigInteger b) {
    //        while (b.compareTo(ZERO) == 1) {
    //            BigInteger temp = b;
    //            b = a.remainder(b);
    //            a = temp;
    //        }
    //        return a;
    //    }
    private fun mdc(input: ArrayList<BigInteger>): BigInteger {
        var result = input[0]
        for (i in 1 until input.size)
            result = result.gcd(input[i])
        return result
    }
}



