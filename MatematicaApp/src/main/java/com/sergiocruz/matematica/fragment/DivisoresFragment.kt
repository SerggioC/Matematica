package com.sergiocruz.matematica.fragment

import android.app.Activity
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
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.FragmentDivisoresBinding
import com.sergiocruz.matematica.databinding.ItemResultDivisoresBinding
import com.sergiocruz.matematica.helper.CreateCardView
import com.sergiocruz.matematica.model.InputTags
import kotlinx.coroutines.*
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

class DivisoresFragment : BaseFragment(), OnCancelBackgroundTask {

    private var textWatcher: NumberFormatterTextWatcher? = null
    private var asyncTask: AsyncTask<BigInteger, Float, SpannableStringBuilder> = BackGroundOperation(0)

    override var title: Int = R.string.divisors
    override var pageIndex: Int = 5

    private var binding: FragmentDivisoresBinding? = null

    override fun getLayoutIdForFragment() = R.layout.fragment_divisores

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                createCardView(fav.primaryKey, gson.fromJson(fav.content, SpannableStringBuilder::class.java), saveToDB = false, startTime = System.nanoTime())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDivisoresBinding.bind(view)
        binding?.run {
            cancelButton.setOnClickListener { displayCancelDialogBox(requireContext(), title, this@DivisoresFragment) }
            calculateButton.setOnClickListener { calcDivisors() }
            clearButton.setOnClickListener { inputEditText.setText("") }
            textWatcher = NumberFormatterTextWatcher(inputEditText, shouldFormatNumbers, onEditor = ::calcDivisors)
            inputEditText.addTextChangedListener(textWatcher)
        }

    }

    override fun optionsMenu() = R.menu.menu_main

    override fun getHelpTextId(): Int = R.string.help_text_divisores

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_divisores

    override fun getHistoryLayout(): LinearLayout? = binding?.history

    override fun onOperationCanceled(canceled: Boolean) {
        cancelAsyncTask()
    }

    override fun onDestroyView() {
        binding?.inputEditText?.removeTextChangedListener(textWatcher)
        binding = null
        super.onDestroyView()
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
        val startTime = System.nanoTime()
        hideKeyboard(activity)
        val editnumText = binding?.inputEditText?.text?.digitsOnly()
        if (editnumText.isNullOrEmpty()) {
            showCustomToast(context, getString(R.string.add_num_inteiro))
            return
        }

        val input: BigInteger? = editnumText.toBigIntegerOrNull()
        if (input == null) {
            showCustomToast(context, getString(R.string.invalid_number), InfoLevel.WARNING)
            return
        }

        if (input == BigInteger.ZERO) {
            CreateCardView.withStringRes(getHistoryLayout(), R.string.zero_no_divisores, activity as Activity, operationName)
            return
        }

        context?.let {
            launchSafeCoroutine {
                val result: HistoryDataClass? = LocalDatabase.getInstance(it).historyDAO().getResultForKeyAndOp(input.toString(), operationName)
                if (result != null) {
                    val ssb: SpannableStringBuilder = gson.fromJson(result.content, SpannableStringBuilder::class.java)
                    withContext(Dispatchers.Main) {
                        createCardView(input.toString(), ssb, limit = false, saveToDB = false, startTime)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        asyncTask = BackGroundOperation(startTime).execute(input)
                    }
                }
            }
        }

    }

    fun getAllDivisoresLong(numero: Long?): ArrayList<Long> {
        val upperlimit = sqrt(numero!!.toDouble()).toLong()
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

    inner class BackGroundOperation(private val startTime: Long) : AsyncTask<BigInteger, Float, SpannableStringBuilder>() {
        private var progressParams: ViewGroup.LayoutParams? = null

        private var input: BigInteger? = null

        private var timerJob: Job = Job()

        private var isFinished = false

        public override fun onPreExecute() {
            lockInput()
            binding?.run {
                progressParams = progressBar.layoutParams
                progressParams?.width = 1
                progressBar.layoutParams = progressParams
                progressBar.visibility = View.VISIBLE
            }
        }

        override fun doInBackground(vararg num: BigInteger?): SpannableStringBuilder {
            this.input = num[0]
            /**
             * Performance update
             * Primeiro obtem os fatores primos depois multiplica-os
             * */
            val divisores = ArrayList<BigInteger>()
            var number: BigInteger = num[0] ?: return SpannableStringBuilder()
            var progress: Float
            var oldProgress = 0.0f

            while (number % BigInteger.valueOf(2L) == BigInteger.ZERO) {
                divisores.add(BigInteger.valueOf(2L))
                number /= BigInteger.valueOf(2L)
            }

            var i: BigInteger = BigInteger.valueOf(3L)

            delayedTimerAsync(repeatMillis = 1000, job = timerJob) {
                progress = try {
                    (i.toBigDecimal(scale = 2) / (number.toBigDecimal(scale = 2) / i.toBigDecimal(scale = 2))).toFloat()
                } catch (e: Exception) {
                    0f
                }
                if (isFinished) return@delayedTimerAsync
                publishProgress(progress)
            }
            timerJob.start()

            while (i <= number / i) {
                while (number % i == BigInteger.ZERO) {
                    divisores.add(i)
                    number /= i
                }
                if (isCancelled) break
                i += BigInteger.valueOf(2L)
            }

            if (number > BigInteger.ONE) {
                divisores.add(number)
            }

            val allDivisores = ArrayList<BigInteger>()
            var size: Int
            allDivisores.add(BigInteger.ONE)
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
            val str = allDivisores.joinToString(
                    prefix = "$input ${getString(R.string.has)} ${allDivisores.size} ${getString(R.string.divisores_)}\n{",
                    postfix = "}",
                    transform = { it.toString() }
            )
            val ssb = SpannableStringBuilder(str)
            if (allDivisores.size == 2) {
                val primeNumber = "\n" + getString(R.string._numero_primo)
                ssb.append(primeNumber)
                ssb.setSafeSpan(ForegroundColorSpan(Color.parseColor("#29712d")), ssb.length - primeNumber.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSafeSpan(RelativeSizeSpan(0.9f), ssb.length - primeNumber.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timerJob.cancel()
            return ssb
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@DivisoresFragment.isVisible) {
                val v0 = values.getOrNull(0) ?: return
                progressParams?.width = (v0 * (binding?.viewHeaderDivisores?.width ?: 0)).roundToInt()
                binding?.progressBar?.layoutParams = progressParams
            }
        }

        override fun onPostExecute(result: SpannableStringBuilder) {
            isFinished = true
            if (this@DivisoresFragment.isVisible) {
                createCardView(input.toString(), result, startTime = startTime)
                resetButtons()
            }
        }

        override fun onCancelled(parcial: SpannableStringBuilder?) {
            super.onCancelled(parcial)
            isFinished = true
            if (this@DivisoresFragment.isVisible && parcial != null) {
                val incomplete = getString(R.string.incomplete_calc)
                val ssb = parcial.append("\n").append(incomplete)
                ssb.setSafeSpan(ForegroundColorSpan(Color.RED), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSafeSpan(RelativeSizeSpan(0.8f), ssb.length - incomplete.length, ssb.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                createCardView(input.toString(), ssb, startTime = startTime)
                resetButtons()
            }
        }
    }

    private fun lockInput() {
        binding?.run {
            calculateButton.isClickable = false
            calculateButton.setText(R.string.working)
            cancelButton.visibility = View.VISIBLE
        }
        hideKeyboard(activity)
    }

    private fun resetButtons() {
        binding?.run {
            calculateButton.setText(R.string.calculate)
            calculateButton.isClickable = true
            cancelButton.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    fun createCardView(input: String, ssb: SpannableStringBuilder, limit: Boolean = true, saveToDB: Boolean = true, startTime: Long) {
        ItemResultDivisoresBinding.inflate(layoutInflater).run {
            textViewTop.text = ssb
            showFavoriteStarForInput(imageStar, input)
            textViewPerformance.writePerformanceValue(startTime)
            divisoresRootCardViewItem.setOnTouchListener(SwipeToDismissTouchListener(divisoresRootCardViewItem,
                    activity as Activity,
                    withExplanations = false,
                    inputTags = InputTags(input = input, operation = operationName))
            )

            if (limit) {
                getHistoryLayout()?.limit(historyLimit)
            }
            getHistoryLayout()?.addView(root, 0)
        }

        if (saveToDB) {
            val data = gson.toJson(ssb)
            saveCardToDatabase(input, data, operationName)
        }
    }


}
