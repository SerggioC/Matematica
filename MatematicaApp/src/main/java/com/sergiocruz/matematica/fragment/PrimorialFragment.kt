package com.sergiocruz.matematica.fragment

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.InputTags
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.FragmentPrimorialBinding
import com.sergiocruz.matematica.databinding.ItemPrimorialResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToInt

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.matematica.fragment
 * Created by Sergio on 05/02/2017 12:47
 */

class PrimorialFragment : BaseFragment(), OnCancelBackgroundTask {

    private var backgroundTask: AsyncTask<Long, Float, BigInteger> = BackGroundOperation()
    private var num: Long = 0
    private var startTime: Long = 0
    private var binding: FragmentPrimorialBinding? = null

    override fun optionsMenu() = R.menu.menu_main

    override var title: Int = R.string.nav_primorial
    override var pageIndex: Int = 8

    override fun getHelpTextId() = R.string.help_text_primorial

    override fun getHelpMenuTitleId() = R.string.action_ajuda_primorial

    override fun getHistoryLayout(): LinearLayout? = binding?.history

    override fun getLayoutIdForFragment() = R.layout.fragment_primorial

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(backgroundTask, context)) resetButtons()
    }

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

        binding = FragmentPrimorialBinding.bind(view)
        binding?.run {
            buttonCalcPrimorial.setOnClickListener { calculatePrimorial() }
            bigNumbersTextWatcher = BigNumbersTextWatcher(inputEditText, shouldFormatNumbers, onEditor = ::calculatePrimorial)
            inputEditText.addTextChangedListener(bigNumbersTextWatcher)
            cancelButton.setOnClickListener { displayCancelDialogBox(requireContext(), title, this@PrimorialFragment) }
            clearButton.setOnClickListener { inputEditText.setText("") }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.inputEditText?.removeTextChangedListener(bigNumbersTextWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(backgroundTask, context)
    }

    private fun calculatePrimorial() {
        startTime = System.nanoTime()
        hideKeyboard(activity)
        val editnumText = binding?.inputEditText?.text.digitsOnly()
        if (editnumText.isEmpty()) {
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
                val result: HistoryDataClass? = LocalDatabase.getInstance(it).historyDAO().getResultForKeyAndOp(num.toString(), operationName)
                if (result != null) {
                    val bi: BigInteger = gson.fromJson(result.content, BigInteger::class.java)
                    withContext(Dispatchers.Main) {
                        createCardView(num, bi, wasCanceled = false, limitHistory = false)
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
        binding?.run {
            progressBar.visibility = View.GONE
            buttonCalcPrimorial.setText(R.string.calculate)
            buttonCalcPrimorial.isClickable = true
            cancelButton.visibility = View.GONE
        }
    }

    fun createCardView(number: Long?, bigIntegerResult: BigInteger, wasCanceled: Boolean, limitHistory: Boolean = true, saveToDB: Boolean = true) {
        val itemBinding = ItemPrimorialResultBinding.inflate(layoutInflater)
        itemBinding.run {
            val text =
                    if (shouldFormatNumbers) {
                        number.toString() + "#=\n" + bigIntegerResult.formatForLocale()
                    } else {
                        number.toString() + "#=\n" + bigIntegerResult
                    }

            val ssb = SpannableStringBuilder(text)
            textViewTop.text = ssb
            textViewPerformance.writePerformanceValue(startTime)
            showFavoriteStarForInput(imageStar, input = number.toString())
            if (wasCanceled) {
                val incomplete = "\n" + getString(R.string.incomplete_calc)
                ssb.append(incomplete)
                ssb.setSafeSpan(ForegroundColorSpan(Color.RED), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSafeSpan(RelativeSizeSpan(0.8f), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            activity?.let {
                root.setOnTouchListener(SwipeToDismissTouchListener(root, it, withExplanations = false, inputTags = InputTags(number.toString(), operationName)))
            }
            if (limitHistory) {
                getHistoryLayout()?.limit(historyLimit)
            }
            getHistoryLayout()?.addView(root, 0)
        }

        if (saveToDB) {
            saveCardToDatabase(number.toString(), bigIntegerResult.toString(), operationName)
        }

    }

    lateinit var progressParams: ViewGroup.LayoutParams

    private inner class BackGroundOperation : AsyncTask<Long, Float, BigInteger>() {
        private var input: Long = 0
        private var primes = ArrayList<Long>()

        public override fun onPreExecute() {
            binding?.run {
                buttonCalcPrimorial.isClickable = false
                buttonCalcPrimorial.setText(R.string.working)
                cancelButton.visibility = View.VISIBLE
                hideKeyboard(activity)
                progressParams = progressBar.layoutParams
                progressParams.width = 1
                progressBar.layoutParams = progressParams
                progressBar.visibility = View.VISIBLE
            }
        }

        override fun doInBackground(vararg num: Long?): BigInteger {
            input = num[0] ?: return BigInteger.ONE
            if (input == 1L) return BigInteger.ONE
            if (input == 2L) return BigInteger.valueOf(2L)

            primes.add(1L)
            primes.add(2L)

            var progress: Float
            var oldProgress = 0.0f

            for (i in 3L..input) {
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
                progress = i.toFloat() / input.toFloat()
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
                progressParams.width = (fl * (binding?.cardView1?.width ?: 0)).roundToInt()
                binding?.progressBar?.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: BigInteger) {
            if (this@PrimorialFragment.isVisible) {
                createCardView(input, result, wasCanceled = false, limitHistory = true)
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