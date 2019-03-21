package com.sergiocruz.MatematicaPro.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.helper.CreateCardView
import com.sergiocruz.MatematicaPro.helper.MenuHelper.removeHistory
import com.sergiocruz.MatematicaPro.helper.MenuHelper.saveHistoryImages
import com.sergiocruz.MatematicaPro.helper.MenuHelper.shareHistory
import com.sergiocruz.MatematicaPro.helper.MenuHelper.shareHistoryImages

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_save_history_images -> saveHistoryImages(activity!!)
            R.id.action_share_history -> shareHistory(activity!!)
            R.id.action_share_history_images -> shareHistoryImages(activity!!)
            R.id.action_clear_all_history -> removeHistory(activity!!)
            R.id.action_help -> CreateCardView.create(
                getHistoryLayout(),
                getHelpTextId(),
                activity!!
            )
            R.id.action_about -> startActivity(Intent(activity, AboutActivity::class.java))
            R.id.action_settings -> startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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
            sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), true)
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

}