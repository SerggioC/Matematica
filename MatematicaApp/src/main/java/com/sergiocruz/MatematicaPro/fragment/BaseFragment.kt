package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.database.HistoryDataClass
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.helper.CreateCardView
import com.sergiocruz.MatematicaPro.helper.MenuHelper
import com.sergiocruz.MatematicaPro.helper.openSettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var sharedPrefs: SharedPreferences
    var historyLimit: Int = 0
    var scale: Float = 0f
    var shouldShowPerformance: Boolean = true
    var shouldShowExplanation: String = "0"
    var shouldFormatNumbers: Boolean = false
    private var shouldShowColors: Boolean = true
    private var privateFColors: MutableList<Int> = mutableListOf()

    fun getRandomFactorsColors(): MutableList<Int> {
        privateFColors = resources.getIntArray(R.array.f_colors_xml).toMutableList()
        if (shouldShowColors) {
            privateFColors.shuffle() // randomizar as cores
        } else {
            privateFColors = MutableList(privateFColors.size) { privateFColors.last() } // just a list with always the same color
        }
        return privateFColors
    }

    abstract var title: Int
    abstract var pageIndex: Int

    private val operationName = javaClass.simpleName

    val gson by lazy { Gson() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        scale = resources.displayMetrics.density
        getBasePreferences()
        clearTemporaryResultsFromDB()
    }

    @StringRes
    abstract fun getHelpTextId(): Int?

    @StringRes
    abstract fun getHelpMenuTitleId(): Int?

    abstract fun getHistoryLayout(): LinearLayout?

    abstract fun loadOptionsMenus(): List<Int>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        loadOptionsMenus().forEach { inflater.inflate(it, menu) }
        getHelpMenuTitleId()?.let { menu.findItem(R.id.action_help).setTitle(it) }
    }

    var allFavoritesCallback: ((List<HistoryDataClass>?) -> Unit)? = null

    open fun getAllFavorites() {
        context?.let { ctx ->
            CoroutineScope(Dispatchers.Default).launch {
                val list: List<HistoryDataClass>? = LocalDatabase.getInstance(ctx).historyDAO()?.getAllFavoritesForOperation(operationName)
                withContext(Dispatchers.Main) {
                    allFavoritesCallback?.invoke(list)
                }
            }
        }
    }

    open fun makeAllResultsFavorite() {
        context?.let { ctx ->
            CoroutineScope(Dispatchers.Default).launch {
                val num = LocalDatabase.getInstance(ctx).historyDAO()?.makeNonFavoriteFavorite(operationName) ?: 0
                if (num > 0) {
                    withContext(Dispatchers.Main) {
                        getHistoryLayout()?.children?.forEach {
                            it.findViewById<View>(R.id.image_star)?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_show_favorites -> getAllFavorites()
            R.id.action_save_all_to_db -> makeAllResultsFavorite()
            R.id.action_save_history_images -> MenuHelper.saveHistoryImages(activity as Activity)
            R.id.action_share_history -> MenuHelper.shareHistory(activity as Activity)
            R.id.action_share_history_images -> MenuHelper.shareHistoryImages(activity as Activity)
            R.id.action_clear_all_history -> clearResultHistory()
            R.id.action_help -> CreateCardView.withStringRes(getHistoryLayout(), getHelpTextId(), activity as Activity)
            R.id.action_about -> startActivity(Intent(activity, AboutActivity::class.java))
            R.id.action_settings -> activity?.openSettingsFragment()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(context)
            .unregisterOnSharedPreferenceChangeListener(this)
        clearTemporaryResultsFromDB()
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    open fun getBasePreferences() {
        val default: Int = resources.getInteger(R.integer.default_history_size)
        historyLimit = sharedPrefs.getString(getString(R.string.pref_key_history_size), default.toString())?.toInt() ?: default
        shouldShowPerformance = sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), true)
        shouldShowExplanation = sharedPrefs.getString(getString(R.string.pref_key_show_explanation), "0") ?: "0"
        shouldShowColors = sharedPrefs.getBoolean(getString(R.string.pref_key_show_colors), true)
        shouldFormatNumbers = sharedPrefs.getBoolean(getString(R.string.pref_key_format_numbers), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutIdForFragment(), container, false)

    @LayoutRes
    abstract fun getLayoutIdForFragment(): Int

    fun saveCardToDatabase(input: String?, data: String?, operation: String) {
        if (input == null || data == null) return
        context?.let { ctx ->
            CoroutineScope(Dispatchers.Default).launch {
                val history = HistoryDataClass(primaryKey = input, operation, content = data, favorite = false)
                LocalDatabase.getInstance(ctx).historyDAO()?.saveCard(history)
            }
        }
    }

    private fun clearResultHistory() {
        MenuHelper.removeResultsFromLayout(activity as Activity)
        clearTemporaryResultsFromDB()
    }

    private fun clearTemporaryResultsFromDB() {
        context?.let {
            CoroutineScope(Dispatchers.Default).launch {
                LocalDatabase.getInstance(it).historyDAO()?.deleteNonFavoritesFromOperation(operationName)
            }
        }
    }

}
