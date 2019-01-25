package com.sergiocruz.MatematicaPro.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        aboutTitle.text = getString(R.string.app_long_description) + BuildConfig.VERSION_NAME
        version.text =
                (getString(R.string.app_version_description) + " " + BuildConfig.VERSION_NAME + "\n" + getString(
                    R.string.app_version_date
                ))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
            return when (item.itemId) {
                android.R.id.home -> {
                    onBackPressed()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
    }
}
