package com.sergiocruz.Matematica.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
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

import com.sergiocruz.Matematica.R;

import static android.widget.Toast.makeText;

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 25/10/2016 23:10
 ******/

public class SwipeToDismissTouchListener implements View.OnTouchListener {

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
        //mAnimationTime = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mAnimationTime = 200;
        mView = view;
        mActivity = activity;
        mCallbacks = callbacks;
    }


    private void showCustomPopup() {

//        Point point = new Point(mDownX, mDownY);

        final CardView theCardView = (CardView) this.mView;

        // Inflate the popup_menu_layout.xml
        LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popup_layout = layoutInflater.inflate(R.layout.popup_menu_layout, null);

        final float scale = mActivity.getResources().getDisplayMetrics().density;

        final int offset_x = (int) ((136) * scale + 0.5f);
        final int offset_y = (int) ((136) * scale + 0.5f);

        // Creating the PopupWindow
        final PopupWindow customPopUp = new PopupWindow(mActivity);
        customPopUp.setContentView(popup_layout);
        customPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        customPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        customPopUp.setFocusable(true);
        customPopUp.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Clear the default translucent background
        customPopUp.setAnimationStyle(R.style.popup_animation);
        customPopUp.showAtLocation(popup_layout, Gravity.NO_GRAVITY, (int) mDownX - offset_x, (int) mDownY - offset_y);

        popup_layout.findViewById(R.id.action_clipboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.setBackgroundColor(ResourcesCompat.getColor(mActivity.getResources(), R.color.bgCardColor, null));

                String theClipText = ((TextView) theCardView.findViewWithTag("texto")).getText().toString();

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
                customPopUp.dismiss();
            }
        });


        popup_layout.findViewById(R.id.action_clear_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(ResourcesCompat.getColor(mActivity.getResources(), R.color.bgCardColor, null));
                final ViewGroup history = (ViewGroup) theCardView.getParent();
                animateRemoving(theCardView, history);
                customPopUp.dismiss();
            }
        });


        popup_layout.findViewById(R.id.action_share_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(ResourcesCompat.getColor(mActivity.getResources(), R.color.bgCardColor, null));
                String text_fromTextView = ((TextView) theCardView.findViewWithTag("texto")).getText().toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Matemática\n" + text_fromTextView);
                sendIntent.setType("text/plain");
                mActivity.startActivity(sendIntent);
                customPopUp.dismiss();
            }
        });

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
                history.removeView(cardview);
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
                                    final ViewGroup history = (ViewGroup) mView.getParent();
                                    history.removeView(mView);
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
                mVelocityTracker.recycle();
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
                mVelocityTracker.recycle();
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
                    cancelEvent.recycle();
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
        boolean canDismiss(Object token);

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view The originating {@link View} to be dismissed.
         */
        void onDismiss(View view);
    }
}
