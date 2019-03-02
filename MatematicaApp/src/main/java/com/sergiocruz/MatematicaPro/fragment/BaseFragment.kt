package com.sergiocruz.MatematicaPro.fragment


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutIdForFragment(), container, false)

    @LayoutRes
    abstract fun getLayoutIdForFragment(): Int

    lateinit var sharedPrefs: SharedPreferences
    var scale: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        scale = resources.displayMetrics.density
    }

    abstract fun loadOptionsMenus(): List<Int>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        loadOptionsMenus().onEach { inflater.inflate(it, menu) }
    }

}