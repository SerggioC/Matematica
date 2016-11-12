package com.sergiocruz.Matematica.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.MenuHelper;

import static java.lang.Integer.parseInt;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 ******/

public class PrimesTableFragment extends Fragment {

    public PrimesTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // havea menu in this fragment
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
            MenuHelper.share_history(getActivity());
        }

        if (id == R.id.action_clear_all_history) {
            MenuHelper.remove_history(getActivity());
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_primes_table, container, false);

        Button button = (Button) view.findViewById(R.id.button_gerar_tabela);
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

                    Toast thetoast = Toast.makeText(getActivity(), "Valor mínimo demasiado grande", Toast.LENGTH_LONG);
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

                    Toast thetoast = Toast.makeText(getActivity(), "Valor máximo demasiado grande", Toast.LENGTH_LONG);
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

    private void gerar_tabela_primos(View view) {

        EditText min_edittext = (EditText) view.findViewById(R.id.min);
        String min_string = (String) min_edittext.getText().toString().replaceAll("[^\\d]", "");

        EditText max_edittext = (EditText) view.findViewById(R.id.max);
        String max_string = (String) max_edittext.getText().toString().replaceAll("[^\\d]", "");

        int num_min, num_max;

        if (min_string.equals(null) || min_string.equals("") || min_string == null || max_string.equals(null) || max_string.equals("") || max_string == null) {
            Toast thetoast = Toast.makeText(getActivity(), "Preencher os campos do valor mínimo e máximo", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Integer 2^31
            num_min = Integer.parseInt(min_string);
            if (num_min < 2) {
                Toast thetoast = Toast.makeText(getActivity(), "Valor mínimo 2", Toast.LENGTH_LONG);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                return;
            }
        } catch (Exception e) {
            Toast thetoast = Toast.makeText(getActivity(), "Valor mínimo demasiado grande", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string do valor mínimo para Integer 2^31
            num_max = Integer.parseInt(max_string);
            if (num_max < 2) {
                Toast thetoast = Toast.makeText(getActivity(), "Valor mínimo 2", Toast.LENGTH_LONG);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
                return;
            }
        } catch (Exception e) {
            Toast thetoast = Toast.makeText(getActivity(), "Valor máximo demasiado grande", Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }


        if (num_min > num_max) {
            min_edittext.setText(max_string);
            max_edittext.setText(min_string);
        }


    }


}
