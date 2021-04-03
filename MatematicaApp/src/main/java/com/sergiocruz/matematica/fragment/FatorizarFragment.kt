package com.sergiocruz.matematica.fragment

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.style.*
import android.view.*
import android.widget.LinearLayout
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.FactorizationData
import com.sergiocruz.matematica.tasks.FactorizationTask
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToInt

class FatorizarFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    private lateinit var textWatcher: BigNumbersTextWatcher

    private var bgOperation: AsyncTask<Unit?, Float, ArrayList<ArrayList<BigInteger>>>? = null
    private var startTime: Long = 0L
    private var timerJob: Job = Job()
    private lateinit var progressParams: ViewGroup.LayoutParams

    private var isCalculating = false
        set(value) {
            field = value
            if (value) {
                calculateButton.isClickable = false
                calculateButton.text = getString(R.string.working)
                Handler(Looper.getMainLooper()).postDelayed({
                    cancelButton?.visibility = if (isCalculating) View.VISIBLE else View.GONE
                }, 2500)
                hideKeyboard(activity)
                progressParams = progressBar.layoutParams
                progressParams.width = 1
                progressBar.layoutParams = progressParams
                progressBar.visibility = View.VISIBLE
                elapsedTimeMillis.text = ""
                clearErrorDelayMillis
                if (shouldShowPerformance) {
                    delayedTimerAsync(job = timerJob) {
                        elapsedTimeMillis?.text = "$it ms"
                    }
                }
            } else {
                calculateButton.setText(R.string.calculate)
                calculateButton.isClickable = true
                cancelButton.visibility = View.GONE
                progressBar.visibility = View.GONE
                timerJob?.cancel()
                Handler(Looper.getMainLooper()).postDelayed({
                    elapsedTimeMillis?.visibility = View.GONE
                }, 2500)
            }
        }

    override fun getLayoutIdForFragment() = R.layout.fragment_fatorizar

    override fun optionsMenu() = R.menu.menu_main

    override var title = R.string.factorize

    override var pageIndex = 4

    override fun getHelpTextId(): Int = R.string.help_text_fatores

    override fun getHelpMenuTitleId(): Int = R.string.action_ajuda_fatorizar

    override fun getHistoryLayout(): LinearLayout? = history

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                startTime = System.nanoTime()
                val data = gson.fromJson(fav.content, FactorizationData::class.java) ?: return@forEach
                val cardView = FactorizationTask.createCardViewLayout(
                        data = data,
                        isFavorite = true,
                        showPerformance = shouldShowPerformance,
                        explanations = explanations,
                        operationName = operationName,
                        startTime = startTime,
                        context = requireActivity(),
                )
                getHistoryLayout()?.limit(historyLimit)
                getHistoryLayout()?.addView(cardView, 0)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cancelButton.setOnClickListener { displayCancelDialogBox(it.context, title, this) }
        calculateButton.setOnClickListener { calculatePrimeFactors() }
        clearButton.setOnClickListener { factorizeTextView.setText("") }

        textWatcher = BigNumbersTextWatcher(factorizeTextView, shouldFormatNumbers, ignoreLongNumbers = true, this)
        factorizeTextView.addTextChangedListener(textWatcher)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(bgOperation, context)) {
            isCalculating = false
        }
    }

    override fun onActionDone() {
        calculatePrimeFactors()
    }

    private fun calculatePrimeFactors() {
        startTime = System.nanoTime()
        val num: BigInteger? = factorizeTextView.text.digitsOnly().toBigIntegerOrNull()

        if (num == null) {
            showCustomToast(context, getString(R.string.insert_integer))
            return
        }

        if (num == BigInteger.ZERO || num == BigInteger.ONE) {
            showCustomToast(context, getString(R.string.the_number) + " " + num + " " + getString(R.string.has_no_factors))
            return
        }

        // Check if result exists in DB
        context?.let { ctx ->
            isCalculating = true
            launchSafeCoroutine {
                val result: HistoryDataClass? = LocalDatabase.getInstance(ctx).historyDAO()?.getResultForKeyAndOp(num.toString(), operationName)
                val isFavorite = LocalDatabase.getInstance(ctx).historyDAO()?.getFavoriteForKeyAndOp(key = num.toString(), operation = operationName) != null
                lateinit var fd: FactorizationData
                if (result != null) {
                    fd = gson.fromJson(result.content, FactorizationData::class.java)
                }
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        val cardView = FactorizationTask.createCardViewLayout(
                                data = fd,
                                isFavorite = isFavorite,
                                showPerformance = shouldShowPerformance,
                                explanations = explanations,
                                operationName = operationName,
                                startTime = startTime,
                                context = requireActivity(),
                        )
                        getHistoryLayout()?.limit(historyLimit)
                        getHistoryLayout()?.addView(cardView, 0)
                        isCalculating = false
                    } else {
                        bgOperation = FactorizationTask(
                                numberToFactorize = num,
                                factorsColors = getRandomFactorsColors(),
                                incompleteCalcText = getString(R.string.incomplete_calc),
                                updateProgress = {
                                    progressParams.width = (it * card_view_1.width).roundToInt()
                                    progressBar.layoutParams = progressParams
                                },
                                onFinished = { fd: FactorizationData ->
                                    val cardView = FactorizationTask.createCardViewLayout(
                                            data = fd,
                                            isFavorite = isFavorite,
                                            showPerformance = shouldShowPerformance,
                                            explanations = explanations,
                                            operationName = operationName,
                                            startTime = startTime,
                                            context = requireActivity(),
                                    )
                                    getHistoryLayout()?.limit(historyLimit)
                                    getHistoryLayout()?.addView(cardView, 0)
                                    val data = gson.toJson(fd, FactorizationData::class.java)
                                    saveCardToDatabase(fd.numberToFatorize.toString(), data, operationName)
                                    isCalculating = false
                                },
                        ).execute()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        factorizeTextView.removeTextChangedListener(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(bgOperation, context)
    }


}