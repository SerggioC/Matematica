package com.sergiocruz.MatematicaPro.fragment


import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.*
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
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_show_explanation)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_history_size)))

        val prefBruteForce =
                findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_brute_force))
        val prefProbabilistic =
                findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_probabilistic))

        prefBruteForce?.setOnPreferenceChangeListener { _: Preference?, _: Any? ->
            prefBruteForce.isChecked = prefBruteForce.isChecked.not()
            prefProbabilistic?.isChecked = prefBruteForce.isChecked.not()
            true
        }
        prefProbabilistic?.setOnPreferenceChangeListener { _: Preference?, _: Any? ->
            prefProbabilistic.isChecked = prefProbabilistic.isChecked.not()
            prefBruteForce?.isChecked = prefBruteForce?.isChecked?.not() ?: false
            true
        }

    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private val sBindPreferenceSummaryToValueListenerr =
            Preference.OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()
                if (preference is ListPreference) {
                    if (preference.title == getString(R.string.pref_title_explanation)) {// For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        val index = preference.findIndexOfValue(stringValue)

                        // Set the summary to reflect the new value.
                        preference.setSummary(if (index >= 0) preference.entries[index] else null)

                        //Ativar ou desativar checkbox das cores com a seleção da apresentação de cores
                        val showColors = findPreference<CheckBoxPreference>(getString(R.string.pref_key_show_colors))
                        if (index == 0 || index == 1) {
                            showColors?.isEnabled = true
                        } else if (index == 2) {
                            showColors?.isEnabled = false
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
    private fun bindPreferenceSummaryToValue(preference: ListPreference?) {
        // Set the listener to watch for value changes.
        //preference.setOnPreferenceChangeListener(this);
        preference?.onPreferenceChangeListener = sBindPreferenceSummaryToValueListenerr

        PreferenceManager.getDefaultSharedPreferences(context)

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListenerr.onPreferenceChange(
                preference,
                PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString(preference?.key, "")
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var index = 9
        var title = R.string.action_settings
    }

}