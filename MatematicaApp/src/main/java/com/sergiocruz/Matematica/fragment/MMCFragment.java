package com.sergiocruz.Matematica.fragment;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.sergiocruz.Matematica.activity.AboutActivity;
import com.sergiocruz.Matematica.activity.SettingsActivity;
import com.sergiocruz.Matematica.helper.MenuHelper;
import com.sergiocruz.Matematica.helper.SwipeToDismissTouchListener;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
import static android.widget.LinearLayout.HORIZONTAL;
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

    Activity mActivity;
    ArrayList<AsyncTask> asyncTaskQueue = new ArrayList<>();
    ArrayList<Integer> fColors;
    EditText mmc_num_1, mmc_num_2, mmc_num_3, mmc_num_4, mmc_num_5, mmc_num_6, mmc_num_7, mmc_num_8;
    float scale;
    Fragment thisFragment = this;
    int height_dip, cv_width;
    int taskNumber = 0;
    long startTime;
    SharedPreferences sharedPrefs;

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
        Toast thetoast = Toast.makeText(mActivity, R.string.maiores_qzero, Toast.LENGTH_SHORT);
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

        // have a menu in this fragment
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
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();

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

        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.menu_sub_main, menu);
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
            LinearLayout history = (LinearLayout) mActivity.findViewById(R.id.history);
            String help_divisores = getString(R.string.help_text_mmc);
            SpannableStringBuilder ssb = new SpannableStringBuilder(help_divisores);
            create(history, ssb, mActivity);
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
                calc_mmc();
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

    private void calc_mmc() {
        startTime = System.nanoTime();
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
        ArrayList<Long> long_numbers = new ArrayList<Long>();

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

        //Adicionar os números a fatorizar na tag do cardview
        MyTags tags = new MyTags(cardview, long_numbers, result_mmc, false, false, "", null, taskNumber);
        cardview.setTag(tags);

        LinearLayout history = (LinearLayout) mActivity.findViewById(R.id.history);
        // Add cardview to history layout at the top (index 0)
        history.addView(cardview, 0);

        LinearLayout ll_vertical_root = new LinearLayout(mActivity);
        ll_vertical_root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_vertical_root.setOrientation(LinearLayout.VERTICAL);

        // criar novo Textview
        final TextView textView = new TextView(mActivity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura

        //Adicionar o texto com o resultado
        textView.setText(mmc_string);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTag("texto");

        // add the textview to the cardview
        ll_vertical_root.addView(textView);

        String shouldShowExplanation = sharedPrefs.getString("pref_show_explanation", "0");
        // -1 = sempre  0 = quando pedidas   1 = nunca
        if (shouldShowExplanation.equals("-1") || shouldShowExplanation.equals("0")) {
            createExplanations(cardview, ll_vertical_root, shouldShowExplanation);
        } else {
            Boolean shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false);
            if (shouldShowPerformance) {
                TextView gradient_separator = getGradientSeparator();
                NumberFormat decimalFormatter = new DecimalFormat("#.###");
                String elapsed = "Performance:" + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s";
                gradient_separator.setText(elapsed);
                ll_vertical_root.addView(gradient_separator, 0);
            }
            cardview.addView(ll_vertical_root);
        }

    }

    @NonNull
    private TextView getGradientSeparator() {
        //View separator with gradient
        TextView gradient_separator = new TextView(mActivity);
        gradient_separator.setTag("gradient_separator");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gradient_separator.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bottom_border2));
        } else {
            gradient_separator.setBackgroundDrawable(ContextCompat.getDrawable(mActivity, R.drawable.bottom_border2));
        }
        gradient_separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
        gradient_separator.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        gradient_separator.setTextColor(ContextCompat.getColor(mActivity, R.color.lightBlue));
        gradient_separator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        return gradient_separator;
    }


    private void createExplanations(CardView cardview, LinearLayout ll_vertical_root, String shouldShowExplanation) {

        final SpannableStringBuilder ssb_hide_expl = new SpannableStringBuilder(getString(R.string.hide_explain));
        ssb_hide_expl.setSpan(new UnderlineSpan(), 0, ssb_hide_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);
        final SpannableStringBuilder ssb_show_expl = new SpannableStringBuilder(getString(R.string.explain));
        ssb_show_expl.setSpan(new UnderlineSpan(), 0, ssb_show_expl.length() - 2, SPAN_EXCLUSIVE_EXCLUSIVE);

        //Linearlayout horizontal com o explainlink e gradiente
        LinearLayout ll_horizontal = new LinearLayout(mActivity);
        ll_horizontal.setOrientation(HORIZONTAL);
        ll_horizontal.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        final TextView explainLink = new TextView(mActivity);
        explainLink.setTag("explainLink");
        explainLink.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
        explainLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainLink.setTextColor(ContextCompat.getColor(mActivity, R.color.linkBlue));
        explainLink.setGravity(Gravity.CENTER_VERTICAL);

        //View separator with gradient
        TextView gradient_separator = getGradientSeparator();

        ll_horizontal.setGravity(Gravity.CENTER_VERTICAL);

        final Boolean[] isExpanded = {false};
        explainLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isExpanded[0]) {
                    ((TextView) view).setText(ssb_hide_expl);
                    view.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,   //largura
                            LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
                    ((LinearLayout) view.getParent().getParent()).findViewWithTag("ll_vertical_expl").setVisibility(View.VISIBLE);
                    isExpanded[0] = true;

                } else if (isExpanded[0]) {
                    ((TextView) view).setText(ssb_show_expl);
                    view.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,   //largura
                            LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
                    ((LinearLayout) view.getParent().getParent()).findViewWithTag("ll_vertical_expl").setVisibility(View.GONE);
                    isExpanded[0] = false;
                }
            }
        });

        ll_horizontal.addView(explainLink);
        ll_horizontal.addView(gradient_separator);

        //LL vertical das explicações
        LinearLayout ll_vertical_expl = new LinearLayout(mActivity);
        ll_vertical_expl.setTag("ll_vertical_expl");
        ll_vertical_expl.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_vertical_expl.setOrientation(LinearLayout.VERTICAL);

        //ProgressBar
        cv_width = mActivity.findViewById(R.id.card_view_1).getWidth();
        height_dip = (int) (3 * scale + 0.5f);
        View progressBar = new View(mActivity);
        progressBar.setTag("progressBar");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, height_dip); //Largura, Altura
        progressBar.setLayoutParams(layoutParams);

        //Ponto 1
        TextView explainTextView_1 = new TextView(mActivity);
        explainTextView_1.setTag("explainTextView_1");
        String fp = "fatores primos:";
        String explain_text_1 = "▻Decompor os números em " + fp + "\n";
        SpannableStringBuilder ssb_explain_1 = new SpannableStringBuilder(explain_text_1);
        ssb_explain_1.setSpan(new UnderlineSpan(), explain_text_1.length() - fp.length() - 1, explain_text_1.length() - 1, SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_1.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.boldColor)), 0, ssb_explain_1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_1.setText(ssb_explain_1);

        //Ponto 2
        TextView explainTextView_2 = new TextView(mActivity);
        explainTextView_2.setTag("explainTextView_2");
        String comuns = "comuns";
        String ncomuns = "não comuns";
        String uma_vez = "apenas uma vez";
        String maior_exps = "maiores expoentes";
        String explain_text_2 = "▻Escolher os fatores" + " " + comuns + " " + "e" + " " + ncomuns + ", " + uma_vez + ", " + "com os" + " " + maior_exps + ":\n";
        SpannableStringBuilder ssb_explain_2 = new SpannableStringBuilder(explain_text_2);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(comuns), explain_text_2.indexOf(comuns) + comuns.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(ncomuns), explain_text_2.indexOf(ncomuns) + ncomuns.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(uma_vez), explain_text_2.indexOf(uma_vez) + uma_vez.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new UnderlineSpan(), explain_text_2.indexOf(maior_exps), explain_text_2.indexOf(maior_exps) + maior_exps.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.boldColor)), 0, ssb_explain_2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_2.setText(ssb_explain_2);

        //Ponto 3
        TextView explainTextView_3 = new TextView(mActivity);
        explainTextView_3.setTag("explainTextView_3");
        String multipl = "▻Multiplicar";
        String explain_text_3 = multipl + " " +
                "os fatores para obter o Mínimo Multiplo Comum:" + "\n";
        SpannableStringBuilder ssb_explain_3 = new SpannableStringBuilder(explain_text_3);
        ssb_explain_3.setSpan(new UnderlineSpan(), explain_text_3.indexOf(multipl) + 1, explain_text_3.indexOf(multipl) + multipl.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_3.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb_explain_3.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb_explain_3.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.boldColor)), 0, ssb_explain_3.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        explainTextView_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        explainTextView_3.setText(ssb_explain_3);

        ll_vertical_expl.addView(explainTextView_1);
        ll_vertical_expl.addView(explainTextView_2);
        ll_vertical_expl.addView(explainTextView_3);
        ll_vertical_root.addView(ll_horizontal);
        ll_vertical_root.addView(progressBar);
        ll_vertical_root.addView(ll_vertical_expl);

        if (shouldShowExplanation.equals("-1")) {  //Always show Explanation
            ll_vertical_expl.setVisibility(View.VISIBLE);
            explainLink.setText(ssb_hide_expl);
            isExpanded[0] = true;
        } else if (shouldShowExplanation.equals("0")) { // Show Explanation on demand on click
            ll_vertical_expl.setVisibility(View.GONE);
            explainLink.setText(ssb_show_expl);
            isExpanded[0] = false;
        }
        cardview.addView(ll_vertical_root);

        MyTags thisCardTags = (MyTags) cardview.getTag();
