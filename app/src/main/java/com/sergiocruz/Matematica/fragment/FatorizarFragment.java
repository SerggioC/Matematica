package com.sergiocruz.Matematica.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FatorizarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FatorizarFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_fatorizar, container, false);
        Button button = (Button) view.findViewById(R.id.button_calc_fatores);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcfatoresPrimos(view);
            }
        });
        return view;


    }

    private void calcfatoresPrimos(View view) {

        EditText edittext = (EditText) view.findViewById(R.id.editNumFact);
        String editnumText = (String) edittext.getText().toString();

        if (editnumText.equals(null) || editnumText.equals("")) {
            return;
        }
        if (editnumText.equals("0") || editnumText.equals("1")) {
            Toast.makeText(getActivity(), "O número " + editnumText + " não tem fatores primos!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            long num = Long.parseLong(editnumText);

            // Lista dos fatores primos
            ArrayList<Long> fatoresPrimos = getFatoresPrimos(num);

            //HashMap
            HashMap<String, Integer> dataset = new HashMap<String, Integer>();

            // String de todos os fatores {2, 2, 2, ... 3, 3, ...}
            String str_fatores = "Fatores primos de " + num + ":\n" + "{";

            // Tamanho da lista de umeros primos
            int sizeList = fatoresPrimos.size();

            for (int i = 1; i <= sizeList; i++) {
                if (i != sizeList) {

                    str_fatores += fatoresPrimos.get(i - 1) + ", ";

                } else if (i == sizeList) {

                    str_fatores += fatoresPrimos.get(i - 1) + "}";

                }
            }

            Integer counter = 1;
            Long lastItem = fatoresPrimos.get(0);

            for (int i = 0; i < sizeList; i++) {
                if (fatoresPrimos.get(i).equals(lastItem) && i > 0) {
                    counter++;
                    dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
                } else if (i == 0){
                    counter = 1;
                    dataset.put(String.valueOf(fatoresPrimos.get(0)), counter);
                }
                else if (!fatoresPrimos.get(i).equals(lastItem) && i > 0){
                    counter = 1;
                    dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
                }
                lastItem = fatoresPrimos.get(i);
            }
            dataset.put(String.valueOf(fatoresPrimos.get(sizeList - 1)), counter);


            Log.e("TAG Sergio >>>", "calcfatoresPrimos: " + dataset);


            createCardViewLayout(str_fatores);

        } catch (NumberFormatException exception) {
            Toast.makeText(getActivity(), "Esse número é demasiado grande.", Toast.LENGTH_LONG).show();
        }
    }

    void createCardViewLayout(String str_divisores) {
        final ViewGroup historyFatores = (ViewGroup) getActivity().findViewById(R.id.history_fatores);

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

        // Add cardview to history_divisores at the top (index 0)
        historyFatores.addView(cardview, 0);

        // criar novo textview
        final TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        textView.setText(str_divisores);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

        // add the textview to the cardview
        cardview.addView(textView);

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(new SwipeToDismissTouchListener(
                cardview,
                null,
                new SwipeToDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        historyFatores.removeView(cardview);
                    }
                }));

//        cardview.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
//            public void onSwipeTop() {
//                Toast.makeText(getActivity(), "top", Toast.LENGTH_SHORT).show();
//            }
//
//            public void onSwipeRight() {
//                animateRemoving(cardview, historyDivisores, 1);
//            }
//
//            public void onSwipeLeft() {
//                animateRemoving(cardview, historyDivisores, -1);
//            }
//
//            public void onSwipeBottom() {
//                Toast.makeText(getActivity(), "bottom", Toast.LENGTH_SHORT).show();
//            }
//
//        });


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
}
