package com.sergiocruz.Matematica.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.CreateCardView;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FatorizarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FatorizarFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p>
 * <p>
 * <p>
 * <p>
 * ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 * clipboard.setText(string);
 */
public class FatorizarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FatorizarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FatorizarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FatorizarFragment newInstance(String param1, String param2) {
        FatorizarFragment fragment = new FatorizarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ArrayList<Long> getFatoresPrimos(long number) {
        ArrayList<Long> factoresPrimos = new ArrayList<Long>();
        for (long i = 2; i <= number / i; i++) {
            while (number % i == 0) {
                factoresPrimos.add(i);
                number /= i;
            }
        }
        if (number > 1) {
            factoresPrimos.add(number);
        }
        return factoresPrimos;
    }

    private ArrayList<ArrayList<Long>> getTabelaFatoresPrimos(long number) {

        ArrayList<ArrayList<Long>> factoresPrimos = new ArrayList<ArrayList<Long>>();
        ArrayList<Long> results = new ArrayList<>();
        ArrayList<Long> divisores = new ArrayList<>();

        results.add(number);
        for (long i = 2; i <= number / i; i++) {
            while (number % i == 0) {
                divisores.add(i);
                number /= i;
                results.add(number);
            }
        }
        if (number > 1) {
            divisores.add(number);
        }

        if (number != 1) {
            results.add(1L);
        }

        factoresPrimos.add(results);
        factoresPrimos.add(divisores);

        return factoresPrimos;
    }

    private ArrayList<ArrayList<Long>> getTabelaFatoresPrimos2(long number) {

        ArrayList<ArrayList<Long>> factoresPrimos = new ArrayList<ArrayList<Long>>();
        ArrayList<Long> results = new ArrayList<>();
        ArrayList<Long> divisores = new ArrayList<>();

        results.add(number);

        while (number % 2L == 0) {
            divisores.add(2L);
            number /= 2;
            results.add(number);
        }

        for (long i = 3; i <= number / i; i+=2) {
            while (number % i == 0) {
                divisores.add(i);
                number /= i;
                results.add(number);
            }
        }
        if (number > 1) {
            divisores.add(number);
        }

        if (number != 1) {
            results.add(1L);
        }

        factoresPrimos.add(results);
        factoresPrimos.add(divisores);

        return factoresPrimos;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_history, menu);
        inflater.inflate(R.menu.menu_help_fatorizar, menu);
        inflater.inflate(R.menu.main, menu); //buy pro
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // and selected 'Mark all as Read'
        if (id == R.id.action_share_history) {
            Toast.makeText(getActivity(), "Partilhar Resultados", Toast.LENGTH_LONG).show();
        }

        // and selected 'Clear All'
        if (id == R.id.action_clear_all_history) {
            Toast.makeText(getActivity(), "Histórico de resultados apagado", Toast.LENGTH_LONG).show();
            remove_history();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_fatorizar, container, false);

