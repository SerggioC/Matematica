package com.sergiocruz.MatematicaPro.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.sergiocruz.MatematicaPro.R

abstract class BaseFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var sharedPrefs: SharedPreferences
    var historyLimit: Int = 0
    var scale: Float = 0f
    var shouldShowPerformance: Boolean = true
    var shouldShowExplanation: String = "0"
    var shouldShowColors: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        scale = resources.displayMetrics.density
        getBasePreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(context)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    fun getBasePreferences() {
        val default: Int = resources.getInteger(R.integer.default_history_size)
        historyLimit = sharedPrefs.getString(
            getString(R.string.pref_key_history_size),
            default.toString()
        )?.toInt() ?: default
        shouldShowPerformance =
            sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), false)
        shouldShowExplanation =
            sharedPrefs.getString(getString(R.string.pref_key_show_explanation), "0") ?: "0"
        shouldShowColors = sharedPrefs.getBoolean(getString(R.string.pref_key_show_colors), true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutIdForFragment(), container, false)

    @LayoutRes
    abstract fun getLayoutIdForFragment(): Int

    abstract fun loadOptionsMenus(): List<Int>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        loadOptionsMenus().forEach { inflater.inflate(it, menu) }
    }

    fun LinearLayout.limit(historyLimit: Int) {
        if (childCount == 0 || historyLimit == 0) return
        if (childCount >= historyLimit) removeViewAt(historyLimit - 1)
    }

}