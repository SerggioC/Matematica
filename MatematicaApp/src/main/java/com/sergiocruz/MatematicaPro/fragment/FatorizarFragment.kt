package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutCompat
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.*
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.R.string.fatorizar_btn
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.fragment.MMCFragment.Companion.CARD_TEXT_SIZE
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.expandIt
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import java.text.DecimalFormat
import java.util.*
import java.util.Map

class FatorizarFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActionDone,
    OnEditorActionError {
    private var BG_Operation: AsyncTask<Long, Float, ArrayList<ArrayList<Long>>> =
        BackGroundOperation()

    internal var scale: Float = 0.toFloat()
    internal var cv_width: Int = 0
    internal var height_dip: Int = 0
    private var startTime: Long? = null
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        scale = resources.displayMetrics.density
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_fatorizar

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        inflater.inflate(R.menu.menu_sub_main, menu)
        inflater.inflate(R.menu.menu_help_fatorizar, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.itemId

        if (id == R.id.action_save_history_images) {
            MenuHelper.save_history_images(activity as Activity)
        }
        if (id == R.id.action_share_history_images) {
            MenuHelper.share_history_images(activity as Activity)
        }
        if (id == R.id.action_share_history) {
            MenuHelper.share_history(activity as Activity)
        }
        if (id == R.id.action_clear_all_history) {
            MenuHelper.remove_history(activity as Activity)
        }
        if (id == R.id.action_ajuda) {
            val history = activity!!.findViewById<View>(R.id.history) as ViewGroup
            val helpDivisores = getString(R.string.help_text_fatores)
            val ssb = SpannableStringBuilder(helpDivisores)
            CreateCardView.create(history, ssb, activity as Activity)
        }
        if (id == R.id.action_about) {
            startActivity(Intent(activity, AboutActivity::class.java))
        }
        if (id == R.id.action_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        //         Checks the orientation of the screen
        //        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //            Toast.makeText(activity, "landscape", Toast.LENGTH_SHORT).show();
        //        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        //            Toast.makeText(activity, "portrait", Toast.LENGTH_SHORT).show();
        //        }

        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        val width = size.x
        //int height = size.y;
        val lr_dip = (4 * scale + 0.5f).toInt() * 2
        cv_width = width - lr_dip

        hideKeyboard(activity as Activity)

    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(BG_Operation, context)
    }

    private fun resetButtons() {
        calculateButton.setText(R.string.calculate)
        calculateButton.isClickable = true
        cancelButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cancelButton.setOnClickListener { displayCancelDialogBox(context!!, this) }
        calculateButton.setOnClickListener { calculatePrimeFactors() }
        clearButton.setOnClickListener { factorizeTextView.setText("") }
        factorizeTextView.watchThis(this, this)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(BG_Operation, context)) resetButtons()
    }

    override fun onActionDone() {
        calculatePrimeFactors()
    }

    override fun onActionError() {
        showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
    }

    private fun calculatePrimeFactors() {
        startTime = System.nanoTime()
        val editnumText = factorizeTextView.text.toString()
        val num: Long

        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.insert_integer))
            return
        }
        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.numero_alto))
            return
        }

        if (num == 0L || num == 1L) {
            showCustomToast(
                context,
                getString(R.string.the_number) + " " + num + " " + getString(R.string.has_no_factors)
            )
            return
        }

        BG_Operation = BackGroundOperation().execute(num)

    }

    private fun createCardViewLayout(
        number: Long?,
        history: ViewGroup,
        str_results: String,
        ssb_str_divisores: SpannableStringBuilder,
        ssbFatores: SpannableStringBuilder,
        str_fact_exp: SpannableStringBuilder,
        hasExpoentes: Boolean?
    ) {

        //criar novo cardview
        val cardview = ClickableCardView(activity as Activity)
        cardview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // height
        cardview.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lr_dip = (6 * scale + 0.5f).toInt()
        val tb_dip = (8 * scale + 0.5f).toInt()
        cardview.radius = (2 * scale + 0.5f).toInt().toFloat()
        cardview.cardElevation = (2 * scale + 0.5f).toInt().toFloat()
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip)
        cardview.useCompatPadding = true

        val cv_color = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardview.setCardBackgroundColor(cv_color)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0)

        val ll_vertical_root = LinearLayout(activity)
        ll_vertical_root.layoutParams = LinearLayout.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        ll_vertical_root.orientation = LinearLayout.VERTICAL

        // criar novo Textview para o resultado da fatorização
        val textView = TextView(activity)
        textView.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        textView.setPadding(0, 0, 0, 0)

        val ssb_fatores_top = SpannableStringBuilder(ssbFatores)
        val spans =
            ssb_fatores_top.getSpans(0, ssb_fatores_top.length, ForegroundColorSpan::class.java)
        for (i in spans.indices) {
            ssb_fatores_top.removeSpan(spans[i])
        }

        //Adicionar o texto com o resultado da fatorizaçãoo com expoentes
        val str_num = getString(R.string.factorization_of) + " " + number + " = \n"
        val ssb_num = SpannableStringBuilder(str_num)
        ssb_num.append(ssb_fatores_top)
        textView.text = ssb_num
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        textView.setTag(R.id.texto, "texto")

        // add the textview com os fatores multiplicados to the Linear layout vertical root
        ll_vertical_root.addView(textView)

        val shouldShowExplanation = sharedPrefs.getString("pref_show_explanation", "0")
        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {

            val ll_vertical_expl = LinearLayout(activity)
            ll_vertical_expl.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            ll_vertical_expl.orientation = LinearLayout.VERTICAL
            ll_vertical_expl.tag = "ll_vertical_expl"

            val textView_expl1 = TextView(activity)
            textView_expl1.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            textView_expl1.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            val explain_text_1 = getString(R.string.expl_text_divisores_1)
            val ssb_explain_1 = SpannableStringBuilder(explain_text_1)
            val boldColorSpan =
                ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.boldColor))
            ssb_explain_1.setSpan(boldColorSpan, 0, ssb_explain_1.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            textView_expl1.text = ssb_explain_1
            textView_expl1.setTag(R.id.texto, "texto")
            ll_vertical_expl.addView(textView_expl1)

            val ll_horizontal = LinearLayout(activity)
            ll_horizontal.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            ll_horizontal.orientation = LinearLayout.HORIZONTAL
            ll_horizontal.tag = "ll_horizontal_expl"

            val ll_vertical_results = LinearLayout(activity)
            ll_vertical_results.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            ll_vertical_results.orientation = LinearLayout.VERTICAL
            ll_vertical_results.setPadding(0, 0, (4 * scale + 0.5f).toInt(), 0)

            val ll_vertical_separador = LinearLayout(activity)
            ll_vertical_separador.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
            ll_vertical_separador.orientation = LinearLayout.VERTICAL
            ll_vertical_separador.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.separatorLineColor
                )
            )
            val um_dip = (1.2 * scale + 0.5f).toInt()
            ll_vertical_separador.setPadding(um_dip, 4, 0, um_dip)

            val ll_vertical_divisores = LinearLayout(activity)
            ll_vertical_divisores.layoutParams = LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            ll_vertical_divisores.orientation = LinearLayout.VERTICAL
            ll_vertical_divisores.setPadding(
                (4 * scale + 0.5f).toInt(),
                0,
                (8 * scale + 0.5f).toInt(),
                0
            )

            val textView_results = TextView(activity)
            textView_results.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            textView_results.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            textView_results.gravity = Gravity.RIGHT
            val ssb_str_results = SpannableStringBuilder(str_results)
            ssb_str_results.setSpan(
                RelativeSizeSpan(0.9f),
                ssb_str_results.length - str_results.length,
                ssb_str_results.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView_results.text = ssb_str_results
            textView_results.setTag(R.id.texto, "texto")

            ll_vertical_results.addView(textView_results)

            val textView_divisores = TextView(activity)
            textView_divisores.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            textView_divisores.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                CARD_TEXT_SIZE
            )
            textView_divisores.gravity = Gravity.LEFT
            ssb_str_divisores.setSpan(
                RelativeSizeSpan(0.9f),
                ssb_str_divisores.length - ssb_str_divisores.length,
                ssb_str_divisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView_divisores.text = ssb_str_divisores
            textView_divisores.setTag(R.id.texto, "texto")

            ll_vertical_divisores.addView(textView_divisores)

            //Adicionar os LL Verticais ao Horizontal
            ll_horizontal.addView(ll_vertical_results)

            ll_horizontal.addView(ll_vertical_separador)

            //LinearLayout divisores
            ll_horizontal.addView(ll_vertical_divisores)

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

            val explainLink = TextView(activity)
            explainLink.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, //largura
                LinearLayout.LayoutParams.WRAP_CONTENT
            ) //altura
            explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
            explainLink.setTextColor(ContextCompat.getColor(activity!!, R.color.linkBlue))

            val isExpanded = arrayOf(false)

            if (shouldShowExplanation == "-1") {  //Always show Explanation
                ll_vertical_expl.visibility = View.VISIBLE
                explainLink.text = ssb_hide_expl
                isExpanded[0] = true
            } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
                ll_vertical_expl.visibility = View.GONE
                explainLink.text = ssb_show_expl
                isExpanded[0] = false
            }

            explainLink.setOnClickListener { view ->
                val explView =
                    (view.parent.parent.parent as CardView).findViewWithTag<View>("ll_vertical_expl")
                if (!isExpanded[0]) {
                    (view as TextView).text = ssb_hide_expl
                    expandIt(explView)
                    isExpanded[0] = true

                } else if (isExpanded[0]) {
                    (view as TextView).text = ssb_show_expl
                    collapseIt(explView)
                    isExpanded[0] = false
                }
            }

            val gradient_separator = getGradientSeparator(context)

            val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false)
            if (shouldShowPerformance) {
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    "Performance:" + " " + decimalFormatter.format((System.nanoTime() - startTime!!) / 1000000000.0) + "s"
                gradient_separator.text = elapsed
            } else {
                gradient_separator.text = ""
            }

            //Linearlayout horizontal com o explainlink e gradiente
            val ll_horizontal_link = LinearLayout(activity)
            ll_horizontal_link.orientation = HORIZONTAL
            ll_horizontal_link.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ll_horizontal_link.addView(explainLink)
            ll_horizontal_link.addView(gradient_separator)

            ll_vertical_root.addView(ll_horizontal_link)

            ll_vertical_expl.addView(ll_horizontal)

            val textView_fact_expanded = TextView(activity)
            textView_fact_expanded.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            textView_fact_expanded.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                CARD_TEXT_SIZE
            )
            textView_fact_expanded.gravity = Gravity.LEFT
            val explain_text_2 = getString(R.string.explain_divisores2) + "\n"
            val ssb_explain_2 = SpannableStringBuilder(explain_text_2)
            ssb_explain_2.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.boldColor
                    )
                ), 0, ssb_explain_2.length, SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb_explain_2.append(str_fact_exp)
            ssb_explain_2.setSpan(
                RelativeSizeSpan(0.9f),
                ssb_explain_2.length - str_fact_exp.length,
                ssb_explain_2.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (hasExpoentes!!) {
                val text_fact_repetidos = "\n" + getString(R.string.explain_divisores3) + "\n"
                ssb_explain_2.append(text_fact_repetidos)
                ssb_explain_2.setSpan(
                    boldColorSpan,
                    ssb_explain_2.length - text_fact_repetidos.length,
                    ssb_explain_2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb_explain_2.append(ssbFatores)
                ssb_explain_2.setSpan(
                    RelativeSizeSpan(0.9f),
                    ssb_explain_2.length - ssbFatores.length,
                    ssb_explain_2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb_explain_2.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    ssb_explain_2.length - ssbFatores.length,
                    ssb_explain_2.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textView_fact_expanded.text = ssb_explain_2
            textView_fact_expanded.setTag(R.id.texto, "texto")

            ll_vertical_expl.addView(textView_fact_expanded)

            ll_vertical_root.addView(ll_vertical_expl)


        } else if (shouldShowExplanation == "1") { //nunca mostrar explicações

            val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false)
            if (shouldShowPerformance) {
                //View separator with gradient
                val gradientSeparator = getGradientSeparator(context)
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime!!) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
                ll_vertical_root.addView(gradientSeparator, 0)
            }
        }

        cardview.addView(ll_vertical_root)

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
                        history.removeView(cardview)
                    }
                })
        )

    }


    inner class BackGroundOperation : AsyncTask<Long, Float, ArrayList<ArrayList<Long>>>() {

        public override fun onPreExecute() {
            calculateButton.isClickable = false
            calculateButton.text = getString(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity as Activity)
            cv_width = card_view_1.width
            height_dip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(1, height_dip)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): ArrayList<ArrayList<Long>> {
            val factoresPrimos = ArrayList<ArrayList<Long>>()
            val results = ArrayList<Long>()
            val divisores = ArrayList<Long>()
            var number: Long = num[0]!!
            var progress: Float?
            var oldProgress: Float? = 0f

            results.add(number)

            while (number % 2L == 0L) {
                divisores.add(2L)
                number /= 2
                results.add(number)
            }

            var i: Long = 3
            while (i <= number / i) {
                while (number % i == 0L) {
                    divisores.add(i)
                    number /= i
                    results.add(number)
                }
                progress = i.toFloat() / (number / i)
                if (progress - oldProgress!! > 0.1f) {
                    publishProgress(progress, i.toFloat())
                    oldProgress = progress
                }
                if (isCancelled) break
                i += 2
            }
            if (number > 1L) {
                divisores.add(number)
            }

            if (number != 1L) {
                results.add(1L)
            }

            factoresPrimos.add(results)
            factoresPrimos.add(divisores)

            return factoresPrimos
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@FatorizarFragment.isVisible && values[0] != null) {
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(Math.round(values[0]!! * cv_width), height_dip)
            }
        }

        override fun onPostExecute(result: ArrayList<ArrayList<Long>>) {
            if (this@FatorizarFragment.isVisible) {
                processData(result, false)
            }
        }

        override fun onCancelled(parcial: ArrayList<ArrayList<Long>>) {
            super.onCancelled(parcial)
            if (this@FatorizarFragment.isVisible) {
                processData(parcial, true)
            }
        }
    }

    private fun processData(result: ArrayList<ArrayList<Long>>, wasCanceled: Boolean) {
        /* resultadosDivisao|fatoresPrimos
            *                100|2
            *                 50|2
            *                 25|5
            *                  5|5
            *                  1|
            *
            * */

        val resultadosDivisao = result[0]
        val fatoresPrimos = result[1]

        // Tamanho da lista de números primos
        val sizeList = fatoresPrimos.size

        var str_fatores = ""
        var str_results = ""
        val ssb_fatores: SpannableStringBuilder

        if (sizeList == 1) {
            str_fatores = resultadosDivisao[0].toString() + " " + getString(R.string.its_a_prime)
            ssb_fatores = SpannableStringBuilder(str_fatores)
            ssb_fatores.setSpan(
                ForegroundColorSpan(Color.parseColor("#29712d")),
                0,
                ssb_fatores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            ) //verde
            CreateCardView.create(history, ssb_fatores, activity as Activity)

        } else {
            str_fatores = ""
            var hasExpoentes: Boolean? = false
            var counter = 1
            var lastItem: Long? = fatoresPrimos[0]

            val fColors: ArrayList<Int> = ArrayList()
            val f_colors = resources.getIntArray(R.array.f_colors_xml)

            val shouldShowColors = sharedPrefs.getBoolean("pref_show_colors", true)
            if (shouldShowColors) {
                for (f_color in f_colors) fColors.add(f_color)
                fColors.shuffle() //randomizar as cores
            } else {
                for (i in f_colors.indices) fColors.add(f_colors[f_colors.size - 1])
            }

            val ssbFactExpanded = SpannableStringBuilder()
            var colorIndex = 0

            //TreeMap
            val dataSet = LinkedHashMap<String, Int>()

            //Contar os expoentes
            for (i in fatoresPrimos.indices) {
                val fatori = fatoresPrimos[i]
                if (lastItem != fatori) {
                    colorIndex++
                }

                val fi = fatori.toString()
                ssbFactExpanded.append(fi)
                ssbFactExpanded.setSpan(
                    ForegroundColorSpan(fColors[colorIndex]),
                    ssbFactExpanded.length - fi.length, ssbFactExpanded.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFactExpanded.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    ssbFactExpanded.length - fi.length, ssbFactExpanded.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssbFactExpanded.append("×")

                if (i == 0) {
                    dataSet[fatoresPrimos[0].toString()] = 1
                } else if (fatori == lastItem && i > 0) {
                    hasExpoentes = true
                    counter++
                    dataSet[fatori.toString()] = counter
                } else if (fatori != lastItem && i > 0) {
                    counter = 1
                    dataSet[fatori.toString()] = counter
                }
                lastItem = fatori
            }
            ssbFactExpanded.delete(ssbFactExpanded.length - 1, ssbFactExpanded.length)

            ssb_fatores = SpannableStringBuilder(str_fatores)

            var valueLength: Int
            colorIndex = 0

            val mapValues =
                dataSet.entries                             // Confusão para sacar o primeiro elemento
            val test = arrayOfNulls<Map.Entry<*, *>>(mapValues.size)    // (fator primo)
            //mapValues.toTypedArray<Map.Entry<String, Int>>()
            var lastKey = test[0]?.key.toString()

            val iterator = dataSet.entries.iterator()

            //Criar os expoentes
            while (iterator.hasNext()) {
                val pair = iterator.next() as Map.Entry<*, *>

                val key = pair.key.toString()
                val value = pair.value.toString()

                //if (lastkey != Integer.parseInt(pair.getKey().toString())) {
                if (lastKey != key) {
                    colorIndex++
                }

                if (Integer.parseInt(value) == 1) {
                    //Expoente 1
                    ssb_fatores.append(key)
                    ssb_fatores.setSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssb_fatores.length - key.length, ssb_fatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                } else if (Integer.parseInt(value) > 1) {
                    //Expoente superior a 1 // pair.getkey = fator; pair.getvalue = expoente

                    ssb_fatores.append(key)
                    ssb_fatores.setSpan(
                        ForegroundColorSpan(fColors[colorIndex]),
                        ssb_fatores.length - key.length, ssb_fatores.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    valueLength = value.length
                    ssb_fatores.append(value)
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
                lastKey = key

                iterator.remove() // avoids a ConcurrentModificationException
            }

            if (wasCanceled) {
                val incompleteCalc = "\n" + getString(R.string._incomplete_calc)
                ssb_fatores.append(incompleteCalc)
                ssb_fatores.setSpan(
                    ForegroundColorSpan(Color.RED),
                    ssb_fatores.length - incompleteCalc.length,
                    ssb_fatores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb_fatores.setSpan(
                    RelativeSizeSpan(0.8f),
                    ssb_fatores.length - incompleteCalc.length,
                    ssb_fatores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // Todos os números primos divisores
            val ssbDivisores = SpannableStringBuilder()
            colorIndex = 0
            var currentLong: Long? = fatoresPrimos[0]
            for (i in 0 until sizeList - 1) {
                val fator_i = fatoresPrimos[i]
                if (currentLong != fator_i) {
                    colorIndex++
                }
                currentLong = fator_i

                val fa = fator_i.toString() + "\n"
                ssbDivisores.append(fa)
                ssbDivisores.setSpan(
                    ForegroundColorSpan(fColors[colorIndex]),
                    ssbDivisores.length - fa.length, ssbDivisores.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val fator_i = fatoresPrimos[sizeList - 1]
            if (currentLong != fator_i) {
                colorIndex++
            }
            ssbDivisores.append(fator_i.toString())
            ssbDivisores.setSpan(
                ForegroundColorSpan(fColors[colorIndex]),
                ssbDivisores.length - fator_i.toString().length, ssbDivisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssbDivisores.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                ssbDivisores.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )

            for (i in 0 until resultadosDivisao.size - 1) {
                str_results += resultadosDivisao[i].toString() + "\n"
            }
            str_results += resultadosDivisao[resultadosDivisao.size - 1].toString()

            createCardViewLayout(
                resultadosDivisao[0],
                history,
                str_results,
                ssbDivisores,
                ssb_fatores,
                ssbFactExpanded,
                hasExpoentes
            )
        }

        progressBar.visibility = View.GONE
        calculateButton.text = getString(fatorizar_btn)
        calculateButton.isClickable = true
        cancelButton.visibility = View.GONE
    }

}