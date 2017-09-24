package com.sergiocruz.MatematicaPro.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 09/11/2016 21:11
 ******/

public class MenuHelper implements ActivityCompat.OnRequestPermissionsResultCallback{
    public static final String IMAGES_FOLDER = "Matematica Images";
    public static final int SAVED_IMAGE_QUALITY = 90;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // if we don't have permission prompt the user to give permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("Sergio>", this + "\nonRequestPermissionsResult:\nrequestCode=\n" + requestCode);
        for (int i = 0; i < permissions.length; i++) {
            Log.i("Sergio>", this + "\nonRequestPermissionsResult:\npermissions=\n" + permissions[i]);
        }
        for (int i = 0; i < grantResults.length; i++) {
            Log.i("Sergio>", this + "\nonRequestPermissionsResult:\ngrantResults=\n" + grantResults[i]);
        }
        // PackageManager.PERMISSION_GRANTED;
        // PackageManager.PERMISSION_DENIED;

    }

    @NonNull
    public static String saveViewToImage(View theViewToSave, int index, boolean drawWhiteBG) {
        String pathname;
        theViewToSave.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(theViewToSave.getWidth(), theViewToSave.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (drawWhiteBG) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.ADD);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        theViewToSave.draw(canvas);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, SAVED_IMAGE_QUALITY, byteArrayOutputStream);
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER);
        try {
            if (!folder.exists()) folder.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            pathname = Environment.getExternalStorageDirectory() + File.separator + IMAGES_FOLDER + File.separator + "img" + timeStamp + "_" + index + ".jpg";
            File image_file = new File(pathname);
            image_file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(image_file);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            pathname = null;
        }
        return pathname;
    }

    public static void openFolder_Snackbar(final Activity mActivity, String toastText) {
        Snackbar snack = Snackbar.make(mActivity.findViewById(android.R.id.content), toastText, Snackbar.LENGTH_LONG);
        snack.setAction(mActivity.getString(R.string.open_folder), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath().toString() + File.separator + IMAGES_FOLDER + File.separator);
                intent.setDataAndType(uri, "*/*");
                try {
                    mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.open_folder)));
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(mActivity, "Error. Please install a file manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        snack.setActionTextColor(ContextCompat.getColor(mActivity, R.color.f_color8));
        snack.show();
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

    // Partilhar todas as imagens
    public static void share_history_images(Activity mActivity) {
        verifyStoragePermissions(mActivity);
        ViewGroup history_view = (ViewGroup) mActivity.findViewById(history);
        int childCount = history_view.getChildCount();

        if (childCount > 0) {
            ArrayList<Uri> file_uris = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                CardView cardAtIndex = (CardView) history_view.getChildAt(i);
                cardAtIndex.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor));
                String img_path = saveViewToImage(cardAtIndex, i, false);
                file_uris.add(Uri.parse(img_path));
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources().getString(R.string.app_long_description) +
                    mActivity.getResources().getString(R.string.app_version_name) + "\n");
            sendIntent.setType("image/jpeg");
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, file_uris);
            mActivity.startActivity(Intent.createChooser(sendIntent, mActivity.getResources().getString(R.string.app_name)));
        } else {
            Toast thetoast = Toast.makeText(mActivity, R.string.nothing_toshare, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

    // Guardar todas as imagens
    public static void save_history_images(final Activity mActivity) {
        verifyStoragePermissions(mActivity);
        ViewGroup history_view = (ViewGroup) mActivity.findViewById(history);
        int childCount = history_view.getChildCount();
        if (childCount > 0) {
            String img_path = null;
            for (int i = 0; i < childCount; i++) {
                CardView cardAtIndex = (CardView) history_view.getChildAt(i);
                cardAtIndex.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor));
                img_path = saveViewToImage(cardAtIndex, i, false);
            }
            if (img_path != null) {
                MenuHelper.openFolder_Snackbar(mActivity, mActivity.getString(R.string.all_images_saved));
            } else {
                Toast thetoast = Toast.makeText(mActivity, mActivity.getString(R.string.errorsavingimg), Toast.LENGTH_SHORT);
                thetoast.setGravity(Gravity.CENTER, 0, 0);
                thetoast.show();
            }


        } else {
            Toast thetoast = Toast.makeText(mActivity, R.string.nothing_tosave, Toast.LENGTH_SHORT);
            thetoast.setGravity(Gravity.CENTER, 0, 0);
            thetoast.show();
        }
    }

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
