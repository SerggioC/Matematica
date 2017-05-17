package com.sergiocruz.MatematicaPro.fragment;

/**
 * Created by Sergio on 13/05/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.MatematicaPro.R;
import com.sergiocruz.MatematicaPro.activity.AboutActivity;
import com.sergiocruz.MatematicaPro.activity.SettingsActivity;
import com.sergiocruz.MatematicaPro.helper.CreateCardView;
import com.sergiocruz.MatematicaPro.helper.MenuHelper;
import com.sergiocruz.MatematicaPro.helper.SwipeToDismissTouchListener;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Long.parseLong;

/*****
 * Project MatematicaFree
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 13/05/2017 14:00
 ******/

public class MultiplosFragment extends Fragment {

    public AsyncTask<Long, Double, String> BG_Operation = new BackGroundOperation(null, null);
    Fragment thisFragment = this;
    Button button;
    long num;
    Activity mActivity;
    SharedPreferences sharedPrefs;
    long startTime;
    float scale;
    View rootView;
    EditText num_1;

    public MultiplosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mActivity = getActivity();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        scale = mActivity.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.menu_sub_main, menu);
        inflater.inflate(R.menu.menu_help_multiplos, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save_history_images) {
            MenuHelper.save_history_images(mActivity);
        }
        if (id == R.id.action_share_history) {
            MenuHelper.share_history(mActivity);
        }
        if (id == R.id.action_share_history_images) {
            MenuHelper.share_history_images(mActivity);
        }
        if (id == R.id.action_clear_all_history) {
            MenuHelper.remove_history(mActivity);
        }
        if (id == R.id.action_help_multiplos) {
            String help_multiplos = getString(R.string.help_text_multiplos);
            SpannableStringBuilder ssb = new SpannableStringBuilder(help_multiplos);
            LinearLayout history = (LinearLayout) mActivity.findViewById(R.id.history);
            CreateCardView.create(history, ssb, mActivity);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("Sergio>>>", "hideKeyboard error: ", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_multiplos, container, false);
        num_1 = (EditText) rootView.findViewById(R.id.editNumMultiplos);

        button = (Button) rootView.findViewById(R.id.button_calc_multiplos);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcMultiplos();
            }
        });

        Button clearTextBtn = (Button) rootView.findViewById(R.id.btn_clear);
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
                    Toast thetoast = Toast.makeText(mActivity, R.string.numero_alto, Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        num_1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    calcMultiplos();
                }
                return true;
            }
        });

        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BG_Operation.getStatus() == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true);
            Toast thetoast = Toast.makeText(mActivity, R.string.canceled_op, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

    public void calcMultiplos() {
        startTime = System.nanoTime();
        hideKeyboard();
        String editnumText = num_1.getText().toString();
        if (editnumText.equals(null) || editnumText.equals("") || editnumText == null) {
            Toast thetoast = Toast.makeText(mActivity, R.string.add_num_inteiro, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }
        if (editnumText.equals("0")) {
            createCardView(0L, "{0}", 0L, false);
            return;
        }
        try {
            // Tentar converter o string para long
            num = Long.parseLong(editnumText);
        } catch (Exception e) {
            Toast thetoast = Toast.makeText(mActivity, R.string.numero_alto, Toast.LENGTH_LONG);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
            return;
        }

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_multiplos);
        Long spinner_max_multiplos = Long.parseLong(spinner.getSelectedItem().toString());
        BG_Operation = new BackGroundOperation(false, null).execute(num, 0L, spinner_max_multiplos);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


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

    public void createCardView(Long number, String multiplos, Long min_multiplos, Boolean showMore) {
        //criar novo cardview
        final CardView cardview = new CardView(mActivity);
        cardview.setTag(min_multiplos);

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

        int cv_color = ContextCompat.getColor(mActivity, R.color.cardsColor);
        cardview.setCardBackgroundColor(cv_color);

        // Add cardview to history layout at the top (index 0)
        final LinearLayout history = (LinearLayout) mActivity.findViewById(R.id.history);
        history.addView(cardview, 0);

        // criar novo Textview
        TextView textView = new TextView(mActivity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura

        String text = "Múltiplos de " + number + "=\n" + multiplos;
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        //Adicionar o texto com o resultado
        textView.setText(ssb);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTag(R.id.texto, "texto");

        LinearLayout ll_vertical_root = new LinearLayout(mActivity);
        ll_vertical_root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll_vertical_root.setOrientation(LinearLayout.VERTICAL);

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
                        history.removeView(cardview);
                    }
                }));

        Boolean shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", true);
        if (shouldShowPerformance) {
            TextView gradient_separator = getGradientSeparator();
            NumberFormat decimalFormatter = new DecimalFormat("#.###");
            String elapsed = getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s";
            gradient_separator.setText(elapsed);
            ll_vertical_root.addView(gradient_separator);
        }

        ll_vertical_root.addView(textView);

        if (showMore) {
            // criar novo Textview com link para mostrar mais números múltiplos
            TextView showmore = new TextView(mActivity);
            showmore.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                    LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
            showmore.setGravity(Gravity.RIGHT);
            showmore.setText(R.string.show_more);
            showmore.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            showmore.setTypeface(null, Typeface.BOLD);
            showmore.setTextColor(ContextCompat.getColor(mActivity, R.color.bgCardColor));
            showmore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            showmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTime = System.nanoTime();
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_multiplos);
                    Long spinner_max_multiplos = Long.parseLong(spinner.getSelectedItem().toString());
                    BG_Operation = new BackGroundOperation(true, cardview).execute(num, (Long) cardview.getTag(), spinner_max_multiplos);
                }
            });

            ll_vertical_root.addView(showmore);
        }

        // add the root layout to the cardview
        cardview.addView(ll_vertical_root);
    }

    public class BackGroundOperation extends AsyncTask<Long, Double, String> {
        Long number;
        Long max_value;
        Boolean expandResult;
        View theCardView;

        BackGroundOperation(Boolean expandResult, View theCardView) {
            this.expandResult = expandResult;
            this.theCardView = theCardView;
        }

        @Override
        public void onPreExecute() {
            if (!expandResult) {
                button.setClickable(false);
                button.setText(R.string.working);
                hideKeyboard();
            }
        }

        @Override
        protected String doInBackground(Long... num) {
            number = num[0];
            Long min_value = num[1];
            max_value = num[2] + num[1];

            String string_multiplos = "";

            for (long i = min_value; i < max_value; i++) {
                BigInteger bigNumber = BigInteger.valueOf(number).multiply(BigInteger.valueOf(i));
                string_multiplos += bigNumber + ", ";
            }

            string_multiplos += "...}";
            return string_multiplos;
        }

        @Override
        protected void onPostExecute(String result) {
            if (thisFragment != null && thisFragment.isVisible()) {
                if (!expandResult) {
                    createCardView(number, "{" + result, max_value, true);
                    button.setText(R.string.calculate);
                    button.setClickable(true);
                } else {
                    theCardView.setTag(max_value);
                    TextView textView_preResult;
                    Boolean shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", true);
                    if (shouldShowPerformance) {
                        textView_preResult = ((TextView) ((LinearLayout) ((CardView) theCardView).getChildAt(0)).getChildAt(1));
                        TextView gradient_separator = ((TextView) ((LinearLayout) ((CardView) theCardView).getChildAt(0)).getChildAt(0));
                        NumberFormat decimalFormatter = new DecimalFormat("#.###");
                        String elapsed = getString(R.string.performance) + " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s";
                        gradient_separator.setText(elapsed);
                    } else {
                        textView_preResult = ((TextView) ((LinearLayout) ((CardView) theCardView).getChildAt(0)).getChildAt(0));
                    }
                    String preResult = textView_preResult.getText().toString();
                    preResult = preResult.substring(0, preResult.length() - 4) + result;
                    textView_preResult.setText(preResult);
                }
            }
        }

    }

}
