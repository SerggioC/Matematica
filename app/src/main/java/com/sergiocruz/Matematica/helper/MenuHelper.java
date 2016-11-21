package com.sergiocruz.Matematica.helper;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;

import java.util.ArrayList;

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
            final Object tagObj = child.getTag();
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
        Toast thetoast = Toast.makeText(activity, "Histórico de resultados apagado", Toast.LENGTH_SHORT);
        thetoast.setGravity(Gravity.CENTER,0,0);
        thetoast.show();
    }

    public static void share_history(Activity activity) {

        ViewGroup history_view = (ViewGroup) activity.findViewById(R.id.history);
        ArrayList<View> textViews_withTAG = getViewsByTag(history_view, "texto");

        if (textViews_withTAG.size() > 0) {
            String text_fromTextViews = "";
            for (int i = 0; i < textViews_withTAG.size(); i++) {
                text_fromTextViews += (((TextView) textViews_withTAG.get(i)).getText().toString()) + "\n";
            }
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Matemática\n" + text_fromTextViews);
            sendIntent.setType("text/plain");
            activity.startActivity(sendIntent);
        } else {
            Toast thetoast = Toast.makeText(activity, "Sem resultados para partilhar", Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER,0,0);
            thetoast.show();
        }
    }






}
