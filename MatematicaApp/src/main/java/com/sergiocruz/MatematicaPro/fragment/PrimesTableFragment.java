package com.sergiocruz.MatematicaPro.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.MatematicaPro.R;
import com.sergiocruz.MatematicaPro.activity.AboutActivity;
import com.sergiocruz.MatematicaPro.activity.SettingsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import static android.widget.Toast.makeText;
import static com.sergiocruz.MatematicaPro.R.id.card_view_1;
import static com.sergiocruz.MatematicaPro.R.id.min;
import static java.lang.Long.parseLong;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 ******/

public class PrimesTableFragment extends Fragment {

    public AsyncTask<Long, Double, ArrayList<String>> BG_Operation = new LongOperation();
    public ArrayList<String> tableData = null;
    int cv_width, height_dip;
    Long num_min, num_max;
    View progressBar;
    GridView history_gridView;
    Fragment thisFragment = this;
    Button button;
    ImageView cancelButton;
    Boolean checkboxChecked = true;
    ArrayList<String> full_table = null;
    Activity mActivity;
    SharedPreferences sharedPrefs;
    TextView numPrimesTV, elapsedTV;

    public PrimesTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // have a menu in this fragment
        setHasOptionsMenu(true);
        mActivity = getActivity();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.menu_sub_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share_history) {
            if (tableData != null) {
                String primes_string = getString(R.string.table_between) + " " +
                        num_min + " " + getString(R.string.and) + " " + num_max + ":\n" +
                        tableData;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources().getString(R.string.app_long_description) +
                        mActivity.getResources().getString(R.string.app_version_name) + "\n" + primes_string);
                sendIntent.setType("text/plain");
                mActivity.startActivity(sendIntent);
            } else {
                Toast thetoast = makeText(mActivity, R.string.no_data, Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
            }
        }

        if (id == R.id.action_clear_all_history) {
            ((GridView) mActivity.findViewById(R.id.history)).setAdapter(null);
            tableData = null;
            num_min = 0L;
            num_max = 50L;
            ((EditText) mActivity.findViewById(min)).setText("1");
            ((EditText) mActivity.findViewById(R.id.max)).setText("50");
            mActivity.findViewById(R.id.numPrimesTextView).setVisibility(View.GONE);
            mActivity.findViewById(R.id.performanceTextView).setVisibility(View.GONE);
        }
        if (id == R.id.action_about) {
            startActivity(new Intent(mActivity, AboutActivity.class));
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(mActivity, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayCancelDialogBox() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.primetable_title));

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.cancel_it)
                .setCancelable(true)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cancel_AsyncTask();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_primes_table, container, false);

        SwitchCompat showAllNumbers = (SwitchCompat) view.findViewById(R.id.switchPrimos);
        showAllNumbers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkboxChecked = b;
                if (tableData != null) {
                    if (checkboxChecked) {
                            full_table = new ArrayList<String>();
                            for (long i = num_min; i <= num_max; i++) {
                                full_table.add(String.valueOf(i));
                        }
                        history_gridView
                                .setAdapter(
                                        new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, full_table) {
                                            @Override
                                            public View getView(int position, View convertView, ViewGroup parent) {
                                                View view = super.getView(position, convertView, parent);
                                                if (tableData.contains(full_table.get(position))) { //Se o número for primo
                                                    ((CardView) view).setCardBackgroundColor(Color.parseColor("#9769bc4d"));
                                                } else {
                                                    ((CardView) view).setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                                                }
                                                return view;
                                            }
                                        }
                                );
                    } else {
                        ArrayAdapter<String> primes_adapter = new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, tableData);
                        history_gridView.setAdapter(primes_adapter);
                    }
                }
            }
        });

        cancelButton = (ImageView) view.findViewById(R.id.cancelTask);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayCancelDialogBox();

            }
        });

        button = (Button) view.findViewById(R.id.button_gerar_tabela);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gerar_tabela_primos(view);
            }
        });

        Button btn_clear_min = (Button) view.findViewById(R.id.btn_clear_min);
        btn_clear_min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText min = (EditText) mActivity.findViewById(R.id.min);
                min.setText("");
            }
        });

        Button btn_clear_max = (Button) view.findViewById(R.id.btn_clear_max);
        btn_clear_max.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText max = (EditText) mActivity.findViewById(R.id.max);
                max.setText("");
            }
        });

        final EditText min_edittext = (EditText) view.findViewById(R.id.min);

        min_edittext.addTextChangedListener(new TextWatcher() {
            Long num1;
            String oldnum1;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum1 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para long
                    num1 = parseLong(s.toString());
                } catch (Exception e) {
                    min_edittext.setText(oldnum1);
                    min_edittext.setSelection(min_edittext.getText().length()); //Colocar o cursor no final do texto

                    Toast thetoast = makeText(mActivity, R.string.lowest_is_high, Toast.LENGTH_LONG);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final EditText max_edittext = (EditText) view.findViewById(R.id.max);

        max_edittext.addTextChangedListener(new TextWatcher() {
            Long num2;
            String oldnum2;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum2 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para long
                    num2 = parseLong(s.toString());
                } catch (Exception e) {
                    max_edittext.setText(oldnum2);
                    max_edittext.setSelection(max_edittext.getText().length()); //Colocar o cursor no final do texto

                    Toast thetoast = makeText(mActivity, R.string.highest_is_high, Toast.LENGTH_LONG);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    public void gerar_tabela_primos(View view) {

        EditText min_edittext = (EditText) view.findViewById(min);
        String min_string = min_edittext.getText().toString().replaceAll("[^\\d]", "");

        EditText max_edittext = (EditText) view.findViewById(R.id.max);
        String max_string = max_edittext.getText().toString().replaceAll("[^\\d]", "");

        if (min_string.equals(null) || min_string.equals("") || max_string.equals(null) || max_string.equals("")) {
            Toast thetoast = makeText(mActivity, R.string.fill_min_max, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Long 2^63-1
            num_min = parseLong(min_string);
            if (num_min < 1L) {
                Toast thetoast = makeText(mActivity, R.string.lowest_prime, Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                min_edittext.setText("1");
                num_min = 1L;
            }
        } catch (Exception e) {
            Toast thetoast = makeText(mActivity, R.string.lowest_is_high, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Long 2^63-1
            num_max = parseLong(max_string);
            if (num_max < 1L) {
                Toast thetoast = makeText(mActivity, R.string.lowest_prime, Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                max_edittext.setText("1");
                num_max = 2L;
            }
        } catch (Exception e) {
            Toast thetoast = makeText(mActivity, R.string.highest_is_high, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }


        if (num_min > num_max) {
            Long swapp = num_min;
            num_min = num_max;
            num_max = swapp;
            min_edittext.setText(String.valueOf(num_min));
            max_edittext.setText(String.valueOf(num_max));
        }

        BG_Operation = new LongOperation().execute(num_min, num_max);

    }

    private ArrayList<String> getPrimes(int num_min, int num_max) {
        ArrayList<String> primes = new ArrayList<>();
        for (int i = num_min; i <= num_max; i++) {
            boolean isPrime = true;
            if (i % 2 == 0) {
                isPrime = false;
            }
            if (isPrime) {
                for (int j = 3; j < i; j = j + 2) {
                    if (i % j == 0) {
                        isPrime = false;
                        break;
                    }
                }
            }

            if (isPrime) {
                primes.add(Integer.toString(i));
            }

        }
        return primes;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(mActivity, getString(R.string.canceled_op), Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }


    public void cancel_AsyncTask() {
        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(mActivity, getString(R.string.canceled_op), Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            cancelButton.setVisibility(View.GONE);
            button.setText(R.string.gerar);
            button.setClickable(true);
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Quando altera a orientação do ecrã
        if (tableData != null) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;  //int height = size.y;
            int min_num_length = tableData.get(tableData.size() - 1).length();
            final float scale = mActivity.getResources().getDisplayMetrics().density;
            int num_length = min_num_length * (int) (18 * scale + 0.5f) + 8;
            int num_columns = Math.round(width / num_length);
            history_gridView.setNumColumns(num_columns);
            int lr_dip = (int) (4 * scale + 0.5f) * 2;
            cv_width = width - lr_dip;
        }
        hideKeyboard();
    }

    public void hideKeyboard() {
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
    }

    public class LongOperation extends AsyncTask<Long, Double, ArrayList<String>> {
        long startTime = System.nanoTime();

        @Override
        public void onPreExecute() {
            button.setClickable(false);
            button.setText(getString(R.string.working));
            cancelButton.setVisibility(View.VISIBLE);
            hideKeyboard();
            history_gridView = (GridView) mActivity.findViewById(R.id.history);
            ViewGroup cardView1 = (ViewGroup) mActivity.findViewById(card_view_1);
            cv_width = cardView1.getWidth();
            progressBar = (View) mActivity.findViewById(R.id.progress);
            float scale = mActivity.getResources().getDisplayMetrics().density;
            height_dip = (int) (4 * scale + 0.5f);
            progressBar.setLayoutParams(new LinearLayout.LayoutParams(10, height_dip));
            progressBar.setVisibility(View.VISIBLE);
            elapsedTV = (TextView) mActivity.findViewById(R.id.performanceTextView);
            numPrimesTV = (TextView) mActivity.findViewById(R.id.numPrimesTextView);
        }

        @Override
        public ArrayList<String> doInBackground(Long... params) {
            num_min = params[0];
            num_max = params[1];
            ArrayList<String> primes = new ArrayList<>();
            double progress;
            double oldProgress = 0d;
            long min = num_min;
            if (min == 1L) min = 2L;
            if (min == 2L) {
                primes.add("2");
                min = 3L;
            }
            for (long i = min; i <= num_max; i++) {
                boolean isPrime = true;
                if (i % 2 == 0) isPrime = false;
                if (isPrime) {
                    for (long j = 3; j < i; j = j + 2) {
                        if (i % j == 0) {
                            isPrime = false;
                            break;
                        }
                    }
                }
                if (isPrime) {
                    primes.add(Long.toString(i));
                }
                progress = (double) i / (double) num_max;
                if (progress - oldProgress > 0.05d) {
                    publishProgress(progress);
                    oldProgress = progress;
                }
                if (isCancelled()) break;
            }
            return primes;
        }

        @Override
        public void onProgressUpdate(Double... values) {
            if (thisFragment != null && thisFragment.isVisible()) {
                progressBar.setLayoutParams(new LinearLayout.LayoutParams((int) Math.round(values[0] * cv_width), height_dip));
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            tableData = result;
            if (thisFragment != null && thisFragment.isVisible()) {
                Display display = mActivity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;  //int height = size.y;
                //int max_num_length = result.get(result.size() - 1).length();
                int max_num_length = num_max.toString().length();
                final float scale = mActivity.getResources().getDisplayMetrics().density;
                int num_length = max_num_length * (int) (18 * scale + 0.5f) + 8;
                int num_columns = Math.round(width / num_length);
                history_gridView.setNumColumns(num_columns);

                if (checkboxChecked) {
                    full_table = new ArrayList<String>();
                    for (long i = num_min; i <= num_max; i++) {
                        full_table.add(String.valueOf(i));
                    }
                    history_gridView
                            .setAdapter(
                                    new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, full_table) {
                                        @Override
                                        public View getView(int position, View convertView, ViewGroup parent) {
                                            View view = super.getView(position, convertView, parent);
                                            if (tableData.contains(full_table.get(position))) { //Se o número for primo
                                                ((CardView) view).setCardBackgroundColor(Color.parseColor("#9769bc4d"));
                                            } else {
                                                ((CardView) view).setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                                            }
                                            return view;
                                        }
                                    }
                            );
                } else {
                    ArrayAdapter<String> primes_adapter = new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, result);
                    history_gridView.setAdapter(primes_adapter);
                }
                numPrimesTV.setVisibility(View.VISIBLE);
                numPrimesTV.setText(getString(R.string.cardinal_primos) + " " + result.size());
                if (result.size() == 0) {
                    Toast thetoast = Toast.makeText(mActivity, R.string.no_primes_range, Toast.LENGTH_LONG);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                } else {
                    Toast.makeText(mActivity, getString(R.string.existem) + " " + result.size() + " " + getString(R.string.primes_in_range), Toast.LENGTH_LONG).show();
                }
                showPerformance();
                resetButtons();
            }

        }

        private void showPerformance() {
            Boolean shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false);
            if (shouldShowPerformance) {
                NumberFormat decimalFormatter = new DecimalFormat("#.###");
                String elapsed = " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s";
                elapsedTV.setVisibility(View.VISIBLE);
                elapsedTV.setText(getString(R.string.performance) + " " + elapsed);
            } else {
                elapsedTV.setVisibility(View.GONE);
            }
        }

        private void resetButtons() {
            progressBar.setVisibility(View.GONE);
            button.setClickable(true);
            button.setText(R.string.gerar);
            cancelButton.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled(ArrayList<String> parcial) { //resultado parcial obtido após cancelar AsyncTask
            super.onCancelled(parcial);
            tableData = parcial;
            if (thisFragment != null && thisFragment.isVisible() && parcial.size() > 0) {
                Display display = mActivity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;  //int height = size.y;
                int max_num_length = parcial.get(parcial.size() - 1).length();
                final float scale = mActivity.getResources().getDisplayMetrics().density;
                int num_length = max_num_length * (int) (18 * scale + 0.5f) + 8;
                int num_columns = (int) Math.round(width / num_length);
                history_gridView.setNumColumns(num_columns);

                if (checkboxChecked) {
                    full_table = new ArrayList<String>();
                    for (long i = num_min; i <= num_max; i++) {
                        full_table.add(String.valueOf(i));
                    }
                    history_gridView
                            .setAdapter(
                                    new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, full_table) {
                                        @Override
                                        public View getView(int position, View convertView, ViewGroup parent) {
                                            View view = super.getView(position, convertView, parent);
                                            if (tableData.contains(full_table.get(position))) { //Se o número for primo
                                                ((CardView) view).setCardBackgroundColor(Color.parseColor("#9769bc4d"));
                                            } else {
                                                ((CardView) view).setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                                            }
                                            return view;
                                        }
                                    }
                            );
                } else {
                    ArrayAdapter<String> primes_adapter = new ArrayAdapter<String>(mActivity, R.layout.table_item, R.id.tableItem, parcial);
                    history_gridView.setAdapter(primes_adapter);
                }
                Toast.makeText(mActivity, getString(R.string.found) + " " + parcial.size() + " " + getString(R.string.primes_in_range), Toast.LENGTH_LONG).show();
                numPrimesTV.setVisibility(View.VISIBLE);
                numPrimesTV.setText(getString(R.string.cardinal_primos) + " (" + parcial.size() + ")");
                showPerformance();
                resetButtons();
            } else if (parcial.size() == 0) {
                Toast thetoast = Toast.makeText(mActivity, R.string.canceled_noprimes, Toast.LENGTH_LONG);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                history_gridView.setAdapter(null);
                resetButtons();
            }

        }
    }
}
















