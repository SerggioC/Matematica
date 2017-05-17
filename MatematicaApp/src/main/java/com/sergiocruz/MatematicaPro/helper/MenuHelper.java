package com.sergiocruz.MatematicaPro.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.MatematicaPro.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.sergiocruz.MatematicaPro.R.id.history;
import static com.sergiocruz.MatematicaPro.helper.SwipeToDismissTouchListener.verifyStoragePermissions;

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
    public static final String IMAGES_FOLDER = "Matematica Images";

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
                if (textViews_withTAG_texto.get(i) instanceof TextView) {
                    String text_fromTextView = ((TextView) textViews_withTAG_texto.get(i)).getText().toString() + "\n";
                    SpannableString ss = new SpannableString(((TextView) textViews_withTAG_texto.get(i)).getText());
                    SuperscriptSpan[] spans = ss.getSpans(0, ((TextView) textViews_withTAG_texto.get(i)).getText().length(), SuperscriptSpan.class);
                    int corr = 0;
                    for (SuperscriptSpan span : spans) {
                        int start = ss.getSpanStart(span) + corr;
                        text_fromTextView = text_fromTextView.substring(0, start) + "^" + text_fromTextView.substring(start);
                        corr++;
                    }
                    text_fromTextViews_final += text_fromTextView + "\n";
                }
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.app_long_description) +
                    activity.getResources().getString(R.string.app_version_name) + "\n" + text_fromTextViews_final);
            sendIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getString(R.string.app_name)));

        } else {
            Toast thetoast = Toast.makeText(activity, R.string.nothing_toshare, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

    public static void share_history_images(Activity mActivity) {
        ViewGroup history_view = (ViewGroup) mActivity.findViewById(history);
        int childCount = history_view.getChildCount();

        Uri[] uris = new Uri[childCount];
        Log.i("Sergio>>>", "share_history_images: childcount " + childCount);
        if (childCount > 0) {

            for (int i = 0; i < childCount; i++) {
                CardView cardAtIndex = (CardView) history_view.getChildAt(i);
                cardAtIndex.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor));
                verifyStoragePermissions(mActivity);
                cardAtIndex.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(cardAtIndex.getWidth(), cardAtIndex.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                cardAtIndex.draw(canvas);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);

                File image_file = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER);
                try {
                    if (!image_file.exists()) {
                        image_file.mkdirs();
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    String pathname = Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER + File.separator + "img" + timeStamp + "_" + i + ".jpg";
                    image_file = new File(pathname);
                    image_file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(image_file);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    uris[i] = Uri.parse(pathname);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Sergio>>>", "onClick: error ", e);
                    Toast thetoast = Toast.makeText(mActivity, mActivity.getString(R.string.errorsavingimg), Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }

            }

            for (int i = 0; i < uris.length; i++) {
                Log.i("Sergio>>>", "share_history_images: uris " + uris[i]);
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            //sendIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources().getString(R.string.app_long_description) +
            //        mActivity.getResources().getString(R.string.app_version_name) + "\n");
//        sendIntent.setType("text/plain");
//        sendIntent.setType("image/jpeg");
            sendIntent.setType("*/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uris);
            mActivity.startActivity(Intent.createChooser(sendIntent, mActivity.getResources().getString(R.string.app_name)));
        } else {
            Toast thetoast = Toast.makeText(mActivity, R.string.nothing_toshare, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

    // Guardar todas as imagens
    public static void save_history_images(final Activity mActivity) {
        ViewGroup history_view = (ViewGroup) mActivity.findViewById(history);
        if (history_view.getChildCount() > 0) {
            for (int i = 0; i < history_view.getChildCount(); i++) {
                CardView cardAtIndex = (CardView) history_view.getChildAt(i);
                cardAtIndex.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor));
                verifyStoragePermissions(mActivity);
                cardAtIndex.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(cardAtIndex.getWidth(), cardAtIndex.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                cardAtIndex.draw(canvas);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);

                File image_file = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER);
                try {
                    if (!image_file.exists()) {
                        image_file.mkdirs();
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    image_file = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER + File.separator + "img" + timeStamp + "_" + i + ".jpg");
                    image_file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(image_file);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Sergio>>>", "onClick: error ", e);
                    Toast thetoast = Toast.makeText(mActivity, mActivity.getString(R.string.errorsavingimg), Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }

            }
            Snackbar snack = Snackbar.make(mActivity.findViewById(android.R.id.content), mActivity.getString(R.string.all_images_saved), Snackbar.LENGTH_LONG);
            snack.setAction("Open Folder", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath().toString() + File.separator + IMAGES_FOLDER + File.separator );
                    intent.setDataAndType(uri, "*/*");
                    try {
                        mActivity.startActivity(Intent.createChooser(intent, "Open folder with"));
                    } catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(mActivity, "Please install a file manager.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            snack.setActionTextColor(Color.RED);
            snack.show();

        } else {
            Toast thetoast = Toast.makeText(mActivity, R.string.nothing_tosave, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }


    public static void expandIt(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapseIt(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


}
