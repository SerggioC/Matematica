package com.sergiocruz.matematica.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sergiocruz.matematica.BuildConfig
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.helper.InfoLevel
import com.sergiocruz.matematica.helper.showCustomToast
import kotlinx.android.synthetic.main.activity_send_mail.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Created by sergi on 21/10/2016.
 */
class SendMailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //        webView.loadUrl("file:///android_res/drawable/mail_smiley.gif");
        webView.loadUrl("file:///android_asset/mail_smiley.gif")
        sendMailButton.setOnClickListener { sendMeMail() }
    }

    var numberOfHits: Int by Delegates.observable(1) { property: KProperty<*>, oldValue: Int, newValue: Int ->

    }

    var numberOfCenas: Double by Delegates.observable(10.1, { property: KProperty<*>, oldValue: Double, newValue: Double ->

    })


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return if (item.itemId == android.R.id.home) {
            // finish the activity
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun sendMeMail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.apply {
            type = "text/plain"
            data = Uri.parse("mailto:" + getString(R.string.app_email)) // only email apps should handle this
            putExtra(
                    Intent.EXTRA_SUBJECT, application.resources.getString(R.string.app_long_description) +
                    BuildConfig.VERSION_NAME + " e-mail"
            )
            val mailText = mail_text.text?.toString() ?: ""
            putExtra(Intent.EXTRA_TEXT, mailText)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showCustomToast(this, getString(R.string.has_email), InfoLevel.ERROR)
        }
    }

}
