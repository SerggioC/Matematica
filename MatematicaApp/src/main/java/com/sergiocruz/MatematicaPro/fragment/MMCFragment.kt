package com.sergiocruz.MatematicaPro.fragment

import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGE_APPEARING
import android.animation.LayoutTransition.CHANGE_DISAPPEARING
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Typeface.BOLD
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutCompat
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE
import android.text.TextUtils
import android.text.style.*
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import com.sergiocruz.MatematicaPro.MyTags
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.CreateCardView.create
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.collapseIt
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.expandIt

import kotlinx.android.synthetic.main.fragment_mmc.*
import java.lang.Long.parseLong
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.Map

class MMCFragment : BaseFragment(), OnEditorActions {
    internal var asyncTaskQueue = ArrayList<AsyncTask<*, *, *>?>()
    internal lateinit var fColors: ArrayList<Int>
    private var height_dip: Int = 0
    private var cv_width: Int = 0
    private var taskNumber = 0
    internal var startTime: Long = 0
    private lateinit var language: String

    private val emptyTextView = ArrayList<TextView>()

    private fun showToastNum(field: String) {
        showCustomToast(context, getString(R.string.number_in_field) + " " + field + " " + getString(R.string.too_high), InfoLevel.WARNING)
    }

    private fun showToastMoreThanZero() {
        showCustomToast(context, getString(R.string.maiores_qzero), InfoLevel.WARNING)
    }
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
        val f_colors = resources.getIntArray(R.array.f_colors_xml)
        fColors = ArrayList()
        for (f_color in f_colors) fColors.add(f_color)
        language = Locale.getDefault().displayLanguage
    }

    override fun onDestroy() {
        super.onDestroy()

        var hasCanceled = false
        for (i in asyncTaskQueue.indices) {
            asyncTaskQueue[i]?.cancel(true)
            hasCanceled = true
        }

        if (hasCanceled) {
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen and keeps the view contents and state
        //        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //            Toast.makeText(activity, "landscape", Toast.LENGTH_SHORT).show();
        //        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        //            Toast.makeText(activity, "portrait", Toast.LENGTH_SHORT).show();
        //        }

        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        val width = size.x  //int height = size.y;
        val lr_dip = (4 * scale + 0.5f).toInt() * 2
        cv_width = width - lr_dip

        hideKeyboard(activity as Activity)
    }

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main, R.menu.menu_help_mmc)


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.itemId
        if (id == R.id.action_save_history_images) {
            MenuHelper.saveHistoryImages(activity as Activity)
        }
        if (id == R.id.action_share_history_images) {
            MenuHelper.shareHistoryImages(activity as Activity)
        }
        if (id == R.id.action_share_history) {
            MenuHelper.shareHistory(activity!!)
        }

        if (id == R.id.action_clear_all_history) {
            MenuHelper.removeHistory(activity!!)
            mmc_num_1.setText("")
            mmc_num_2.setText("")
            mmc_num_3.setText("")
            mmc_num_4.setText("")
            mmc_num_5.setText("")
            mmc_num_6.setText("")
            mmc_num_7.setText("")
            mmc_num_8.setText("")
        }
        if (id == R.id.action_ajuda) {
            val history = activity!!.findViewById<View>(R.id.history) as LinearLayout
            val helpDivisores = getString(R.string.help_text_mmc)
            val ssb = SpannableStringBuilder(helpDivisores)
            create(history, ssb, activity!!)
        }
        if (id == R.id.action_about) {
            startActivity(Intent(activity, AboutActivity::class.java))
        }
        if (id == R.id.action_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActionDone() = calculateMMC()

    override fun getLayoutIdForFragment() = R.layout.fragment_mmc

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateButton.setOnClickListener { calculateMMC() }

        clearButton1.setOnClickListener { mmc_num_1.setText("") }
        clearButton2.setOnClickListener { mmc_num_2.setText("") }
        clearButton3.setOnClickListener { mmc_num_3.setText("") }
        clearButton4.setOnClickListener { mmc_num_4.setText("") }
        clearButton5.setOnClickListener { mmc_num_5.setText("") }
        clearButton6.setOnClickListener { mmc_num_6.setText("") }
        clearButton7.setOnClickListener { mmc_num_7.setText("") }
        clearButton8.setOnClickListener { mmc_num_8.setText("") }

        button_add_mmc.setOnClickListener { add_mmc() }
        button_remove_mmc.setOnClickListener { remove_mmc() }

        mmc_num_1.watchThis(this)
        mmc_num_2.watchThis(this)
        mmc_num_3.watchThis(this)
        mmc_num_4.watchThis(this)
        mmc_num_5.watchThis(this)
        mmc_num_6.watchThis(this)
        mmc_num_7.watchThis(this)
        mmc_num_8.watchThis(this)
    }

    fun add_mmc() {
        val ll_34_visibe = linear_layout_34.visibility == View.VISIBLE
        val f3_visible = frame_3.visibility == View.VISIBLE
        val f4_visible = frame_4.visibility == View.VISIBLE
        val ll_56_visibe = linear_layout_56.visibility == View.VISIBLE
        val f5_visible = frame_5.visibility == View.VISIBLE
        val f6_visible = frame_6.visibility == View.VISIBLE
        val ll_78_visibe = linear_layout_78.visibility == View.VISIBLE
        val f7_visible = frame_7.visibility == View.VISIBLE
        val f8_visible = frame_8.visibility == View.VISIBLE


        if (!ll_34_visibe || f3_visible || f4_visible) {
            linear_layout_34.visibility = View.VISIBLE

            if (!f3_visible) {
                frame_3.visibility = View.VISIBLE
                button_remove_mmc.visibility = View.VISIBLE
                mmc_num_2.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f4_visible) {
                frame_4.visibility = View.VISIBLE
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (!ll_56_visibe || f5_visible || f6_visible) {
            linear_layout_56.visibility = View.VISIBLE

            if (!f5_visible) {
                frame_5.visibility = View.VISIBLE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f6_visible) {
                frame_6.visibility = View.VISIBLE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }
        if (!ll_78_visibe || f7_visible || f8_visible) {
            linear_layout_78.visibility = View.VISIBLE

            if (!f7_visible) {
                frame_7.visibility = View.VISIBLE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (!f8_visible) {
                frame_8.visibility = View.VISIBLE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.INVISIBLE
                return
            }
        }

    }

    fun remove_mmc() {
        val ll34Visibe = linear_layout_34.visibility == View.VISIBLE
        val f3_visible = frame_3.visibility == View.VISIBLE
        val f4_visible = frame_4.visibility == View.VISIBLE
        val ll56Visible = linear_layout_56.visibility == View.VISIBLE
        val f5_visible = frame_5.visibility == View.VISIBLE
        val f6_visible = frame_6.visibility == View.VISIBLE
        val ll78Visible = linear_layout_78.visibility == View.VISIBLE
        val f7_visible = frame_7.visibility == View.VISIBLE
        val f8_visible = frame_8.visibility == View.VISIBLE

        if (ll78Visible) {
            if (f8_visible) {
                mmc_num_8.setText("")
                frame_8.visibility = View.GONE
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_8.imeOptions = EditorInfo.IME_ACTION_DONE
                button_add_mmc.visibility = View.VISIBLE
                return
            }
            if (f7_visible) {
                mmc_num_7.setText("")
                linear_layout_78.visibility = View.GONE
                frame_7.visibility = View.GONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_7.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll56Visible) {
            if (f6_visible) {
                mmc_num_6.setText("")
                frame_6.visibility = View.GONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f5_visible) {
                mmc_num_5.setText("")
                linear_layout_56.visibility = View.GONE
                frame_5.visibility = View.GONE
                mmc_num_5.imeOptions = EditorInfo.IME_ACTION_NEXT
                mmc_num_6.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
        }

        if (ll34Visibe) {
            if (f4_visible) {
                mmc_num_4.setText("")
                frame_4.visibility = View.GONE
                frame_4.alpha = 0f
                mmc_num_3.imeOptions = EditorInfo.IME_ACTION_DONE
                mmc_num_4.imeOptions = EditorInfo.IME_ACTION_DONE
                return
            }
            if (f3_visible) {
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

        val str_num1 = mmc_num_1.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num2 = mmc_num_2.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num3 = mmc_num_3.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num4 = mmc_num_4.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num5 = mmc_num_5.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num6 = mmc_num_6.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num7 = mmc_num_7.text.toString().replace("[^\\d]".toRegex(), "")
        val str_num8 = mmc_num_8.text.toString().replace("[^\\d]".toRegex(), "")

        val num1: Long
        val num2: Long
        val num3: Long
        val num4: Long
        val num5: Long
        val num6: Long
        val num7: Long
        val num8: Long

        val numbers = ArrayList<BigInteger>()
        val long_numbers = ArrayList<Long>()

        if (TextUtils.isEmpty(str_num1).not()) {
            try {
                // Tentar converter o string para Long
                num1 = parseLong(str_num1)
                if (num1 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num1 > 0L) {
                    val num1b = BigInteger(str_num1)
                    numbers.add(num1b)
                    long_numbers.add(num1)
                }
            } catch (e: Exception) {
                showToastNum("1")
                showCustomToast(context,
                    "${getString(R.string.number_in_field)} 1 ${getString(R.string.too_high)}"
                )
                return
            }

        } else {
            emptyTextView.add(mmc_num_1)
        }

        if (str_num2 != "") {
            try {
                // Tentar converter o string para Long
                num2 = parseLong(str_num2)
                if (num2 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num2 > 0L) {
                    val num2b = BigInteger(str_num2)
                    numbers.add(num2b)
                    long_numbers.add(num2)
                }
            } catch (e: Exception) {
                showToastNum("2")
                return
            }

        } else {
            emptyTextView.add(mmc_num_2)
        }

        if (str_num3 != "") {
            try {
                // Tentar converter o string para Long
                num3 = parseLong(str_num3)
                if (num3 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num3 > 0L) {
                    val num3b = BigInteger(str_num3)
                    numbers.add(num3b)
                    long_numbers.add(num3)
                }
            } catch (e: Exception) {
                showToastNum("3")
                return
            }

        } else {
            emptyTextView.add(mmc_num_3)
        }
        if (str_num4 != "") {
            try {
                // Tentar converter o string para Long
                num4 = parseLong(str_num4)
                if (num4 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num4 > 0L) {
                    val num4b = BigInteger(str_num4)
                    numbers.add(num4b)
                    long_numbers.add(num4)
                }
            } catch (e: Exception) {
                showToastNum("4")
                return
            }

        } else {
            emptyTextView.add(mmc_num_4)
        }
        if (str_num5 != "") {
            try {
                // Tentar converter o string para Long
                num5 = parseLong(str_num5)
                if (num5 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num5 > 0L) {
                    val num5b = BigInteger(str_num5)
                    numbers.add(num5b)
                    long_numbers.add(num5)
                }
            } catch (e: Exception) {
                showToastNum("5")
                return
            }

        } else {
            emptyTextView.add(mmc_num_5)
        }
        if (str_num6 != "") {
            try {
                // Tentar converter o string para Long
                num6 = parseLong(str_num6)
                if (num6 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num6 > 0L) {
                    val num6b = BigInteger(str_num6)
                    numbers.add(num6b)
                    long_numbers.add(num6)
                }
            } catch (e: Exception) {
                showToastNum("6")
                return
            }

        } else {
            emptyTextView.add(mmc_num_6)
        }

        if (str_num7 != "") {
            try {
                // Tentar converter o string para Long
                num7 = parseLong(str_num7)
                if (num7 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num7 > 0L) {
                    val num7b = BigInteger(str_num7)
                    numbers.add(num7b)
                    long_numbers.add(num7)
                }
            } catch (e: Exception) {
                showToastNum("7")
                return
            }

        } else {
            emptyTextView.add(mmc_num_7)
        }

        if (str_num8 != "") {
            try {
                // Tentar converter o string para Long
                num8 = parseLong(str_num8)
                if (num8 == 0L) {
                    showToastMoreThanZero()
                    return
                } else if (num8 > 0L) {
                    val num8b = BigInteger(str_num8)
                    numbers.add(num8b)
                    long_numbers.add(num8)
                }
            } catch (e: Exception) {
                showToastNum("8")
                return
            }

        } else {
            emptyTextView.add(mmc_num_8)
        }
        if (numbers.size < 2) {
            val thetoast = Toast.makeText(activity, R.string.add_number_pair, Toast.LENGTH_SHORT)
            thetoast.setGravity(Gravity.CENTER, 0, 0)
            thetoast.show()
            if (emptyTextView[0] != null) {
                emptyTextView[0].requestFocus()
                val imm =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(emptyTextView[0], 0)
            }
            return
        } else {
            hideKeyboard(activity as Activity)
        }

        var mmc_string = getString(R.string.mmc_result_prefix)
        var result_mmc: BigInteger? = null

        if (numbers.size > 1) {
            for (i in 0 until numbers.size - 1) {
                mmc_string += "${numbers[i]}, "
            }
            mmc_string += "${numbers[numbers.size - 1]})= "
            result_mmc = mmc(numbers)
        }

        mmc_string += result_mmc

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
            cardview.layoutTransition = lt
        }

        val cv_color = ContextCompat.getColor(activity!!, R.color.cardsColor)
        cardview.setCardBackgroundColor(cv_color)

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
                        check_bg_operation(view)
                    }
                })
        )

        //Adicionar os números a fatorizar na tag do cardview
        val tags = MyTags(cardview, long_numbers, result_mmc, false, false, "", null, taskNumber)
        cardview.tag = tags

        val history = activity!!.findViewById<View>(R.id.history) as LinearLayout
        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0)

        val ll_vertical_root = LinearLayout(activity)
        ll_vertical_root.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        ll_vertical_root.orientation = LinearLayout.VERTICAL

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, //largura
            LinearLayout.LayoutParams.WRAP_CONTENT
        ) //altura

        //Adicionar o texto com o resultado
        textView.text = mmc_string
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        textView.setTag(R.id.texto, "texto")

        // add the textview to the cardview
        ll_vertical_root.addView(textView)

        val shouldShowExplanation = sharedPrefs.getString(getString(R.string.pref_key_show_explanation), "0")
        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation == "-1" || shouldShowExplanation == "0") {
            createExplanations(cardview, ll_vertical_root, shouldShowExplanation)
        } else {
            val shouldShowPerformance = sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), false)
            if (shouldShowPerformance) {
                val gradientSeparator = getGradientSeparator(context)
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                gradientSeparator.text = elapsed
                ll_vertical_root.addView(gradientSeparator, 0)
            }
            cardview.addView(ll_vertical_root)
        }

    }

    private fun createExplanations(
        cardview: CardView,
        ll_vertical_root: LinearLayout,
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

        //Linearlayout horizontal com o explainlink e gradiente
        val ll_horizontal = LinearLayout(activity)
        ll_horizontal.orientation = HORIZONTAL
        ll_horizontal.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val explainLink = TextView(activity)
        explainLink.tag = "explainLink"
        explainLink.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, //largura
            LinearLayout.LayoutParams.WRAP_CONTENT //altura
        )
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE)
        explainLink.setTextColor(ContextCompat.getColor(activity!!, R.color.linkBlue))
        explainLink.gravity = Gravity.CENTER_VERTICAL

        //View separator with gradient
        val gradientSeparator = getGradientSeparator(context)

        ll_horizontal.gravity = Gravity.CENTER_VERTICAL

        val isExpanded = arrayOf(false)
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

        ll_horizontal.addView(explainLink)
        ll_horizontal.addView(gradientSeparator)

        //LL vertical das explicações
        val ll_vertical_expl = LinearLayout(activity)
        ll_vertical_expl.tag = "ll_vertical_expl"
        ll_vertical_expl.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        ll_vertical_expl.orientation = LinearLayout.VERTICAL
        ll_vertical_expl.layoutTransition = LayoutTransition()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val lt = LayoutTransition()
            lt.enableTransitionType(CHANGE_APPEARING)
            lt.enableTransitionType(CHANGE_DISAPPEARING)
            ll_vertical_expl.layoutTransition = lt
        }
        //ProgressBar
        cv_width = activity!!.findViewById<View>(R.id.card_view_1).width
        height_dip = (3 * scale + 0.5f).toInt()
        val progressBar = View(activity)
        progressBar.tag = "progressBar"
        val layoutParams = LinearLayout.LayoutParams(1, height_dip) //Largura, Altura
        progressBar.layoutParams = layoutParams

        //Ponto 1
        val explainTextView_1 = TextView(activity)
        explainTextView_1.tag = "explainTextView_1"
        val fp = getString(R.string.fatores_primos)
        val explain_text_1 = getString(R.string.decompor_num) + " " + fp + "\n"
        val ssb_explain_1 = SpannableStringBuilder(explain_text_1)
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
        explainTextView_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explainTextView_1.text = ssb_explain_1
        explainTextView_1.setTag(R.id.texto, "texto")

        //Ponto 2
        val explainTextView_2 = TextView(activity)
        explainTextView_2.tag = "explainTextView_2"
        val comuns = getString(R.string.comuns)
        val ncomuns = getString(R.string.nao_comuns)
        val uma_vez = getString(R.string.uma_vez)
        val maior_exps = getString(R.string.maior_exps)
        val explain_text_2: String
        if (language == "português" || language == "español" || language == "français") {
            explain_text_2 = getString(R.string.escolher) + " " + getString(R.string.os_fatores) +
                    " " + comuns + " " + getString(R.string.and) + " " + ncomuns + ", " + uma_vez +
                    ", " + getString(R.string.with_the) + " " + maior_exps + ":\n"
        } else {
            explain_text_2 = getString(R.string.escolher) + " " + comuns + " " +
                    getString(R.string.and) + " " + ncomuns + " " + getString(R.string.os_fatores) +
                    ", " + uma_vez + ", " + getString(R.string.with_the) + " " + maior_exps + ":\n"
        }
        val ssb_explain_2 = SpannableStringBuilder(explain_text_2)
        ssb_explain_2.setSpan(
            UnderlineSpan(),
            explain_text_2.indexOf(comuns),
            explain_text_2.indexOf(comuns) + comuns.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb_explain_2.setSpan(
            UnderlineSpan(),
            explain_text_2.indexOf(ncomuns),
            explain_text_2.indexOf(ncomuns) + ncomuns.length,
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
            explain_text_2.indexOf(maior_exps),
            explain_text_2.indexOf(maior_exps) + maior_exps.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //ssb_explain_2.setSpan(new StyleSpan(BOLD), 0, ssb_explain_2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    activity!!,
                    R.color.boldColor
                )
            ), 0, ssb_explain_2.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explainTextView_2.text = ssb_explain_2
        explainTextView_2.setTag(R.id.texto, "texto")

        //Ponto 3
        val explainTextView_3 = TextView(activity)
        explainTextView_3.tag = "explainTextView_3"
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
                    activity!!,
                    R.color.boldColor
                )
            ), 0, ssb_explain_3.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        explainTextView_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, CARD_TEXT_SIZE.toFloat())
        explainTextView_3.text = ssb_explain_3
        explainTextView_3.setTag(R.id.texto, "texto")

        ll_vertical_expl.addView(explainTextView_1)
        ll_vertical_expl.addView(explainTextView_2)
        ll_vertical_expl.addView(explainTextView_3)
        ll_vertical_root.addView(ll_horizontal)
        ll_vertical_root.addView(progressBar)
        ll_vertical_root.addView(ll_vertical_expl)

        if (shouldShowExplanation == "-1") {  //Always show Explanation
            ll_vertical_expl.visibility = View.VISIBLE
            explainLink.text = ssb_hide_expl
            isExpanded[0] = true
        } else if (shouldShowExplanation == "0") { // Show Explanation on demand on click
            ll_vertical_expl.visibility = View.GONE
            explainLink.text = ssb_show_expl
            isExpanded[0] = false
        }
        cardview.addView(ll_vertical_root)

        val thisCardTags = cardview.tag as MyTags
        //        Boolean hasExplanation = thisCardTags.getHasExplanation();
        //        Boolean hasBGOperation = thisCardTags.getHasBGOperation();

        //        if (!hasBGOperation && !hasExplanation) {
        thisCardTags.taskNumber = taskNumber
        val BG_Operation_MMC = BackGroundOperation_MMC(thisCardTags)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        asyncTaskQueue.add(BG_Operation_MMC)
        taskNumber++
        //        }
    }

    fun check_bg_operation(view: View?) {
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
    inner class BackGroundOperation_MMC internal constructor(private var cardTags: MyTags) :
        AsyncTask<Void, Double, Void>() {
        lateinit var theCardViewBG: CardView
        lateinit var mmc_numbers: ArrayList<Long>
        lateinit var result_mmc: BigInteger
        lateinit var bgfatores: ArrayList<ArrayList<Long>>

        lateinit var gradient_separator: TextView
        lateinit var progressBar: View
        lateinit var percent_formatter: NumberFormat
        lateinit var f_colors: IntArray
        var f_colors_length: Int = 0


        public override fun onPreExecute() {
            percent_formatter = DecimalFormat("#.###%")
            theCardViewBG = cardTags.cardView
            progressBar = theCardViewBG.findViewWithTag("progressBar")
            progressBar.visibility = View.VISIBLE
            gradient_separator =
                    theCardViewBG.findViewWithTag<View>("gradient_separator") as TextView
            cardTags.hasBGOperation = true

            val shouldShowColors = sharedPrefs.getBoolean("pref_show_colors", true)
            f_colors = activity!!.resources.getIntArray(R.array.f_colors_xml)
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
                var oldProgress = 0.0
                var progress: Double
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
                    progress = j.toDouble() / (number_i.toDouble() / j.toDouble())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress, i.toDouble())
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

        override fun onProgressUpdate(vararg values: Double?) {

            if (this@MMCFragment.isVisible) {
                val color = fColors[Math.round(values[1]!!).toInt()]
                progressBar.setBackgroundColor(color)
                var value0 = values[0]
                if (value0!! > 1f) value0 = 1.0
                progressBar.layoutParams =
                        LinearLayout.LayoutParams(Math.round(value0 * cv_width).toInt(), height_dip)
                val text =
                    " " + getString(R.string.factorizing) + " " + percent_formatter.format(value0)
                val ssb = SpannableStringBuilder(text)
                ssb.setSpan(ForegroundColorSpan(color), 0, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                gradient_separator.text = ssb
            }
        }

        override fun onPostExecute(result: Void) {
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

                val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false)
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