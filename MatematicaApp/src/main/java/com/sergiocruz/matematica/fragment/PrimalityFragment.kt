package com.sergiocruz.matematica.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.ItemResultPrimalityBinding
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.FactorizationData
import com.sergiocruz.matematica.model.InputTags
import com.sergiocruz.matematica.model.OperationStatusTags
import com.sergiocruz.matematica.model.PrimalityData
import com.sergiocruz.matematica.tasks.FactorizationTask
import kotlinx.android.synthetic.main.fragment_fatorizar.*
import kotlinx.android.synthetic.main.fragment_primality.*
import kotlinx.android.synthetic.main.fragment_primality.calculateButton
import kotlinx.android.synthetic.main.fragment_primality.clearButton
import kotlinx.android.synthetic.main.fragment_primality.history
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class PrimalityFragment : BaseFragment() {

    private var startTime: Long = 0

    override var title = R.string.primality
    override var pageIndex: Int = 1

    private var textWatcher: TextWatcher? = null

    override fun getHelpTextId(): Int? = null

    override fun getHelpMenuTitleId(): Int? = null

    override fun getHistoryLayout(): LinearLayout? = history

    override fun optionsMenu() = R.menu.menu_main

    override fun getLayoutIdForFragment() = R.layout.fragment_primality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            startTime = System.nanoTime()
            list?.forEach { fav ->
                val data = gson.fromJson(fav.content, PrimalityData::class.java) ?: return@forEach
                createCardView(fav.primaryKey.toBigIntegerOrNull() ?: BigInteger.ZERO, data.isPrime, data.factorizationData)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateButton.setOnClickListener { checkNumberFromInput() }
        clearButton.setOnClickListener { inputEditText.setText("") }

        val onEditor = object : OnEditorActions {
            override fun onActionDone() {
                checkNumberFromInput()
            }
        }

        textWatcher = NumberFormatterTextWatcher(inputEditText, shouldFormatNumbers, onEditor)
        inputEditText.addTextChangedListener(textWatcher)

    }

    private fun checkNumberFromInput() {
        val number = inputEditText.text.digitsOnly()
        val bigNumber = number.toBigIntegerOrNull()
        if (bigNumber != null) {
            checkIfProbablePrime(bigNumber)
        } else {
            showCustomToast(context, getString(R.string.invalid_number), InfoLevel.WARNING)
        }
    }

    private fun checkIfProbablePrime(bigNumber: BigInteger) {
        startTime = System.nanoTime()
        val isPrime = bigNumber.isProbablePrime(100)
        hideKeyboard(activity)
        context?.let { ctx ->
            launchSafeCoroutine {
                val savedFavorite = LocalDatabase.getInstance(ctx).historyDAO()?.getFavoriteForKeyAndOp(key = bigNumber.toString(), operation = operationName)
                val fav = if (savedFavorite?.content != null) {
                    gson.fromJson(savedFavorite.content, PrimalityData::class.java).factorizationData
                } else null
                withContext(Dispatchers.Main) {
                    createCardView(bigNumber, isPrime, factorizationData = fav)
                }
                val data = gson.toJson(PrimalityData(isPrime), PrimalityData::class.java)
                saveCardToDatabase(bigNumber.toString(), data, operationName)
            }
        }
    }

    private var currentTasks: MutableMap<String, AsyncTask<Unit?, Float, ArrayList<ArrayList<BigInteger>>>> = mutableMapOf()

    @SuppressLint("SetTextI18n")
    private fun createCardView(bigNumber: BigInteger, isPrime: Boolean, factorizationData: FactorizationData?) {
        val layout = ItemResultPrimalityBinding.inflate(LayoutInflater.from(context))
        with(layout) {

            val color = ContextCompat.getColor(requireContext(), if (isPrime) R.color.greener else R.color.cardsColor)
            root.setCardBackgroundColor(color)

            val theNumber = if (shouldFormatNumbers) {
                NumberFormat
                        .getNumberInstance(Locale.getDefault())
                        .format(bigNumber)
            } else {
                bigNumber.toString()
            }

            textViewTop.text = "$theNumber \n ${
                if (isPrime)
                    getString(R.string.prime_number) else
                    getString(R.string.not_prime_number)
            }"

            if (shouldShowPerformance) {
                val formatter = DecimalFormat("#.###")
                val elapsed = context?.getString(R.string.performance) + " " + formatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                textViewPerformance.text = elapsed
                textViewPerformance.visibility = View.VISIBLE
            } else {
                textViewPerformance.visibility = View.GONE
            }

            if (withExplanations.not() || isPrime || bigNumber < BigInteger.valueOf(2L)) {
                fatorizeLink.visibility = View.GONE
                explain.explainContainer.visibility = View.GONE
                if (shouldShowPerformance.not()) {
                    gradientSeparator.visibility = View.GONE
                }
            } else if (withExplanations && isPrime.not()) {
                fatorizeLink.tag = OperationStatusTags()
                val progressParams: ViewGroup.LayoutParams = progressBar.layoutParams
                progressParams.width = 1
                progressBar.layoutParams = progressParams
                progressBar.visibility = View.VISIBLE

                fun displayFactorizationData(fd: FactorizationData) {
                    explain.explainContainer.visibility = View.VISIBLE
                    explain.textViewResults.text = fd.strResults
                    explain.textViewDivisores.text = fd.ssbStrDivisores
                    explain.textViewAllResults.text = fd.strFactExp
                    if (fd.hasExpoentes) {
                        explain.explainTitle3.visibility = View.VISIBLE
                        explain.textViewFatores.visibility = View.VISIBLE
                        explain.textViewFatores.text = fd.ssbFatores
                    } else {
                        explain.explainTitle3.visibility = View.GONE
                        explain.textViewFatores.visibility = View.GONE
                    }

                    if ((fatorizeLink.tag as? OperationStatusTags)?.vtoIsCompleted == false) {
                        explain.explainContainer.viewTreeObserver.let { vto ->
                            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    if (vto.isAlive) {
                                        vto.removeOnGlobalLayoutListener(this)
                                    } else {
                                        explain.explainContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                    }
                                    val hei = explain.explainContainer.measuredHeight
                                    explain.explainContainer.setTag(R.id.initialHeight, hei)
                                    fatorizeLink.setText(R.string.hide_fatorize)
                                    explain.explainContainer.visibility = View.GONE
                                    val ost = fatorizeLink.tag as? OperationStatusTags
                                    if (ost?.isExpanded == false) {
                                        expandThis(explain.explainContainer)
                                        ost.isExpanded = true
                                    }
                                    ost?.vtoIsCompleted = true
                                    fatorizeLink.tag = ost
                                }
                            })
                        }
                    }
                }

                fatorizeLink.setOnClickListener { tv -> tv as TextView
                    val tags = tv.tag as OperationStatusTags

                    if (factorizationData != null) {
                        tags.isCalculating = false
                        tags.isCompleted = true
                        tv.tag = tags
                        displayFactorizationData(factorizationData)

                    } else if (tags.isCalculating.not() && tags.isCompleted.not() && bigNumber > BigInteger.valueOf(3)) {
                        tags.isCalculating = true
                        tags.isCompleted = false
                        tv.tag = tags
                        val task = FactorizationTask(
                                numberToFactorize = bigNumber,
                                incompleteCalcText = context?.getString(R.string.incomplete_calc) ?: "",
                                factorsColors = getRandomFactorsColors(),
                                updateProgress = { fraction ->
                                    progressParams.width = (fraction * content.width).roundToInt()
                                    progressBar.layoutParams = progressParams
                                },
                                onFinished = { fd: FactorizationData ->
                                    tags.isCalculating = false
                                    tags.isCompleted = true
                                    tv.tag = tags
                                    progressBar.visibility = View.GONE
                                    currentTasks.remove(bigNumber.toString())

                                    displayFactorizationData(fd)

                                    context?.let { ctx ->
                                        launchSafeCoroutine {
                                            val pd = PrimalityData(isPrime = false, factorizationData = fd)
                                            val data = gson.toJson(pd, PrimalityData::class.java)
                                            LocalDatabase.getInstance(ctx).historyDAO()?.updateHistoryData(key = bigNumber.toString(), operationName, data)
                                        }
                                    }
                                }
                        ).execute()
                        currentTasks[bigNumber.toString()] = task
                        fatorizeLink.setText(R.string.calculating)
                    }

                    if (tags.isCompleted) {
                        if (tags.isExpanded) {
                            collapseThis(explain.explainContainer)
                            tags.isExpanded = false
                            fatorizeLink.tag = tags
                            tv.setText(R.string.show_fatorize)
                        } else if (tags.vtoIsCompleted) {
                            expandThis(explain.explainContainer)
                            tags.isExpanded = true
                            fatorizeLink.tag = tags
                            tv.setText(R.string.hide_fatorize)
                        }
                    }

                }

                if (explanations == Explanations.Always) {
                    fatorizeLink.callOnClick()
                }
            }

            showFavoriteStarForInput(imageStar, bigNumber.toString())

            root.setOnTouchListener(SwipeToDismissTouchListener(root,
                    activity as Activity,
                    withExplanations = false,
                    inputTags = InputTags(input = bigNumber.toString(), operation = operationName))
            )

            history.limit(historyLimit)
            history.addView(root, 0)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputEditText?.removeTextChangedListener(textWatcher)
    }

}