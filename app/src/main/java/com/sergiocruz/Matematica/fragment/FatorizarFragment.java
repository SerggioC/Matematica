package com.sergiocruz.Matematica.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

        factoresPrimos.get(1).add(new ArrayList<Long>());
        factoresPrimos.get(1).add(number);

        for (long i = 2; i <= number / i; i++) {
            while (number % i == 0) {
                factoresPrimos.get(0).add(i);
                number /= i;
                factoresPrimos.get(1).add(number);
            }
        }
        if (number > 1) {
            factoresPrimos.get(0).add(number);
        }
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

            // Lista dos fatores primos
            ArrayList<Long> fatoresPrimos = getFatoresPrimos(num);

            ArrayList<ArrayList<Long>> tabela_fatores = new ArrayList<>();
            tabela_fatores = getTabelaFatoresPrimos(num);
            Log.i("Sergio>>>", "calcfatoresPrimos:  tabela fatores: " + tabela_fatores);

            // String de todos os fatores {2, 2, 2, ... 3, 3, ...}
            //String str_fatores = "Fatores primos de " + num + ":\n" + "{";

            // Tamanho da lista de números primos
            int sizeList = fatoresPrimos.size();

//            for (int i = 0; i < sizeList - 1; i++) {
//              str_fatores += fatoresPrimos.get(i) + ", ";
//            }
//            str_fatores += fatoresPrimos.get(sizeList - 1) + "}\n = ";

            String str_fatores;
            SpannableStringBuilder ssb;

            if (sizeList == 1) {
                str_fatores = num + " é um número primo.";
                ssb = new SpannableStringBuilder(str_fatores);

            } else {
                str_fatores = "Fatorização de " + num + " = \n";
                ssb = new SpannableStringBuilder(str_fatores);

                Integer counter = 1;
                Long lastItem = fatoresPrimos.get(0);

                //TreeMap
                LinkedHashMap<String, Integer> dataset = new LinkedHashMap<>();

                for (int i = 0; i < fatoresPrimos.size(); i++) {
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

                int value_length;

                Iterator iterator = dataset.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator.next();

                    if (Integer.parseInt(pair.getValue().toString()) == 1) {
                        //Expoente 1
                        ssb.append(pair.getKey().toString());

                    } else if (Integer.parseInt(pair.getValue().toString()) > 1) {
                        //Expoente superior a 1
                        value_length = pair.getValue().toString().length();
                        ssb.append(pair.getKey().toString() + pair.getValue().toString());
                        ssb.setSpan(new SuperscriptSpan(), ssb.length() - value_length, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new RelativeSizeSpan(0.8f), ssb.length() - value_length, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new ForegroundColorSpan(Color.RED), ssb.length() - value_length, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (iterator.hasNext()) {
                        ssb.append("×");
                    }

                    iterator.remove(); // avoids a ConcurrentModificationException
                }
            }
            ViewGroup history = (ViewGroup) view.findViewById(R.id.history_fatores);

            //Criar o cardview com os resultados
            CreateCardView.create(history, ssb, getActivity());

        } catch (ArrayIndexOutOfBoundsException exception) {
            Toast.makeText(getActivity(), "Erro: " + exception, Toast.LENGTH_LONG).show();
        }
    }

    void createCardViewLayout(final ViewGroup history, SpannableStringBuilder str_result) {
        //final ViewGroup historyFatores = (ViewGroup) getActivity().findViewById(R.id.history_fatores);
        //final ViewGroup history = (ViewGroup) view.findViewById(R.id.history_fatores);

        //criar novo cardview
        final CardView cardview = new CardView(getActivity());
        cardview.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,   // width
                CardView.LayoutParams.WRAP_CONTENT)); // height
        cardview.setPreventCornerOverlap(true);

        //int pixels = (int) (dips * scale + 0.5f);
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        int lr_dip = (int) (16 * scale + 0.5f);
        int tb_dip = (int) (8 * scale + 0.5f);
        cardview.setRadius((int) (4 * scale + 0.5f));
        cardview.setCardElevation((int) (2 * scale + 0.5f));
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip);
        cardview.setUseCompatPadding(true);

        int cv_color = ContextCompat.getColor(getActivity(), R.color.lightGreen);
        cardview.setCardBackgroundColor(cv_color);

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0);

        // criar novo Textview
        final TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //Adicionar o texto com o resultado
        textView.setText(str_result);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        // add the textview to the cardview
        cardview.addView(textView);

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(new SwipeToDismissTouchListener(
                cardview,
                getActivity(),
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
