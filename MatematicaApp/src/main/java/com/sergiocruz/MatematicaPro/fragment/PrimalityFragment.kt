package com.sergiocruz.MatematicaPro.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.Ui.ClickableCardView
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.helper.*
import kotlinx.android.synthetic.main.fragment_primality.calculateButton
import kotlinx.android.synthetic.main.fragment_primality.clearButton
import kotlinx.android.synthetic.main.fragment_primality.history
import kotlinx.android.synthetic.main.fragment_primality.inputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.text.NumberFormat
import java.util.*

@SuppressLint("LogNotTimber")
class PrimalityFragment : BaseFragment() {

    private var startTime: Long = 0

    override var title = R.string.primality
    override var pageIndex: Int = 1

    private var textWatcher: TextWatcher? = null

    override fun getHelpTextId(): Int? = null

    override fun getHelpMenuTitleId(): Int? = null

    override fun getHistoryLayout(): LinearLayout? = history

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getLayoutIdForFragment() = R.layout.fragment_primality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allFavoritesCallback = { list: List<HistoryDataClass>? ->
            list?.forEach { fav ->
                createCardView(fav.primaryKey.toBigIntegerOrNull() ?: BigInteger.ZERO, fav.content.toBoolean())
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
        createCardView(bigNumber, isPrime)
    }

    @SuppressLint("SetTextI18n")
    private fun createCardView(bigNumber: BigInteger, isPrime: Boolean) {
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

        if (isPrime) {
            val color = ContextCompat.getColor(requireContext(), R.color.greener)
            cardView.setCardBackgroundColor(color)
        } else {
            val color = ContextCompat.getColor(requireContext(), R.color.cardsColor)
            cardView.setCardBackgroundColor(color)
        }

        history.limit(historyLimit)

        // Add cardview to history layout at the top (index 0)
        history.addView(cardView, 0)

        // criar novo Textview
        val textView = TextView(activity)
        textView.layoutParams = getMatchWrapParams()

        val theNumber = if (shouldFormatNumbers) {
            NumberFormat
                    .getNumberInstance(Locale.getDefault())
                    .format(bigNumber)
        } else {
            bigNumber.toString()
        }

        //Adicionar o texto com o resultado
        textView.text = "$theNumber \n ${
            if (isPrime)
                getString(R.string.prime_number) else
                getString(R.string.not_prime_number)
        }"

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setTag(R.id.texto, "texto")
        textView.gravity = Gravity.CENTER_HORIZONTAL

        val llVerticalRoot = LinearLayout(activity)
        llVerticalRoot.layoutParams = getMatchWrapParams()
        llVerticalRoot.orientation = LinearLayout.VERTICAL

        // Create a generic swipe-to-dismiss touch listener.
        cardView.setOnTouchListener(
                SwipeToDismissTouchListener(
                        cardView,
                        activity as Activity,
                        object : SwipeToDismissTouchListener.DismissCallbacks {
                            override fun onDismiss(view: View?) {
                                CoroutineScope(Dispatchers.Default).launch {
                                    context?.let { ctx ->
                                        LocalDatabase.getInstance(ctx).historyDAO()?.deleteHistoryItem(cardView.getTag(R.id.pk) as String, PrimalityFragment::class.java.simpleName)
                                    }
                                }
                                history.removeView(cardView)
                            }
                        })
        )
        context?.let {
            val separator = getGradientSeparator(it, false, startTime, bigNumber.toString(), PrimalityFragment::class.java.simpleName)
            llVerticalRoot.addView(separator)
        }
        llVerticalRoot.addView(textView)

        cardView.setTag(R.id.pk, bigNumber.toString())
        cardView.setTag(R.id.op, PrimalityFragment::class.java.simpleName)

        saveCardToDatabase(bigNumber.toString(), isPrime.toString(), PrimalityFragment::class.java.simpleName)

        // add the textview to the cardview
        cardView.addView(llVerticalRoot)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputEditText?.removeTextChangedListener(textWatcher)
    }

}