package com.sergiocruz.MatematicaPro.activity


import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.MenuItem

import com.sergiocruz.MatematicaPro.R


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private val sBindPreferenceSummaryToValueListener =
        Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(if (index >= 0) preference.entries[index] else null)

                //Ativar ou desativar checkbox das cores com a seleção da apresentação de cores
                val pref_show_colors = findPreference("pref_show_colors")
                if (index == 0 || index == 1) {
                    pref_show_colors.isEnabled = true
                } else if (index == 2) {
                    pref_show_colors.isEnabled = false
                }


            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see .sBindPreferenceSummaryToValueListener
     */
    private fun bindPreferenceSummaryToValue(preference: Preference) {
        // Set the listener to watch for value changes.
        //preference.setOnPreferenceChangeListener(this);
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
            preference,
            PreferenceManager
                .getDefaultSharedPreferences(preference.context)
                .getString(preference.key, "")
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        addPreferencesFromResource(R.xml.pref_general)
        bindPreferenceSummaryToValue(findPreference("pref_show_explanation"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


}