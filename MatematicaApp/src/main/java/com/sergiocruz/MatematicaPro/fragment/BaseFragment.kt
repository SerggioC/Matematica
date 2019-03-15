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
    var historySize: Int = 0
    var shouldShowPerformance: Boolean = true
    var scale: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        scale = resources.displayMetrics.density
        getPreferences()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getPreferences()
    }

    private fun getPreferences() {
        val default: Int = resources.getInteger(R.integer.default_history_size)
        historySize = sharedPrefs.getString(
            getString(R.string.pref_key_history_size),
            default.toString()
        )?.toInt() ?: default
        shouldShowPerformance =
            sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), false)
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
        loadOptionsMenus().onEach { inflater.inflate(it, menu) }
    }

    fun LinearLayout.limit(historySize: Int) {
        if (childCount >= historySize) removeViewAt(historySize - 1)
    }

}