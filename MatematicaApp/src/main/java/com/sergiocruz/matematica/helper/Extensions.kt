package com.sergiocruz.matematica.helper

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.*
import android.text.style.SuperscriptSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.fragment.SettingsFragment
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

/** onActionDone() */
interface OnEditorActions {
    fun onActionDone()
}

const val clearErrorDelayMillis: Long = 4500

/** EditText Editor Action Extension Function */
//fun EditText.watchThis(onActionDone : OnEditorActionDone, onActionError: OnEditorActionError) {
fun EditText.watchThis(onEditor: OnEditorActions) {

    this.addTextChangedListener(object : TextWatcher {
        lateinit var oldNum: String

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            oldNum = "$s"
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (TextUtils.isEmpty(s)) return
            if (s.digitsOnly().toLongOrNull() == null) {
                this@watchThis.setText(oldNum)
                this@watchThis.setSelection(this@watchThis.text?.length
                        ?: 0) // Colocar o cursor no final do texto
                this@watchThis.error = this@watchThis.context.getString(R.string.numero_alto)
                // Remove error after 4 seconds
                postDelayed({ this@watchThis.error = null }, clearErrorDelayMillis)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    })

    this.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
            onEditor.onActionDone()
            return@setOnEditorActionListener true
        }
        false
    }

}


class BigNumbersTextWatcher(private val inputEditText: EditText, formatInput: Boolean, private val ignoreLongNumbers: Boolean = false, onEditor: OnEditorActions) : NumberFormatterTextWatcher(inputEditText, formatInput, onEditor) {

    private lateinit var oldNum: String

    override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
        super.beforeTextChanged(p0, start, count, after)
        oldNum = "$p0"
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (TextUtils.isEmpty(s)) return
        if (s?.digitsOnly()?.toLongOrNull() == null && ignoreLongNumbers.not()) {
            inputEditText.setText(oldNum)
            inputEditText.setSelection(inputEditText.text?.length
                    ?: 0) // Colocar o cursor no final do texto
            inputEditText.error = inputEditText.context.getString(R.string.numero_alto)
            // Remove error after 4 seconds
            inputEditText.postDelayed({ inputEditText.error = null }, clearErrorDelayMillis)
        } else {
            super.onTextChanged(s, start, before, count)
        }
    }

}

open class NumberFormatterTextWatcher(private val inputEditText: EditText,
                                      private val formatInput: Boolean,
                                      onEditor: OnEditorActions) : TextWatcher {

    private var initialStart = 0
    private var initialString = ""
    private var changedText = ""

    init {
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                onEditor.onActionDone()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
        if (formatInput.not()) return
        initialString = p0.toString()
        initialStart = start + after
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (formatInput.not()) return
        changedText = s.toString()
    }

    override fun afterTextChanged(p0: Editable?) {
        if (formatInput.not()) return
        inputEditText.removeTextChangedListener(this)
        var finalString = inputEditText.getFormattedString()
        if (initialString == finalString && initialStart - 1 >= 0) {
            try {
                initialString = initialString.replaceRange(initialStart - 1, initialStart, "")
            } catch (ignored: Exception) {
            }
            inputEditText.setText(initialString)
            finalString = inputEditText.getFormattedString()
            initialStart -= 1
        }
        inputEditText.setText(finalString)
        if (finalString.length > changedText.length && initialStart + 1 <= finalString.length) {
            inputEditText.setSelection(initialStart + 1)
        } else {
            when {
                initialStart <= finalString.length -> inputEditText.setSelection(initialStart)
                initialStart - 1 <= finalString.length -> inputEditText.setSelection(initialStart - 1)
                initialStart <= 0 -> inputEditText.setSelection(0)
            }
        }
        inputEditText.addTextChangedListener(this)
    }

}

fun CharSequence?.digitsOnly(): String {
    this?.let {
        return filter { it.isDigit() }.toString()
    }
    return ""
}

