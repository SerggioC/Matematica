package com.sergiocruz.MatematicaPro.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.SpannableString
import android.text.style.SuperscriptSpan
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import kotlinx.android.synthetic.main.popup_menu_layout.view.*
import java.util.*

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 25/10/2016 23:10
 */

class SwipeToDismissTouchListener
/**
 * Constructs a new swipe-to-dismiss touch listener for the given view.
 *
 * @param view      The view to make dismissable.
 * @param activity  An optional token/cookie object to be passed through to the callback.
 * @param callbacks The callback to trigger when the user has indicated that he would like to
 * dismiss this view.
 */
    (// Fixed properties
    private val mView: View,
    private val mActivity: Activity,
    private val mCallbacks: DismissCallbacks
) : View.OnTouchListener {

    private val handler = Handler()
    private var mBooleanIsPressed = false
    // Cached ViewConfiguration and system-wide constant values
    private val mSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mAnimationTime: Long
    private var mViewWidth = 1 // 1 and not 0 to prevent dividing by zero
    // Transient properties
    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()
    private var mSwiping: Boolean = false
    private var mSwipingSlop: Int = 0
    private val runnable = Runnable {
        if (mBooleanIsPressed) {
            showCustomMenuPopup()
            //show_popup_options();
            mBooleanIsPressed = false
        }
    }
    private var mVelocityTracker: VelocityTracker? = null
    private var mTranslationX: Float = 0.toFloat()

    private val formatedTextFromTextView: String
        get() {
            val viewsWithTAGTexto = getViewsByTag((mView as ViewGroup?)!!, "texto")
            var finalText = ""
            for (i in viewsWithTAGTexto.indices) {
                if (viewsWithTAGTexto[i] is TextView) {
                    var text =
                        (viewsWithTAGTexto[i] as TextView).text.toString() + "\n"
                    val ss = SpannableString((viewsWithTAGTexto[i] as TextView).text)
                    val spans = ss.getSpans(
                        0,
                        (viewsWithTAGTexto[i] as TextView).text.length,
                        SuperscriptSpan::class.java
                    )
                    for ((corr, span) in spans.withIndex()) {
                        val start = ss.getSpanStart(span) + corr
                        text = text.substring(0, start) + "^" +
                                text.substring(start)
                    }
                    finalText += text
                }
            }
            return finalText
        }


    init {
        val vc = ViewConfiguration.get(mView.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = 200
    }

    private fun showCustomMenuPopup() {

        // Inflate the popup_menu_layout.xml
        val layoutInflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popup_layout = layoutInflater.inflate(R.layout.popup_menu_layout, null)

        val scale = mView.context.resources.displayMetrics.density

        val offsetX = (POPUP_WIDTH * scale + 0.5f).toInt()
        val offsetY = (POPUP_HEIGHT * scale + 0.5f).toInt()

        // Creating the PopupWindow
        val customPopUp = PopupWindow(mView.context)
        customPopUp.contentView = popup_layout
        customPopUp.width = LinearLayout.LayoutParams.WRAP_CONTENT
        customPopUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            customPopUp.setElevation(scale * 10.0f);
        //        }
        customPopUp.isFocusable = true
        customPopUp.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //Clear the default translucent background
        customPopUp.animationStyle = R.style.popup_animation
        customPopUp.showAtLocation(
            popup_layout,
            Gravity.NO_GRAVITY,
            mDownX.toInt() - offsetX,
            mDownY.toInt() - offsetY
        )

        val theCardView = this.mView as CardView
        val selectedColor = ContextCompat.getColor(mActivity, R.color.selected_color)
        theCardView.setCardBackgroundColor(selectedColor)

        customPopUp.setOnDismissListener {
            val color = ContextCompat.getColor(mActivity, R.color.cardsColor)
            theCardView.setCardBackgroundColor(color)
        }
        popup_layout.action_clipboard.setOnClickListener {
            // aceder ao clipboard manager
            val clipboard =
                mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var hasEqualItem = false
            if (clipboard.hasPrimaryClip()) {
                val clipItems = clipboard.primaryClip!!.itemCount
                for (i in 0 until clipItems) {
                    if (clipboard.primaryClip!!.getItemAt(i).text.toString() == formatedTextFromTextView) {
                        hasEqualItem = true
                    }
                }
            }
            if (hasEqualItem) {
                showCustomToast(
                    mView.context,
                    mView.context.getString(R.string.already_inclipboard)
                )
            } else {
                val clip = ClipData.newPlainText("Clipboard", formatedTextFromTextView)
                clipboard.primaryClip = clip
                showCustomToast(mView.context, mView.context.getString(R.string.copied_toclipboard))
            }
            customPopUp.dismiss()
        }

        popup_layout.action_clear_result.setOnClickListener {
            val history = theCardView.parent as ViewGroup
            animateRemoving(theCardView, history)
            customPopUp.dismiss()
        }

        popup_layout.findViewById<View>(R.id.action_share_result).setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT, mActivity.resources.getString(R.string.app_long_description) +
                        BuildConfig.VERSION_NAME + "\n" + formatedTextFromTextView
            )
            sendIntent.type = "text/plain"
            mActivity.startActivity(
                Intent.createChooser(
                    sendIntent,
                    mActivity.resources.getString(R.string.app_name)
                )
            )
            customPopUp.dismiss()
        }

        popup_layout.findViewById<View>(R.id.action_save_image).setOnClickListener {
            MenuHelper.verifyStoragePermissions(mActivity)
            theCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    mActivity,
                    R.color.cardsColor
                )
            )
            val imgageFilePath = MenuHelper.saveViewToImage(theCardView, 0, false)
            if (imgageFilePath != null) {
                MenuHelper.openFolderSnackbar(mActivity, mActivity.getString(R.string.image_saved))
            } else {
                showCustomToast(
                    mView.context,
                    mView.context.getString(R.string.errorsavingimg),
                    InfoLevel.ERROR
                )
                customPopUp.dismiss()
            }

        }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        // offset because the view is translated during swipe
        motionEvent?.offsetLocation(mTranslationX, 0f)

        if (mViewWidth < 2) {
            mViewWidth = mView.width
        }

        when (motionEvent?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // TODO: ensure this is a finger, and set a flag
                mDownX = motionEvent.rawX
                mDownY = motionEvent.rawY

                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker!!.addMovement(motionEvent)

                // Execute your Runnable after 600 milliseconds = 0.5 second
                handler.postDelayed(runnable, 600)
                mBooleanIsPressed = true

                return true
            }

            MotionEvent.ACTION_UP -> {

                if (mBooleanIsPressed) {
                    mBooleanIsPressed = false
                    handler.removeCallbacks(runnable)
                }

                if (mVelocityTracker == null) {
                    return true
                }
                val deltaX = motionEvent.rawX - mDownX
                mVelocityTracker!!.addMovement(motionEvent)
                mVelocityTracker!!.computeCurrentVelocity(1000)
                val velocityX = mVelocityTracker!!.xVelocity
                val absVelocityX = Math.abs(velocityX)
                val absVelocityY = Math.abs(mVelocityTracker!!.yVelocity)
                var dismiss = false
                var dismissRight = false
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true
                    dismissRight = deltaX > 0
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                    && absVelocityY < absVelocityX
                    && absVelocityY < absVelocityX && mSwiping
                ) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = velocityX < 0 == deltaX < 0
                    dismissRight = mVelocityTracker!!.xVelocity > 0
                }
                if (dismiss) {

                    // dismiss
                    mView.animate()
                        .translationX((if (dismissRight) mViewWidth else -mViewWidth).toFloat())
                        .alpha(0f)
                        .setDuration(mAnimationTime)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                if (mView != null) {
                                    val history = mView.parent as ViewGroup
                                    history.removeView(mView)
                                    mCallbacks.onDismiss(mView)
                                }
                                //performDismiss();
                            }
                        })
                } else if (mSwiping) {
                    // cancel
                    mView.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(mAnimationTime)
                        .setListener(null)
                }
                try {
                    mVelocityTracker!!.recycle()
                } catch (e: Exception) {
                }

                mVelocityTracker = null
                mTranslationX = 0f
                mDownX = 0f
                mDownY = 0f
                mSwiping = false
            }
            MotionEvent.ACTION_SCROLL -> {
                run {
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false
                        handler.removeCallbacks(runnable)
                    }
                }
                run {

                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false
                        handler.removeCallbacks(runnable)
                    }

                    if (mVelocityTracker == null) {
                        return true
                    }
                    mView.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(mAnimationTime)
                        .setListener(null)
                    try {
                        mVelocityTracker!!.recycle()
                    } catch (e: Exception) {
                    }

                    mVelocityTracker = null
                    mTranslationX = 0f
                    mDownX = 0f
                    mDownY = 0f
                    mSwiping = false
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mBooleanIsPressed) {
                    mBooleanIsPressed = false
                    handler.removeCallbacks(runnable)
                }
                if (mVelocityTracker == null) {
                    return true
                }
                mView.animate().translationX(0f).alpha(1f).setDuration(mAnimationTime)
                    .setListener(null)
                try {
                    mVelocityTracker!!.recycle()
                } catch (e: Exception) {
                }

                mVelocityTracker = null
                mTranslationX = 0f
                mDownX = 0f
                mDownY = 0f
                mSwiping = false
            }

            MotionEvent.ACTION_MOVE -> {

                if (mVelocityTracker == null) {
                    return true
                }

                mVelocityTracker!!.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - mDownX
                val deltaY = motionEvent.rawY - mDownY

                if (deltaY > 10 && mBooleanIsPressed) {

                    //a mover verticalmente não disparar longpress
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false
                        handler.removeCallbacks(runnable)
                    }
                }

                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true
                    mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                    mView.parent.requestDisallowInterceptTouchEvent(true)

                    // Cancel listview's touch
                    val cancelEvent = MotionEvent.obtain(motionEvent)
                    cancelEvent.action =
                            MotionEvent.ACTION_CANCEL or
                            (motionEvent.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                    mView.onTouchEvent(cancelEvent)
                    try {
                        cancelEvent.recycle()
                    } catch (e: Exception) {
                    }

                }

                if (mSwiping) {

                    //a mover não disparar longpress
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false
                        handler.removeCallbacks(runnable)
                    }
                    mTranslationX = deltaX
                    mView.translationX = deltaX - mSwipingSlop
                    // TODO: use an ease-out interpolator or such
                    mView.alpha =
                            Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth))
                    return true
                }
            }
        }
        return false
    }

    private fun animateRemoving(cardview: CardView, history: ViewGroup) {
        cardview.animate().translationX(cardview.width.toFloat()).alpha(0f).setDuration(200)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    try {
                        history.removeView(cardview)
                        mCallbacks.onDismiss(mView)
                    } catch (e: Exception) {
                    }
                    //performDismiss();
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    companion object {

        private const val POPUP_WIDTH = 188
        private const val POPUP_HEIGHT = 190

        private fun getViewsByTag(root: ViewGroup, tag: String?): ArrayList<View> {
            val views = ArrayList<View>()
            val childCount = root.childCount
            for (i in 0 until childCount) {
                val child = root.getChildAt(i)
                if (child is ViewGroup) {
                    views.addAll(getViewsByTag(child, tag))
                }
                val tagObj = child.getTag(R.id.texto)
                if (tagObj != null && tagObj == tag) {
                    views.add(child)
                }
            }
            return views
        }
    }


    /**
     * The callback interface used by [SwipeToDismissTouchListener] to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    interface DismissCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        fun canDismiss(token: Boolean?): Boolean

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view The originating [View] to be dismissed.
         */
        fun onDismiss(view: View?)
    }


}