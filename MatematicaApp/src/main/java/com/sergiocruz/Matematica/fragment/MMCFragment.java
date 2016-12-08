package com.sergiocruz.Matematica.fragment;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.MenuHelper;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
import static com.sergiocruz.Matematica.helper.CreateCardView.create;
import static java.lang.Long.parseLong;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MMCFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MMCFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MMCFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public AsyncTask<ArrayList<Long>, Float, ArrayList<ArrayList<Long>>> BG_Operation_MMC = new BackGroundOperation_MMC();
    public float scale;
    ViewGroup history;
    CardView cardview;
    EditText mmc_num_1, mmc_num_2, mmc_num_3, mmc_num_4, mmc_num_5, mmc_num_6, mmc_num_7, mmc_num_8;
    TextView explainTextView_1, explainTextView_2, explainTextView_3;
    Activity mActivity;
    Fragment thisFragment = this;
    ArrayList<Long> long_numbers = new ArrayList<Long>();
    View progressBar;
    LinearLayout ll_vertical_expl;
    int height_dip, cv_width;

    int f_colors[];

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public MMCFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MMCFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MMCFragment newInstance(String param1, String param2) {
        MMCFragment fragment = new MMCFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /*****************************************************************
     * MMC: Mínimo múltiplo Comum (LCM: Least Common Multiplier)
     *****************************************************************/
    private static BigInteger mmc(BigInteger a, BigInteger b) {
        return b.divide(a.gcd(b)).multiply(a);
    }

    private static BigInteger mmc(ArrayList<BigInteger> input) {
        BigInteger result = input.get(0);
        for (int i = 1; i < input.size(); i++)
            result = mmc(result, input.get(i));
        return result;
    }

    private void showToast() {
        Toast thetoast = Toast.makeText(mActivity, R.string.numero_alto, Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    private void showToastNum(String field) {
        Toast thetoast = Toast.makeText(mActivity, getString(R.string.number_in_field) + " " + field + " " + getString(R.string.too_high), Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    private void showToastMoreThanZero() {
        Toast thetoast = Toast.makeText(mActivity, R.string.maiores_qzero, Toast.LENGTH_LONG);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }
    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
     *****************************************************************/
//    private static BigInteger mdc(BigInteger a, BigInteger b) {
////        while (b > 0) {
//        while (b.compareTo(ZERO) == 1) {
//            BigInteger temp = b;
////            b = a % b;
//            b = a.remainder(b);
//            a = temp;
//        }
//        return a;
//    }

//    private static BigInteger mdc(BigInteger[] input) {
//        BigInteger result = input[0];
//        for (int i = 1; i < input.length; i++)
//            result = mdc(result, input[i]);
//        return result;
//    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     *****************************************************************/
//    private final static BigInteger mdc2(BigInteger a, BigInteger b) {
//        return b == 0 ? a : mdc(b, a % b);
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // havea menu in this fragment
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mActivity = getActivity();
        scale = mActivity.getResources().getDisplayMetrics().density;
        f_colors = mActivity.getResources().getIntArray(R.array.f_colors_xml);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
/*        Log.i("Sergio>>>", "onActivityCreated: ");
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            int state_size = savedInstanceState.size();
            for (int i = 0; i < state_size; i++) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(savedInstanceState.getCharSequence("Card" + i));
                CreateCardView.create(history, ssb, mActivity);
            }
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

/*        Log.i("Sergio>>>", "onSaveInstanceState: ");
        //Saving the fragment's state
        ViewGroup history = (ViewGroup) mActivity.findViewById(R.id.history);
        int cards = history.getChildCount();
        for (int i = 0; i < cards; i++) {
            CharSequence text = ((TextView) ((CardView) history.getChildAt(i)).getChildAt(0)).getText();
            outState.putCharSequence("Card" + i, text);
            Log.d("Sergio>>>", "onSaveInstanceState: text(i)= " + text);
        }

        Log.i("Sergio>>>", "onSaveInstanceState: cards= " + cards);*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen and keeps the view contents and state
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(mActivity, "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(mActivity, "portrait", Toast.LENGTH_SHORT).show();
//        }

        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;  //int height = size.y;
        int lr_dip = (int) (4 * scale + 0.5f) * 2;
        cv_width = width - lr_dip;

        hideKeyboard();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_history, menu);
        inflater.inflate(R.menu.menu_help_mmc, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share_history) {
            MenuHelper.share_history(mActivity);
        }

        if (id == R.id.action_clear_all_history) {
            MenuHelper.remove_history(mActivity);

            mmc_num_1.setText("");
            mmc_num_2.setText("");
            mmc_num_3.setText("");
            mmc_num_4.setText("");
            mmc_num_5.setText("");
            mmc_num_6.setText("");
            mmc_num_7.setText("");
            mmc_num_8.setText("");
        }
        if (id == R.id.action_ajuda) {
            ViewGroup history = (ViewGroup) mActivity.findViewById(R.id.history);
            String help_divisores = getString(R.string.help_text_mmc);
            SpannableStringBuilder ssb = new SpannableStringBuilder(help_divisores);
            create(history, ssb, mActivity);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mmc, container, false);

        mmc_num_1 = (EditText) view.findViewById(R.id.mmc_num_1);
        mmc_num_2 = (EditText) view.findViewById(R.id.mmc_num_2);
        mmc_num_3 = (EditText) view.findViewById(R.id.mmc_num_3);
        mmc_num_4 = (EditText) view.findViewById(R.id.mmc_num_4);
        mmc_num_5 = (EditText) view.findViewById(R.id.mmc_num_5);
        mmc_num_6 = (EditText) view.findViewById(R.id.mmc_num_6);
        mmc_num_7 = (EditText) view.findViewById(R.id.mmc_num_7);
        mmc_num_8 = (EditText) view.findViewById(R.id.mmc_num_8);

        Button button = (Button) view.findViewById(R.id.button_calc_mmc);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calc_mmc(view);
            }
        });

        Button clearTextBtn_1 = (Button) view.findViewById(R.id.btn_clear_1);
        clearTextBtn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_1.setText("");
            }
        });
        Button clearTextBtn_2 = (Button) view.findViewById(R.id.btn_clear_2);
        clearTextBtn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_2.setText("");
            }
        });
        Button clearTextBtn_3 = (Button) view.findViewById(R.id.btn_clear_3);
        clearTextBtn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_3.setText("");
            }
        });
        Button clearTextBtn_4 = (Button) view.findViewById(R.id.btn_clear_4);
        clearTextBtn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_4.setText("");
            }
        });
        Button clearTextBtn_5 = (Button) view.findViewById(R.id.btn_clear_5);
        clearTextBtn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_5.setText("");
            }
        });
        Button clearTextBtn_6 = (Button) view.findViewById(R.id.btn_clear_6);
        clearTextBtn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_6.setText("");
            }
        });
        Button clearTextBtn_7 = (Button) view.findViewById(R.id.btn_clear_7);
        clearTextBtn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_7.setText("");
            }
        });
        Button clearTextBtn_8 = (Button) view.findViewById(R.id.btn_clear_8);
        clearTextBtn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmc_num_8.setText("");
            }
        });

        ImageButton add_mmc = (ImageButton) view.findViewById(R.id.button_add_mmc);
        add_mmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_mmc(view);
            }
        });

        ImageButton remove_mmc = (ImageButton) view.findViewById(R.id.button_remove_mmc);
        remove_mmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove_mmc(view);
            }
        });

        mmc_num_1.addTextChangedListener(new TextWatcher() {
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
                    mmc_num_1.setText(oldnum1);
                    mmc_num_1.setSelection(mmc_num_1.getText().length()); //Colocar o cursor no final do texto
                    showToast();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mmc_num_2.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para Long
                    num2 = parseLong(s.toString());
                } catch (Exception e) {
                    mmc_num_2.setText(oldnum2);
                    mmc_num_2.setSelection(mmc_num_2.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mmc_num_3.addTextChangedListener(new TextWatcher() {
            Long num3;
            String oldnum3;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum3 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num3 = parseLong(s.toString());

                } catch (Exception e) {
                    mmc_num_3.setText(oldnum3);
                    mmc_num_3.setSelection(mmc_num_3.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mmc_num_4.addTextChangedListener(new TextWatcher() {
            Long num4;
            String oldnum4;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum4 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num4 = parseLong(s.toString());

                } catch (Exception e) {
                    mmc_num_4.setText(oldnum4);
                    mmc_num_4.setSelection(mmc_num_4.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mmc_num_5.addTextChangedListener(new TextWatcher() {
            Long num5;
            String oldnum5;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum5 = s.toString();

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num5 = parseLong(s.toString());

                } catch (Exception e) {
                    mmc_num_5.setText(oldnum5);
                    mmc_num_5.setSelection(mmc_num_5.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mmc_num_6.addTextChangedListener(new TextWatcher() {
            Long num6;
            String oldnum6;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum6 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num6 = parseLong(s.toString());
                } catch (Exception e) {
                    mmc_num_6.setText(oldnum6);
                    mmc_num_6.setSelection(mmc_num_6.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mmc_num_7.addTextChangedListener(new TextWatcher() {
            Long num7;
            String oldnum7;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum7 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num7 = parseLong(s.toString());
                } catch (Exception e) {
                    mmc_num_7.setText(oldnum7);
                    mmc_num_7.setSelection(mmc_num_7.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mmc_num_8.addTextChangedListener(new TextWatcher() {
            Long num8;
            String oldnum8;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldnum8 = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    return;
                }
                try {
                    // Tentar converter o string para Long
                    num8 = parseLong(s.toString());
                } catch (Exception e) {
                    mmc_num_8.setText(oldnum8);
                    mmc_num_8.setSelection(mmc_num_8.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }


    public void add_mmc(View view) {

        LinearLayout ll_34 = (LinearLayout) view.findViewById(R.id.linear_layout_34);
        LinearLayout ll_56 = (LinearLayout) view.findViewById(R.id.linear_layout_56);
        LinearLayout ll_78 = (LinearLayout) view.findViewById(R.id.linear_layout_78);
        FrameLayout f_3 = (FrameLayout) view.findViewById(R.id.frame_3);
        FrameLayout f_4 = (FrameLayout) view.findViewById(R.id.frame_4);
        FrameLayout f_5 = (FrameLayout) view.findViewById(R.id.frame_5);
        FrameLayout f_6 = (FrameLayout) view.findViewById(R.id.frame_6);
        FrameLayout f_7 = (FrameLayout) view.findViewById(R.id.frame_7);
        FrameLayout f_8 = (FrameLayout) view.findViewById(R.id.frame_8);
        ImageButton add_one = (ImageButton) view.findViewById(R.id.button_add_mmc);
        ImageButton less_one = (ImageButton) view.findViewById(R.id.button_remove_mmc);

        boolean ll_34_visibe = ll_34.getVisibility() == View.VISIBLE;
        boolean f3_visible = f_3.getVisibility() == View.VISIBLE;
        boolean f4_visible = f_4.getVisibility() == View.VISIBLE;
        boolean ll_56_visibe = ll_56.getVisibility() == View.VISIBLE;
        boolean f5_visible = f_5.getVisibility() == View.VISIBLE;
        boolean f6_visible = f_6.getVisibility() == View.VISIBLE;
        boolean ll_78_visibe = ll_78.getVisibility() == View.VISIBLE;
        boolean f7_visible = f_7.getVisibility() == View.VISIBLE;
        boolean f8_visible = f_8.getVisibility() == View.VISIBLE;


        if (!ll_34_visibe || f3_visible || f4_visible) {
            ll_34.setVisibility(View.VISIBLE);

            if (!f3_visible) {
                f_3.setVisibility(View.VISIBLE);
                less_one.setVisibility(View.VISIBLE);
                return;
            }
            if (!f4_visible) {
                f_4.setVisibility(View.VISIBLE);
                return;
            }
        }

        if (!ll_56_visibe || f5_visible || f6_visible) {
            ll_56.setVisibility(View.VISIBLE);

            if (!f5_visible) {
                f_5.setVisibility(View.VISIBLE);
                return;
            }
            if (!f6_visible) {
                f_6.setVisibility(View.VISIBLE);
                return;
            }
        }
        if (!ll_78_visibe || f7_visible || f8_visible) {
            ll_78.setVisibility(View.VISIBLE);

            if (!f7_visible) {
                f_7.setVisibility(View.VISIBLE);
                return;
            }
            if (!f8_visible) {
                f_8.setVisibility(View.VISIBLE);
                add_one.setVisibility(View.INVISIBLE);
                return;
            }
        }

    }

    public void remove_mmc(View view) {

        LinearLayout ll_34 = (LinearLayout) view.findViewById(R.id.linear_layout_34);
        LinearLayout ll_56 = (LinearLayout) view.findViewById(R.id.linear_layout_56);
        LinearLayout ll_78 = (LinearLayout) view.findViewById(R.id.linear_layout_78);

        FrameLayout f_3 = (FrameLayout) view.findViewById(R.id.frame_3);
        FrameLayout f_4 = (FrameLayout) view.findViewById(R.id.frame_4);
        FrameLayout f_5 = (FrameLayout) view.findViewById(R.id.frame_5);
        FrameLayout f_6 = (FrameLayout) view.findViewById(R.id.frame_6);
        FrameLayout f_7 = (FrameLayout) view.findViewById(R.id.frame_7);
        FrameLayout f_8 = (FrameLayout) view.findViewById(R.id.frame_8);

        ImageButton add_one = (ImageButton) view.findViewById(R.id.button_add_mmc);
        ImageButton less_one = (ImageButton) view.findViewById(R.id.button_remove_mmc);

        boolean ll_34_visibe = ll_34.getVisibility() == View.VISIBLE;
        boolean f3_visible = f_3.getVisibility() == View.VISIBLE;
        boolean f4_visible = f_4.getVisibility() == View.VISIBLE;
        boolean ll_56_visibe = ll_56.getVisibility() == View.VISIBLE;
        boolean f5_visible = f_5.getVisibility() == View.VISIBLE;
        boolean f6_visible = f_6.getVisibility() == View.VISIBLE;
        boolean ll_78_visibe = ll_78.getVisibility() == View.VISIBLE;
        boolean f7_visible = f_7.getVisibility() == View.VISIBLE;
        boolean f8_visible = f_8.getVisibility() == View.VISIBLE;

        if (ll_78_visibe) {
            if (f8_visible) {
                mmc_num_8.setText("");
                f_8.setVisibility(View.GONE);
                add_one.setVisibility(View.VISIBLE);
                return;
            }
            if (f7_visible) {
                mmc_num_7.setText("");
                f_7.setVisibility(View.GONE);
                ll_78.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_56_visibe) {
            if (f6_visible) {
                mmc_num_6.setText("");
                f_6.setVisibility(View.GONE);
                return;
            }
            if (f5_visible) {
                mmc_num_5.setText("");
                f_5.setVisibility(View.GONE);
                ll_56.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_34_visibe) {
            if (f4_visible) {
                mmc_num_4.setText("");
                f_4.setVisibility(View.GONE);
                f_4.setAlpha(0);
                return;
            }
            if (f3_visible) {
                mmc_num_3.setText("");
                f_3.setVisibility(View.GONE);
                f_3.setAlpha(0);
                ll_34.setVisibility(View.GONE);
                less_one.setVisibility(View.INVISIBLE);
                return;
            }
        }

    }

    public void hideKeyboard() {
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
    }

    private void calc_mmc(View view) {
        hideKeyboard();

        String str_num1 = mmc_num_1.getText().toString().replaceAll("[^\\d]", "");
        String str_num2 = mmc_num_2.getText().toString().replaceAll("[^\\d]", "");
        String str_num3 = mmc_num_3.getText().toString().replaceAll("[^\\d]", "");
        String str_num4 = mmc_num_4.getText().toString().replaceAll("[^\\d]", "");
        String str_num5 = mmc_num_5.getText().toString().replaceAll("[^\\d]", "");
        String str_num6 = mmc_num_6.getText().toString().replaceAll("[^\\d]", "");
        String str_num7 = mmc_num_7.getText().toString().replaceAll("[^\\d]", "");
        String str_num8 = mmc_num_8.getText().toString().replaceAll("[^\\d]", "");

        long num1, num2, num3, num4, num5, num6, num7, num8;

        ArrayList<BigInteger> numbers = new ArrayList<BigInteger>();

        if (!str_num1.equals("")) {
            try {
                // Tentar converter o string para Long
                num1 = parseLong(str_num1);
                if (num1 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num1 > 0L) {
                    BigInteger num1b = new BigInteger(str_num1);
                    numbers.add(num1b);
                    long_numbers.add(num1);
                }
            } catch (Exception e) {
                showToastNum("1");
                return;
            }
        }
        if (!str_num2.equals("")) {
            try {
                // Tentar converter o string para Long
                num2 = parseLong(str_num2);
                if (num2 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num2 > 0L) {
                    BigInteger num2b = new BigInteger(str_num2);
                    numbers.add(num2b);
                    long_numbers.add(num2);
                }
            } catch (Exception e) {
                showToastNum("2");
                return;
            }
        }
        if (!str_num3.equals("")) {
            try {
                // Tentar converter o string para Long
                num3 = parseLong(str_num3);
                if (num3 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num3 > 0L) {
                    BigInteger num3b = new BigInteger(str_num3);
                    numbers.add(num3b);
                    long_numbers.add(num3);
                }
            } catch (Exception e) {
                showToastNum("3");
                return;
            }
        }
        if (!str_num4.equals("")) {
            try {
                // Tentar converter o string para Long
                num4 = parseLong(str_num4);
                if (num4 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num4 > 0L) {
                    BigInteger num4b = new BigInteger(str_num4);
                    numbers.add(num4b);
                    long_numbers.add(num4);
                }
            } catch (Exception e) {
                showToastNum("4");
                return;
            }
        }
        if (!str_num5.equals("")) {
            try {
                // Tentar converter o string para Long
                num5 = parseLong(str_num5);
                if (num5 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num5 > 0L) {
                    BigInteger num5b = new BigInteger(str_num5);
                    numbers.add(num5b);
                    long_numbers.add(num5);
                }
            } catch (Exception e) {
                showToastNum("5");
                return;
            }
        }
        if (!str_num6.equals("")) {
            try {
                // Tentar converter o string para Long
                num6 = parseLong(str_num6);
                if (num6 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num6 > 0L) {
                    BigInteger num6b = new BigInteger(str_num6);
                    numbers.add(num6b);
                    long_numbers.add(num6);
                }
            } catch (Exception e) {
                showToastNum("6");
                return;
            }
        }

        if (!str_num7.equals("")) {
            try {
                // Tentar converter o string para Long
                num7 = parseLong(str_num7);
                if (num7 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num7 > 0L) {
                    BigInteger num7b = new BigInteger(str_num7);
                    numbers.add(num7b);
                    long_numbers.add(num7);
                }
            } catch (Exception e) {
                showToastNum("7");
                return;
            }
        }

        if (!str_num8.equals("")) {
            try {
                // Tentar converter o string para Long
                num8 = parseLong(str_num8);
                if (num8 == 0L) {
                    showToastMoreThanZero();
                    return;
                } else if (num8 > 0L) {
                    BigInteger num8b = new BigInteger(str_num8);
                    numbers.add(num8b);
                    long_numbers.add(num8);
                }
            } catch (Exception e) {
                showToastNum("8");
                return;
            }
        }
        if (numbers.size() < 2) {
            Toast thetoast = Toast.makeText(mActivity, R.string.add_number_pair, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        String mmc_string = getString(R.string.mmc_result_prefix);
        BigInteger result_mmc = null;

        if (numbers.size() > 1) {
            for (int i = 0; i < numbers.size() - 1; i++) {
                mmc_string += numbers.get(i) + ", ";
            }
            mmc_string += numbers.get(numbers.size() - 1) + ")= ";
            result_mmc = mmc(numbers);
        }

        mmc_string += result_mmc;
        history = (ViewGroup) view.findViewById(R.id.history);

        //criar novo cardview
        cardview = new CardView(mActivity);
        cardview.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,   // width
                CardView.LayoutParams.WRAP_CONTENT)); // height
        cardview.setPreventCornerOverlap(true);

        //int pixels = (int) (dips * scale + 0.5f);
        int lr_dip = (int) (6 * scale + 0.5f);
        int tb_dip = (int) (8 * scale + 0.5f);
        cardview.setRadius((int) (4 * scale + 0.5f));
        cardview.setCardElevation((int) (2 * scale + 0.5f));
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip);
        cardview.setUseCompatPadding(true);
        cardview.setLayoutTransition(new LayoutTransition());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition lt = new LayoutTransition();
            lt.enableTransitionType(CHANGE_APPEARING);
            lt.enableTransitionType(CHANGE_DISAPPEARING);
        }

        int cv_color = ContextCompat.getColor(mActivity, R.color.lightGreen);
        cardview.setCardBackgroundColor(cv_color);

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(new SwipeToDismissTouchListener(
                cardview,
                mActivity,
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

        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0);


        LinearLayout ll_vertical_root = new LinearLayout(mActivity);
        ll_vertical_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_root.setOrientation(LinearLayout.VERTICAL);


        // criar novo Textview
        final TextView textView = new TextView(mActivity);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,   //largura
                ViewGroup.LayoutParams.WRAP_CONTENT)); //altura

        //Adicionar o texto com o resultado
        textView.setText(mmc_string);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTag("texto");

        // add the textview to the cardview
        ll_vertical_root.addView(textView);

        /*
        *
        * Parte das Explicações
        *
        * */

        ll_vertical_expl = new LinearLayout(mActivity);
        ll_vertical_expl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_expl.setOrientation(LinearLayout.VERTICAL);
        ll_vertical_expl.setTag(false);

        final Boolean[] isExpanded = {false};
        final TextView explainLink = new TextView(mActivity);
        explainLink.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,   //largura
                ViewGroup.LayoutParams.WRAP_CONTENT)); //altura
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainLink.setTextColor(ContextCompat.getColor(mActivity, R.color.linkBlue));
        SpannableStringBuilder ssb_show_expl = new SpannableStringBuilder(getString(R.string.explain));
        ssb_show_expl.setSpan(new UnderlineSpan(), 0, ssb_show_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
        explainLink.setText(ssb_show_expl);
        explainLink.setGravity(Gravity.CENTER_HORIZONTAL);

        explainLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExpanded[0]) {
                    Boolean hasExplanation = (Boolean) ll_vertical_expl.getTag();
                    if (BG_Operation_MMC.getStatus() == AsyncTask.Status.PENDING && !hasExplanation ||
                            BG_Operation_MMC.getStatus() == AsyncTask.Status.FINISHED && !hasExplanation) {
                        BG_Operation_MMC = new BackGroundOperation_MMC().execute(long_numbers);
                    }
                    SpannableStringBuilder ssb_hide_expl = new SpannableStringBuilder(getString(R.string.hide_explain));
                    ssb_hide_expl.setSpan(new UnderlineSpan(), 0, ssb_hide_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
                    explainLink.setText(ssb_hide_expl);
                    ((LinearLayout) view.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
                    isExpanded[0] = true;
                } else if (isExpanded[0]) {
                    SpannableStringBuilder ssb_show_expl = new SpannableStringBuilder(getString(R.string.explain));
                    ssb_show_expl.setSpan(new UnderlineSpan(), 0, ssb_show_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
                    explainLink.setText(ssb_show_expl);
                    ((LinearLayout) view.getParent()).getChildAt(2).setVisibility(View.GONE);
                    isExpanded[0] = false;
                }
            }
        });

        //ProgressBar
        cv_width = mActivity.findViewById(R.id.card_view_1).getWidth();
        height_dip = (int) (3 * scale + 0.5f);
        progressBar = new View(mActivity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, height_dip); //Largura, Altura
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);

        //Ponto 1
        explainTextView_1 = new TextView(mActivity);
        String fp = "fatores primos:";
        String explain_text_1 = "1 ▻Decompor os números em " + fp + "\n";
        SpannableStringBuilder ssb_explain_1 = new SpannableStringBuilder(explain_text_1);
        ssb_explain_1.setSpan(new UnderlineSpan(), explain_text_1.length() - fp.length() - 1, explain_text_1.length() - 1, SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_1.setText(ssb_explain_1);

        //Ponto 2
        explainTextView_2 = new TextView(mActivity);
        String comuns = "comuns";
        String ncomuns = "não comuns";
        String uma_vez = "apenas uma vez";
        String maior_exps = "maiores expoentes";
        String explain_text_2 = "2 ▻Escolher os fatores" + " " + comuns + " " + "e" + " " + ncomuns + ", " + uma_vez + ", " + "com os" + " " + maior_exps + ":\n";
        SpannableStringBuilder ssb_explain_2 = new SpannableStringBuilder(explain_text_2);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(comuns), explain_text_2.indexOf(comuns) + comuns.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(ncomuns), explain_text_2.indexOf(ncomuns) + ncomuns.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(uma_vez), explain_text_2.indexOf(uma_vez) + uma_vez.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(maior_exps), explain_text_2.indexOf(maior_exps) + maior_exps.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_2.setText(ssb_explain_2);

        //Ponto 3
        explainTextView_3 = new TextView(mActivity);
        String multipl = "Multiplicar";
        String explain_text_3 = "3 ▻" + multipl + " os fatores para obter o Mínimo Multiplo Comum:" + "\n";
        SpannableStringBuilder ssb_explain_3 = new SpannableStringBuilder(explain_text_3);
        ssb_explain_3.setSpan(new UnderlineSpan(), explain_text_3.indexOf(multipl), explain_text_3.indexOf(multipl) + multipl.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_3.setText(ssb_explain_3);

        ll_vertical_expl.setVisibility(View.GONE);

        ll_vertical_expl.addView(progressBar);
        ll_vertical_expl.addView(explainTextView_1);
        ll_vertical_expl.addView(explainTextView_2);
        ll_vertical_expl.addView(explainTextView_3);
        ll_vertical_root.addView(explainLink);
        ll_vertical_root.addView(ll_vertical_expl);
        cardview.addView(ll_vertical_root);


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

    public class BackGroundOperation_MMC extends AsyncTask<ArrayList<Long>, Float, ArrayList<ArrayList<Long>>> {

        @Override
        public void onPreExecute() {

        }

        @Override
        protected ArrayList<ArrayList<Long>> doInBackground(ArrayList<Long>... numbers) {
            ArrayList<ArrayList<Long>> fatores = new ArrayList<>();

            int numbersSize = numbers[0].size();
            for (int i = 0; i < numbersSize; i++) { // fatorizar todos os números inseridos em MMC

                ArrayList<Long> fatores_ix = new ArrayList<>();

                Long number_i = numbers[0].get(i);
                if (number_i == 1L) {
                    fatores_ix.add(1L);
                }
                while (number_i % 2L == 0) {
                    fatores_ix.add(2L);
                    number_i /= 2L;
                }

                for (long j = 3; j <= number_i / j; j += 2) {
                    while (number_i % j == 0) {
                        fatores_ix.add(j);
                        number_i /= j;
                    }
                    publishProgress(((float) j / ((float) number_i / (float) j)), (float) i);
                    if (isCancelled()) break;
                }
                if (number_i > 1) {
                    fatores_ix.add(number_i);
                }

                fatores.add(fatores_ix);

            }
            Log.d("Sergio>>>", "doInBackground: fatores" + fatores);
            return fatores;
        }

        @Override
        public void onProgressUpdate(Float... values) {
            if (thisFragment != null && thisFragment.isVisible()) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setBackgroundColor(f_colors[Math.round(values[1])]);
                int progress_width = (int) Math.round(values[0] * cv_width);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(progress_width, height_dip);
                progressBar.setLayoutParams(layoutParams);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Long>> result) {
            if (thisFragment != null && thisFragment.isVisible()) {

                ArrayList<ArrayList<Long>> fatoresPrimos = result;
                ArrayList<ArrayList<Long>> datasets = new ArrayList<>();

                for (int k = 0; k < fatoresPrimos.size(); k++) {
                    ArrayList<Long> bases = new ArrayList<>();
                    ArrayList<Long> exps = new ArrayList<>();

                    String str_fatores = long_numbers.get(k) + "=";
                    SpannableStringBuilder ssb_fatores;
                    ssb_fatores = new SpannableStringBuilder(str_fatores);
                    ssb_fatores.setSpan(new ForegroundColorSpan(f_colors[k]), 0, ssb_fatores.length(), SPAN_EXCLUSIVE_INCLUSIVE);

                    Integer counter = 1;
                    Integer nextfactor = 0;
                    Long lastItem = fatoresPrimos.get(k).get(0);

                    //TreeMap
                    LinkedHashMap<String, Integer> dataset = new LinkedHashMap<>();

                    //Contar os expoentes  (sem comentários....)
                    for (int i = 0; i < fatoresPrimos.get(k).size(); i++) {
                        if (i == 0) {
                            dataset.put(String.valueOf(fatoresPrimos.get(k).get(0)), 1);
                            bases.add(fatoresPrimos.get(k).get(0));
                            exps.add(1L);
                        } else if (fatoresPrimos.get(k).get(i).equals(lastItem) && i > 0) {
                            counter++;
                            dataset.put(String.valueOf(fatoresPrimos.get(k).get(i)), counter);
                            bases.set(nextfactor, fatoresPrimos.get(k).get(i));
                            exps.set(nextfactor, (long) counter);
                        } else if (!fatoresPrimos.get(k).get(i).equals(lastItem) && i > 0) {
                            counter = 1;
                            nextfactor++;
                            dataset.put(String.valueOf(fatoresPrimos.get(k).get(i)), counter);
                            bases.add(fatoresPrimos.get(k).get(i));
                            exps.add((long) counter);
                        }
                        lastItem = fatoresPrimos.get(k).get(i);
                    }

                    datasets.add(bases);
                    datasets.add(exps);

                    //Criar os expoentes
                    int value_length;
                    Iterator iterator = dataset.entrySet().iterator();
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
                        }

                        if (iterator.hasNext()) {
                            ssb_fatores.append("×");
                        }

                        iterator.remove(); // avoids a ConcurrentModificationException
                    }
                    if (k < fatoresPrimos.size() - 1) ssb_fatores.append("\n");
                    explainTextView_1.append(ssb_fatores);


                }
                Log.i("Sergio>>>", "onPostExecute: datasets" + datasets);

                int f = 0;
                ArrayList<ArrayList<Long>> mmc_fatores = new ArrayList<>();
                ArrayList<Long> maiores_bases = new ArrayList<>();
                ArrayList<Long> maiores_exps = new ArrayList<>();

                for (int i = 0; i < datasets.size(); i += 2) {

                    ArrayList<Long> bases = datasets.get(i);
                    ArrayList<Long> exps = datasets.get(i + 1);
                    Log.w("Sergio>>>", "indice " + i + " bases " + bases + " exps " + exps);

                    for (int cb = 0; cb < bases.size(); cb++) {
                        Long current_base = bases.get(cb);
                        Long current_exp = exps.get(cb);
                        Log.d("Sergio>>>", "indice cb " + cb + " current_base " + current_base + " current_exp " + current_exp);

                        if (!maiores_bases.contains(current_base)) {
                            maiores_bases.add(current_base);
                            maiores_exps.add(current_exp);
                            Log.d("Sergio>>>", "!contains indice cb " + cb + " maiores_bases " + maiores_bases + " maiores_exps " + maiores_exps);

                        }
//                        if (maiores_bases.contains(current_base)) {
//                            maiores_exps.set(maiores_exps.indexOf(current_exp), current_exp);
//                            Log.d("Sergio>>>", "!contains indice cb " + cb + " maiores_bases " + maiores_bases + " maiores_exps " + maiores_exps);
//
//                        }

                        for (int j = i + 2; j < datasets.size(); j += 2) {
                            ArrayList<Long> next_bases = datasets.get(j);
                            ArrayList<Long> next_exps = datasets.get(j + 1);

                            for (int nb = 0; nb < next_bases.size(); nb++) {
                                Long next_base = next_bases.get(nb);
                                Long next_exp = next_exps.get(nb);

                                if (next_base == current_base && next_exp > current_exp && maiores_bases.contains(current_base)) {

                                    maiores_exps.set(maiores_exps.indexOf(current_exp), next_exp);
                                    Log.i("Sergio>>>", "!contains indice nb next base " + nb + " maiores_bases " + maiores_bases + " maiores_exps " + maiores_exps);
                                }

//                                if (next_base == current_base && next_exp > current_exp && !maiores_bases.contains(current_base)) {
//                                    maiores_bases.add(next_base);
//                                    maiores_exps.add(next_exp);
//                                }
                            }
                        }
                    }
                }

                mmc_fatores.add(maiores_bases);
                mmc_fatores.add(maiores_exps);
                Log.e("Sergio>>>", "final dos ciclos, mmc_fatores " + mmc_fatores);

                progressBar.setVisibility(View.GONE);
                ll_vertical_expl.setTag(true); // true - já com explicação
                long_numbers.clear();


                datasets.clear();
            }
//
//        @Override
//        protected void onCancelled(ArrayList<Long> parcial) {
//            super.onCancelled(parcial);
//
//
//            if (thisFragment != null && thisFragment.isVisible()) {
//
//                /* resultadosDivisao|fatoresPrimos
//                *                100|2
//                *                 50|2
//                *                 25|5
//                *                  5|5
//                *                  1|1
//                *
//                * */
//
//                ArrayList<Long> resultadosDivisao = parcial.get(0);
//                ArrayList<Long> fatoresPrimos = parcial.get(1);
//
//                // Tamanho da lista de números primos
//                int sizeList = fatoresPrimos.size();
//
//                String str_fatores = "";
//                String str_results = "";
//                String str_divisores = "";
//                SpannableStringBuilder ssb_fatores;
//
//                if (sizeList == 1) {
//                    str_fatores = resultadosDivisao.get(0) + " " + getString(R.string.its_a_prime);
//                    ssb_fatores = new SpannableStringBuilder(str_fatores);
//                    CreateCardView.create(history, ssb_fatores, getActivity());
//
//                } else {
//                    str_fatores = getString(R.string.factorization_of) + " " + resultadosDivisao.get(0) + " = \n";
//
//                    Boolean hasExpoentes = false;
//                    Integer counter = 1;
//                    Long lastItem = fatoresPrimos.get(0);
//
//                    //TreeMap
//                    LinkedHashMap<String, Integer> dataset = new LinkedHashMap<>();
//
//                    //Contar os expoentes
//                    for (int i = 0; i < fatoresPrimos.size(); i++) {
//                        str_fatores += fatoresPrimos.get(i) + "×";
//                        if (i == 0) {
//                            dataset.put(String.valueOf(fatoresPrimos.get(0)), 1);
//                        } else if (fatoresPrimos.get(i).equals(lastItem) && i > 0) {
//                            hasExpoentes = true;
//                            counter++;
//                            dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
//                        } else if (!fatoresPrimos.get(i).equals(lastItem) && i > 0) {
//                            counter = 1;
//                            dataset.put(String.valueOf(fatoresPrimos.get(i)), counter);
//                        }
//                        lastItem = fatoresPrimos.get(i);
//                    }
//                    str_fatores = str_fatores.substring(0, str_fatores.length() - 1) + "=\n";
//                    ssb_fatores = new SpannableStringBuilder(str_fatores);
//
//                    if (hasExpoentes) {
//                        int value_length;
//
//                        Iterator iterator = dataset.entrySet().iterator();
//
//                        //Criar os expoentes
//                        while (iterator.hasNext()) {
//                            Map.Entry pair = (Map.Entry) iterator.next();
//
//                            if (Integer.parseInt(pair.getValue().toString()) == 1) {
//                                //Expoente 1
//                                ssb_fatores.append(pair.getKey().toString());
//
//                            } else if (Integer.parseInt(pair.getValue().toString()) > 1) {
//                                //Expoente superior a 1
//                                value_length = pair.getValue().toString().length();
//                                ssb_fatores.append(pair.getKey().toString() + pair.getValue().toString());
//                                ssb_fatores.setSpan(new SuperscriptSpan(), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
//                                ssb_fatores.setSpan(new RelativeSizeSpan(0.8f), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
//                                ssb_fatores.setSpan(new ForegroundColorSpan(Color.RED), ssb_fatores.length() - value_length, ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
//                            }
//
//                            if (iterator.hasNext()) {
//                                ssb_fatores.append("×");
//                            }
//
//                            iterator.remove(); // avoids a ConcurrentModificationException
//                        }
//                    }
//                    String incomplete_calc = "\n" + getString(R.string._incomplete_calc);
//                    ssb_fatores.append(incomplete_calc);
//                    ssb_fatores.setSpan(new ForegroundColorSpan(Color.RED), ssb_fatores.length() - incomplete_calc.length(), ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
//                    ssb_fatores.setSpan(new RelativeSizeSpan(0.8f), ssb_fatores.length() - incomplete_calc.length(), ssb_fatores.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    for (int i = 0; i < sizeList - 1; i++) {
//                        str_divisores += String.valueOf(fatoresPrimos.get(i)) + "\n";
//                    }
//                    str_divisores += String.valueOf(fatoresPrimos.get(sizeList - 1));
//
//                    for (int i = 0; i < resultadosDivisao.size() - 1; i++) {
//                        str_results += String.valueOf(resultadosDivisao.get(i)) + "\n";
//                    }
//                    str_results += String.valueOf(resultadosDivisao.get(resultadosDivisao.size() - 1));
//
//                    createCardViewLayout(history, str_results, str_divisores, ssb_fatores);
//                }
//
//                progressBar.setVisibility(View.GONE);
//                button.setText(getString(R.string.fatorizar_btn));
//                button.setClickable(true);
//                hasExplanation[0] = true;
//            }
//
//
//        }
//

        }
    }
}
