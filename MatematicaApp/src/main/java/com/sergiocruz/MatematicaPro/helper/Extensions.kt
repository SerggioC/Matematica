package com.sergiocruz.MatematicaPro.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.gson.Gson

import com.sergiocruz.MatematicaPro.R

/** onActionDone() */
interface OnEditorActions {
    fun onActionDone()
}

const val clearErrorDelayMillis: Long = 4000

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
            if (s.toString().toLongOrNull() == null) {
                this@watchThis.setText(oldNum)
                this@watchThis.setSelection(this@watchThis.text?.length ?: 0) //Colocar o cursor no final do texto
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



















