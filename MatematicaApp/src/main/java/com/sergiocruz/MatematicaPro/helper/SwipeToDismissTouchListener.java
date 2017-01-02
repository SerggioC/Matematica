package com.sergiocruz.MatematicaPro.helper;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.MatematicaPro.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.widget.Toast.makeText;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 25/10/2016 23:10
 ******/

public class SwipeToDismissTouchListener implements View.OnTouchListener {

    private static final int POPUP_WIDTH = 188;
    private static final int POPUP_HEIGHT = 190;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final Handler handler = new Handler();
    boolean mBooleanIsPressed = false;
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;
    // Fixed properties
    private View mView;
    private DismissCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private Activity mActivity;
    private final Runnable runnable = new Runnable() {
        public void run() {
            if (mBooleanIsPressed) {
                showCustomPopup();
                //show_popup_options();
                mBooleanIsPressed = false;
            }
        }
    };
    private VelocityTracker mVelocityTracker;
    private float mTranslationX;

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     *
     * @param view      The view to make dismissable.
     * @param activity  An optional token/cookie object to be passed through to the callback.
     * @param callbacks The callback to trigger when the user has indicated that he would like to
     *                  dismiss this view.
     */
    public SwipeToDismissTouchListener(View view, Activity activity, DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = 200;
        mView = view;
        mActivity = activity;
        mCallbacks = callbacks;
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

    private void showCustomPopup() {

        // Inflate the popup_menu_layout.xml
        LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popup_layout = layoutInflater.inflate(R.layout.popup_menu_layout, null);

        final float scale = mActivity.getResources().getDisplayMetrics().density;

        final int offset_x = (int) ((POPUP_WIDTH) * scale + 0.5f);
        final int offset_y = (int) ((POPUP_HEIGHT) * scale + 0.5f);

        // Creating the PopupWindow
        final PopupWindow customPopUp = new PopupWindow(mActivity);
        customPopUp.setContentView(popup_layout);
        customPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        customPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            customPopUp.setElevation(scale * 10.0f);
//        }
        customPopUp.setFocusable(true);
        customPopUp.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Clear the default translucent background
        customPopUp.setAnimationStyle(R.style.popup_animation);
        customPopUp.showAtLocation(popup_layout, Gravity.NO_GRAVITY, (int) mDownX - offset_x, (int) mDownY - offset_y);

        final CardView theCardView = (CardView) this.mView;
        int cv_color = ContextCompat.getColor(mActivity, R.color.greener);
        theCardView.setCardBackgroundColor(cv_color);

        customPopUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                int cv_color = ContextCompat.getColor(mActivity, R.color.cardsColor);
                theCardView.setCardBackgroundColor(cv_color);
            }
        });
        popup_layout.findViewById(R.id.action_clipboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text_fromTextViews_final = getFormatedTextFromTextView();
                // aceder ao clipboard manager
                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                boolean hasEqualItem = false;
                if (clipboard.hasPrimaryClip()) {
                    int clipItems = clipboard.getPrimaryClip().getItemCount();
                    for (int i = 0; i < clipItems; i++) {
                        if (clipboard.getPrimaryClip().getItemAt(i).getText().toString().equals(text_fromTextViews_final)) {
                            hasEqualItem = true;
                        }
                    }
                }
                if (hasEqualItem) {
                    Toast thetoast = makeText(mView.getContext(), R.string.already_inclipboard, Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                } else {
                    ClipData clip = ClipData.newPlainText("Clipboard", text_fromTextViews_final);
                    clipboard.setPrimaryClip(clip);
                    Toast thetoast = Toast.makeText(mView.getContext(), R.string.copied_toclipboard, Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }
                customPopUp.dismiss();
            }
        });

        popup_layout.findViewById(R.id.action_clear_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ViewGroup history = (ViewGroup) theCardView.getParent();
                animateRemoving(theCardView, history);
                customPopUp.dismiss();
            }
        });

        popup_layout.findViewById(R.id.action_share_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text_fromTextViews_final = getFormatedTextFromTextView();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources().getString(R.string.app_long_description) +
                        mActivity.getResources().getString(R.string.app_version_name) + "\n" + text_fromTextViews_final);
                sendIntent.setType("text/plain");
                mActivity.startActivity(sendIntent);
                customPopUp.dismiss();
            }
        });

        popup_layout.findViewById(R.id.action_save_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                theCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor));
                verifyStoragePermissions(mActivity);
                theCardView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(theCardView.getWidth(), theCardView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                theCardView.draw(canvas);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

                String folder = "Matematica Images";
                File image_file = new File(Environment.getExternalStorageDirectory() + File.separator + folder);
                try {
                    if (!image_file.exists()) {
                        image_file.mkdirs();
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    image_file = new File(Environment.getExternalStorageDirectory() + File.separator + folder + File.separator + "img" + timeStamp + ".jpg");
                    image_file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(image_file);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    Toast thetoast = Toast.makeText(mView.getContext(), mActivity.getString(R.string.image_saved) + " " + folder, Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Sergio>>>", "onClick: error ", e);
                    Toast thetoast = Toast.makeText(mView.getContext(), mActivity.getString(R.string.errorsavingimg), Toast.LENGTH_SHORT);
                    thetoast.setGravity(Gravity.CENTER, 0, 0);
                    thetoast.show();
                }

//
//                try {
//                    theCardView.setDrawingCacheEnabled(true);
//                    Bitmap bitmap2 = theCardView.getDrawingCache();
//                    File file, f = null;
//                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        file = new File(Environment.getExternalStorageDirectory(), "Android");
//                        if (!file.exists()) {
//                            file.mkdirs();
//                            Log.i("Sergio>>>", "mkdirs");
//                        }
//                        f = new File(file.getAbsolutePath() + "/filename" + ".png");
//                    }
//                    FileOutputStream fostream = new FileOutputStream(f);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fostream);
//                    fostream.close();
//                } catch (Exception e) {
//                    Log.d("Sergio>>>", "onClick: error? " + e);
//                    e.printStackTrace();
//                }
                customPopUp.dismiss();
            }
        });

    }

    @NonNull
    public String getFormatedTextFromTextView() {
        ArrayList<View> textViews_withTAG_texto = getViewsByTag((ViewGroup) mView, "texto");
        String text_fromTextViews_final = "";
        for (int i = 0; i < textViews_withTAG_texto.size(); i++) {
            String text_fromTextView = (((TextView) textViews_withTAG_texto.get(i)).getText().toString()) + "\n";
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
        return text_fromTextViews_final;
    }


    private void show_popup_options() {

        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(mView.getContext(), this.mView, Gravity.TOP);

        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        final CardView theView = (CardView) this.mView;

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                if (id == R.id.action_clipboard) {

                    String theClipText = ((TextView) theView.findViewWithTag("texto")).getText().toString();

                    // aceder ao clipboard manager
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);

                    boolean hasEqualItem = false;

                    if (clipboard.hasPrimaryClip()) {

                        int clipItems = clipboard.getPrimaryClip().getItemCount();
                        for (int i = 0; i < clipItems; i++) {
                            if (clipboard.getPrimaryClip().getItemAt(i).getText().toString().equals(theClipText)) {
                                hasEqualItem = true;
                            }
                        }
                    }

                    if (hasEqualItem) {
                        Toast thetoast = makeText(mView.getContext(), R.string.already_inclipboard, Toast.LENGTH_SHORT);
                        thetoast.setGravity(Gravity.CENTER, 0, 0);
                        thetoast.show();
                    } else {
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Clipboard", theClipText);
                        clipboard.setPrimaryClip(clip);
                        Toast thetoast = Toast.makeText(mView.getContext(), R.string.copied_toclipboard, Toast.LENGTH_SHORT);
                        thetoast.setGravity(Gravity.CENTER, 0, 0);
                        thetoast.show();
                    }
                }

                if (id == R.id.action_clear_result) {
                    final ViewGroup history = (ViewGroup) theView.getParent();
                    animateRemoving(theView, history);
                }
                if (id == R.id.action_share_result) {

                    String text_fromTextView = ((TextView) theView.findViewWithTag("texto")).getText().toString();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Matemática\n" + text_fromTextView);
                    sendIntent.setType("text/plain");
                    mActivity.startActivity(sendIntent);

                }

                return true;
            }
        });
        popup.show();
    }


    void animateRemoving(final CardView cardview, final ViewGroup history) {
        cardview.animate().translationX(cardview.getWidth()).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    history.removeView(cardview);
                    mCallbacks.onDismiss(mView);
                } catch (Exception e) {
                }

                //performDismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, 0);

        if (mViewWidth < 2) {
            mViewWidth = mView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                // TODO: ensure this is a finger, and set a flag
                mDownX = motionEvent.getRawX();
                mDownY = motionEvent.getRawY();

                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(motionEvent);

                // Execute your Runnable after 600 milliseconds = 0.5 second
                handler.postDelayed(runnable, 600);
                mBooleanIsPressed = true;

                return true;
            }

            case MotionEvent.ACTION_UP: {

                if (mBooleanIsPressed) {
                    mBooleanIsPressed = false;
                    handler.removeCallbacks(runnable);
                }

                if (mVelocityTracker == null) {
                    break;
                }
                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                if (dismiss) {

                    // dismiss
                    mView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (mView != null) {
                                        final ViewGroup history = (ViewGroup) mView.getParent();
                                        history.removeView(mView);
                                        mCallbacks.onDismiss(mView);
                                    }
                                    //performDismiss();
                                }
                            });
                } else if (mSwiping) {
                    // cancel
                    mView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                try {
                    mVelocityTracker.recycle();
                } catch (Exception e) {
                }
                mVelocityTracker = null;
                mTranslationX = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                break;
            }
            case MotionEvent.ACTION_SCROLL: {
                if (mBooleanIsPressed) {
                    mBooleanIsPressed = false;
                    handler.removeCallbacks(runnable);
                }
            }

            case MotionEvent.ACTION_CANCEL: {

                if (mBooleanIsPressed) {
                    mBooleanIsPressed = false;
                    handler.removeCallbacks(runnable);
                }

                if (mVelocityTracker == null) {
                    break;
                }
                mView.animate()
                        .translationX(0)
                        .alpha(1)
                        .setDuration(mAnimationTime)
                        .setListener(null);
                try {
                    mVelocityTracker.recycle();
                } catch (Exception e) {
                }
                mVelocityTracker = null;
                mTranslationX = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                if (mVelocityTracker == null) {
                    break;
                }


                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;

                if (deltaY > 10 && mBooleanIsPressed) {

                    //a mover verticalmente não disparar longpress
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false;
                        handler.removeCallbacks(runnable);
                    }
                }


                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mView.getParent().requestDisallowInterceptTouchEvent(true);

                    // Cancel listview's touch
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mView.onTouchEvent(cancelEvent);
                    try {
                        cancelEvent.recycle();
                    } catch (Exception e) {
                    }
                }

                if (mSwiping) {

                    //a mover não disparar longpress
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false;
                        handler.removeCallbacks(runnable);
                    }
                    mTranslationX = deltaX;
                    mView.setTranslationX(deltaX - mSwipingSlop);
                    // TODO: use an ease-out interpolator or such
                    mView.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void performDismiss() {
        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.
        // set animateLayoutChanges="true" in xml layout
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
        final int originalHeight = mView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation2) {
                mCallbacks.onDismiss(mView);
                // Reset view presentation
                mView.setAlpha(1f);
                mView.setTranslationX(0);
                lp.height = originalHeight;
                mView.setLayoutParams(lp);

            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                lp.height = (Integer) valueAnimator.getAnimatedValue();
                mView.setLayoutParams(lp);
            }
        });

        animator.start();
    }

    /**
     * The callback interface used by {@link SwipeToDismissTouchListener} to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        boolean canDismiss(Boolean token);

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view The originating {@link View} to be dismissed.
         */
        void onDismiss(View view);
    }
}
