package com.sergiocruz.Matematica.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.sergiocruz.Matematica.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView aboutTittle = (TextView) this.findViewById(R.id.aboutTitle);
        aboutTittle.setText(getString(R.string.app_long_description)+getString(R.string.app_version_name));

        TextView versionTV = (TextView) this.findViewById(R.id.version);
        versionTV.setText(getString(R.string.app_version_description) + " " + getString(R.string.app_version_name) + "\n"
                + getString(R.string.app_version_date));
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
}
