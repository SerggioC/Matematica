package com.sergiocruz.Matematica.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;

/**
 * Created by sergi on 21/10/2016.
 */
public class SendMailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl("file:///android_res/drawable/mail_smiley.gif");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // finish the activity
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void send_me_mail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse(getString(R.string.app_email))); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Matemática APP e-mail");
        intent.putExtra(Intent.EXTRA_TEXT, ((android.support.design.widget.TextInputEditText) this.findViewById(R.id.mail_text)).getText().toString());
        if (intent.resolveActivity(getPackageManager()) != null) {
            displayDialogBox(intent);
        } else {
            Toast toast = Toast.makeText(this, R.string.has_email, Toast.LENGTH_LONG);
            toast.show();
        }
    }


    public void displayDialogBox(final Intent intent) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        //alertDialogBuilder.setTitle("Enviar email?");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.send_it)
                .setCancelable(true)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();        // create alert dialog
        alertDialog.show();                                           // show it
    }

}
