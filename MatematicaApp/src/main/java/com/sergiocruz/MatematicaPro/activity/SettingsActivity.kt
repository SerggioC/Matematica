package com.sergiocruz.MatematicaPro.activity


import android.os.Bundle
import android.preference.*
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
                if (preference.title == getString(R.string.pref_title_explanation)) {// For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(if (index >= 0) preference.entries[index] else null)

                    //Ativar ou desativar checkbox das cores com a seleção da apresentação de cores
                    val pref_show_colors = findPreference(getString(R.string.pref_key_show_colors))
                    if (index == 0 || index == 1) {
                        pref_show_colors.isEnabled = true
                    } else if (index == 2) {
                        pref_show_colors.isEnabled = false
                    }
                } else if (preference.title == getString(R.string.pref_title_history_size)) {
                    preference.summary =
                        "${getString(R.string.pref_summary_hisory_size)} ($stringValue)"
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
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_show_explanation)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_history_size)))

        val prefBruteForce =
            findPreference(getString(R.string.pref_key_brute_force)) as SwitchPreference
        val prefProbabilistic =
            findPreference(getString(R.string.pref_key_probabilistic)) as SwitchPreference

        prefBruteForce.setOnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            prefBruteForce.isChecked = !prefBruteForce.isChecked
            prefProbabilistic.isChecked = !prefBruteForce.isChecked
            true
        }
        prefProbabilistic.setOnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            prefProbabilistic.isChecked = !prefProbabilistic.isChecked
            prefBruteForce.isChecked = !prefBruteForce.isChecked
            true
        }

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