package com.sergiocruz.MatematicaPro.helper

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.gson.Gson
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.fragment.SettingsFragment
import java.math.BigInteger
import java.text.NumberFormat
import java.util.*

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


class BigNumbersTextWatcher(private val inputEditText: EditText, formatInput: Boolean, onEditor: OnEditorActions) : NumberFormatterTextWatcher(inputEditText, formatInput, onEditor) {

    private lateinit var oldNum: String

    override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
        super.beforeTextChanged(p0, start, count, after)
        oldNum = "$p0"
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (TextUtils.isEmpty(s)) return
        if (s?.digitsOnly()?.toLongOrNull() == null) {
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

    override fun afterTextChanged(p0: Editable?) {
        super.afterTextChanged(p0)
    }

}

open class NumberFormatterTextWatcher(private val inputEditText: EditText, private val formatInput: Boolean, onEditor: OnEditorActions) : TextWatcher {

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
            initialString = initialString.replaceRange(initialStart - 1, initialStart, "")
            inputEditText.setText(initialString)
            finalString = inputEditText.getFormattedString()
            initialStart -= 1
        }
        inputEditText.setText(finalString)
        if (finalString.length > changedText.length && initialStart + 1 <= finalString.length) {
            inputEditText.setSelection(initialStart + 1)
        } else {
            if (initialStart <= finalString.length) {
                inputEditText.setSelection(initialStart)
            } else if (initialStart - 1 <= finalString.length) {
                inputEditText.setSelection(initialStart - 1)
            } else if (initialStart <= 0) {
                inputEditText.setSelection(0)
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
    if (end < start || start > length || end > length || start < 0 || end < 0) return
    setSpan(which, start, end, flags)
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


//    inline operator fun <reified T : Any?> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
//        return when (T::class) {
//            String::class -> getString(key, defaultValue as? String ?: "") as T?
//            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
//            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
//            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
//            Long::class -> getLong(key, defaultValue as? Long ?: -1L) as T?
//            else -> throw UnsupportedOperationException("Not yet implemented")
//        }
//    }
//
//operator fun SharedPreferences.set(key: String, value: Any?) {
//    when (value) {
//        is String? -> edit { it.putString(key, value) }
//        is Int -> edit { it.putInt(key, value) }
//        is Boolean -> edit { it.putBoolean(key, value) }
//        is Float -> edit { it.putFloat(key, value) }
//        is Long -> edit { it.putLong(key, value) }
//        else -> throw UnsupportedOperationException("Not yet implemented")
//    }
//}

class OutOfPatienteException : Exception("The coder is out of patience!!")


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

//@Suppress("UNCHECKED_CAST")
//inline operator fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
//    when (T::class) {
//        Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
//        Float::class -> return this.getFloat(key, defaultValue as Float) as T
//        Int::class -> return this.getInt(key, defaultValue as Int) as T
//        Long::class -> return this.getLong(key, defaultValue as Long) as T
//        String::class -> return this.getString(key, defaultValue as String) as T
//        else -> {
//            if (defaultValue is Set<*>) {
//                return this.getStringSet(key, defaultValue as Set<String>) as T
//            }
//        }
//    }
//
//    return defaultValue


//
//
//object PreferenceHelper {
//
//    fun defaultPrefs(context: Context): SharedPreferences
//            = PreferenceManager.getDefaultSharedPreferences(context)
//
//    fun customPrefs(context: Context, name: String): SharedPreferences
//            = context.getSharedPreferences(name, Context.MODE_PRIVATE)
//
//    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
//        val editor = this.edit()
//        operation(editor)
//        editor.apply()
//    }
//
//    /**
//     * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
//     */
//    operator fun SharedPreferences.set(key: String, value: Any?) {
//        when (value) {
//            is String? -> edit { it.putString(key, value) }
//            is Int -> edit { it.putInt(key, value) }
//            is Boolean -> edit { it.putBoolean(key, value) }
//            is Float -> edit { it.putFloat(key, value) }
//            is Long -> edit { it.putLong(key, value) }
//            else -> throw UnsupportedOperationException("Not yet implemented")
//        }
//    }
//
//    /**
//     * finds value on given key.
//     * [T] is the type of value
//     * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
//     */
//    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
//        return when (T::class) {
//            String::class -> getString(key, defaultValue as? String) as T?
//            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
//            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
//            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
//            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
//            else -> throw UnsupportedOperationException("Not yet implemented")
//        }
//    }
//}
//



















