package com.sergiocruz.Matematica.fragment;

import android.animation.Animator;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DivisoresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DivisoresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DivisoresFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DivisoresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DivisoresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DivisoresFragment newInstance(String param1, String param2) {
        DivisoresFragment fragment = new DivisoresFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        final View view = inflater.inflate(R.layout.fragment_divisores, container, false);

        Button button = (Button) view.findViewById(R.id.button_calc_divisores);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcDivisores(view);
            }
        });
        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    public void calcDivisores(View view) {
        EditText edittext = (EditText) view.findViewById(R.id.editNum);
        String editnumText = (String) edittext.getText().toString();
        if (editnumText.equals(null) || editnumText.equals("")) {
            return;
        }
        long num = Long.parseLong(editnumText);
        if (editnumText.equals("0") || num == 0L) {
            Toast.makeText(getActivity(), "O número "+ num + " não tem divisores!", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            ArrayList<Long> nums = getAllDivisoresLong(num);
            String str = "";
            for (long i : nums) {
                str = str + ", " + i;
                if (i == 1L) {
                    str = num + " tem " + nums.size() + " divisores:\n" + "{" + i;
                }
            }
            String str_divisores = str + "}";
            createCardViewLayout(str_divisores);
        } catch (NumberFormatException exception) {
            Toast.makeText(getActivity(), "Esse número é demasiado grande.", Toast.LENGTH_LONG).show();
        }
    }

    void createCardViewLayout(String str_divisores) {
        final ViewGroup historyDivisores = (ViewGroup) getActivity().findViewById(R.id.history_divisores);

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
        historyDivisores.addView(cardview, 0);

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
                        historyDivisores.removeView(cardview);
                    }
                }));
    }

    void animateRemoving(final CardView cardview, final LinearLayout historyDivisores, int left_right) {
        cardview.animate().translationX(left_right * 500).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                historyDivisores.removeView(cardview);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    public ArrayList<Integer> getAllDivisores(int numero) {
        int upperlimit = (int) (Math.sqrt(numero));
        ArrayList<Integer> divisores = new ArrayList<Integer>();
        for (int i = 1; i <= upperlimit; i += 1) {
            if (numero % i == 0) {
                divisores.add(i);
                if (i != numero / i) {
                    divisores.add(numero / i);
                }
            }
        }
        Collections.sort(divisores);
        return divisores;
    }

    public ArrayList<Long> getAllDivisoresLong(Long numero) {
        long upperlimit = (long) (Math.sqrt(numero));
        ArrayList<Long> divisores = new ArrayList<Long>();
        for (int i = 1; i <= upperlimit; i += 1) {
            if (numero % i == 0) {
                divisores.add((long) i);
                if (i != numero / i) {
                    long elem = numero / i;
                    divisores.add(elem);
                }
            }
        }
        Collections.sort(divisores);
        return divisores;
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
