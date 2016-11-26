package com.sergiocruz.Matematica.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.CreateCardView;
import com.sergiocruz.Matematica.helper.MenuHelper;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.util.ArrayList;
import java.util.Collections;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static java.lang.Long.parseLong;

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
    public AsyncTask<Long, Float, ArrayList<Long>> BG_Operation = new BackGroundOperation();
    int cv_width, height_dip;
    View progressBar;
    LinearLayout history;
    Fragment thisFragment = this;
    Button button;
    ImageView cancelButton;
    long num;

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
        inflater.inflate(R.menu.menu_help_divisores, menu);
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
        if (id == R.id.action_help_divisores) {
            ViewGroup history = (ViewGroup) getActivity().findViewById(R.id.history);
            String help_divisores = getString(R.string.help_text_divisores);
            SpannableStringBuilder ssb = new SpannableStringBuilder(help_divisores);
            CreateCardView.create(history, ssb, getActivity());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//         Checks the orientation of the screen

//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
//        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //int height = size.y;
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        int lr_dip = (int) (4 * scale + 0.5f) * 2;
        cv_width = width - lr_dip;

        hideKeyboard();
    }

    public void onAfterConfigurationChanged(Configuration config){
        
        
    }
    
    public void hideKeyboard() {
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_divisores, container, false);
        final EditText num_1 = (EditText) view.findViewById(R.id.editNum);

        cancelButton = (ImageView) view.findViewById(R.id.cancelTask);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel_AsyncTask();
            }
        });

        button = (Button) view.findViewById(R.id.button_calc_divisores);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcDivisores(view);
            }
        });

        Button clearTextBtn = (Button) view.findViewById(R.id.btn_clear);
        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num_1.setText("");
            }
        });

        num_1.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para Long
                    num1 = parseLong(s.toString());
                } catch (Exception e) {
                    num_1.setText(oldnum1);
                    num_1.setSelection(num_1.getText().length()); //Colocar o cursor no final do texto
                    Toast thetoast = Toast.makeText(getActivity(), R.string.numero_alto, Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER,0,0);
                    thetoast.show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(getActivity(), R.string.canceled_op, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    public void cancel_AsyncTask() {
        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(getActivity(), R.string.canceled_op, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            cancelButton.setVisibility(View.GONE);
            button.setText(getString(R.string.calculate));
            button.setClickable(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void calcDivisores(View view) {
        hideKeyboard();
        EditText edittext = (EditText) view.findViewById(R.id.editNum);
        String editnumText = (String) edittext.getText().toString();

        if (editnumText.equals(null) || editnumText.equals("") || editnumText == null) {
            Toast thetoast = Toast.makeText(getActivity(), R.string.add_num_inteiro, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        try {
            // Tentar converter o string para long
            num = Long.parseLong(editnumText);
        } catch (Exception e) {
            Toast thetoast = Toast.makeText(getActivity(), R.string.numero_alto, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        if (editnumText.equals("0") || num == 0L) {
            Toast thetoast = Toast.makeText(getActivity(),  R.string.zero_no_divisores, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        BG_Operation = new BackGroundOperation().execute(num);

    }

    void createCardViewLayout(String str_divisores) {
        final ViewGroup historyDivisores = (ViewGroup) getActivity().findViewById(R.id.history);

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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

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
                        historyDivisores.removeView(cardview);
                    }
                }));
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



    public class BackGroundOperation extends AsyncTask<Long, Float, ArrayList<Long>> {

        @Override
        public void onPreExecute() {
            button.setClickable(false);
            button.setText(R.string.working);
            cancelButton.setVisibility(View.VISIBLE);
            hideKeyboard();
            history = (LinearLayout) getActivity().findViewById(R.id.history);
            ViewGroup cardView1 = (ViewGroup) getActivity().findViewById(R.id.card_view_1);
            cv_width = cardView1.getWidth();
            progressBar = (View) getActivity().findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);
            float scale = getActivity().getResources().getDisplayMetrics().density;
            height_dip = (int) (4 * scale + 0.5f);
        }

        @Override
        protected ArrayList<Long> doInBackground(Long... num) {
            Long numero = num[0];
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
                publishProgress((float) i / (float) upperlimit);
                if (isCancelled()) break;
            }
            Collections.sort(divisores);
            return divisores;
        }

        @Override
        public void onProgressUpdate(Float... values) {
            if (thisFragment != null && thisFragment.isVisible()) {
                int progress_width = (int) Math.round(values[0] * cv_width);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(progress_width, height_dip);
                progressBar.setLayoutParams(layoutParams);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Long> result) {

            if (thisFragment != null && thisFragment.isVisible()) {
                ArrayList<Long> nums = result;
                String str = "";
                for (long i : nums) {
                    str = str + ", " + i;
                    if (i == 1L) {
                        str = num + getString(R.string.has) + nums.size() + getString(R.string.divisores_barra_n) + "{" + i;
                    }
                }
                String str_divisores = str + "}";
                SpannableStringBuilder ssb = new SpannableStringBuilder(str_divisores);
                if (nums.size() == 2) {
                    ssb.append(getString(R.string.barra_n_numero_primo));
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#29712d")), ssb.length() - 12, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.setSpan(new RelativeSizeSpan(0.9f), ssb.length() - 12, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                ViewGroup history = (ViewGroup) getActivity().findViewById(R.id.history);

                CreateCardView.create(history, ssb, getActivity());

                progressBar.setVisibility(View.GONE);
                button.setText(R.string.calculate);
                button.setClickable(true);
                cancelButton.setVisibility(View.GONE);

            }
        }

        @Override
        protected void onCancelled(ArrayList<Long> parcial){
            super.onCancelled(parcial);

            if (thisFragment != null && thisFragment.isVisible()) {
                ArrayList<Long> nums = parcial;
                String str = "";
                for (long i : nums) {
                    str = str + ", " + i;
                    if (i == 1L) {
                        str = getString(R.string.divisors_of) + num + ":\n" + "{" + i;
                    }
                }
                String str_divisores = str + "}";
                SpannableStringBuilder ssb = new SpannableStringBuilder(str_divisores);
                ssb.append(getString(R.string.barra_n_incomplete_calc));
                ssb.setSpan(new ForegroundColorSpan(Color.RED), ssb.length() - 20, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(0.8f), ssb.length() - 20, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                ViewGroup history = (ViewGroup) getActivity().findViewById(R.id.history);

                CreateCardView.create(history, ssb, getActivity());

                progressBar.setVisibility(View.GONE);
                button.setText(R.string.calculate);
                button.setClickable(true);
                cancelButton.setVisibility(View.GONE);
            }
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
