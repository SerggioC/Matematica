package com.sergiocruz.Matematica.fragment;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
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
import com.sergiocruz.Matematica.activity.AboutActivity;
import com.sergiocruz.Matematica.activity.SettingsActivity;
import com.sergiocruz.Matematica.helper.CreateCardView;
import com.sergiocruz.Matematica.helper.MenuHelper;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
import static android.widget.Toast.makeText;
import static java.lang.Long.parseLong;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MDCFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MDCFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MDCFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public float scale;
    EditText mdc_num_1, mdc_num_2, mdc_num_3, mdc_num_4, mdc_num_5, mdc_num_6, mdc_num_7, mdc_num_8;
    ArrayList<AsyncTask> asyncTaskQueue = new ArrayList<>();

    int taskNumber = 0;
    int height_dip, cv_width;
    ArrayList<Integer> fColors;

    Activity mActivity;
    Fragment thisFragment = this;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MDCFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MDCFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MDCFragment newInstance(String param1, String param2) {
        MDCFragment fragment = new MDCFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /*****************************************************************
     * MMC: Mínimo múltiplo comum (lcm: least common multiplier)
     *****************************************************************/
//    private static long mmc(long a, long b) {
//        return (b / mdc(a, b)) * a;
//    }
//    private static BigInteger mmc(BigInteger a, BigInteger b) {
//        return b.divide(a.gcd(b)).multiply(a);
//    }
//
//    private static BigInteger mmc(ArrayList<BigInteger> input) {
//        BigInteger result = input.get(0);
//        for (int i = 1; i < input.size(); i++)
//            result = mmc(result, input.get(i));
//        return result;
//    }
//
//    /****************************************************************
//     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
//     *****************************************************************/
//    private static BigInteger mdc(BigInteger a, BigInteger b) {
//        while (b.compareTo(ZERO) == 1) {
//            BigInteger temp = b;
//            b = a.remainder(b);
//            a = temp;
//        }
//        return a;
//    }
    private static BigInteger mdc(ArrayList<BigInteger> input) {
        BigInteger result = input.get(0);
        for (int i = 1; i < input.size(); i++)
            result = result.gcd(input.get(i));
        return result;
    }

    public void showToast() {
        Toast thetoast = makeText(getActivity(), R.string.numero_alto, Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    private void showToastNum(String field) {
        Toast thetoast = makeText(getActivity(), R.string.number_in_field + " " + field + " " + R.string.too_high, Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    private void showToastMoreThanZero() {
        Toast thetoast = makeText(getActivity(), R.string.maiores_qzero, Toast.LENGTH_LONG);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     *****************************************************************/
    //private final static BigInteger mdc2(BigInteger a, BigInteger b) {
    //    return b.compareTo(ZERO) == 1 ? a : mdc(b, a.remainder(b));
    //}
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//         Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
//        }

        hideKeyboard();
    }

    public void hideKeyboard() {
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

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
        int[] f_colors = mActivity.getResources().getIntArray(R.array.f_colors_xml);
        fColors = new ArrayList<>();
        for (int i = 0; i < f_colors.length; i++) {
            fColors.add(f_colors[i]);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Boolean hasCanceled = false;
        for (int i = 0; i < asyncTaskQueue.size(); i++) {
            if (asyncTaskQueue.get(i) != null) {
                asyncTaskQueue.get(i).cancel(true);
                hasCanceled = true;
            }
        }

        if (hasCanceled) {
            Toast thetoast = Toast.makeText(mActivity, getString(R.string.canceled_op), Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.menu_sub_main, menu);
        inflater.inflate(R.menu.menu_help_mdc, menu);
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
            mdc_num_1.setText("");
            mdc_num_2.setText("");
            mdc_num_3.setText("");
            mdc_num_4.setText("");
            mdc_num_5.setText("");
            mdc_num_6.setText("");
            mdc_num_7.setText("");
            mdc_num_8.setText("");
        }

        if (id == R.id.action_ajuda) {
            ViewGroup history = (ViewGroup) getActivity().findViewById(R.id.history);
            String help_divisores = getString(R.string.help_text_mdc);
            SpannableStringBuilder ssb = new SpannableStringBuilder(help_divisores);
            CreateCardView.create(history, ssb, getActivity());
        }

        if (id == R.id.action_about) {
            startActivity(new Intent(mActivity, AboutActivity.class));
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(mActivity, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mdc, container, false);
        mdc_num_1 = (EditText) view.findViewById(R.id.mdc_num_1);
        mdc_num_2 = (EditText) view.findViewById(R.id.mdc_num_2);
        mdc_num_3 = (EditText) view.findViewById(R.id.mdc_num_3);
        mdc_num_4 = (EditText) view.findViewById(R.id.mdc_num_4);
        mdc_num_5 = (EditText) view.findViewById(R.id.mdc_num_5);
        mdc_num_6 = (EditText) view.findViewById(R.id.mdc_num_6);
        mdc_num_7 = (EditText) view.findViewById(R.id.mdc_num_7);
        mdc_num_8 = (EditText) view.findViewById(R.id.mdc_num_8);

        Button button = (Button) view.findViewById(R.id.button_calc_mdc);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calc_mdc(view);
            }
        });

        Button clearTextBtn_1 = (Button) view.findViewById(R.id.btn_clear_1);
        clearTextBtn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_1.setText("");
            }
        });
        Button clearTextBtn_2 = (Button) view.findViewById(R.id.btn_clear_2);
        clearTextBtn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_2.setText("");
            }
        });
        Button clearTextBtn_3 = (Button) view.findViewById(R.id.btn_clear_3);
        clearTextBtn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_3.setText("");
            }
        });
        Button clearTextBtn_4 = (Button) view.findViewById(R.id.btn_clear_4);
        clearTextBtn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_4.setText("");
            }
        });
        Button clearTextBtn_5 = (Button) view.findViewById(R.id.btn_clear_5);
        clearTextBtn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_5.setText("");
            }
        });
        Button clearTextBtn_6 = (Button) view.findViewById(R.id.btn_clear_6);
        clearTextBtn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_6.setText("");
            }
        });
        Button clearTextBtn_7 = (Button) view.findViewById(R.id.btn_clear_7);
        clearTextBtn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_7.setText("");
            }
        });
        Button clearTextBtn_8 = (Button) view.findViewById(R.id.btn_clear_8);
        clearTextBtn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdc_num_8.setText("");
            }
        });

        ImageButton add_mdc = (ImageButton) view.findViewById(R.id.button_add_mdc);
        add_mdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_mdc(view);
            }
        });

        ImageButton remove_mdc = (ImageButton) view.findViewById(R.id.button_remove_mdc);
        remove_mdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove_mdc(view);
            }
        });

        mdc_num_1.addTextChangedListener(new TextWatcher() {
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
                    mdc_num_1.setText(oldnum1);
                    mdc_num_1.setSelection(mdc_num_1.getText().length()); //Colocar o cursor no final do texto
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mdc_num_2.addTextChangedListener(new TextWatcher() {
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
                    mdc_num_2.setText(oldnum2);
                    mdc_num_2.setSelection(mdc_num_2.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mdc_num_3.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num3 = parseLong(s.toString());

                } catch (Exception e) {
                    mdc_num_3.setText(oldnum3);
                    mdc_num_3.setSelection(mdc_num_3.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mdc_num_4.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num4 = parseLong(s.toString());

                } catch (Exception e) {
                    mdc_num_4.setText(oldnum4);
                    mdc_num_4.setSelection(mdc_num_4.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mdc_num_5.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num5 = parseLong(s.toString());

                } catch (Exception e) {
                    mdc_num_5.setText(oldnum5);
                    mdc_num_5.setSelection(mdc_num_5.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mdc_num_6.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num6 = parseLong(s.toString());
                } catch (Exception e) {
                    mdc_num_6.setText(oldnum6);
                    mdc_num_6.setSelection(mdc_num_6.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mdc_num_7.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num7 = parseLong(s.toString());
                } catch (Exception e) {
                    mdc_num_7.setText(oldnum7);
                    mdc_num_7.setSelection(mdc_num_7.getText().length());
                    showToast();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mdc_num_8.addTextChangedListener(new TextWatcher() {
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
                    // Tentar converter o string para long
                    num8 = parseLong(s.toString());
                } catch (Exception e) {
                    mdc_num_8.setText(oldnum8);
                    mdc_num_8.setSelection(mdc_num_8.getText().length());
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

    public void add_mdc(View view) {

        LinearLayout ll_34 = (LinearLayout) view.findViewById(R.id.linear_layout_34);
        LinearLayout ll_56 = (LinearLayout) view.findViewById(R.id.linear_layout_56);
        LinearLayout ll_78 = (LinearLayout) view.findViewById(R.id.linear_layout_78);
        FrameLayout f_3 = (FrameLayout) view.findViewById(R.id.frame_3);
        FrameLayout f_4 = (FrameLayout) view.findViewById(R.id.frame_4);
        FrameLayout f_5 = (FrameLayout) view.findViewById(R.id.frame_5);
        FrameLayout f_6 = (FrameLayout) view.findViewById(R.id.frame_6);
        FrameLayout f_7 = (FrameLayout) view.findViewById(R.id.frame_7);
        FrameLayout f_8 = (FrameLayout) view.findViewById(R.id.frame_8);
        ImageButton add_one = (ImageButton) view.findViewById(R.id.button_add_mdc);
        ImageButton less_one = (ImageButton) view.findViewById(R.id.button_remove_mdc);

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

    public void remove_mdc(View view) {

        LinearLayout ll_34 = (LinearLayout) view.findViewById(R.id.linear_layout_34);
        LinearLayout ll_56 = (LinearLayout) view.findViewById(R.id.linear_layout_56);
        LinearLayout ll_78 = (LinearLayout) view.findViewById(R.id.linear_layout_78);

        FrameLayout f_3 = (FrameLayout) view.findViewById(R.id.frame_3);
        FrameLayout f_4 = (FrameLayout) view.findViewById(R.id.frame_4);
        FrameLayout f_5 = (FrameLayout) view.findViewById(R.id.frame_5);
        FrameLayout f_6 = (FrameLayout) view.findViewById(R.id.frame_6);
        FrameLayout f_7 = (FrameLayout) view.findViewById(R.id.frame_7);
        FrameLayout f_8 = (FrameLayout) view.findViewById(R.id.frame_8);

        ImageButton add_one = (ImageButton) view.findViewById(R.id.button_add_mdc);
        ImageButton less_one = (ImageButton) view.findViewById(R.id.button_remove_mdc);

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
                mdc_num_8.setText("");
                f_8.setVisibility(View.GONE);
                add_one.setVisibility(View.VISIBLE);
                return;
            }
            if (f7_visible) {
                mdc_num_7.setText("");
                f_7.setVisibility(View.GONE);
                ll_78.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_56_visibe) {
            if (f6_visible) {
                mdc_num_6.setText("");
                f_6.setVisibility(View.GONE);
                return;
            }
            if (f5_visible) {
                mdc_num_5.setText("");
                f_5.setVisibility(View.GONE);
                ll_56.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_34_visibe) {
            if (f4_visible) {
                mdc_num_4.setText("");
                f_4.setVisibility(View.GONE);
                return;
            }
            if (f3_visible) {
                mdc_num_3.setText("");
                f_3.setVisibility(View.GONE);
                ll_34.setVisibility(View.GONE);
                less_one.setVisibility(View.INVISIBLE);
                return;
            }
        }

    }

    private void calc_mdc(View view) {
        hideKeyboard();

        String str_num1 = mdc_num_1.getText().toString();
        String str_num2 = mdc_num_2.getText().toString();
        String str_num3 = mdc_num_3.getText().toString();
        String str_num4 = mdc_num_4.getText().toString();
        String str_num5 = mdc_num_5.getText().toString();
        String str_num6 = mdc_num_6.getText().toString();
        String str_num7 = mdc_num_7.getText().toString();
        String str_num8 = mdc_num_8.getText().toString();

        long num1, num2, num3, num4, num5, num6, num7, num8;

        ArrayList<BigInteger> numbers = new ArrayList<BigInteger>();
        ArrayList<Long> long_numbers = new ArrayList<Long>();

        if (!str_num1.equals("")) {
            try {
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
                // Tentar converter o string para long
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
            Toast thetoast = Toast.makeText(getActivity(), R.string.add_number_pair, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        String mdc_string = getString(R.string.mdc_result_prefix);
        BigInteger result_mdc = null;

        if (numbers.size() > 1) {
            for (int i = 0; i < numbers.size() - 1; i++) {
                mdc_string += numbers.get(i) + ", ";
            }
            mdc_string += numbers.get(numbers.size() - 1) + ")= ";
            result_mdc = mdc(numbers);
        }

        mdc_string += result_mdc;
        SpannableStringBuilder ssb = new SpannableStringBuilder(mdc_string);
        if (result_mdc.toString().equals("1")) {
            ssb.append("\n" + getString(R.string.primos_si));
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#29712d")), ssb.length() - 24, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new RelativeSizeSpan(0.9f), ssb.length() - 24, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ViewGroup history = (ViewGroup) view.findViewById(R.id.history);

        //criar novo cardview
        CardView cardview = new CardView(mActivity);
        cardview.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,   // width
                CardView.LayoutParams.WRAP_CONTENT)); // height
        cardview.setPreventCornerOverlap(true);

        //int pixels = (int) (dips * scale + 0.5f);
        int lr_dip = (int) (6 * scale + 0.5f);
        int tb_dip = (int) (8 * scale + 0.5f);
        cardview.setRadius((int) (2 * scale + 0.5f));
        cardview.setCardElevation((int) (2 * scale + 0.5f));
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip);
        cardview.setUseCompatPadding(true);
        cardview.setLayoutTransition(new LayoutTransition());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition lt = new LayoutTransition();
            lt.enableTransitionType(CHANGE_APPEARING);
            lt.enableTransitionType(CHANGE_DISAPPEARING);
        }

        int cv_color = ContextCompat.getColor(mActivity, R.color.cardsColor);
        cardview.setCardBackgroundColor(cv_color);

        // Create a generic swipe-to-dismiss touch listener.
        cardview.setOnTouchListener(new SwipeToDismissTouchListener(
                cardview,
                mActivity,
                new SwipeToDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Boolean token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view) {
                        //history.removeView(cardview);
                        check_bg_operation(view);
                    }
                }));

        MyTags tags = new MyTags(cardview, long_numbers, result_mdc, false, false, "", null, 0);
        cardview.setTag(tags);

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
        textView.setText(ssb);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setTag("texto");

        // add the textview to the cardview
        ll_vertical_root.addView(textView);

        /*
        *
        * Parte das Explicações
        *
        * */


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

                MyTags thisCardTags = (MyTags) ((CardView) view.getParent().getParent()).getTag();
                Boolean hasExplanation = thisCardTags.getHasExplanation();
                Boolean hasBGOperation = thisCardTags.getHasBGOperation();

                if (!isExpanded[0]) {

                    if (!hasBGOperation && !hasExplanation) {
                        thisCardTags.setTaskNumber(taskNumber);
                        AsyncTask<Void, Float, Void> BG_Operation_MMC = new BackGroundOperation_MDC(thisCardTags)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        asyncTaskQueue.add(BG_Operation_MMC);
                        taskNumber++;
                    }


                    SpannableStringBuilder ssb_hide_expl = new SpannableStringBuilder(getString(R.string.hide_explain));
                    ssb_hide_expl.setSpan(new UnderlineSpan(), 0, ssb_hide_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
                    ((TextView) view).setText(ssb_hide_expl);
                    ((LinearLayout) view.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
                    isExpanded[0] = true;

                } else if (isExpanded[0]) {
                    SpannableStringBuilder ssb_show_expl = new SpannableStringBuilder(getString(R.string.explain));
                    ssb_show_expl.setSpan(new UnderlineSpan(), 0, ssb_show_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
                    ((TextView) view).setText(ssb_show_expl);
                    ((LinearLayout) view.getParent()).getChildAt(2).setVisibility(View.GONE);
                    isExpanded[0] = false;

                }
            }
        });

        //LL vertical das explicações
        LinearLayout ll_vertical_expl = new LinearLayout(mActivity);
        ll_vertical_expl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_expl.setOrientation(LinearLayout.VERTICAL);
        //ll_vertical_expl.setTag(false); //has explanation

        //ProgressBar
        cv_width = mActivity.findViewById(R.id.card_view_1).getWidth();
        height_dip = (int) (3 * scale + 0.5f);
        View progressBar = new View(mActivity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, height_dip); //Largura, Altura
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        //progressBar.setTag(false); //No BG operation

        //Ponto 1
        TextView explainTextView_1 = new TextView(mActivity);
        String fp = "fatores primos:";
        String explain_text_1 = "▻Decompor os números em" + " " + fp + "\n";
        SpannableStringBuilder ssb_explain_1 = new SpannableStringBuilder(explain_text_1);
        ssb_explain_1.setSpan(new UnderlineSpan(), explain_text_1.length() - fp.length() - 1, explain_text_1.length() - 1, SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        explainTextView_1.setText(ssb_explain_1);

        //Ponto 2
        TextView explainTextView_2 = new TextView(mActivity);
        String comuns = "comuns";
        String uma_vez = "apenas uma vez";
        String menor_exps = "menores expoentes";
        String explain_text_2 = "▻Escolher os fatores" + " " + comuns + ", " + uma_vez + ", " + "com os" + " " + menor_exps + ":\n";
        SpannableStringBuilder ssb_explain_2 = new SpannableStringBuilder(explain_text_2);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(comuns), explain_text_2.indexOf(comuns) + comuns.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(uma_vez), explain_text_2.indexOf(uma_vez) + uma_vez.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(menor_exps), explain_text_2.indexOf(menor_exps) + menor_exps.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        explainTextView_2.setText(ssb_explain_2);

        //Ponto 3
        TextView explainTextView_3 = new TextView(mActivity);
        String multipl = "▻Multiplicar";
        String explain_text_3 = multipl + " " +
                "os fatores para obter o Máximo Divisor Comum:" + "\n";
        SpannableStringBuilder ssb_explain_3 = new SpannableStringBuilder(explain_text_3);
        ssb_explain_3.setSpan(new UnderlineSpan(), explain_text_3.indexOf(multipl) + 1, explain_text_3.indexOf(multipl) + multipl.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
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

    public void check_bg_operation(View view) {
        MyTags theTags = (MyTags) view.getTag();
        if (theTags.getHasBGOperation()) {
            int taskNumber = theTags.getTaskNumber();
            AsyncTask task = asyncTaskQueue.get(taskNumber);
            if (task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
                asyncTaskQueue.set(taskNumber, null);
                theTags.setHasBGOperation(false);
                Toast thetoast = Toast.makeText(mActivity, getString(R.string.canceled_op), Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
            }
        }
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

    public class MyTags {
        CardView theCardView;
        ArrayList<Long> long_numbers;
        BigInteger result_mdc;
        Boolean hasExplanation;
        Boolean hasBGOperation;
        String texto;
        ArrayList<ArrayList<Long>> bgfatores;
        int taskNumber;

        MyTags(CardView theCardView, ArrayList<Long> long_numbers, BigInteger result_mdc,
               Boolean hasExplanation, Boolean hasBGOperation, String texto, ArrayList<ArrayList<Long>> bgfatores, int taskNumber) {
            this.theCardView = theCardView;
            this.long_numbers = long_numbers;
            this.result_mdc = result_mdc;
            this.hasExplanation = hasExplanation;
            this.hasBGOperation = hasBGOperation;
            this.texto = texto;
            this.bgfatores = bgfatores;
            this.taskNumber = taskNumber;
        }

        //Methods
        CardView getCardView() {
            return theCardView;
        }

        void setCardView(CardView theCardView) {
            this.theCardView = theCardView;
        }

        ArrayList<Long> getLongNumbers() {
            return long_numbers;
        }

        void setLongNumbers(ArrayList<Long> long_numbers) {
            this.long_numbers = long_numbers;
        }

        BigInteger getResultMDC() {
            return result_mdc;
        }

        void setResultMDC(BigInteger result_mdc) {
            this.result_mdc = result_mdc;
        }

        Boolean getHasExplanation() {
            return hasExplanation;
        }

        void setHasExplanation(Boolean hasExplanation) {
            this.hasExplanation = hasExplanation;
        }

        Boolean getHasBGOperation() {
            return hasBGOperation;
        }

        void setHasBGOperation(Boolean hasBGOperation) {
            this.hasBGOperation = hasBGOperation;
        }

        String getTexto() {
            return texto;
        }

        void setTexto(String texto) {
            this.texto = texto;
        }

        ArrayList<ArrayList<Long>> getBGfatores() {
            return bgfatores;
        }

        void setBGfatores(ArrayList<ArrayList<Long>> bgfatores) {
            this.bgfatores = bgfatores;
        }

        int getTaskNumber() {
            return taskNumber;
        }

        void setTaskNumber(int taskNumber) {
            this.taskNumber = taskNumber;
        }


    }

    // Asynctask <Params, Progress, Result>
    public class BackGroundOperation_MDC extends AsyncTask<Void, Float, Void> {
        MyTags cardTags;
        CardView theCardViewBG;
        ArrayList<Long> mdc_numbers;
        BigInteger result_mdc;
        ArrayList<ArrayList<Long>> bgfatores;

        View progressBar;

        BackGroundOperation_MDC(MyTags cardTags) {
            this.cardTags = cardTags;
        }


        @Override
        public void onPreExecute() {
            theCardViewBG = cardTags.getCardView();
            progressBar = ((LinearLayout) ((LinearLayout) ((CardView) theCardViewBG).getChildAt(0)).getChildAt(2)).getChildAt(0);
            progressBar.setVisibility(View.VISIBLE);
            cardTags.setHasBGOperation(true);
            Collections.shuffle(fColors); //randomizar as cores
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<ArrayList<Long>> fatores = new ArrayList<>();
            mdc_numbers = cardTags.getLongNumbers();
            int numbersSize = mdc_numbers.size();
            for (int i = 0; i < numbersSize; i++) { // fatorizar todos os números inseridos em MMC
                ArrayList<Long> fatores_ix = new ArrayList<>();
                Long number_i = mdc_numbers.get(i);
                //if (number_i == 1L) { //adicionar o fator 1 para calibrar em baixo a contagem....
                fatores_ix.add(1L);
                //}
                while (number_i % 2L == 0) {
                    fatores_ix.add(2L);
                    number_i /= 2L;
                }
                for (long j = 3; j <= number_i / j; j += 2) {
                    while (number_i % j == 0) {
                        fatores_ix.add(j);
                        number_i /= j;
                    }
                    publishProgress(((float) j) / ((float) number_i / (float) j), (float) i); //+10 para dar visibilidade inicial à barra de progresso
                    if (isCancelled()) break;
                }
                if (number_i > 1) {
                    fatores_ix.add(number_i);
                }
                fatores.add(fatores_ix);
            }
            cardTags.setBGfatores(fatores);
            return null;
        }

        @Override
        public void onProgressUpdate(Float... values) {
            if (thisFragment != null && thisFragment.isVisible()) {
                progressBar.setBackgroundColor(fColors.get(Math.round(values[1])));
                int progress_width = Math.round(values[0] * cv_width);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(progress_width, height_dip);
                progressBar.setLayoutParams(layoutParams);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (thisFragment != null && thisFragment.isVisible()) {
                bgfatores = cardTags.getBGfatores();
                ArrayList<ArrayList<Long>> datasets = new ArrayList<>();

                for (int k = 0; k < bgfatores.size(); k++) {
                    ArrayList<Long> bases = new ArrayList<>();
                    ArrayList<Long> exps = new ArrayList<>();

                    String str_fatores = mdc_numbers.get(k) + "=";
                    SpannableStringBuilder ssb_fatores = new SpannableStringBuilder(str_fatores);
                    ssb_fatores.setSpan(new ForegroundColorSpan(fColors.get(k)), 0, ssb_fatores.length(), SPAN_EXCLUSIVE_INCLUSIVE);
                    ssb_fatores.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_fatores.length(), SPAN_EXCLUSIVE_INCLUSIVE);

                    Integer counter = 1;
                    Integer nextfactor = 0;
                    Long lastItem = bgfatores.get(k).get(0);

                    //TreeMap
                    LinkedHashMap<String, Integer> dataset = new LinkedHashMap<>();

                    //Contar os expoentes  (sem comentários....)
                    for (int i = 0; i < bgfatores.get(k).size(); i++) {
                        if (i == 0) {
                            dataset.put(String.valueOf(bgfatores.get(k).get(0)), 1);
                            bases.add(bgfatores.get(k).get(0));
                            exps.add(1L);
                        } else if (bgfatores.get(k).get(i).equals(lastItem) && i > 0) {
                            counter++;
                            dataset.put(String.valueOf(bgfatores.get(k).get(i)), counter);
                            bases.set(nextfactor, bgfatores.get(k).get(i));
                            exps.set(nextfactor, (long) counter);
                        } else if (!bgfatores.get(k).get(i).equals(lastItem) && i > 0) {
                            counter = 1;
                            nextfactor++;
                            dataset.put(String.valueOf(bgfatores.get(k).get(i)), counter);
                            bases.add(bgfatores.get(k).get(i));
                            exps.add((long) counter);
                        }
                        lastItem = bgfatores.get(k).get(i);
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
                    if (k < bgfatores.size() - 1) ssb_fatores.append("\n");

                    //explainTextView_1;
                    ((TextView) ((LinearLayout) ((LinearLayout) ((CardView) theCardViewBG).getChildAt(0)).getChildAt(2)).getChildAt(1)).append(ssb_fatores);
                }

                ArrayList<Long> bases_comuns = new ArrayList<>();
                ArrayList<Long> exps_comuns = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();
                ArrayList<Long> bases = datasets.get(0);
                ArrayList<Long> exps = datasets.get(1);
                currentBaseLoop:
                for (int cb = 0; cb < bases.size(); cb++) {
                    Long current_base = bases.get(cb);
                    Long current_exp = exps.get(cb);
                    ArrayList<Long> temp_bases = new ArrayList();
                    ArrayList<Long> temp_exps = new ArrayList();
                    ArrayList<Integer> temp_colors = new ArrayList();
                    temp_bases.add(current_base);
                    temp_exps.add(current_exp);
                    temp_colors.add(0);
                    nextBasesLoop:
                    for (int j = 2; j < datasets.size(); j += 2) {
                        ArrayList<Long> next_bases = datasets.get(j);
                        if (!next_bases.contains(current_base)) {
                            break nextBasesLoop;
                        }
                        ArrayList<Long> next_exps = datasets.get(j + 1);
                        innerLoop:
                        for (int nb = 0; nb < next_bases.size(); nb++) {
                            Long next_base = next_bases.get(nb);
                            Long next_exp = next_exps.get(nb);
                            if (next_base == current_base) {
                                temp_bases.add(next_base);
                                temp_exps.add(next_exp);
                                temp_colors.add(j / 2);
                            }
                        }
                    }
                    Long lower_exp = temp_exps.get(0);
                    int lowerIndex = 0;
                    if (Collections.frequency(temp_bases, current_base) == datasets.size() / 2) {
                        for (int i = 0; i < temp_exps.size(); i++) {
                            if (temp_exps.get(i) < lower_exp) {
                                lower_exp = temp_exps.get(i);
                                lowerIndex = i;
                            }
                        }
                        bases_comuns.add(temp_bases.get(lowerIndex));
                        exps_comuns.add(lower_exp);
                        colors.add(temp_colors.get(lowerIndex));
                    }
                }

                SpannableStringBuilder ssb_mdc = new SpannableStringBuilder();

                //Criar os expoentes do MDC com os maiores fatores com cores e a negrito
                for (int i = 0; i < bases_comuns.size(); i++) {
                    int base_length = bases_comuns.get(i).toString().length();

                    if (exps_comuns.get(i) == 1L) {
                        //Expoente 1
                        ssb_mdc.append(bases_comuns.get(i).toString());
                        ssb_mdc.setSpan(new ForegroundColorSpan(fColors.get(colors.get(i))),
                                ssb_mdc.length() - base_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mdc.setSpan(new StyleSpan(Typeface.BOLD), ssb_mdc.length() - base_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else if (exps_comuns.get(i) > 1L) {
                        //Expoente superior a 1
                        int exp_length = exps_comuns.get(i).toString().length();
                        ssb_mdc.append(bases_comuns.get(i).toString() + exps_comuns.get(i).toString());
                        ssb_mdc.setSpan(new SuperscriptSpan(), ssb_mdc.length() - exp_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mdc.setSpan(new RelativeSizeSpan(0.8f), ssb_mdc.length() - exp_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mdc.setSpan(new ForegroundColorSpan(fColors.get(colors.get(i))),
                                ssb_mdc.length() - exp_length - base_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mdc.setSpan(new StyleSpan(Typeface.BOLD), ssb_mdc.length() - exp_length - base_length, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    ssb_mdc.append("×");
                }
                ssb_mdc.replace(ssb_mdc.length() - 1, ssb_mdc.length(), "");

                //explainTextView_2
                ((TextView) ((LinearLayout) ((LinearLayout) ((CardView) theCardViewBG).getChildAt(0)).getChildAt(2)).getChildAt(2)).append(ssb_mdc);

                ssb_mdc.delete(0, ssb_mdc.length());
                result_mdc = cardTags.getResultMDC();
                ssb_mdc.append(result_mdc.toString());
                ssb_mdc.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_mdc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                //explainTextView_3
                ((TextView) ((LinearLayout) ((LinearLayout) ((CardView) theCardViewBG).getChildAt(0)).getChildAt(2)).getChildAt(3))
                        .append(ssb_mdc);

                progressBar.setVisibility(View.GONE);
                cardTags.setHasBGOperation(false);
                cardTags.setHasExplanation(true);
                asyncTaskQueue.set(cardTags.getTaskNumber(), null);

                datasets.clear();
                bgfatores.clear();
            }
        }
    }
}