fun Number.formatForLocale(): String {
    return NumberFormat
            .getNumberInstance(Locale.getDefault())
            .format(this)
}

fun String.getFormattedString(): String {
    val bigNumber = this.digitsOnly().toBigIntegerOrNull()
    return bigNumber?.formatForLocale() ?: this
}

fun EditText.getFormattedString(): String {
    return this.text.toString().getFormattedString()
}

infix fun ViewGroup.limit(historyLimit: Int) {
    if (childCount == 0 || historyLimit == 0) return
    if (childCount >= historyLimit) removeViewAt(historyLimit - 1)
}

fun getMatchWrapParams() = LinearLayout.LayoutParams(
        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
)

fun getWrapWrapParams() = LinearLayout.LayoutParams(
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
)

fun SpannableStringBuilder.setSafeSpan(which: Any, start: Int, end: Int, flags: Int) {
    if (end < start || start > length || end > length || start < 0 || end < 0 || this.isEmpty()) return
    try {
        setSpan(which, start, end, flags)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun Activity.getNewTextView(withTag: String, @StringRes andText: Int, textSize: Float = 15f): TextView {
    val newTextView = TextView(this)
    newTextView.layoutParams = getMatchWrapParams()
    newTextView.text = HtmlCompat.fromHtml(getString(andText), 0)
    newTextView.setTextColor(ContextCompat.getColor(this, R.color.boldColor))
    newTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
    newTextView.tag = withTag
    newTextView.setTag(R.id.texto, "texto")
    return newTextView
}

object Extensions {

    val Context.myAppPreferences: SharedPreferences
        get() = getSharedPreferences(
                "${this.packageName}_${this.javaClass.simpleName}",
                MODE_PRIVATE
        )

    inline fun <reified T : Any> SharedPreferences.getObject(key: String): T? {
        return Gson().fromJson<T>(getString(key, null), T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T {
        return when (T::class) {
            Boolean::class -> getBoolean(key, defaultValue as? Boolean? ?: false) as T
            Float::class -> getFloat(key, defaultValue as? Float? ?: 0.0f) as T
            Int::class -> getInt(key, defaultValue as? Int? ?: 0) as T
            Long::class -> getLong(key, defaultValue as? Long? ?: 0L) as T
            String::class -> getString(key, defaultValue as? String? ?: "") as T
            else -> {
                if (defaultValue is Set<*>) {
                    getStringSet(key, defaultValue as Set<String>) as T
                } else {
                    val typeName = T::class.java.simpleName
                    throw Error("Unable to get shared preference with value type '$typeName'. Use getObject")
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T : Any> SharedPreferences.set(key: String, value: T) {
        with(edit()) {
            when (T::class) {
                Boolean::class -> putBoolean(key, value as Boolean)
                Float::class -> putFloat(key, value as Float)
                Int::class -> putInt(key, value as Int)
                Long::class -> putLong(key, value as Long)
                String::class -> putString(key, value as String)
                else -> {
                    if (value is Set<*>) {
                        putStringSet(key, value as Set<String>)
                    } else {
                        val json = Gson().toJson(value)
                        putString(key, json)
                    }
                }
            }
            commit()
        }
    }
}

fun FragmentActivity.openSettingsFragment() {
    val fragment = SettingsFragment()
    supportFragmentManager.commit {
        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        addToBackStack(fragment::class.java.simpleName)
        add(R.id.frame, fragment, fragment::class.java.simpleName)
    }
}


val Context.wmyPreferences: SharedPreferences
    get() = this.getSharedPreferences(
            "${this.packageName} ${this.javaClass.simpleName}",
            MODE_PRIVATE
    )

//}

private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}

fun LifecycleOwner.launchSafeCoroutine(context: CoroutineContext = Dispatchers.Default,
                                       block: suspend () -> Unit) {
    try {
        lifecycleScope.launch(context) {
            block.invoke()
        }
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun launchSafeCoroutine(block: suspend () -> Unit) {
    try {
        CoroutineScope(Dispatchers.Default).launch {
            block.invoke()
        }
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun getTextViewsRecursively(root: ViewGroup, withExplanations: Boolean = false): ArrayList<TextView> {
    val textViews = ArrayList<TextView>()
    root.children.forEach { child ->
        if (child is ViewGroup) {
            textViews.addAll(getTextViewsRecursively(child, withExplanations))
        } else if (child is TextView) {
            if (withExplanations) {
                textViews.add(child)
            } else if (child.tag == "0") {
                textViews.add(child)
            }
        }
    }
    return textViews
}

fun ViewGroup.getTextFromTextViews(withExplanations: Boolean = false): String {
    val textViews = getTextViewsRecursively(this, withExplanations)
    var finalText = ""
    if (textViews.size > 0) {
        textViews.forEach { textView ->
            var textFromView = textView.text.toString() + "\n"
            val ss = SpannableString(textView.text)
            val spans = ss.getSpans(0, textView.text.length, SuperscriptSpan::class.java)
            for ((corr, span) in spans.withIndex()) {
                val start = ss.getSpanStart(span) + corr
                textFromView = textFromView.substring(0, start) + "^" + textFromView.substring(start)
            }
            finalText += textFromView + "\n"
        }
    }
    return finalText
}

@SuppressLint("LogNotTimber")
fun delayedTimerAsync(delay: Long = 1000, repeatMillis: Long = 60, job: Job = Job(), action: (Long) -> Unit) {
    var start: Long = delay
    CoroutineScope(Dispatchers.Main + job).launch {
        delay(start)
        if (repeatMillis > 0) {
            while (job.isActive) {
                action(start)
                delay(repeatMillis)
                Log.i("TAG", "delayedTimerAsync: ${job.isActive}")
                Log.i("TAG", "delayedTimerAsync: ${job}")
                start += repeatMillis
            }
        } else {
            action(start)
        }
    }
}


fun expandThis(view: View, newHeight: Int? = null) {
    val targetHeight = view.getTag(R.id.initialHeight) as? Int ?: newHeight ?: 0
    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    view.layoutParams.height = 1
    view.visibility = View.VISIBLE
    val animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            view.layoutParams.height = (targetHeight * interpolatedTime).toInt() + 1
            view.alpha = interpolatedTime
            view.requestLayout()
        }

        override fun willChangeBounds() = true
    }
    animation.duration = 300L
    var isAnimating = false
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            isAnimating = false
        }

        override fun onAnimationStart(animation: Animation?) {
            isAnimating = true
        }
    })

    if (isAnimating.not()) {
        view.startAnimation(animation)
    }
}

fun collapseThis(view: View) {
    val initialHeight = view.measuredHeight

    val animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                view.visibility = View.GONE
            } else {
                view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                view.alpha = 1 - interpolatedTime
                view.requestLayout()
            }
        }

        override fun willChangeBounds() = true
    }
    var isAnimating = false

    animation.duration = 300L
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            isAnimating = false
        }

        override fun onAnimationStart(animation: Animation?) {
            isAnimating = true
        }
    })

    if (isAnimating.not()) {
        view.startAnimation(animation)
    }
}


fun Context.showConfirmAlert(@StringRes title: Int, @StringRes message: Int, onConfirm: (() -> Unit)?) {
    val alertDialogBuilder = AlertDialog.Builder(this)
    alertDialogBuilder
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(R.string.sim) { dialog, _ ->
                onConfirm?.invoke()
                dialog.cancel()
            }

    val alertDialog = alertDialogBuilder.create()        // create alert dialog
    alertDialog.show()                                   // show it
}

fun View.rotateYAnimation() {
    if (this.isVisible) {
        val animation = ObjectAnimator.ofFloat(this, View.ROTATION_Y, 0.0f, 360f)
        animation.duration = 1500
        animation.start()
    }
}











