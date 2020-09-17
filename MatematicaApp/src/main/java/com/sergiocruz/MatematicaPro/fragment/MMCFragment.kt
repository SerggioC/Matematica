package com.sergiocruz.MatematicaPro.fragment

import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGE_APPEARING
import android.animation.LayoutTransition.CHANGE_DISAPPEARING
import android.app.Activity
import android.content.SharedPreferences
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
import com.sergiocruz.MatematicaPro.MyTags
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.expandIt
import kotlinx.android.synthetic.main.fragment_mmc.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class MMCFragment : BaseFragment(), OnEditorActions {
    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    internal lateinit var fColors: ArrayList<Int>
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
        val xmlColors = resources.getIntArray(R.array.f_colors_xml)
        fColors = ArrayList()
        for (color in xmlColors) fColors.add(color)
        language = Locale.getDefault().displayLanguage
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
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

    override fun getHelpTextId(): Int? = R.string.help_text_mmc

    override fun getHelpMenuTitleId(): Int? = R.string.action_ajuda_mmc

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
        cardView.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardView.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
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
                    override fun canDismiss(token: Boolean?) = true
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
            createExplanations(cardView, llVerticalRoot, shouldShowExplanation)
        } else {
            if (shouldShowPerformance) {
                val gradientSeparator = getGradientSeparator(context)
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
                llVerticalRoot.addView(gradientSeparator, 0)
            }
            cardView.addView(llVerticalRoot)
        }

    }

    private fun createExplanations(
        cardview: CardView,
        llVerticalRoot: LinearLayout,
        shouldShowExplanation: String?
    ) {

        val hideExpl = SpannableStringBuilder(getString(R.string.hide_explain))
        hideExpl.setSpan(UnderlineSpan(), 0, hideExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)
        val showExpl = SpannableStringBuilder(getString(R.string.explain))
        showExpl.setSpan(UnderlineSpan(), 0, showExpl.length - 2, SPAN_EXCLUSIVE_EXCLUSIVE)

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

        //View separator with gradient
        val gradientSeparator = getGradientSeparator(context)

        llHorizontal.gravity = Gravity.CENTER_VERTICAL

        val isExpanded = arrayOf(false)
        explainLink.setOnClickListener { view ->
            val explView =
                (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
            if (!isExpanded[0]) {
                (view as TextView).text = hideExpl
                expandIt(explView, null)
                isExpanded[0] = true

            } else if (isExpanded[0]) {
                (view as TextView).text = showExpl
                collapseIt(explView)
                isExpanded[0] = false
            }
        }

        llHorizontal.addView(explainLink)
        llHorizontal.addView(gradientSeparator)

        //LL vertical das explicações
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
        //ProgressBar
        val heightDp = (3 * scale + 0.5f).toInt()
        val progressBar = View(activity)
        progressBar.tag = "progressBar"
        val layoutParams = LinearLayout.LayoutParams(1, heightDp) //Largura, Altura
        progressBar.layoutParams = layoutParams

        //Ponto 1
        val explainTextView1 = TextView(activity)
        explainTextView1.tag = "explainTextView_1"
        val fp = getString(R.string.fatores_primos)
        val explainText1 = getString(R.string.decompor_num) + " " + fp + "\n"
        val ssbExplain1 = SpannableStringBuilder(explainText1)
        ssbExplain1.setSpan(
            UnderlineSpan(),
            explainText1.length - fp.length - 1,
            explainText1.length - 1,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssbExplain1.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.boldColor
                )
            ), 0, ssbExplain1.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explainTextView1.text = ssbExplain1
        explainTextView1.setTag(R.id.texto, "texto")

        //Ponto 2
        val explainTextView2 = TextView(activity)
        explainTextView2.tag = "explainTextView_2"
        val comuns = getString(R.string.comuns)
        val ncomuns = getString(R.string.nao_comuns)
        val uma_vez = getString(R.string.uma_vez)
        val maior_exps = getString(R.string.maior_exps)
        val explainText2: String
        if (language == "português" || language == "español" || language == "français") {
            explainText2 = getString(R.string.escolher) + " " + getString(R.string.os_fatores) +
                    " " + comuns + " " + getString(R.string.and) + " " + ncomuns + ", " + uma_vez +
                    ", " + getString(R.string.with_the) + " " + maior_exps + ":\n"
        } else {
            explainText2 = getString(R.string.escolher) + " " + comuns + " " +
                    getString(R.string.and) + " " + ncomuns + " " + getString(R.string.os_fatores) +
                    ", " + uma_vez + ", " + getString(R.string.with_the) + " " + maior_exps + ":\n"
        }
        val ssbExplain2 = SpannableStringBuilder(explainText2)
        ssbExplain2.setSpan(
            UnderlineSpan(),
            explainText2.indexOf(comuns),
            explainText2.indexOf(comuns) + comuns.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssbExplain2.setSpan(
            UnderlineSpan(),
            explainText2.indexOf(ncomuns),
            explainText2.indexOf(ncomuns) + ncomuns.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssbExplain2.setSpan(
            UnderlineSpan(),
            explainText2.indexOf(uma_vez),
            explainText2.indexOf(uma_vez) + uma_vez.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssbExplain2.setSpan(
            UnderlineSpan(),
            explainText2.indexOf(maior_exps),
            explainText2.indexOf(maior_exps) + maior_exps.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //ssb_explain_2.setSpan(new StyleSpan(BOLD), 0, ssb_explain_2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssbExplain2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.boldColor
                )
            ), 0, ssbExplain2.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explainTextView2.text = ssbExplain2
        explainTextView2.setTag(R.id.texto, "texto")

        //Ponto 3
        val explaintextview3 = TextView(activity)
        explaintextview3.tag = "explainTextView_3"
        val multipl = getString(R.string.multiply)
        val explain_text_3 = multipl + " " +
                getString(R.string.to_obtain_mmc) + "\n"
        val ssb_explain_3 = SpannableStringBuilder(explain_text_3)
        ssb_explain_3.setSpan(
            UnderlineSpan(),
            explain_text_3.indexOf(multipl) + 1,
            explain_text_3.indexOf(multipl) + multipl.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //ssb_explain_3.setSpan(new StyleSpan(BOLD), 0, ssb_explain_3.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_3.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.boldColor
                )
            ), 0, ssb_explain_3.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explaintextview3.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explaintextview3.text = ssb_explain_3
        explaintextview3.setTag(R.id.texto, "texto")

        verticalExpl.addView(explainTextView1)
        verticalExpl.addView(explainTextView2)
        verticalExpl.addView(explaintextview3)
        llVerticalRoot.addView(llHorizontal)
        llVerticalRoot.addView(progressBar)
        llVerticalRoot.addView(verticalExpl)

        if (shouldShowExplanation == "-1") {  //Always show Explanation
            verticalExpl.visibility = View.VISIBLE
            explainLink.text = hideExpl
            isExpanded[0] = true
        } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
            verticalExpl.visibility = View.GONE
            explainLink.text = showExpl
            isExpanded[0] = false
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

    // Asynctask <Params, Progress, Result>
    inner class BackGroundOperationMMC internal constructor(private var cardTags: MyTags) :
        AsyncTask<Void, Float, Void>() {
        lateinit var theCardViewBG: CardView
        lateinit var mmc_numbers: ArrayList<Long>
        lateinit var result_mmc: BigInteger
        lateinit var bgfatores: ArrayList<ArrayList<Long>>

        lateinit var gradient_separator: TextView
        lateinit var progressBar: View
        lateinit var percent_formatter: NumberFormat
        lateinit var f_colors: IntArray
        var f_colors_length: Int = 0
        private lateinit var progressParams: ViewGroup.LayoutParams

        public override fun onPreExecute() {
            percent_formatter = DecimalFormat("#.###%")
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
            progressParams = progressBar.layoutParams
            gradient_separator =
                theCardViewBG.findViewWithTag<View>("gradient_separator") as TextView
            cardTags.hasBGOperation = true

            f_colors = requireActivity().resources.getIntArray(R.array.f_colors_xml)
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
            mmc_numbers = cardTags.longNumbers

            val numbersSize = mmc_numbers.size
            for (i in 0 until numbersSize) { // fatorizar todos os números inseridos em MMC
                var oldProgress = 0.0f
                var progress: Float
                val fatores_ix = ArrayList<Long>()

                var number_i: Long? = mmc_numbers[i]
                if (number_i == 1L) {
                    fatores_ix.add(1L)
                }
                while (number_i!! % 2L == 0L) {
                    fatores_ix.add(2L)
                    number_i /= 2L
                }

                var j: Long = 3
                while (j <= number_i / j) {
                    if (isCancelled) break
                    while (number_i % j == 0L) {
                        fatores_ix.add(j)
                        number_i /= j
                    }
                    progress = j.toFloat() / (number_i.toFloat() / j.toFloat())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toFloat())
                        oldProgress = progress
                    }
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

        override fun onProgressUpdate(vararg values: Float?) {

            if (this@MMCFragment.isVisible) {
                val color = fColors[Math.round(values[1]!!)]
                progressBar.setBackgroundColor(color)
                var value0 = values[0]
                if (value0!! > 1f) value0 = 1.0f
                progressParams.width = Math.round(value0 * card_view_1.width)
                progressBar.layoutParams = progressParams

                val text =
                    " " + getString(R.string.factorizing) + " " + percent_formatter.format(value0)
                val ssb = SpannableStringBuilder(text)
                ssb.setSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                gradient_separator.text = ssb
            }
        }

        override fun onPostExecute(result: Void?) {
            if (this@MMCFragment.isVisible) {

                bgfatores = cardTags.bGfatores!!

                val datasets = ArrayList<ArrayList<Long>>()

                for (k in bgfatores.indices) {
                    val bases = ArrayList<Long>()
                    val exps = ArrayList<Long>()

                    val str_fatores = mmc_numbers[k].toString() + "="
                    val ssb_fatores: SpannableStringBuilder
                    ssb_fatores = SpannableStringBuilder(str_fatores)
                    ssb_fatores.setSpan(
                        ForegroundColorSpan(fColors[k]),
                        0,
                        ssb_fatores.length,
                        SPAN_EXCLUSIVE_INCLUSIVE
                    )

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
                    var value_length: Int
                    val iterator = dataset.entries.iterator()
                    while (iterator.hasNext()) {
                        val pair = iterator.next() as Map.Entry<*, *>

                        if (Integer.parseInt(pair.value.toString()) == 1) {
                            //Expoente 1
                            ssb_fatores.append(pair.key.toString())

                        } else if (Integer.parseInt(pair.value.toString()) > 1) {
                            //Expoente superior a 1
                            value_length = pair.value.toString().length
                            ssb_fatores.append(pair.key.toString() + pair.value.toString())
                            ssb_fatores.setSpan(
                                SuperscriptSpan(),
                                ssb_fatores.length - value_length,
                                ssb_fatores.length,
                                SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            ssb_fatores.setSpan(
                                RelativeSizeSpan(0.8f),
                                ssb_fatores.length - value_length,
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

                val maiores_bases = ArrayList<Long>()
                val maiores_exps = ArrayList<Long>()
                val colors = ArrayList<Long>()

                run {
                    var i = 0
                    while (i < datasets.size) {
                        val bases = datasets[i]
                        val exps = datasets[i + 1]

                        for (cb in bases.indices) {
                            val current_base = bases[cb]
                            val current_exp = exps[cb]

                            if (!maiores_bases.contains(current_base)) {
                                maiores_bases.add(current_base)
                                maiores_exps.add(current_exp)
                                colors.add(i.toLong() / 2)
                            }

                            if (maiores_bases.contains(current_base) && current_exp > maiores_exps[maiores_bases.indexOf(
                                    current_base
                                )]
                            ) {
                                maiores_exps[maiores_bases.indexOf(current_base)] = current_exp
                                colors[maiores_bases.indexOf(current_base)] = (i / 2).toLong()
                            }

                            var j = i + 2
                            while (j < datasets.size) {
                                val next_bases = datasets[j]
                                val next_exps = datasets[j + 1]

                                for (nb in next_bases.indices) {
                                    val next_base = next_bases[nb]
                                    val next_exp = next_exps[nb]

                                    if (next_base == current_base && next_exp > maiores_exps[maiores_bases.indexOf(
                                            current_base
                                        )] && maiores_bases.contains(next_base)
                                    ) {
                                        maiores_exps[maiores_bases.indexOf(next_base)] = next_exp
                                        colors[maiores_bases.indexOf(current_base)] =
                                            (j / 2).toLong()
                                    }

                                }
                                j += 2
                            }
                        }
                        i += 2
                    }
                }

                val ssb_mmc = SpannableStringBuilder()

                //Criar os expoentes do MMC com os maiores fatores com cores e a negrito
                for (i in maiores_bases.indices) {
                    val base_length = maiores_bases[i].toString().length

                    if (maiores_exps[i] == 1L) {
                        //Expoente 1
                        ssb_mmc.append(maiores_bases[i].toString())
                        ssb_mmc.setSpan(
                            ForegroundColorSpan(fColors[colors[i].toInt()]),
                            ssb_mmc.length - base_length, ssb_mmc.length, SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                    } else if (maiores_exps[i] > 1L) {
                        //Expoente superior a 1
                        val exp_length = maiores_exps[i].toString().length
                        ssb_mmc.append(maiores_bases[i].toString() + maiores_exps[i].toString())
                        ssb_mmc.setSpan(
                            SuperscriptSpan(),
                            ssb_mmc.length - exp_length,
                            ssb_mmc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        ssb_mmc.setSpan(
                            RelativeSizeSpan(0.8f),
                            ssb_mmc.length - exp_length,
                            ssb_mmc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        ssb_mmc.setSpan(
                            ForegroundColorSpan(fColors[colors[i].toInt()]),
                            ssb_mmc.length - exp_length - base_length,
                            ssb_mmc.length,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    ssb_mmc.append("×")
                }
                ssb_mmc.replace(ssb_mmc.length - 1, ssb_mmc.length, "")

                ssb_mmc.setSpan(StyleSpan(BOLD), 0, ssb_mmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mmc.setSpan(RelativeSizeSpan(0.9f), 0, ssb_mmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                //explainTextView_2
                (theCardViewBG.findViewWithTag<View>("explainTextView_2") as TextView).append(
                    ssb_mmc
                )

                ssb_mmc.delete(0, ssb_mmc.length)
                result_mmc = cardTags.resultMDC!!
                ssb_mmc.append(result_mmc.toString())

                ssb_mmc.setSpan(StyleSpan(BOLD), 0, ssb_mmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mmc.setSpan(RelativeSizeSpan(0.9f), 0, ssb_mmc.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb_mmc.setSpan(
                    ForegroundColorSpan(f_colors[f_colors.size - 1]),
                    0,
                    ssb_mmc.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )

                //explainTextView_3
                (theCardViewBG.findViewWithTag<View>("explainTextView_3") as TextView).append(
                    ssb_mmc
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