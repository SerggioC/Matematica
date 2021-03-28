package com.sergiocruz.MatematicaPro.fragment

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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.model.InputTags
import kotlinx.android.synthetic.main.fragment_primorial.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToInt

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 05/02/2017 12:47
 */

class PrimorialFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    private var backgroundTask: AsyncTask<Long, Float, BigInteger> = BackGroundOperation()
    private var num: Long = 0
    private var startTime: Long = 0

    override fun optionsMenu() = R.menu.menu_sub_main

    override var title: Int = R.string.nav_primorial
    override var pageIndex: Int = 8

    override fun getHelpTextId() = R.string.help_text_primorial

    override fun getHelpMenuTitleId() = R.string.action_ajuda_primorial

    override fun getHistoryLayout(): LinearLayout = history

    override fun getLayoutIdForFragment() = R.layout.fragment_primorial

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(backgroundTask, context)) resetButtons()
    }

    override fun onActionDone() = calculatePrimorial()

    private lateinit var bigNumbersTextWatcher: BigNumbersTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                createCardView(fav.primaryKey.toLongOrNull(), fav.content.toBigInteger(), wasCanceled = false, saveToDB = false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_calc_primorial.setOnClickListener { calculatePrimorial() }

        bigNumbersTextWatcher = BigNumbersTextWatcher(inputEditText, shouldFormatNumbers, onEditor = this)
        inputEditText.addTextChangedListener(bigNumbersTextWatcher)

        cancelButton.setOnClickListener { displayCancelDialogBox(requireContext(), title, this) }
        clearButton.setOnClickListener { inputEditText.setText("") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputEditText.removeTextChangedListener(bigNumbersTextWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(backgroundTask, context)
    }

    private fun calculatePrimorial() {
        startTime = System.nanoTime()
        hideKeyboard(activity)
        val editnumText = inputEditText.text.digitsOnly()
        if (TextUtils.isEmpty(editnumText)) {
            showCustomToast(context, getString(R.string.add_num_inteiro))
            return
        }

        try {
            // Tentar converter o string para long
            num = java.lang.Long.parseLong(editnumText)
            if (num == 0L) {
                showCustomToast(context, getString(R.string.zeroPrimorial), InfoLevel.WARNING)
                return
            }
        } catch (e: Exception) {
            showCustomToast(context, getString(R.string.zeroPrimorial), InfoLevel.WARNING)
            return
        }

        // check if temp result exists in DB
        context?.let {
            launchSafeCoroutine {
                val result: HistoryDataClass? = LocalDatabase.getInstance(it).historyDAO()?.getResultForKeyAndOp(num.toString(), operationName)
                if (result != null) {
                    val result: BigInteger = gson.fromJson(result.content, BigInteger::class.java)
                    withContext(Dispatchers.Main) {
                        createCardView(num, result, wasCanceled = false, limitHistory = false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        backgroundTask = BackGroundOperation().execute(num)
                    }
                }
            }
        }

    }

    private fun resetButtons() {
        progressBar.visibility = View.GONE
        button_calc_primorial.setText(R.string.calculate)
        button_calc_primorial.isClickable = true
        cancelButton.visibility = View.GONE
    }

    fun createCardView(number: Long?, bigIntegerResult: BigInteger, wasCanceled: Boolean, limitHistory: Boolean = true, saveToDB: Boolean = true) {
        //criar novo cardview
        val cardView = ClickableCardView(requireActivity())

        cardView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // height
        cardView.preventCornerOverlap = true

        //int pixels = (int) (dips * scale + 0.5f);
        val lrDip = (6 * scale + 0.5f).toInt()
        val tbDip = (8 * scale + 0.5f).toInt()
        cardView.radius = (2 * scale + 0.5f)
        cardView.cardElevation = (2 * scale + 0.5f)
        cardView.setContentPadding(lrDip, tbDip, lrDip, tbDip)
        cardView.useCompatPadding = true

        val cvColor = ContextCompat.getColor(requireActivity(), R.color.cardsColor)
        cardView.setCardBackgroundColor(cvColor)

        if (limitHistory) {
            history.limit(historyLimit)
        }
        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        val text =
                if (shouldFormatNumbers) {
                    number.toString() + "#=\n" + bigIntegerResult.formatForLocale()
                } else {
                    number.toString() + "#=\n" + bigIntegerResult
                }

        val ssb = SpannableStringBuilder(text)
        if (wasCanceled) {
            val incomplete = "\n" + getString(R.string.incomplete_calc)
            ssb.append(incomplete)
            ssb.setSafeSpan(ForegroundColorSpan(Color.RED), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSafeSpan(RelativeSizeSpan(0.8f), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        //Adicionar o texto com o resultado
        val textWithStar = getFavoriteStarForCard(ssb, input = number.toString())

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = LinearLayout.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(SwipeToDismissTouchListener(cardView, requireActivity(), withExplanations = false, inputTags = InputTags(number.toString(), operationName)))

        context?.let {
            val separator = getGradientSeparator(it, shouldShowPerformance, startTime)
            llVerticalRoot.addView(separator, 0)
        }

        llVerticalRoot.addView(textWithStar.root)

        // add the root layout to the cardview
        cardView.addView(llVerticalRoot)

        if (saveToDB) {
            saveCardToDatabase(number.toString(), bigIntegerResult.toString(), operationName)
        }

    }

    lateinit var progressParams: ViewGroup.LayoutParams

    private inner class BackGroundOperation : AsyncTask<Long, Float, BigInteger>() {
        var number: Long? = null
        var primes = ArrayList<Long>()

        public override fun onPreExecute() {
            button_calc_primorial.isClickable = false
            button_calc_primorial.setText(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity)
            progressParams = progressBar.layoutParams
            progressParams.width = 1
            progressBar.layoutParams = progressParams
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg num: Long?): BigInteger {
            number = num[0] ?: return BigInteger.ONE
            if (number == 1L) return BigInteger.ONE
            if (number == 2L) return BigInteger.valueOf(2L)

            primes.add(1L)
            primes.add(2L)

            var progress: Float
            var oldProgress = 0.0f

            for (i in 3L..number!!) {
                var isPrime = true
                if (i % 2 == 0L) isPrime = false
                if (isPrime) {
                    var j: Long = 3
                    while (j < i) {
                        if (i % j == 0L) {
                            isPrime = false
                            break
                        }
                        j += 2
                    }
                }
                if (isPrime) {
                    primes.add(i)
                }
                progress = i.toFloat() / number!!.toFloat()
                if (progress - oldProgress > 0.05) { // update a cada 5%
                    publishProgress(progress)
                    oldProgress = progress
                }
                if (isCancelled) break
            }

            var primorial = BigInteger.ONE
            for (j in primes.indices) {
                primorial = primorial.multiply(BigInteger.valueOf(primes[j]))
            }

            return primorial

        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@PrimorialFragment.isVisible) {
                val fl = values[0] ?: 0f
                progressParams.width = (fl * card_view_1.width).roundToInt()
                progressBar.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: BigInteger) {
            if (this@PrimorialFragment.isVisible) {
                createCardView(number, result, wasCanceled = false, limitHistory = true)
                resetButtons()
            }
        }

        override fun onCancelled(parcial: BigInteger?) {
            super.onCancelled(parcial)
            if (this@PrimorialFragment.isVisible && parcial != null) {
                createCardView(primes[primes.size - 1], parcial, wasCanceled = true, limitHistory = true)
                resetButtons()
            }
        }
    }

}