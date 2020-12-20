package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.model.InputTags
import kotlinx.android.synthetic.main.fragment_divisores.*
import kotlinx.android.synthetic.main.popup_menu_layout.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.roundToInt

class DivisoresFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    private var textWatcher: BigNumbersTextWatcher? = null
    private var asyncTask: AsyncTask<Long, Float, ArrayList<Long>> = BackGroundOperation()
    internal var num: Long = 0
    private var startTime: Long = 0

    override var title: Int = R.string.divisors
    override var pageIndex: Int = 5

    override fun getLayoutIdForFragment() = R.layout.fragment_divisores

    override fun onActionDone() = calcDivisors()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                createCardView(fav.primaryKey, gson.fromJson(fav.content, SpannableStringBuilder::class.java), saveToDB = false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancelButton.setOnClickListener { displayCancelDialogBox(requireContext(), this) }
        calculateButton.setOnClickListener { calcDivisors() }
        clearButton.setOnClickListener { inputEditText.setText("") }

        textWatcher = BigNumbersTextWatcher(inputEditText, shouldFormatNumbers, this)
        inputEditText.addTextChangedListener(textWatcher)
    }

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getHelpTextId(): Int = R.string.help_text_divisores

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_divisores

    override fun getHistoryLayout(): LinearLayout? = history

    override fun onOperationCanceled(canceled: Boolean) {
        cancelAsyncTask()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputEditText.removeTextChangedListener(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (asyncTask.status == AsyncTask.Status.RUNNING) {
            asyncTask.cancel(true)
            showCustomToast(context, getString(R.string.canceled_op))
        }
    }

    private fun cancelAsyncTask() {
        if (asyncTask.status == AsyncTask.Status.RUNNING) {
            asyncTask.cancel(true)
            showCustomToast(context, getString(R.string.canceled_op), InfoLevel.WARNING)
            resetButtons()
        }
    }

    private fun calcDivisors() {
        startTime = System.nanoTime()
        hideKeyboard(activity as Activity)
        val editnumText = inputEditText.text.digitsOnly()
        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.add_num_inteiro))
            return
        }

        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
            return
        }

        if (editnumText == "0" || num == 0L) {
            CreateCardView.withStringRes(history, R.string.zero_no_divisores, activity as Activity)
            return
        }

        context?.let {
            CoroutineScope(Dispatchers.Default).launch {
                val result: HistoryDataClass? = LocalDatabase.getInstance(it).historyDAO()?.getResultForKeyAndOp(num.toString(), operationName)
                if (result != null) {
                    val ssb: SpannableStringBuilder = gson.fromJson(result.content, SpannableStringBuilder::class.java)
                    withContext(Dispatchers.Main) {
                        createCardView(num.toString(), ssb, limit = false, saveToDB = false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        asyncTask = BackGroundOperation().execute(num)
                    }
                }
            }
        }

    }

    fun getAllDivisoresLong(numero: Long?): ArrayList<Long> {
        val upperlimit = Math.sqrt(numero!!.toDouble()).toLong()
        val divisores = ArrayList<Long>()
        var i = 1
        while (i <= upperlimit) {
            if (numero % i == 0L) {
                divisores.add(i.toLong())
                if (i.toLong() != numero / i) {
                    val elem = numero / i
                    divisores.add(elem)
                }
            }
            i += 1
        }
        divisores.sort()
        return divisores
    }

    lateinit var progressParams: ViewGroup.LayoutParams

    inner class BackGroundOperation : AsyncTask<Long, Float, ArrayList<Long>>() {

        private var input: Long? = null

        public override fun onPreExecute() {
            lockInput()
            progressParams = progressBar.layoutParams
            progressParams.width = 1
            progressBar.layoutParams = progressParams
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): ArrayList<Long> {
            this.input = num[0]
            /**
             * Performance update
             * Primeiro obtem os fatores primos depois multiplica-os
             * */
            val divisores = ArrayList<Long>()
            var number: Long = num[0] ?: return ArrayList()
            var progress: Float
            var oldProgress = 0.0f

            while (number % 2L == 0L) {
                divisores.add(2L)
                number /= 2
            }

            run {
                var i: Long = 3
                while (i <= number / i) {
                    while (number % i == 0L) {
                        divisores.add(i)
                        number /= i
                    }
                    progress = i.toFloat() / (number.toFloat() / i.toFloat())
                    if (progress - oldProgress > 0.1) {
                        publishProgress(progress)
                        oldProgress = progress
                    }
                    if (isCancelled) break
                    i += 2
                }
            }
            if (number > 1) {
                divisores.add(number)
            }

            val allDivisores = ArrayList<Long>()
            var size: Int
            allDivisores.add(1L)
            for (i in divisores.indices) {
                size = allDivisores.size
                for (j in 0 until size) {
                    val valor = allDivisores[j] * divisores[i]
                    if (!allDivisores.contains(valor)) {
                        allDivisores.add(valor)
                    }
                }
            }
            allDivisores.sort()
            return allDivisores
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@DivisoresFragment.isVisible) {
                val v0 = values[0] ?: return
                progressParams.width = (v0 * card_view_1.width).roundToInt()
                progressBar.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: ArrayList<Long>) {
            if (this@DivisoresFragment.isVisible) {
                var str = ""
                for (i in result) {
                    str = "$str, $i"
                    if (i == 1L) {
                        str = num.toString() + " " + getString(R.string.has) + " " + result.size +
                                " " + getString(R.string.divisores_) + "\n{" + i
                    }
                }
                val strDivisores = "$str}"
                val ssb = SpannableStringBuilder(strDivisores)
                if (result.size == 2) {
                    val primeNumber = "\n" + getString(R.string._numero_primo)
                    ssb.append(primeNumber)
                    ssb.setSafeSpan(ForegroundColorSpan(Color.parseColor("#29712d")), ssb.length - primeNumber.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssb.setSafeSpan(RelativeSizeSpan(0.9f), ssb.length - primeNumber.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                createCardView(input.toString(), ssb)
                resetButtons()
            }
        }

        override fun onCancelled(parcial: ArrayList<Long>?) {
            super.onCancelled(parcial)
            if (this@DivisoresFragment.isVisible && parcial != null) {
                var str = ""
                for (i in parcial) {
                    str = "$str, $i"
                    if (i == 1L) {
                        str = getString(R.string.divisors_of) + " " + num + ":\n" + "{" + i
                    }
                }
                val strDivisores = "$str}"
                val ssb = SpannableStringBuilder(strDivisores)
                val incompleteCalc = "\n" + getString(R.string._incomplete_calc)
                ssb.append(incompleteCalc)
                ssb.setSafeSpan(ForegroundColorSpan(Color.RED), ssb.length - incompleteCalc.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSafeSpan(RelativeSizeSpan(0.8f), ssb.length - incompleteCalc.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                createCardView(input.toString(), ssb)
                resetButtons()
            }
        }
    }

    private fun lockInput() {
        calculateButton.isClickable = false
        calculateButton.setText(R.string.working)
        cancelButton.visibility = View.VISIBLE
        hideKeyboard(activity)
    }

    private fun resetButtons() {
        calculateButton.setText(R.string.calculate)
        calculateButton.isClickable = true
        cancelButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    fun createCardView(input: String, ssb: SpannableStringBuilder, limit: Boolean = true, saveToDB: Boolean = true) {
        //criar novo cardview
        val cardView = ClickableCardView(activity as Activity)
        cardView.tag = InputTags(input = input, operation = operationName)
        cardView.layoutParams = getMatchWrapParams()
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f)
        cardView.cardElevation = (2 * scale + 0.5f)
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        val color = ContextCompat.getColor(requireContext(), R.color.cardsColor)
        cardView.setCardBackgroundColor(color)

        // Add cardview to history layout at the top (index 0)
        if (limit) {
            history.limit(historyLimit)
        }
        history.addView(cardView, 0)

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(SwipeToDismissTouchListener(cardView, activity as Activity))

        context?.let {
            val separator = getGradientSeparator(it, shouldShowPerformance, startTime)
            llVerticalRoot.addView(separator)
        }

        // criar novo Textview para o resultado da fatorização e estrela dos favoritos
        val textWithStar = getFavoriteStarForCard(ssb, input)
        llVerticalRoot.addView(textWithStar.root)

        // add the textview to the cardview
        cardView.addView(llVerticalRoot)

        if (saveToDB) {
            val data = gson.toJson(ssb)
            saveCardToDatabase(input, data, operationName)
        }
    }


}
