package com.sergiocruz.Matematica.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;

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
        TextView text_divisores = (TextView) view.findViewById(R.id.result_divisores);
        EditText edittext = (EditText) view.findViewById(R.id.editNum);
        String editnumText = (String) edittext.getText().toString();

        if (editnumText.equals(null) || editnumText.equals("")) {
//            text_divisores.setText("");
            return;
        }
        if (editnumText.equals("0")) {
            text_divisores.setText("O número zero não tem divisores.");
            return;
        }
        try {
            int num = Integer.parseInt(editnumText);
            ArrayList<Integer> nums = getAllDivisores(num);
            String str = "";
            for (int i : nums) {
                str = str + ", " + i;
                if (i == 1) {
                    str = "{" + i;
                }
            }
            str = str + "}";
            text_divisores.setText(str);

            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.teste);

            // Add textview 1
            TextView textView1 = new TextView(getContext());
            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView1.setText("programmatically created TextView1");
            textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
            textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            linearLayout.addView(textView1);

            // Add textview 2
            TextView textView2 = new TextView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.setMargins(10, 10, 10, 10); // (left, top, right, bottom)
            textView2.setLayoutParams(layoutParams);
            textView2.setText("programmatically created TextView2");
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView2.setBackgroundColor(0xffffdbdb); // hex color 0xAARRGGBB
            linearLayout.addView(textView2);


        } catch (NumberFormatException exception) {
            Toast.makeText(getActivity(), "Esse número é demasiado grande!", Toast.LENGTH_LONG).show();
        }
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
