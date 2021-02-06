package com.sergiocruz.MatematicaPro.activity

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.databinding.ActivityAboutBinding
import java.util.*

class AboutActivity : AppCompatActivity() {

    private var _binding: ActivityAboutBinding? = null
    private val binding: ActivityAboutBinding
        get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.aboutTitle.text = getString(R.string.app_long_description) + BuildConfig.VERSION_NAME
        binding.version.text = (getString(R.string.app_version_description) + " " + BuildConfig.VERSION_NAME + " b" + BuildConfig.VERSION_CODE+ "\n" + BuildConfig.BUILD_TIME.toString())
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