//        Boolean hasExplanation = thisCardTags.getHasExplanation();
//        Boolean hasBGOperation = thisCardTags.getHasBGOperation();

//        if (!hasBGOperation && !hasExplanation) {
        thisCardTags.setTaskNumber(taskNumber);
        AsyncTask<Void, Double, Void> BG_Operation_MMC = new BackGroundOperation_MMC(thisCardTags)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        asyncTaskQueue.add(BG_Operation_MMC);
        taskNumber++;
//        }
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
        BigInteger result_mmc;
        Boolean hasExplanation;
        Boolean hasBGOperation;
        String texto;
        ArrayList<ArrayList<Long>> bgfatores;
        int taskNumber;

        MyTags(CardView theCardView, ArrayList<Long> long_numbers, BigInteger result_mmc,
               Boolean hasExplanation, Boolean hasBGOperation, String texto, ArrayList<ArrayList<Long>> bgfatores, int taskNumber) {
            this.theCardView = theCardView;
            this.long_numbers = long_numbers;
            this.result_mmc = result_mmc;
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

        BigInteger getResultMMC() {
            return result_mmc;
        }

        void setResultMMC(BigInteger result_mmc) {
            this.result_mmc = result_mmc;
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
    public class BackGroundOperation_MMC extends AsyncTask<Void, Double, Void> {
        MyTags cardTags;
        CardView theCardViewBG;
        ArrayList<Long> mmc_numbers;
        BigInteger result_mmc;
        ArrayList<ArrayList<Long>> bgfatores;

        TextView gradient_separator;
        View progressBar;
        NumberFormat percent_formatter;

        BackGroundOperation_MMC(MyTags cardTags) {
            this.cardTags = cardTags;
        }


        @Override
        public void onPreExecute() {
            percent_formatter = new DecimalFormat("#.###%");
            theCardViewBG = cardTags.getCardView();
            progressBar = theCardViewBG.findViewWithTag("progressBar");
            progressBar.setVisibility(View.VISIBLE);
            gradient_separator = (TextView) theCardViewBG.findViewWithTag("gradient_separator");
            cardTags.setHasBGOperation(true);

            Boolean shouldShowColors = sharedPrefs.getBoolean("pref_show_colors", true);
            int[] f_colors = mActivity.getResources().getIntArray(R.array.f_colors_xml);
            int f_colors_length = f_colors.length;
            fColors = new ArrayList<>();
            if (shouldShowColors) {
                for (int i = 0; i < f_colors_length; i++) fColors.add(f_colors[i]);
                Collections.shuffle(fColors); //randomizar as cores
            } else {
                for (int i = 0; i < f_colors_length; i++) fColors.add(f_colors[f_colors_length - 1]);
            }

            String text = " " + "A fatorizar... 0%";
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            ssb.setSpan(new ForegroundColorSpan(fColors.get(0)), 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            gradient_separator.setText(ssb);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<ArrayList<Long>> fatores = new ArrayList<>();
            mmc_numbers = cardTags.getLongNumbers();

            int numbersSize = mmc_numbers.size();
            for (int i = 0; i < numbersSize; i++) { // fatorizar todos os números inseridos em MMC
                double oldProgress = 0;
                double progress;
                ArrayList<Long> fatores_ix = new ArrayList<>();

                Long number_i = mmc_numbers.get(i);
                if (number_i == 1L) {
                    fatores_ix.add(1L);
                }
                while (number_i % 2L == 0) {
                    fatores_ix.add(2L);
                    number_i /= 2L;
                }

                for (long j = 3; j <= number_i / j; j += 2) {
                    if (isCancelled()) break;
                    while (number_i % j == 0) {
                        fatores_ix.add(j);
                        number_i /= j;
                    }
                    progress = (double) j / ((double) number_i / (double) j);
                    if (progress - oldProgress > 0.1d) {
                        publishProgress(progress, (double) i);
                        oldProgress = progress;
                    }
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
        public void onProgressUpdate(Double... values) {

            if (thisFragment != null && thisFragment.isVisible()) {
                Integer color = fColors.get((int) Math.round(values[1]));
                progressBar.setBackgroundColor(color);
                double value0 = values[0];
                if (value0 > 1f) value0 = 1f;
                progressBar.setLayoutParams(
                        new LinearLayout.LayoutParams((int) Math.round(value0 * cv_width), height_dip));
                String text = " " + "A fatorizar..." + " " + percent_formatter.format(value0);
                SpannableStringBuilder ssb = new SpannableStringBuilder(text);
                ssb.setSpan(new ForegroundColorSpan(color), 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                gradient_separator.setText(ssb);
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

                    String str_fatores = mmc_numbers.get(k) + "=";
                    SpannableStringBuilder ssb_fatores;
                    ssb_fatores = new SpannableStringBuilder(str_fatores);
                    ssb_fatores.setSpan(new ForegroundColorSpan(fColors.get(k)), 0, ssb_fatores.length(), SPAN_EXCLUSIVE_INCLUSIVE);

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
                    ((TextView) theCardViewBG.findViewWithTag("explainTextView_1")).append(ssb_fatores);

                }

                ArrayList<Long> maiores_bases = new ArrayList<>();
                ArrayList<Long> maiores_exps = new ArrayList<>();
                ArrayList<Long> colors = new ArrayList<>();

                for (int i = 0; i < datasets.size(); i += 2) {
                    ArrayList<Long> bases = datasets.get(i);
                    ArrayList<Long> exps = datasets.get(i + 1);

                    for (int cb = 0; cb < bases.size(); cb++) {
                        Long current_base = bases.get(cb);
                        Long current_exp = exps.get(cb);

                        if (!maiores_bases.contains(current_base)) {
                            maiores_bases.add(current_base);
                            maiores_exps.add(current_exp);
                            colors.add((long) i / 2);
                        }

                        if (maiores_bases.contains(current_base) && current_exp > maiores_exps.get(maiores_bases.indexOf(current_base))) {
                            maiores_exps.set(maiores_bases.indexOf(current_base), current_exp);
                            colors.set(maiores_bases.indexOf(current_base), (long) (i / 2));
                        }

                        for (int j = i + 2; j < datasets.size(); j += 2) {
                            ArrayList<Long> next_bases = datasets.get(j);
                            ArrayList<Long> next_exps = datasets.get(j + 1);

                            for (int nb = 0; nb < next_bases.size(); nb++) {
                                Long next_base = next_bases.get(nb);
                                Long next_exp = next_exps.get(nb);

                                if (next_base == current_base && next_exp > maiores_exps.get(maiores_bases.indexOf(current_base)) && maiores_bases.contains(next_base)) {
                                    maiores_exps.set(maiores_bases.indexOf(next_base), next_exp);
                                    colors.set(maiores_bases.indexOf(current_base), (long) (j / 2));
                                }

                            }
                        }
                    }
                }


                SpannableStringBuilder ssb_mmc = new SpannableStringBuilder();

                //Criar os expoentes do MMC com os maiores fatores com cores e a negrito
                for (int i = 0; i < maiores_bases.size(); i++) {
                    int base_length = maiores_bases.get(i).toString().length();

                    if (maiores_exps.get(i) == 1L) {
                        //Expoente 1
                        ssb_mmc.append(maiores_bases.get(i).toString());
                        ssb_mmc.setSpan(new ForegroundColorSpan(fColors.get(colors.get(i).intValue())),
                                ssb_mmc.length() - base_length, ssb_mmc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else if (maiores_exps.get(i) > 1L) {
                        //Expoente superior a 1
                        int exp_length = maiores_exps.get(i).toString().length();
                        ssb_mmc.append(maiores_bases.get(i).toString() + maiores_exps.get(i).toString());
                        ssb_mmc.setSpan(new SuperscriptSpan(), ssb_mmc.length() - exp_length, ssb_mmc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mmc.setSpan(new RelativeSizeSpan(0.8f), ssb_mmc.length() - exp_length, ssb_mmc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb_mmc.setSpan(new ForegroundColorSpan(fColors.get(colors.get(i).intValue())),
                                ssb_mmc.length() - exp_length - base_length, ssb_mmc.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    ssb_mmc.append("×");
                }
                ssb_mmc.replace(ssb_mmc.length() - 1, ssb_mmc.length(), "");

                //explainTextView_2
                ((TextView) theCardViewBG.findViewWithTag("explainTextView_2")).append(ssb_mmc);


                ssb_mmc.delete(0, ssb_mmc.length());
                result_mmc = cardTags.getResultMMC();
                ssb_mmc.append(result_mmc.toString());

                //explainTextView_3
                ((TextView) theCardViewBG.findViewWithTag("explainTextView_3")).append(ssb_mmc);

                progressBar.setVisibility(View.GONE);

                Boolean shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false);
                if (shouldShowPerformance) {
                    NumberFormat decimalFormatter = new DecimalFormat("#.###");
                    String elapsed = "Performance:" + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s";
                    gradient_separator.setText(elapsed);
                } else {
                    gradient_separator.setText("");
                }

                cardTags.setHasBGOperation(false);
                cardTags.setHasExplanation(true);
                asyncTaskQueue.set(cardTags.getTaskNumber(), null);

                datasets.clear();
            }

        }
    }
}
