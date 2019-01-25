package com.sergiocruz.MatematicaPro.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.helper.InfoLevel
import com.sergiocruz.MatematicaPro.helper.showCustomToast
import kotlinx.android.synthetic.main.activity_send_mail.*

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

    fun sendMeMail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.data = Uri.parse(getString(R.string.app_email)) // only email apps should handle this
        intent.putExtra(
            Intent.EXTRA_SUBJECT, application.resources.getString(R.string.app_long_description) +
                    BuildConfig.VERSION_NAME + "e-mail"
        )
        val mailText = mail_text.text?.toString() ?: ""
        intent.putExtra(Intent.EXTRA_TEXT, mailText)
        if (intent.resolveActivity(packageManager) != null) {
            displayDialogBox(intent)
        } else {
            showCustomToast(this, getString(R.string.has_email), InfoLevel.ERROR)
        }
    }


    private fun displayDialogBox(intent: Intent) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // set title
        //alertDialogBuilder.setTitle("Enviar email?");

        // set dialog message
        alertDialogBuilder
            .setMessage(R.string.send_it)
            .setCancelable(true)
            .setPositiveButton(R.string.sim) { dialog, id ->
                startActivity(intent)
                dialog.cancel()
            }
            .setNegativeButton(R.string.nao) { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()        // create alert dialog
        alertDialog.show()                                           // show it
    }

}
