package com.sergiocruz.Matematica.helper;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.SuperscriptSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;

import java.util.ArrayList;

import static com.sergiocruz.Matematica.R.id.history;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 09/11/2016 21:11
 ******/

public class MenuHelper {

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }
            final Object tagObj = child.getTag(R.id.texto);
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

    public static void remove_history(Activity activity) {
        ViewGroup history = (ViewGroup) activity.findViewById(R.id.history);
        if ((history).getChildCount() > 0)
            (history).removeAllViews();
        Toast thetoast = Toast.makeText(activity, R.string.history_deleted, Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER, 0, 0);
        thetoast.show();
    }

    public static void share_history(Activity activity) {
        ViewGroup history_view = (ViewGroup) activity.findViewById(history);
        ArrayList<View> textViews_withTAG_texto = getViewsByTag(history_view, "texto");
        if (textViews_withTAG_texto.size() > 0) {
            String text_fromTextViews_final = "";
            for (int i = 0; i < textViews_withTAG_texto.size(); i++) {
                String text_fromTextView = ((TextView) textViews_withTAG_texto.get(i)).getText().toString() + "\n";
                SpannableString ss = new SpannableString(((TextView) textViews_withTAG_texto.get(i)).getText());
                SuperscriptSpan[] spans = ss.getSpans(0, ((TextView) textViews_withTAG_texto.get(i)).getText().length(), SuperscriptSpan.class);
                int corr = 0;
                for (SuperscriptSpan span : spans) {
                    int start = ss.getSpanStart(span) + corr;
                    text_fromTextView = text_fromTextView.substring(0, start) + "^" + text_fromTextView.substring(start);
                    corr++;
                }
                text_fromTextViews_final += text_fromTextView;
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.app_long_description) +
                    activity.getResources().getString(R.string.app_version_name) + "\n" + text_fromTextViews_final);
            sendIntent.setType("text/plain");
            activity.startActivity(sendIntent);
        } else {
            Toast thetoast = Toast.makeText(activity, R.string.nothing_toshare, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }


}
