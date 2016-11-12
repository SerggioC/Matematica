package com.sergiocruz.Matematica.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.helper.CreateCardView;
import com.sergiocruz.Matematica.helper.MenuHelper;

import java.util.ArrayList;

import static com.sergiocruz.Matematica.R.id.mdc_num_1;
import static com.sergiocruz.Matematica.R.id.mdc_num_2;
import static java.lang.Integer.parseInt;
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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MDCFragment() {
        // Required empty public constructor
    }

    public void showToast() {
        Toast thetoast = Toast.makeText(getActivity(), "Número demasiado grande", Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER,0,0);
        thetoast.show();
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
    private static long mmc(long a, long b) {
        return (b / mdc(a, b)) * a;
    }

    private static long mmc(ArrayList<Long> input) {
        long result = input.get(0);
        for (int i = 1; i < input.size(); i++)
            result = mmc(result, input.get(i));
        return result;
    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v2
     *****************************************************************/
    private static long mdc(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static long mdc(ArrayList<Long> input) {
        long result = input.get(0);
        for (int i = 1; i < input.size(); i++)
            result = mdc(result, input.get(i));
        return result;
    }

    /****************************************************************
     * MDC: Máximo divisor comum (gcd: Greatest Common Divisor) v1
     *****************************************************************/
    private final static Long mdc2(Long a, Long b) {
        return b == 0 ? a : mdc(b, a % b);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_history, menu);
        inflater.inflate(R.menu.menu_help_mdc, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mdc, container, false);

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
                EditText ed1 = (EditText) getActivity().findViewById(R.id.mdc_num_1);
                ed1.setText("");
            }
        });
        Button clearTextBtn_2 = (Button) view.findViewById(R.id.btn_clear_2);
        clearTextBtn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed2 = (EditText) getActivity().findViewById(R.id.mdc_num_2);
                ed2.setText("");
            }
        });
        Button clearTextBtn_3 = (Button) view.findViewById(R.id.btn_clear_3);
        clearTextBtn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed3 = (EditText) getActivity().findViewById(R.id.mdc_num_3);
                ed3.setText("");
            }
        });
        Button clearTextBtn_4 = (Button) view.findViewById(R.id.btn_clear_4);
        clearTextBtn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed4 = (EditText) getActivity().findViewById(R.id.mdc_num_4);
                ed4.setText("");
            }
        });
        Button clearTextBtn_5 = (Button) view.findViewById(R.id.btn_clear_5);
        clearTextBtn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed5 = (EditText) getActivity().findViewById(R.id.mdc_num_5);
                ed5.setText("");
            }
        });
        Button clearTextBtn_6 = (Button) view.findViewById(R.id.btn_clear_6);
        clearTextBtn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed6 = (EditText) getActivity().findViewById(R.id.mdc_num_6);
                ed6.setText("");
            }
        });
        Button clearTextBtn_7 = (Button) view.findViewById(R.id.btn_clear_7);
        clearTextBtn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed7 = (EditText) getActivity().findViewById(R.id.mdc_num_7);
                ed7.setText("");
            }
        });
        Button clearTextBtn_8 = (Button) view.findViewById(R.id.btn_clear_8);
        clearTextBtn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed8 = (EditText) getActivity().findViewById(R.id.mdc_num_8);
                ed8.setText("");
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


        final EditText mdc_num_1 = (EditText) view.findViewById(R.id.mdc_num_1);
        mdc_num_1.addTextChangedListener(new TextWatcher() {
            Integer num1;
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
                    num1 = parseInt(s.toString());
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

        final EditText mdc_num_2 = (EditText) view.findViewById(R.id.mdc_num_2);
        mdc_num_2.addTextChangedListener(new TextWatcher() {
            Integer num2;
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
                    num2 = parseInt(s.toString());
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

        final EditText mdc_num_3 = (EditText) view.findViewById(R.id.mdc_num_3);
        mdc_num_3.addTextChangedListener(new TextWatcher() {
            Integer num3;
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
                    num3 = parseInt(s.toString());

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

        final EditText mdc_num_4 = (EditText) view.findViewById(R.id.mdc_num_4);
        mdc_num_4.addTextChangedListener(new TextWatcher() {
            Integer num4;
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
                    num4 = parseInt(s.toString());

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

        final EditText mdc_num_5 = (EditText) view.findViewById(R.id.mdc_num_5);
        mdc_num_5.addTextChangedListener(new TextWatcher() {
            Integer num5;
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
                    num5 = parseInt(s.toString());

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

        final EditText mdc_num_6 = (EditText) view.findViewById(R.id.mdc_num_6);
        mdc_num_6.addTextChangedListener(new TextWatcher() {
            Integer num6;
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
                    num6 = parseInt(s.toString());
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

        final EditText mdc_num_7 = (EditText) view.findViewById(R.id.mdc_num_7);
        mdc_num_7.addTextChangedListener(new TextWatcher() {
            Integer num7;
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
                    num7 = parseInt(s.toString());
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

        final EditText mdc_num_8 = (EditText) view.findViewById(R.id.mdc_num_8);
        mdc_num_8.addTextChangedListener(new TextWatcher() {
            Integer num8;
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
                    num8 = parseInt(s.toString());
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
        EditText edittext3 = (EditText) view.findViewById(R.id.mdc_num_3);
        EditText edittext4 = (EditText) view.findViewById(R.id.mdc_num_4);
        EditText edittext5 = (EditText) view.findViewById(R.id.mdc_num_5);
        EditText edittext6 = (EditText) view.findViewById(R.id.mdc_num_6);
        EditText edittext7 = (EditText) view.findViewById(R.id.mdc_num_7);
        EditText edittext8 = (EditText) view.findViewById(R.id.mdc_num_8);

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
                edittext8.setText("");
                f_8.setVisibility(View.GONE);
                add_one.setVisibility(View.VISIBLE);
                return;
            }
            if (f7_visible) {
                edittext7.setText("");
                f_7.setVisibility(View.GONE);
                ll_78.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_56_visibe) {
            if (f6_visible) {
                edittext6.setText("");
                f_6.setVisibility(View.GONE);
                return;
            }
            if (f5_visible) {
                edittext5.setText("");
                f_5.setVisibility(View.GONE);
                ll_56.setVisibility(View.GONE);
                return;
            }
        }

        if (ll_34_visibe) {
            if (f4_visible) {
                edittext4.setText("");
                f_4.setVisibility(View.GONE);
                return;
            }
            if (f3_visible) {
                edittext3.setText("");
                f_3.setVisibility(View.GONE);
                ll_34.setVisibility(View.GONE);
                less_one.setVisibility(View.INVISIBLE);
                return;
            }
        }

    }

    private void calc_mdc(View view) {

        EditText edittext1 = (EditText) view.findViewById(mdc_num_1);
        String str_num1 = edittext1.getText().toString();
        EditText edittext2 = (EditText) view.findViewById(mdc_num_2);
        String str_num2 = edittext2.getText().toString();
        EditText edittext3 = (EditText) view.findViewById(R.id.mdc_num_3);
        String str_num3 = edittext3.getText().toString();
        EditText edittext4 = (EditText) view.findViewById(R.id.mdc_num_4);
        String str_num4 = edittext4.getText().toString();
        EditText edittext5 = (EditText) view.findViewById(R.id.mdc_num_5);
        String str_num5 = edittext5.getText().toString();
        EditText edittext6 = (EditText) view.findViewById(R.id.mdc_num_6);
        String str_num6 = edittext6.getText().toString();
        EditText edittext7 = (EditText) view.findViewById(R.id.mdc_num_7);
        String str_num7 = edittext7.getText().toString();
        EditText edittext8 = (EditText) view.findViewById(R.id.mdc_num_8);
        String str_num8 = edittext8.getText().toString();

        long num1, num2, num3, num4, num5, num6, num7, num8;

        ArrayList<Long> numbers = new ArrayList<Long>();

        if (!str_num1.equals("")) {
            try {
                // Tentar converter o string para long
                num1 = parseLong(str_num1);
                if (num1 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num1 > 0L) {
                    numbers.add(num1);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 1" + str_num1 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!str_num2.equals("")) {
            try {
                // Tentar converter o string para long
                num2 = parseLong(str_num2);
                if (num2 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num2 > 0L) {
                    numbers.add(num2);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 2" + str_num2 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!str_num3.equals("")) {
            try {
                // Tentar converter o string para long
                num3 = parseLong(str_num3);
                if (num3 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num3 > 0L) {
                    numbers.add(num3);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 3" + str_num3 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!str_num4.equals("")) {
            try {
                // Tentar converter o string para long
                num4 = parseLong(str_num4);
                if (num4 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num4 > 0L) {
                    numbers.add(num4);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 4" + str_num4 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!str_num5.equals("")) {
            try {
                // Tentar converter o string para long
                num5 = parseLong(str_num5);
                if (num5 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num5 > 0L) {
                    numbers.add(num5);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 5" + str_num5 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!str_num6.equals("")) {
            try {
                // Tentar converter o string para long
                num6 = parseLong(str_num6);
                if (num6 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num6 > 0L) {
                    numbers.add(num6);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 6" + str_num6 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!str_num7.equals("")) {
            try {
                // Tentar converter o string para long
                num7 = parseLong(str_num7);
                if (num7 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num7 > 0L) {
                    numbers.add(num7);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 7" + str_num7 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!str_num8.equals("")) {
            try {
                // Tentar converter o string para long
                num8 = parseLong(str_num8);
                if (num8 == 0L) {
                    Toast.makeText(getActivity(), "Números maiores que zero.", Toast.LENGTH_LONG).show();
                    return;
                } else if (num8 > 0L) {
                    numbers.add(num8);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Número 8" + str_num8 + " demasiado grande.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (numbers.size() < 2) {
            Toast.makeText(getActivity(), "Introduzir pelo menos um par de números inteiros.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mdc_string = "mdc(";
        Long result_mdc = null;

        if (numbers.size() > 1) {
            for (int i = 0; i < numbers.size() - 1; i++) {
                mdc_string += numbers.get(i) + ", ";
            }
            mdc_string += numbers.get(numbers.size() - 1) + ")= ";
            result_mdc = mdc(numbers);
        }

        mdc_string += result_mdc.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(mdc_string);
        ViewGroup history = (ViewGroup) view.findViewById(R.id.history);
        CreateCardView.create(history, ssb, getActivity());



/*         if (num == 0L || num == 1L) {
            Toast.makeText(getActivity(), "O número " + num + " não tem fatores primos!", Toast.LENGTH_LONG).show();
            return;
        }

        try {

            // Lista dos fatores primos
            ArrayList<Long> fatoresPrimos = getFatoresPrimos(num);

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
            ViewGroup history = (ViewGroup) view.findViewById(R.id.history_mmc);

            //Criar o cardview com os resultados
            CreateCardView.create(history, ssb, getActivity());


        } catch (ArrayIndexOutOfBoundsException exception) {
            Toast.makeText(getActivity(), "Erro: " + exception, Toast.LENGTH_LONG).show();
        }*/


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

        return super.onOptionsItemSelected(item);
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
