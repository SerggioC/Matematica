package com.sergiocruz.matematica.fragment

/**
 * Created by Sergio on 13/05/2017.
 */

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.ItemResultMultiplosBinding
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.InputTags
import com.sergiocruz.matematica.model.MultiplesData
import kotlinx.android.synthetic.main.fragment_multiplos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.DecimalFormat

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.matematica.fragment
 * Created by Sergio on 13/05/2017 14:00
 */

class MultiplosFragment : BaseFragment(), OnEditorActions {

    internal var startTime: Long = 0

    override var title = R.string.nav_multiplos

    override var pageIndex: Int = 7

    override fun getHelpTextId() = R.string.help_text_multiplos

    override fun getHelpMenuTitleId() = R.string.action_ajuda_multiplos

    override fun getHistoryLayout(): LinearLayout = history

    override fun optionsMenu() = R.menu.menu_main

    override fun getLayoutIdForFragment() = R.layout.fragment_multiplos

    override fun onActionDone() = calculateMultiples()

    private var bigNumbersTextWatcher: BigNumbersTextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                gson.fromJson(fav.content, MultiplesData::class.java)?.let { md ->
                    createCardViewMultiplos(fav.primaryKey.toBigInteger(), multiplos = md.stringMultiplos, lastIteration = md.lastIteration)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bigNumbersTextWatcher = BigNumbersTextWatcher(editNumMultiplos, shouldFormatNumbers, onEditor = this)
        editNumMultiplos.addTextChangedListener(bigNumbersTextWatcher)

        clearButton.setOnClickListener { editNumMultiplos.setText("") }
        buttonCalcMultiplos.setOnClickListener { calculateMultiples() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editNumMultiplos.removeTextChangedListener(bigNumbersTextWatcher)
    }

    private fun calculateMultiples() {
        startTime = System.nanoTime()
        hideKeyboard(activity)
        val strNum = editNumMultiplos.text.digitsOnly()
        if (TextUtils.isEmpty(strNum)) {
            showCustomToast(context, getString(R.string.add_num_inteiro), InfoLevel.WARNING)
            editNumMultiplos.apply {
                requestFocus()
                error = getString(R.string.add_num_inteiro)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            return
        }
        if (strNum == "0") {
            context?.let { ctx ->
                launchSafeCoroutine {
                    val newMD = gson.toJson(MultiplesData("{0}", 0))
                    LocalDatabase.getInstance(ctx).historyDAO()?.saveResult(HistoryDataClass("0", operationName, newMD, favorite = false))
                    withContext(Dispatchers.Main) {
                        createCardViewMultiplos(BigInteger.ZERO, multiplos = "{0}", lastIteration = 0)
                    }
                }
            }
            return
        }

        val num: BigInteger? = strNum.toBigIntegerOrNull()
        if (num == null) {
            showCustomToast(context, getString(R.string.numero_alto), InfoLevel.WARNING)
            return
        }

        context?.let { ctx ->
            val maxMultiplos = spinnerMultiplosCount.selectedItem.toString().toLongOrNull() ?: 0L
            launchSafeCoroutine {
                val isFavorite = LocalDatabase.getInstance(ctx).historyDAO()?.getFavoriteForKeyAndOp(key = num.toString(), operation = operationName) != null
                val result: HistoryDataClass? = LocalDatabase.getInstance(ctx).historyDAO()?.getResultForKeyAndOp(num.toString(), operationName)
                val md = result?.content?.let {
                    gson.fromJson(it, MultiplesData::class.java)
                }
                val multiplosStr: String
                val lastIteration: Long
                if (md == null) {
                    multiplosStr = calculateMultiplos(number = num, minIteration = 0, iterations = maxMultiplos)
                    lastIteration = 10L
                    val newMD = gson.toJson(MultiplesData(multiplosStr, lastIteration))
                    LocalDatabase.getInstance(ctx).historyDAO()?.saveResult(HistoryDataClass(num.toString(), operationName, newMD, isFavorite))
                } else {
                    multiplosStr = md.stringMultiplos
                    lastIteration = md.lastIteration
                    val newMD = gson.toJson(MultiplesData(multiplosStr, lastIteration))
                    LocalDatabase.getInstance(ctx).historyDAO()?.updateHistoryData(key = num.toString(), operationName, newMD)
                }

                withContext(Dispatchers.Main) {
                    createCardViewMultiplos(num, multiplos = multiplosStr, lastIteration = lastIteration)
                }
            }
        }

    }

    private fun createCardViewMultiplos(number: BigInteger, multiplos: String, lastIteration: Long) {
        val layout = ItemResultMultiplosBinding.inflate(layoutInflater)
        with(layout) {
            // Create a generic swipe-to-dismiss touch listener.
            root.setOnTouchListener(SwipeToDismissTouchListener(root, requireActivity(), withExplanations = false, inputTags = InputTags(number.toString(), operationName)))
            val text = if (shouldFormatNumbers) {
                getString(R.string.multiplosde) + " " + number.formatForLocale() + "=\n" + multiplos
            } else {
                getString(R.string.multiplosde) + " " + number + "=\n" + multiplos
            }

            textViewTop.text = text
            textViewTop.setTag(R.id.texto, "texto")

            if (shouldShowPerformance) {
                val formatter1 = DecimalFormat("#.###")
                val elapsed = root.context.getString(R.string.performance) + " " + formatter1.format((System.nanoTime() - startTime) / 1_000_000_000.0) + "s"
                textViewPerformance.text = elapsed
                textViewPerformance.visibility = View.VISIBLE
            } else {
                textViewPerformance.visibility = View.GONE
            }

            showFavoriteStarForInput(imageStar, number.toString())

            if (number > BigInteger.ZERO) {
                textViewShowMore.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                textViewShowMore.setOnClickListener {
                    startTime = System.nanoTime()
                    val iterations = spinnerMultiplosCount.selectedItem.toString().toLongOrNull() ?: 10L
                    launchSafeCoroutine {
                        val savedResult: HistoryDataClass? = LocalDatabase.getInstance(root.context).historyDAO()?.getResultForKeyAndOp(number.toString(), operationName)
                        val saved = savedResult?.content?.let {
                            gson.fromJson(it, MultiplesData::class.java)
                        }

                        val multiplosStr = calculateMultiplos(number = number, minIteration = saved?.lastIteration ?: lastIteration, iterations = iterations)
                        val multiplesData = MultiplesData((saved?.stringMultiplos ?: "").dropLast(3) + multiplosStr, (saved?.lastIteration ?: lastIteration) + iterations)
                        val newData = gson.toJson(multiplesData)
                        LocalDatabase.getInstance(root.context).historyDAO()?.updateHistoryData(key = number.toString(), operationName, newData)
                        withContext(Dispatchers.Main) {
                            if (shouldShowPerformance) {
                                val decimalFormatter = DecimalFormat("#.###")
                                val elapsed = getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                                textViewPerformance.text = elapsed
                            } else {
                                textViewPerformance.visibility = View.GONE
                            }
                            val print = textViewTop.text.toString().dropLast(3) + multiplosStr
                            textViewTop.text = print
                        }
                    }
                }
            } else {
                textViewShowMore.visibility = View.GONE
            }
            history.limit(historyLimit)
            history.addView(root, 0)
        }

    }

    private fun calculateMultiplos(number: BigInteger, minIteration: Long, iterations: Long): String {
        val maxIteration = minIteration + iterations
        val stringMultiples = StringBuilder()
        for (i in minIteration until maxIteration) {
            val bigNumber = number.multiply(BigInteger.valueOf(i))
            stringMultiples.append(if (shouldFormatNumbers) {
                "${bigNumber.formatForLocale()}, "
            } else {
                "$bigNumber, "
            })
        }
        stringMultiples.append("...")
        return stringMultiples.toString()
    }

}
