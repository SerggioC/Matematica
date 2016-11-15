package com.sergiocruz.Matematica.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import static android.widget.Toast.makeText;
import static com.sergiocruz.Matematica.R.layout.fragment_primes_table;
import static java.lang.Integer.parseInt;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 ******/

public class PrimesTableFragment extends Fragment {

    int num_min, num_max, cv_width, height_dip;
    View progressBar;
    GridView history_gridView;
    NumberFormat number_formatter;
    Fragment thisFragment = this;
    Button button;
    private AsyncTask<Integer, Float, ArrayList<String>> BG_Operation = new LongOperation();
    private ArrayList<String> tableData;

    public PrimesTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // have a menu in this fragment
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_history, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share_history) {
            String primes_string = "Tabela de Números Primos entre " +
                    num_min + " e " + num_max + ":\n" +
                    tableData;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Matemática\n" + primes_string);
            sendIntent.setType("text/plain");
            getActivity().startActivity(sendIntent);
        }

        if (id == R.id.action_clear_all_history) {
            GridView history_gridView = (GridView) getActivity().findViewById(R.id.history);
            history_gridView.setAdapter(null);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(fragment_primes_table, container, false);

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
                EditText min = (EditText) getActivity().findViewById(R.id.min);
                min.setText("");
            }
        });

        Button btn_clear_max = (Button) view.findViewById(R.id.btn_clear_max);
        btn_clear_max.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText max = (EditText) getActivity().findViewById(R.id.max);
                max.setText("");
            }
        });

        final EditText min_edittext = (EditText) view.findViewById(R.id.min);

        min_edittext.addTextChangedListener(new TextWatcher() {
            Integer num1;
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
                    num1 = parseInt(s.toString());
                } catch (Exception e) {
                    min_edittext.setText(oldnum1);
                    min_edittext.setSelection(min_edittext.getText().length()); //Colocar o cursor no final do texto

                    Toast thetoast = makeText(getActivity(), "Valor mínimo demasiado alto", Toast.LENGTH_LONG);
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
            Integer num2;
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
                    num2 = parseInt(s.toString());
                } catch (Exception e) {
                    max_edittext.setText(oldnum2);
                    max_edittext.setSelection(max_edittext.getText().length()); //Colocar o cursor no final do texto

                    Toast thetoast = makeText(getActivity(), "Valor máximo demasiado alto", Toast.LENGTH_LONG);
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

        EditText min_edittext = (EditText) view.findViewById(R.id.min);
        String min_string = (String) min_edittext.getText().toString().replaceAll("[^\\d]", "");

        EditText max_edittext = (EditText) view.findViewById(R.id.max);
        String max_string = (String) max_edittext.getText().toString().replaceAll("[^\\d]", "");

        if (min_string.equals(null) || min_string.equals("") || min_string == null || max_string.equals(null) || max_string.equals("") || max_string == null) {
            Toast thetoast = makeText(getActivity(), "Preencher os campos do valor mínimo e máximo", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Integer 2^31
            num_min = Integer.parseInt(min_string);
            if (num_min < 2) {
                Toast thetoast = makeText(getActivity(), "Menor número primo é 2", Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                min_edittext.setText("2");
                num_min = 2;
            }
        } catch (Exception e) {
            Toast thetoast = makeText(getActivity(), "Valor mínimo demasiado alto", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Integer 2^31
            num_max = Integer.parseInt(max_string);
            if (num_max < 2) {
                Toast thetoast = makeText(getActivity(), "Menor número primo é 2", Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                max_edittext.setText("2");
                num_max = 2;
            }
        } catch (Exception e) {
            Toast thetoast = makeText(getActivity(), "Valor máximo demasiado alto", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }


        if (num_min > num_max) {
            int swapp = num_min;
            num_min = num_max;
            num_max = swapp;
            min_edittext.setText(String.valueOf(num_min));
            max_edittext.setText(String.valueOf(num_max));
        }

        BG_Operation = new LongOperation().execute(num_min, num_max);

//        final ArrayList<String> primes = getPrimes(num_min, num_max);
//
//        final GridView history_gridView = (GridView) view.findViewById(R.id.history);
//        ArrayAdapter<String> primes_adapter = new ArrayAdapter<String>(getActivity(), R.layout.table_item, R.id.tableItem, primes);
//        history_gridView.setAdapter(primes_adapter);

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
    public void onStart() {
        super.onStart();  // Always call the superclass method first
        Log.d("Sergio>>>", "onStart: Starting " + "PrimesTableFragment");
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.i("Sergio>>>", "onPause: Pausing " + "PrimesTableFragment");
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.e("Sergio>>>", "onStop: Stopping " + "PrimesTableFragment");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(getActivity(), "Operação cancelada", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            Log.e("Sergio>>>", "onDestroy: Destroying " + "PrimesTableFragment " + thisFragment);
        }


    }

    public class LongOperation extends AsyncTask<Integer, Float, ArrayList<String>> {

        @Override
        public void onPreExecute() {
            button.setClickable(false);
            history_gridView = (GridView) getActivity().findViewById(R.id.history);
            ViewGroup cardView1 = (ViewGroup) getActivity().findViewById(R.id.card_view_1);
            cv_width = cardView1.getWidth();
            progressBar = (View) getActivity().findViewById(R.id.progress);
            number_formatter = new DecimalFormat("#0.00");
            float scale = getActivity().getResources().getDisplayMetrics().density;
            height_dip = (int) (4 * scale + 0.5f);
        }

        @Override
        public ArrayList<String> doInBackground(Integer... params) {
            int num_min = params[0];
            int num_max = params[1];
            ArrayList<String> primes = new ArrayList<>();

            if (num_min == 2) {
                primes.add(Integer.toString(2));
            }
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
                float percent = (float) ((float) i / (float) num_max);
                number_formatter.format(percent);
                publishProgress(percent);
            }
            return primes;
        }

        @Override
        public void onProgressUpdate(Float... values) {
            //try {
              //  PrimesTableFragment thisFragment = (PrimesTableFragment) getFragmentManager().findFragmentByTag("primes_table");
                if (thisFragment != null && thisFragment.isVisible()) {
                    progressBar.setVisibility(View.VISIBLE);
                    int progress_width = (int) Math.round(values[0] * cv_width);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(progress_width, height_dip);
                    progressBar.setLayoutParams(layoutParams);
                }
            //} catch (Exception e) {
            //    return;
            //}
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            //try {
             //   PrimesTableFragment thisFragment = (PrimesTableFragment) getFragmentManager().findFragmentByTag("primes_table");
                if (thisFragment != null && thisFragment.isVisible() && result.size() > 0) {
                    tableData = result;
                    int history_width = history_gridView.getWidth();
                    int max_num_length = result.get(result.size() - 1).toString().length();
                    int num_length = max_num_length * 38 + 26;
                    int num_columns = (int) Math.round(history_width / num_length);
                    history_gridView.setNumColumns(num_columns);
                    ArrayAdapter<String> primes_adapter = new ArrayAdapter<String>(getActivity(), R.layout.table_item, R.id.tableItem, result);
                    history_gridView.setAdapter(primes_adapter);
                    progressBar.setVisibility(View.GONE);
                    button.setClickable(true);
                } else if (result.size() == 0) {
                    Toast thetoast = Toast.makeText(getActivity(), "Sem números primos no intervalo", Toast.LENGTH_LONG);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                    history_gridView.setAdapter(null);
                    progressBar.setVisibility(View.GONE);
                    button.setClickable(true);
                }
            //} catch (Exception e) {
            //    return;
           // }
        }
    }
}