        Button button = (Button) view.findViewById(R.id.button_calc_fatores);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcfatoresPrimos(view);
            }
        });

        Button clearTextBtn = (Button) view.findViewById(R.id.btn_clear);
        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTextview(view);
            }
        });

        return view;
    }

    private void clearTextview(View view) {
        EditText ed = (EditText) view.findViewById(R.id.editNumFact);
        ed.setText("");
    }

    public void remove_history() {
        ViewGroup historyFatores = (ViewGroup) getActivity().findViewById(R.id.history_fatores);
        if ((historyFatores).getChildCount() > 0)
            (historyFatores).removeAllViews();
    }

    private void calcfatoresPrimos(View view) {

        EditText edittext = (EditText) view.findViewById(R.id.editNumFact);
        String editnumText = edittext.getText().toString();
        long num;

        if (editnumText.equals(null) || editnumText.equals("") || editnumText == null) {
            Toast.makeText(getActivity(), "Introduzir um número inteiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Tentar converter o string para long
            num = Long.parseLong(editnumText);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Esse número é demasiado grande.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (num == 0L || num == 1L) {
            Toast.makeText(getActivity(), "O número " + num + " não tem fatores primos!", Toast.LENGTH_LONG).show();
            return;
        }

        try {

            //Array de arrays
            ArrayList<ArrayList<Long>> tabela_fatores = new ArrayList<>();

            tabela_fatores = getTabelaFatoresPrimos(num);

            // Lista dos fatores primos
            //ArrayList<Long> fatoresPrimos = getFatoresPrimos(num);

            /* resultadosDivisao|fatoresPrimos
            *                100|2
            *                 50|2
            *                 25|5
            *                  5|5
            *                  1|1
            *
            * */

            ArrayList<Long> resultadosDivisao = tabela_fatores.get(0);
            ArrayList<Long> fatoresPrimos = tabela_fatores.get(1);

            // Tamanho da lista de números primos
            int sizeList = fatoresPrimos.size();

            String str_fatores = "";
            String str_results = "";
            String str_divisores = "";
            SpannableStringBuilder ssb_fatores;
            ViewGroup history = (ViewGroup) view.findViewById(R.id.history_fatores);

            if (sizeList == 1) {
                str_fatores = num + " é um número primo.";
                ssb_fatores = new SpannableStringBuilder(str_fatores);
                CreateCardView.create(history, ssb_fatores, getActivity());

            } else {
                str_fatores = "Fatorização de " + num + " = \n";

                Integer counter = 1;
                Long lastItem = fatoresPrimos.get(0);

                //TreeMap
                LinkedHashMap<String, Integer> dataset = new LinkedHashMap<>();

                //Contar os expoentes
                for (int i = 0; i < fatoresPrimos.size(); i++) {
                    str_fatores += fatoresPrimos.get(i) + "×";
                    if (i == 0) {
                        dataset.put(String.valueOf(fatoresPrimos.get(0)), 1);
                    } else if (fatoresPrimos.get(i).equals(lastItem) && i > 0) {
                        counter++;
                        dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
                    } else if (!fatoresPrimos.get(i).equals(lastItem) && i > 0) {
                        counter = 1;
                        dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
                    }
                    lastItem = fatoresPrimos.get(i);
                }
                str_fatores = str_fatores.substring(0,str_fatores.length()-1) + "=\n";
                ssb_fatores = new SpannableStringBuilder(str_fatores);

                int value_length;

                Iterator iterator = dataset.entrySet().iterator();

                //Criar os expoentes
                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator.next();

                    if (Integer.parseInt(pair.getValue().toString()) == 1) {
                        //Expoente 1
                        ssb_fatores.append(pair.getKey().toString());

                    } else if (Integer.parseInt(pair.getValue().toString()) > 1) {
                        //Expoente superior a 1
                        value_length = pair.getValue().toString().length();
                        ssb_fatores.append(pair.getKey().toString() + pair.getValue().toString());
                        ssb_fatores.setSpan(new SuperscriptSpan(), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_fatores.setSpan(new RelativeSizeSpan(0.8f), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_fatores.setSpan(new ForegroundColorSpan(Color.RED), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (iterator.hasNext()) {
                        ssb_fatores.append("×");
                    }

                    iterator.remove(); // avoids a ConcurrentModificationException
                }


                for (int i = 0; i < sizeList - 1; i++) {
                    str_divisores += String.valueOf(fatoresPrimos.get(i)) + "\n";
                }
                str_divisores += String.valueOf(fatoresPrimos.get(sizeList - 1));

                for (int i = 0; i < resultadosDivisao.size() - 1; i++) {
                    str_results += String.valueOf(resultadosDivisao.get(i)) + "\n";
                }
                str_results += String.valueOf(resultadosDivisao.get(resultadosDivisao.size() - 1));

                createCardViewLayout(history, str_results, str_divisores, ssb_fatores);

            }

        } catch (ArrayIndexOutOfBoundsException exception) {
            Toast.makeText(getActivity(), "Erro: " + exception, Toast.LENGTH_LONG).show();
        }
    }

    void createCardViewLayout(final ViewGroup history, String str_results, String str_divisores, SpannableStringBuilder ssb_fatores) {

        Activity thisActivity = getActivity();
        //criar novo cardview
        final CardView cardview = new CardView(thisActivity);
        cardview.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,   // width
                CardView.LayoutParams.WRAP_CONTENT)); // height
        cardview.setPreventCornerOverlap(true);

        //int pixels = (int) (dips * scale + 0.5f);
        final float scale = thisActivity.getResources().getDisplayMetrics().density;
        int lr_dip = (int) (4 * scale + 0.5f);
        int tb_dip = (int) (8 * scale + 0.5f);
        cardview.setRadius((int) (4 * scale + 0.5f));
        cardview.setCardElevation((int) (2 * scale + 0.5f));
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip);
        cardview.setUseCompatPadding(true);

        int cv_color = ContextCompat.getColor(thisActivity, R.color.lightGreen);
        cardview.setCardBackgroundColor(cv_color);

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0);

        LinearLayout ll_horizontal = new LinearLayout(thisActivity);
        ll_horizontal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_horizontal.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout ll_vertical_results = new LinearLayout(thisActivity);
        ll_vertical_results.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_results.setOrientation(LinearLayout.VERTICAL);
        ll_vertical_results.setPadding(0, 0, 16, 0);

        LinearLayout ll_vertical_separador = new LinearLayout(thisActivity);
        ll_vertical_separador.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT));
        ll_vertical_separador.setOrientation(LinearLayout.VERTICAL);
        ll_vertical_separador.setBackgroundColor(ContextCompat.getColor(thisActivity, R.color.separatorLineColor));
        ll_vertical_separador.setPadding(2, 4, 2, 4);

        LinearLayout ll_vertical_divisores = new LinearLayout(thisActivity);
        ll_vertical_divisores.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_divisores.setOrientation(LinearLayout.VERTICAL);
        ll_vertical_divisores.setPadding(16, 0, 16, 0);


        LinearLayout ll_horizontal_ssb_fatores = new LinearLayout(thisActivity);
        ll_horizontal_ssb_fatores.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_horizontal_ssb_fatores.setOrientation(LinearLayout.HORIZONTAL);


        TextView textView_results = new TextView(thisActivity);
        textView_results.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView_results.setText(str_results);
        textView_results.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView_results.setGravity(Gravity.RIGHT);

        ll_vertical_results.addView(textView_results);

        TextView textView_divisores = new TextView(thisActivity);
        textView_divisores.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView_divisores.setText(str_divisores);
        textView_divisores.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView_divisores.setGravity(Gravity.LEFT);

        ll_vertical_divisores.addView(textView_divisores);

        //Adicionar os LL Verticais ao Horizontal
        ll_horizontal.addView(ll_vertical_results);

        ll_horizontal.addView(ll_vertical_separador);

        //LinearLayout divisores
        ll_horizontal.addView(ll_vertical_divisores);

        // criar novo Textview
        final TextView textView = new TextView(thisActivity);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(16, 0, 0, 0);

        //Adicionar o texto com o resultado da fatorizaçãoo com expoentes
        textView.setText(ssb_fatores);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setTag("texto");

        // add the textview to the Linear layout horizontal
        ll_horizontal.addView(textView);

        cardview.addView(ll_horizontal);


        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(new SwipeToDismissTouchListener(
                cardview,
                thisActivity,
                new SwipeToDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view) {
                        history.removeView(cardview);
                    }
                }));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static class MapUtil {

    }
}
