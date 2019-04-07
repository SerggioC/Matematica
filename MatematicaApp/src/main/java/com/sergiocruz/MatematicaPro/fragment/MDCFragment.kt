package com.sergiocruz.MatematicaPro.fragment

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
import com.sergiocruz.MatematicaPro.MyTags
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.expandIt
import kotlinx.android.synthetic.main.fragment_mdc.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class MDCFragment : BaseFragment(), OnEditorActions,
    SharedPreferences.OnSharedPreferenceChangeListener {

    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()

    internal lateinit var fColors: ArrayList<Int>
    internal var heightDip: Int = 0
    internal var cvWidth: Int = 0
    private var taskNumber = 0
    internal var startTime: Long = 0
    private lateinit var language: String
    private lateinit var arrayOfEditTexts: Array<EditText>
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     */
    //private final static BigInteger mdc2(BigInteger a, BigInteger b) {
    //    return b.compareTo(ZERO) == 1 ? a : mdc(b, a.remainder(b));
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fColors = resources.getIntArray(R.array.f_colors_xml)
        this.fColors = ArrayList()
        for (i in fColors.indices) {
            this.fColors.add(fColors[i])
        }
        language = Locale.getDefault().displayLanguage
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_mdc

    override fun getHelpTextId(): Int? = R.string.help_text_mdc

    override fun getHelpMenuTitleId(): Int? = R.string.action_ajuda_mdc

    override fun getHistoryLayout(): LinearLayout? = history

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun onActionDone() = calculateMDC()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayOfEditTexts = arrayOf(
            mdc_num_1,
            mdc_num_2,
            mdc_num_3,
            mdc_num_4,
            mdc_num_5,
            mdc_num_6,
            mdc_num_7,
            mdc_num_8
        )

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

        arrayOfEditTexts.forEach { it.watchThis(this) }

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
        val ll_34_visibe = linear_layout_34.visibility == View.VISIBLE
        val f3_visible = frame_3.visibility == View.VISIBLE
        val f4_visible = frame_4.visibility == View.VISIBLE
        val linear_layout_56_visibe = linear_layout_56.visibility == View.VISIBLE
        val f5_visible = frame_5.visibility == View.VISIBLE
        val f6_visible = frame_6.visibility == View.VISIBLE
        val linear_layout_78_visibe = linear_layout_78.visibility == View.VISIBLE
        val f7_visible = frame_7.visibility == View.VISIBLE
        val f8_visible = frame_8.visibility == View.VISIBLE


        if (!ll_34_visibe || f3_visible || f4_visible) {
            linear_layout_34.visibility = View.VISIBLE

            if (!f3_visible) {
                frame_3.visibility = View.VISIBLE
                button_remove_mdc.visibility = View.VISIBLE
                mdc_num_2.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f4_visible) {
                frame_4.visibility = View.VISIBLE
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (!linear_layout_56_visibe || f5_visible || f6_visible) {
            linear_layout_56.visibility = View.VISIBLE

            if (!f5_visible) {
                frame_5.visibility = View.VISIBLE
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f6_visible) {
                frame_6.visibility = View.VISIBLE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }
        if (!linear_layout_78_visibe || f7_visible || f8_visible) {
            linear_layout_78.visibility = View.VISIBLE

            if (!f7_visible) {
                frame_7.visibility = View.VISIBLE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f8_visible) {
                frame_8.visibility = View.VISIBLE
                button_add_mdc.visibility = View.INVISIBLE
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

    }

    private fun removeMdcField() {
        val ll_34_visibe = linear_layout_34.visibility == View.VISIBLE
        val f3_visible = frame_3.visibility == View.VISIBLE
        val f4_visible = frame_4.visibility == View.VISIBLE
        val linear_layout_56_visibe = linear_layout_56.visibility == View.VISIBLE
        val f5_visible = frame_5.visibility == View.VISIBLE
        val f6_visible = frame_6.visibility == View.VISIBLE
        val linear_layout_78_visibe = linear_layout_78.visibility == View.VISIBLE
        val f7_visible = frame_7.visibility == View.VISIBLE
        val f8_visible = frame_8.visibility == View.VISIBLE

        if (linear_layout_78_visibe) {
            if (f8_visible) {
                mdc_num_8.setText("")
                frame_8.visibility = View.GONE
                button_add_mdc.visibility = View.VISIBLE
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f7_visible) {
                mdc_num_7.setText("")
                frame_7.visibility = View.GONE
                linear_layout_78.visibility = View.GONE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (linear_layout_56_visibe) {
            if (f6_visible) {
                mdc_num_6.setText("")
                frame_6.visibility = View.GONE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f5_visible) {
                mdc_num_5.setText("")
                frame_5.visibility = View.GONE
                linear_layout_56.visibility = View.GONE
                mdc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mdc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll_34_visibe) {
            if (f4_visible) {
                mdc_num_4.setText("")
                frame_4.visibility = View.GONE
                mdc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                mdc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f3_visible) {
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
        val longNumbers = ArrayList<Long>()

        arrayOfEditTexts.forEach {
            val numString = it.text.toString().replace("[^\\d]".toRegex(), "")
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
        val ssb = SpannableStringBuilder(mdcString)
        if (resultMDC.toString() == "1") {
            ssb.append("\n" + getString(R.string.primos_si))
            ssb.setSpan(
                ForegroundColorSpan(Color.parseColor("#29712d")),
                ssb.length - 24,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                RelativeSizeSpan(0.9f),
                ssb.length - 24,
                ssb.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //criar novo cardview
        val cardview = ClickableCardView(context!!)
        cardview.layoutParams = getMatchWrapParams()
        cardview.preventCornerOverlap = true
        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardview.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardview.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardview.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardview.useCompatPadding = true
        cardview.layoutTransition = LayoutTransition()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
        }

        val cvColor = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardview.setCardBackgroundColor(cvColor)

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(
            SwipeToDismissTouchListener(
                cardview,
                activity as Activity,
                object : SwipeToDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Boolean?): Boolean {
                        return true
                    }

                    override fun onDismiss(view: View?) {
                        //history.removeView(cardview);
                        checkBackgroundOperation(view)
                    }
                })
        )

        val tags = MyTags(cardview, longNumbers, resultMDC, false, false, "", null, taskNumber)
        cardview.tag = tags

        history.limit(historyLimit)
        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0)

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

        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {
            createExplanations(cardview, llVerticalRoot, shouldShowExplanation)
        } else {
            if (shouldShowPerformance) {
                val gradientSeparator = getGradientSeparator(context)
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
                llVerticalRoot.addView(gradientSeparator, 0)
            }
            cardview.addView(llVerticalRoot) //Só o resultado sem explicações
        }
    }

    private fun createExplanations(
        cardview: CardView,
        llVerticalRoot: LinearLayout,
        shouldShowExplanation: String?
    ) {
        val ssb_hide_expl = SpannableStringBuilder(getString(R.string.hide_explain))
        ssb_hide_expl.setSpan(
            UnderlineSpan(),
            0,
            ssb_hide_expl.length - 2,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val ssb_show_expl = SpannableStringBuilder(getString(R.string.explain))
        ssb_show_expl.setSpan(
            UnderlineSpan(),
            0,
            ssb_show_expl.length - 2,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )

        //Linearlayout
        val ll_horizontal = LinearLayout(activity)
        ll_horizontal.orientation = HORIZONTAL
        ll_horizontal.layoutParams = getMatchWrapParams()

        val explainLink = TextView(activity)
        explainLink.tag = "explainLink"
        explainLink.layoutParams = getWrapWrapParams()
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainLink.setTextColor(ContextCompat.getColor(activity!!, R.color.linkBlue))
        //explainLink.setGravity(Gravity.CENTER_VERTICAL);

        //View separator with gradient
        val gradientSeparator = getGradientSeparator(context)

        val isExpanded = arrayOf(false)
        explainLink.setOnClickListener { view ->
            val explView =
                (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
            if (!isExpanded[0]) {
                (view as TextView).text = ssb_hide_expl
                expandIt(explView, null)
                isExpanded[0] = true

            } else if (isExpanded[0]) {
                (view as TextView).text = ssb_show_expl
                collapseIt(explView)
                isExpanded[0] = false
            }
        }

        ll_horizontal.addView(explainLink)
        ll_horizontal.addView(gradientSeparator)

        //LL vertical das explicações
        val ll_vertical_expl = LinearLayout(activity)
        ll_vertical_expl.tag = "ll_vertical_expl"
        ll_vertical_expl.layoutParams = getMatchWrapParams()
        ll_vertical_expl.orientation = LinearLayout.VERTICAL

        //ProgressBar
        cvWidth = card_view_1.width
        heightDip = (3 * scale + 0.5f).toInt()
        val progressBar = View(activity)
        progressBar.tag = "progressBar"
        val layoutParams = LinearLayout.LayoutParams(1, heightDip) //Largura, Altura
        progressBar.layoutParams = layoutParams

        //Ponto 1
        val explainTextView_1 = TextView(activity)
        explainTextView_1.tag = "explainTextView_1"
        val fp = getString(R.string.fatores_primos)
        val explain_text_1 = getString(R.string.decompor_num) + " " + fp + "\n"
        val ssb_explain_1 = SpannableStringBuilder(explain_text_1)
        //ssb_explain_1.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_1.setSpan(
            UnderlineSpan(),
            explain_text_1.length - fp.length - 1,
            explain_text_1.length - 1,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_1.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    activity!!,
                    R.color.boldColor
                )
            ), 0, ssb_explain_1.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainTextView_1.text = ssb_explain_1
        explainTextView_1.setTag(R.id.texto, "texto")

        //Ponto 2
        val explainTextView_2 = TextView(activity)
        explainTextView_2.tag = "explainTextView_2"
        val comuns = getString(R.string.comuns)
        val uma_vez = getString(R.string.uma_vez)
        val menor_exps = getString(R.string.menor_exps)
        val explain_text_2: String
        if (language == "português" || language == "español" || language == "français") {
            explain_text_2 = getString(R.string.escolher) + " " + getString(R.string.os_fatores) +
                    " " + comuns + ", " + uma_vez + ", " + getString(R.string.with_the) + " " +
                    menor_exps + ":\n"
        } else {
            explain_text_2 = getString(R.string.escolher) + " " + comuns + " " +
                    getString(R.string.os_fatores) + ", " + uma_vez + ", " +
                    getString(R.string.with_the) + " " + menor_exps + ":\n"
        }
        val ssb_explain_2 = SpannableStringBuilder(explain_text_2)
        //ssb_explain_2.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(
            UnderlineSpan(),
            explain_text_2.indexOf(comuns),
            explain_text_2.indexOf(comuns) + comuns.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_2.setSpan(
            UnderlineSpan(),
            explain_text_2.indexOf(uma_vez),
            explain_text_2.indexOf(uma_vez) + uma_vez.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_2.setSpan(
            UnderlineSpan(),
            explain_text_2.indexOf(menor_exps),
            explain_text_2.indexOf(menor_exps) + menor_exps.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    activity!!,
                    R.color.boldColor
                )
            ), 0, ssb_explain_2.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainTextView_2.text = ssb_explain_2
        explainTextView_2.setTag(R.id.texto, "texto")

        //Ponto 3
        val explainTextView_3 = TextView(activity)
        explainTextView_3.tag = "explainTextView_3"
        val multipl = getString(R.string.multiply)
        val explain_text_3 = multipl + " " + getString(R.string.to_obtain_mdc) + "\n"
        val ssb_explain_3 = SpannableStringBuilder(explain_text_3)
        ssb_explain_3.setSpan(
            UnderlineSpan(),
            explain_text_3.indexOf(multipl) + 1,
            explain_text_3.indexOf(multipl) + multipl.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_3.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    activity!!,
                    R.color.boldColor
                )
            ), 0, ssb_explain_3.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //ssb_explain_3.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_3.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainTextView_3.text = ssb_explain_3
        explainTextView_3.setTag(R.id.texto, "texto")

        ll_vertical_expl.addView(explainTextView_1)
        ll_vertical_expl.addView(explainTextView_2)
        ll_vertical_expl.addView(explainTextView_3)
        llVerticalRoot.addView(ll_horizontal)
        llVerticalRoot.addView(progressBar)
        llVerticalRoot.addView(ll_vertical_expl)

        if (shouldShowExplanation == "-1") {  //Always show Explanation
            ll_vertical_expl.visibility = View.VISIBLE
            explainLink.text = ssb_hide_expl
            isExpanded[0] = true
        } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
            ll_vertical_expl.visibility = View.GONE
            explainLink.text = ssb_show_expl
            isExpanded[0] = false
        }

        cardview.addView(llVerticalRoot)

        val thisCardTags = cardview.tag as MyTags

        thisCardTags.taskNumber = taskNumber
        val asyncTask = BackGroundOperation_MDC(thisCardTags)
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
                showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
            }
        }
    }

    // Asynctask <Params, Progress, Result>
    inner class BackGroundOperation_MDC internal constructor(private var cardTags: MyTags) :
        AsyncTask<Void, Double, Void>() {
        private lateinit var theCardViewBG: CardView
        private lateinit var mdcNumbers: ArrayList<Long>
        private lateinit var result_mdc: BigInteger
        private lateinit var bgfatores: ArrayList<ArrayList<Long>>

        private lateinit var gradient_separator: TextView
        private lateinit var progressBar: View
        private lateinit var percent_formatter: NumberFormat
        private lateinit var f_colors: IntArray
        private var f_colors_length: Int = 0

        public override fun onPreExecute() {
            percent_formatter = DecimalFormat("#.###%")
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
            gradient_separator =
                theCardViewBG.findViewWithTag<View>("gradient_separator") as TextView
            cardTags.hasBGOperation = true

            f_colors = resources.getIntArray(R.array.f_colors_xml)
            f_colors_length = f_colors.size
            fColors = ArrayList()
            if (shouldShowColors) {
                for (i in 0 until f_colors_length) fColors.add(f_colors[i])
                fColors.shuffle() //randomizar as cores
            } else {
                for (i in 0 until f_colors_length) fColors.add(f_colors[f_colors_length - 1])
            }

            val text = " " + getString(R.string.factorizing) + " 0%"
            val ssb = SpannableStringBuilder(text)
            ssb.setSpan(ForegroundColorSpan(fColors[0]), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            gradient_separator.text = ssb
        }

        override fun doInBackground(vararg voids: Void): Void? {
            val fatores = ArrayList<ArrayList<Long>>()
            mdcNumbers = cardTags.longNumbers
            val numbersSize = mdcNumbers.size
            for (i in 0 until numbersSize) { // fatorizar todos os números inseridos em MMC
                var oldProgress = 0.0
                var progress: Double
                val fatores_ix = ArrayList<Long>()
                var number_i: Long? = mdcNumbers[i]
                //if (number_i == 1L) { //adicionar o fator 1 para calibrar em baixo a contagem....
                fatores_ix.add(1L)
                //}
                while (number_i!! % 2L == 0L) {
                    fatores_ix.add(2L)
                    number_i /= 2L
                }
                var j: Long = 3
                while (j <= number_i / j) {
                    while (number_i % j == 0L) {
                        fatores_ix.add(j)
                        number_i /= j
                    }
                    progress = j.toDouble() / (number_i.toDouble() / j.toDouble())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toDouble())
                        oldProgress = progress
                    }
                    if (isCancelled) break
                    j += 2
                }
                if (number_i > 1) {
                    fatores_ix.add(number_i)
                }
                fatores.add(fatores_ix)
            }
            cardTags.bGfatores = fatores
            return null
        }

        override fun onProgressUpdate(vararg values: Double?) {
            if (this@MDCFragment.isVisible) {
                val color = fColors[Math.round(values[1]!!).toInt()]
                progressBar.setBackgroundColor(color)
                var value0 = values[0]
                if (value0!! > 1f) value0 = 1.0
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(Math.round(value0 * cvWidth).toInt(), heightDip)
                val text =
                    " " + getString(R.string.factorizing) + " " + percent_formatter.format(value0)
                val ssb = SpannableStringBuilder(text)
                ssb.setSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                gradient_separator.text = ssb
            }
        }

        override fun onPostExecute(result: Void?) {
            if (this@MDCFragment.isVisible) {
                bgfatores = cardTags.bGfatores!!
                val datasets = ArrayList<ArrayList<Long>>()

                for (k in bgfatores.indices) {
                    val bases = ArrayList<Long>()
                    val exps = ArrayList<Long>()

                    val str_fatores = mdcNumbers[k].toString() + "="
                    val ssb_fatores = SpannableStringBuilder(str_fatores)
                    ssb_fatores.setSpan(
                        ForegroundColorSpan(fColors[k]),
                        0,
                        ssb_fatores.length,
                        SPAN_EXCLUSIVE_INCLUSIVE
                    )

                    var counter = 1
                    var nextFactor = 0
                    var lastItem: Long = bgfatores[k][0]

                    //TreeMap
                    val dataSet = LinkedHashMap<String, Int>()

                    //Contar os expoentes  (sem comentários....)
                    for (i in 0 until bgfatores[k].size) {
                        if (i == 0) {
                            dataSet[bgfatores[k][0].toString()] = 1
                            bases.add(bgfatores[k][0])
                            exps.add(1L)
                        } else if (bgfatores[k][i] == lastItem && i > 0) {
                            counter++
                            dataSet[bgfatores[k][i].toString()] = counter
                            bases[nextFactor] = bgfatores[k][i]
                            exps[nextFactor] = counter.toLong()
                        } else if (bgfatores[k][i] != lastItem && i > 0) {
                            counter = 1
                            nextFactor++
                            dataSet[bgfatores[k][i].toString()] = counter
                            bases.add(bgfatores[k][i])
                            exps.add(counter.toLong())
                        }
                        lastItem = bgfatores[k][i]
                    }

                    datasets.add(bases)
                    datasets.add(exps)

                    //Criar os expoentes
                    var valueLength: Int
                    val iterator = dataSet.entries.iterator()
                    while (iterator.hasNext()) {
                        val pair = iterator.next() as Map.Entry<*, *>

                        if (Integer.parseInt(pair.value.toString()) == 1) {
                            //Expoente 1
                            ssb_fatores.append(pair.key.toString())

                        } else if (Integer.parseInt(pair.value.toString()) > 1) {
                            //Expoente superior a 1
                            valueLength = pair.value.toString().length
                            ssb_fatores.append(pair.key.toString() + pair.value.toString())
                            ssb_fatores.setSpan(
                                SuperscriptSpan(),
                                ssb_fatores.length - valueLength,
                                ssb_fatores.length,
                                SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            ssb_fatores.setSpan(
                                RelativeSizeSpan(0.8f),
                                ssb_fatores.length - valueLength,
                                ssb_fatores.length,
                                SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        if (iterator.hasNext()) {
                            ssb_fatores.append("×")
                        }

                        iterator.remove() // avoids a ConcurrentModificationException
                    }
                    if (k < bgfatores.size - 1) ssb_fatores.append("\n")

                    ssb_fatores.setSpan(
                        StyleSpan(BOLD),
                        0,
                        ssb_fatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ssb_fatores.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        ssb_fatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    //explainTextView_1;
                    (theCardViewBG.findViewWithTag<View>("explainTextView_1") as TextView).append(
                        ssb_fatores
                    )
                }

                val bases_comuns = ArrayList<Long>()
                val exps_comuns = ArrayList<Long>()
                val colors = ArrayList<Int>()
                val bases = datasets[0]
                val exps = datasets[1]
                currentBaseLoop@ for (cb in bases.indices) {
                    val current_base = bases[cb]
                    val current_exp = exps[cb]
                    val temp_bases = ArrayList<Long>()
                    val temp_exps = ArrayList<Long>()
                    val temp_colors = ArrayList<Long>()
                    temp_bases.add(current_base)
                    temp_exps.add(current_exp)
                    temp_colors.add(0)
                    var j = 2
                    nextBasesLoop@ while (j < datasets.size) {
                        val next_bases = datasets[j]
                        if (!next_bases.contains(current_base)) {
                            break@nextBasesLoop
                        }
                        val next_exps = datasets[j + 1]
                        innerLoop@ for (nb in next_bases.indices) {
                            val next_base = next_bases[nb]
                            val next_exp = next_exps[nb]
                            if (next_base == current_base) {
                                temp_bases.add(next_base)
                                temp_exps.add(next_exp)
                                temp_colors.add((j / 2).toLong())
                            }
                        }
                        j += 2
                    }
                    var lower_exp: Long = temp_exps.get(0)
                    var lowerIndex = 0
                    if (Collections.frequency(temp_bases, current_base) == datasets.size / 2) {
                        for (i in temp_exps.indices) {
                            if (temp_exps[i] < lower_exp) {
                                lower_exp = temp_exps[i]
                                lowerIndex = i
                            }
                        }
                        bases_comuns.add(temp_bases[lowerIndex])
                        exps_comuns.add(lower_exp)
                        colors.add(temp_colors[lowerIndex].toInt())
                    }
                }

                val ssb_mdc = SpannableStringBuilder()

                //Criar os expoentes do MDC com os maiores fatores com cores e a negrito
                for (i in bases_comuns.indices) {
                    val base_length = bases_comuns[i].toString().length

                    if (exps_comuns[i] == 1L) {
                        //Expoente 1
                        ssb_mdc.append(bases_comuns[i].toString())
                        ssb_mdc.setSpan(
                            ForegroundColorSpan(fColors[colors[i]]),
                            ssb_mdc.length - base_length, ssb_mdc.length, SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        //ssb_mdc.setSpan(new StyleSpan(Typeface.BOLD), ssb_mdc.length() - base_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else if (exps_comuns[i] > 1L) {
                        //Expoente superior a 1
                        val exp_length = exps_comuns[i].toString().length
                        ssb_mdc.append(bases_comuns[i].toString() + exps_comuns[i].toString())
                        ssb_mdc.setSpan(
                            SuperscriptSpan(),
                            ssb_mdc.length - exp_length,
                            ssb_mdc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        ssb_mdc.setSpan(
                            RelativeSizeSpan(0.8f),
                            ssb_mdc.length - exp_length,
                            ssb_mdc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        ssb_mdc.setSpan(
                            ForegroundColorSpan(fColors[colors[i]]),
                            ssb_mdc.length - exp_length - base_length,
                            ssb_mdc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    ssb_mdc.append("×")
                }
                ssb_mdc.replace(ssb_mdc.length - 1, ssb_mdc.length, "")

                ssb_mdc.setSpan(StyleSpan(BOLD), 0, ssb_mdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mdc.setSpan(RelativeSizeSpan(0.9f), 0, ssb_mdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                //explainTextView_2
                (theCardViewBG.findViewWithTag<View>("explainTextView_2") as TextView).append(
                    ssb_mdc
                )

                ssb_mdc.delete(0, ssb_mdc.length)
                result_mdc = cardTags.resultMDC!!
                ssb_mdc.append(result_mdc.toString())

                ssb_mdc.setSpan(StyleSpan(BOLD), 0, ssb_mdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mdc.setSpan(RelativeSizeSpan(0.9f), 0, ssb_mdc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mdc.setSpan(
                    ForegroundColorSpan(f_colors[f_colors.size - 1]),
                    0,
                    ssb_mdc.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )

                //explainTextView_3
                (theCardViewBG.findViewWithTag<View>("explainTextView_3") as TextView).append(
                    ssb_mdc
                )

                progressBar.visibility = View.GONE

                if (shouldShowPerformance) {
                    val decimalFormatter = DecimalFormat("#.###")
                    val elapsed =
                        getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                    gradient_separator.text = elapsed
                } else {
                    gradient_separator.text = ""
                }

                cardTags.hasBGOperation = false
                cardTags.hasExplanation = true
                asyncTaskQueue[cardTags.taskNumber] = null

                datasets.clear()
                bgfatores.clear()
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



