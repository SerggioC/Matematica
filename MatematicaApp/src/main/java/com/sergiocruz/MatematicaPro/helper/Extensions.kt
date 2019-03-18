package com.sergiocruz.MatematicaPro.helper

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import com.sergiocruz.MatematicaPro.R

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
            try {
                // Tentar converter o string para long
                var num = java.lang.Long.parseLong("$s")
            } catch (e: Exception) {
                this@watchThis.setText(oldNum)
                this@watchThis.setSelection(
                    this@watchThis.text?.length ?: 0
                ) //Colocar o cursor no final do texto
                this@watchThis.error = this@watchThis.context.getString(R.string.numero_alto)
                // Remove error after 4 seconds
                postDelayed({ this@watchThis.error = null }, clearErrorDelayMillis)
                return
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


fun ViewGroup.limit(historyLimit: Int) {
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