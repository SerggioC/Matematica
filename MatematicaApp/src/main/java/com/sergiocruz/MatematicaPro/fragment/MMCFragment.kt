package com.sergiocruz.MatematicaPro.fragment

import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGE_APPEARING
import android.animation.LayoutTransition.CHANGE_DISAPPEARING
import android.app.Activity
import android.graphics.Typeface.BOLD
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.*
import android.text.TextUtils
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
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.expandIt
import com.sergiocruz.MatematicaPro.model.MyTags
import kotlinx.android.synthetic.main.fragment_mmc.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class MMCFragment : BaseFragment(), OnEditorActions {
    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    private var taskNumber = 0
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

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

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

        clearButton1.setOnClickListener { mmc_num_1.setText("") }
        clearButton2.setOnClickListener { mmc_num_2.setText("") }
        clearButton3.setOnClickListener { mmc_num_3.setText("") }
        clearButton4.setOnClickListener { mmc_num_4.setText("") }
        clearButton5.setOnClickListener { mmc_num_5.setText("") }
        clearButton6.setOnClickListener { mmc_num_6.setText("") }
        clearButton7.setOnClickListener { mmc_num_7.setText("") }
        clearButton8.setOnClickListener { mmc_num_8.setText("") }

        button_add_mmc.setOnClickListener { addMMC() }
        button_remove_mmc.setOnClickListener { removeMMC() }

        arrayOfEditTexts.forEach {
            it.addTextChangedListener(BigNumbersTextWatcher(it, shouldFormatNumbers, this))
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
        val longNumbers = ArrayList<Long>()

        arrayOfEditTexts.forEach {
            val numString = it.text.digitsOnly()
            if (TextUtils.isEmpty(numString)) {
                emptyTextView.add(it)
            } else {
                val number = numString.toLongOrNull()
                when (number) {
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
                    else -> {
                        longNumbers.add(number)
                        bigNumbers.add(number.toBigInteger())
                    }

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

        var mmcString = getString(R.string.mmc_result_prefix)
        var resultMMC: BigInteger? = null

        if (bigNumbers.size > 1) {
            for (i in 0 until bigNumbers.size - 1) {
                mmcString += "${bigNumbers[i]}, "
            }
            mmcString += "${bigNumbers[bigNumbers.size - 1]})= "
            resultMMC = mmc(bigNumbers)
        }

        mmcString += resultMMC

        //criar novo cardview
        val cardView = ClickableCardView(activity as Activity)
        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true
        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f)
        cardView.cardElevation = (2 * scale + 0.5f)
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
            cardView.layoutTransition = lt
        }

        val cvColor = ContextCompat.getColor(requireContext(), R.color.cardsColor)
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
                })
        )

        // Adicionar os números a fatorizar na tag do cardview
        cardView.tag = MyTags(cardView, longNumbers, resultMMC, false, false, "", null, taskNumber)

        history.limit(historyLimit)
        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()

        //Adicionar o texto com o resultado
        textView.text = mmcString
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        textView.setTag(R.id.texto, "texto")

        // add the textview to the cardview
        llVerticalRoot.addView(textView)

        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {
            createExplanations(mmcString, cardView, llVerticalRoot, shouldShowExplanation)
        } else {
            context?.let {
                val separator = getGradientSeparator(it, shouldShowPerformance, startTime, mmcString, DivisoresFragment::class.java.simpleName)
                llVerticalRoot.addView(separator, 0)
            }
            cardView.addView(llVerticalRoot)
        }

    }

    private fun createExplanations(
            mmcString: String,
            cardview: CardView,
            llVerticalRoot: LinearLayout,
            shouldShowExplanation: String?
    ) {

        val hideExpl = SpannableStringBuilder(getString(R.string.hide_explain))
        hideExpl.setSafeSpan(UnderlineSpan(), 0, hideExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)
        val showExpl = SpannableStringBuilder(getString(R.string.show_explain))
        showExpl.setSafeSpan(UnderlineSpan(), 0, showExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)

        //Linearlayout horizontal com o explainlink e gradiente
        val llHorizontal = LinearLayout(activity)
        llHorizontal.orientation = HORIZONTAL
        llHorizontal.layoutParams = getMatchWrapParams()
        val explainLink = TextView(activity)
        explainLink.tag = "explainLink"
        explainLink.layoutParams = getWrapWrapParams()
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainLink.setTextColor(ContextCompat.getColor(requireActivity(), R.color.linkBlue))
        explainLink.gravity = Gravity.CENTER_VERTICAL

        llHorizontal.gravity = Gravity.CENTER_VERTICAL

        var isExpanded = false
        explainLink.setOnClickListener { view ->
            val explView =
                (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
            if (!isExpanded) {
                (view as TextView).text = hideExpl
                expandIt(explView, null)
                isExpanded = true

            } else if (isExpanded) {
                (view as TextView).text = showExpl
                collapseIt(explView)
                isExpanded = false
            }
        }

        llHorizontal.addView(explainLink)

        context?.let {
            val separator = getGradientSeparator(it, shouldShowPerformance, startTime, mmcString, DivisoresFragment::class.java.simpleName)
            llVerticalRoot.addView(separator)
        }

        // LL vertical das explicações
        val verticalExpl = LinearLayout(activity)
        verticalExpl.tag = "ll_vertical_expl"
        verticalExpl.layoutParams = getMatchWrapParams()
        verticalExpl.orientation = LinearLayout.VERTICAL
        verticalExpl.layoutTransition = LayoutTransition()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
            verticalExpl.layoutTransition = lt
        }

        // ProgressBar
        val heightDp = (3 * scale + 0.5f).toInt()
        val progressBar = View(activity)
        progressBar.tag = "progressBar"
        val layoutParams = LinearLayout.LayoutParams(1, heightDp) //Largura, Altura
        progressBar.layoutParams = layoutParams


        // Ponto 1
        val explainTextView1 = requireActivity().getNewTextView("explainTextView_1", R.string.explainMMC1html)
        val explainTextView2 = requireActivity().getNewTextView("explainTextView_2", R.string.explainMMC2html)
        val explaintextView3 = requireActivity().getNewTextView("explainTextView_3", R.string.explainMMC3html)

        verticalExpl.addView(explainTextView1)
        verticalExpl.addView(explainTextView2)
        verticalExpl.addView(explaintextView3)

        llVerticalRoot.addView(llHorizontal)
        llVerticalRoot.addView(progressBar)
        llVerticalRoot.addView(verticalExpl)

        if (shouldShowExplanation == "-1") {  //Always show Explanation
            verticalExpl.visibility = View.VISIBLE
            explainLink.text = hideExpl
            isExpanded = true
        } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
            verticalExpl.visibility = View.GONE
            explainLink.text = showExpl
            isExpanded = false
        }
        cardview.addView(llVerticalRoot)

        val thisCardTags = cardview.tag as MyTags

        thisCardTags.taskNumber = taskNumber
        val asyncTask = BackGroundOperationMMC(thisCardTags)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
                showCustomToast(context, getString(R.string.canceled_op))
            }
        }
    }

    private var fColors: MutableList<Int> = mutableListOf()

    // Asynctask <Params, Progress, Result>
    inner class BackGroundOperationMMC internal constructor(private var cardTags: MyTags) :
        AsyncTask<Void, Float, Void>() {
        private lateinit var theCardViewBG: CardView
        private lateinit var mmcNumbers: ArrayList<Long>
        private lateinit var bgfatores: ArrayList<ArrayList<Long>>

        private lateinit var gradientSeparator: TextView
        lateinit var progressBar: View
        private lateinit var percentFormatter: NumberFormat

        private lateinit var progressParams: ViewGroup.LayoutParams

        public override fun onPreExecute() {
            percentFormatter = DecimalFormat("#.###%")
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
            progressParams = progressBar.layoutParams
            gradientSeparator =
                theCardViewBG.findViewWithTag<View>("gradient_separator") as TextView
            cardTags.hasBGOperation = true

            fColors = getRandomFactorsColors()

            val text = " " + getString(R.string.factorizing) + " 0%"
            val ssb = SpannableStringBuilder(text)
            ssb.setSafeSpan(ForegroundColorSpan(fColors[0]), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            gradientSeparator.text = ssb
        }

        override fun doInBackground(vararg voids: Void): Void? {
            val fatores = ArrayList<ArrayList<Long>>()
            mmcNumbers = cardTags.longNumbers

            val numbersSize = mmcNumbers.size
            for (i in 0 until numbersSize) { // fatorizar todos os números inseridos em MMC
                var oldProgress = 0.0f
                var progress: Float
                val fatoresIx = ArrayList<Long>()

                var number: Long = mmcNumbers[i]
                if (number == 1L) {
                    fatoresIx.add(1L)
                }
                while (number % 2L == 0L) {
                    fatoresIx.add(2L)
                    number /= 2L
                }

                var j: Long = 3
                while (j <= number / j) {
                    if (isCancelled) break
                    while (number % j == 0L) {
                        fatoresIx.add(j)
                        number /= j
                    }
                    progress = j.toFloat() / (number.toFloat() / j.toFloat())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toFloat())
                        oldProgress = progress
                    }
                    j += 2
                }
                if (number > 1) {
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
            if (this@MMCFragment.isVisible) {

                bgfatores = cardTags.bGfatores!!

                val datasets = ArrayList<ArrayList<Long>>()

                for (k in bgfatores.indices) {
                    val bases = ArrayList<Long>()
                    val exps = ArrayList<Long>()

                    val strFatores = mmcNumbers[k].toString() + "="
                    val ssbFatores: SpannableStringBuilder
                    ssbFatores = SpannableStringBuilder(strFatores)
                    ssbFatores.setSafeSpan(ForegroundColorSpan(fColors[k]), 0, ssbFatores.length, SPAN_EXCLUSIVE_INCLUSIVE)

                    var counter = 1
                    var nextFactor = 0
                    var lastItem: Long? = bgfatores[k][0]

                    //TreeMap
                    val dataset = LinkedHashMap<String, Int>()

                    //Contar os expoentes  (sem comentários....)
                    for (i in 0 until bgfatores[k].size) {
                        if (i == 0) {
                            dataset[bgfatores[k][0].toString()] = 1
                            bases.add(bgfatores[k][0])
                            exps.add(1L)
                        } else if (bgfatores[k][i] == lastItem && i > 0) {
                            counter++
                            dataset[bgfatores[k][i].toString()] = counter
                            bases[nextFactor] = bgfatores[k][i]
                            exps[nextFactor] = counter.toLong()
                        } else if (bgfatores[k][i] != lastItem && i > 0) {
                            counter = 1
                            nextFactor++
                            dataset[bgfatores[k][i].toString()] = counter
                            bases.add(bgfatores[k][i])
                            exps.add(counter.toLong())
                        }
                        lastItem = bgfatores[k][i]
                    }

                    datasets.add(bases)
                    datasets.add(exps)

                    //Criar os expoentes
                    var valueLength: Int
                    val iterator = dataset.entries.iterator()
                    while (iterator.hasNext()) {
                        val pair = iterator.next() as Map.Entry<*, *>

                        if (Integer.parseInt(pair.value.toString()) == 1) {
                            //Expoente 1
                            ssbFatores.append(pair.key.toString())

                        } else if (Integer.parseInt(pair.value.toString()) > 1) {
                            //Expoente superior a 1
                            valueLength = pair.value.toString().length
                            ssbFatores.append(pair.key.toString() + pair.value.toString())
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
                    (theCardViewBG.findViewWithTag<View>("explainTextView_1") as TextView).append(
                        ssbFatores
                    )

                }

                val maioresBases = ArrayList<Long>()
                val maioresExps = ArrayList<Long>()
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

                            if (maioresBases.contains(currentBase) && currentExp > maioresExps[maioresBases.indexOf(
                                    currentBase
                                )]
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

                    if (maioresExps[i] == 1L) {
                        //Expoente 1
                        ssbMmc.append(maioresBases[i].toString())
                        ssbMmc.setSafeSpan(
                            ForegroundColorSpan(fColors[colors[i].toInt()]),
                            ssbMmc.length - baseLength, ssbMmc.length, SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                    } else if (maioresExps[i] > 1L) {
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