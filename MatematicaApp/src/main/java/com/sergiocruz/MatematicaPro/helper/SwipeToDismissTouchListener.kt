package com.sergiocruz.MatematicaPro.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.database.LocalDatabase
import com.sergiocruz.MatematicaPro.helper.MenuHelper.checkPermissionsWithCallback
import com.sergiocruz.MatematicaPro.model.InputTags
import kotlinx.android.synthetic.main.popup_menu_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 25/10/2016 23:10
 */
/**
 * Constructs a new swipe-to-dismiss touch listener for the given view.
 *
 * @param view      The view to make dismissable.
 * @param activity  An optional token/cookie object to be passed through to the callback.
 * @param callbacks The callback to trigger when the user has indicated that he would like to
 * dismiss this view.
 */
class SwipeToDismissTouchListener(
        private val mView: View,
        private val mActivity: Activity,
        private val mCallbacks: DismissCallbacks? = null
) : View.OnTouchListener {

    private val handler = Handler(Looper.getMainLooper())
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
            mBooleanIsPressed = false
        }
    }
    private var mVelocityTracker: VelocityTracker? = null
    private var mTranslationX: Float = 0.toFloat()

    private val formattedTextFromTextView: String
        get() {
            val root = mView as? ViewGroup ?: return ""
            return root.getTextFromTextViews()
        }

    init {
        val vc = ViewConfiguration.get(mView.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = 200
    }

    private fun showCustomMenuPopup() {

        val context = mView.context
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupLayout = layoutInflater.inflate(R.layout.popup_menu_layout, null)

        val scale = context.resources.displayMetrics.density

        val offsetX = (POPUP_WIDTH * scale + 0.5f).toInt()
        val offsetY = (POPUP_HEIGHT * scale + 0.5f).toInt()

        // Creating the PopupWindow
        val customPopUp = PopupWindow(context)
        customPopUp.contentView = popupLayout
        customPopUp.width = LinearLayout.LayoutParams.WRAP_CONTENT
        customPopUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    customPopUp.setElevation(scale * 10.0f);
//                }
        customPopUp.isFocusable = true
        customPopUp.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //Clear the default translucent background
        customPopUp.animationStyle = R.style.popup_animation
        customPopUp.showAtLocation(popupLayout, Gravity.NO_GRAVITY, mDownX.toInt() - offsetX, mDownY.toInt() - offsetY)


        val theCardView = this.mView as CardView
        val cardOriginalColor = theCardView.cardBackgroundColor

        val selectedColor = ContextCompat.getColor(context, R.color.selected_color)
        theCardView.setCardBackgroundColor(selectedColor)

        customPopUp.setOnDismissListener {
            theCardView.setCardBackgroundColor(cardOriginalColor)
        }

        val tags = mView.tag as? InputTags?
        val pk = tags?.input ?: ""
        val op = tags?.operation ?: ""
        var saved = false
        if (pk.isNotEmpty() && op.isNotBlank()) {
            launchSafeCoroutine {
                saved = LocalDatabase.getInstance(context).historyDAO()?.getFavoriteForKeyAndOp(key = pk, operation = op) != null
                if (saved) {
                    withContext(Dispatchers.Main) {
                        popupLayout.image_popup_favorite.setImageResource(android.R.drawable.btn_star_big_on)
                        popupLayout.text_popup_favorite.setText(R.string.action_remove_favorite)
                    }
                }
            }
        }
        popupLayout.action_favorite.setOnClickListener {
            launchSafeCoroutine {
                if (pk.isEmpty() || op.isEmpty()) return@launchSafeCoroutine
                LocalDatabase.getInstance(context).historyDAO()?.makeHistoryItemFavorite(operation = op, key = pk, saved.not())
                withContext(Dispatchers.Main) {
                    val star = theCardView.findViewById<View>(R.id.image_star) ?: return@withContext
                    star.visibility = if (saved) View.GONE else View.VISIBLE
                    val animation = ObjectAnimator.ofFloat(star, View.ROTATION_Y, 0.0f, 360f)
                    animation.duration = 1500
                    animation.start()
                }
            }
            customPopUp.dismiss()
        }
        popupLayout.action_clipboard.setOnClickListener {
            // aceder ao clipboard manager
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var hasEqualItem = false
            if (clipboard.hasPrimaryClip()) {
                val clipItems = clipboard.primaryClip?.itemCount ?: 0
                for (i in 0 until clipItems) {
                    if (clipboard.primaryClip?.getItemAt(i)?.text?.toString() == formattedTextFromTextView) {
                        hasEqualItem = true
                    }
                }
            }
            if (hasEqualItem) {
                showCustomToast(mView.context, mView.context.getString(R.string.already_inclipboard))
            } else {
                val clip = ClipData.newPlainText("Clipboard", formattedTextFromTextView)
                clipboard.setPrimaryClip(clip)
                showCustomToast(mView.context, mView.context.getString(R.string.copied_toclipboard))
            }
            customPopUp.dismiss()
        }

        popupLayout.action_clear_result.setOnClickListener {
            animateRemoving(theCardView)
            customPopUp.dismiss()
        }

        popupLayout.action_share_result_as_text.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_long_description) + BuildConfig.VERSION_NAME + "\n" + formattedTextFromTextView)
            sendIntent.type = "text/plain"
            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.app_name)))
            customPopUp.dismiss()
        }

        popupLayout.action_share_result_as_image.setOnClickListener {

            val imageURI = MenuHelper.saveViewToImage(theCardView, 0, true)
            if (imageURI.isNullOrEmpty()) {
                showCustomToast(context, context.getString(R.string.errorsavingimg), InfoLevel.ERROR)
            } else {
                val uri = Uri.parse(imageURI)
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_long_description) + BuildConfig.VERSION_NAME + "\n")
                sendIntent.type = "image/jpeg"
                sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.app_name)))
            }
            customPopUp.dismiss()
        }

        popupLayout.action_save_image.setOnClickListener {
            checkPermissionsWithCallback(mActivity) {
                theCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardsColor))
                MenuHelper.saveViewToImageAndOpenSnackbar(theCardView, 0, false)
            }
            customPopUp.dismiss()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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
                mVelocityTracker?.addMovement(motionEvent)

                // Execute your Runnable after 600 milliseconds = 0.6 second
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
                mVelocityTracker?.addMovement(motionEvent)
                mVelocityTracker?.computeCurrentVelocity(1000)
                val velocityX = mVelocityTracker?.xVelocity ?: 0f
                val absVelocityX = abs(velocityX)
                val absVelocityY = abs(mVelocityTracker?.yVelocity ?: 0f)
                var dismiss = false
                var dismissRight = false
                if (abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true
                    dismissRight = deltaX > 0
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && mSwiping
                ) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = velocityX < 0 == deltaX < 0
                    dismissRight = (mVelocityTracker?.xVelocity ?: 0f) > 0
                }
                if (dismiss) {

                    // dismiss
                    mView.animate()
                            .translationX((if (dismissRight) mViewWidth else -mViewWidth).toFloat())
                            .alpha(0f)
                            .setDuration(mAnimationTime)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    removeTemporaryResultFromDB(mView)
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
                    mVelocityTracker?.recycle()
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
                        mVelocityTracker?.recycle()
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
                    mVelocityTracker?.recycle()
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

                mVelocityTracker?.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - mDownX
                val deltaY = motionEvent.rawY - mDownY

                if (deltaY > 10 && mBooleanIsPressed) {

                    //a mover verticalmente não disparar longpress
                    if (mBooleanIsPressed) {
                        mBooleanIsPressed = false
                        handler.removeCallbacks(runnable)
                    }
                }

                if (abs(deltaX) > mSlop && abs(deltaY) < abs(deltaX) / 2) {
                    mSwiping = true
                    mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                    mView.parent.requestDisallowInterceptTouchEvent(true)

                    // Cancel listview's touch
                    val cancelEvent = MotionEvent.obtain(motionEvent)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL or (motionEvent.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
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
                            max(0f, min(1f, 1f - 2f * abs(deltaX) / mViewWidth))
                    return true
                }
            }
        }
        return false
    }

    private fun removeTemporaryResultFromDB(cardview: View) {
        val history = cardview.parent as? ViewGroup?
        history?.removeView(cardview)
        mCallbacks?.onDismiss(mView)
        launchSafeCoroutine {
            val tags = mView.tag as? InputTags?
            val pk = tags?.input ?: ""
            val op = tags?.operation ?: ""
            cardview.context?.let { ctx ->
                LocalDatabase.getInstance(ctx).historyDAO()?.deleteTemporaryHistoryItem(key = pk, operation = op)
            }
        }
    }

    private fun animateRemoving(cardview: CardView) {

        cardview.animate().translationX(cardview.width.toFloat()).alpha(0f).setDuration(200)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        removeTemporaryResultFromDB(cardview)
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
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view The originating [View] to be dismissed.
         */
        fun onDismiss(view: View?)
    }


}