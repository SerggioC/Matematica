package com.sergiocruz.matematica.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.billingclient.api.*
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.Ui.TooltipManager
import com.sergiocruz.matematica.activity.AboutActivity
import com.sergiocruz.matematica.database.HistoryDataClass
import com.sergiocruz.matematica.database.LocalDatabase
import com.sergiocruz.matematica.databinding.TextviewStarBinding
import com.sergiocruz.matematica.helper.*
import com.sergiocruz.matematica.model.SpannableSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.math.BigInteger
import java.text.DecimalFormat

abstract class BaseFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var sharedPrefs: SharedPreferences
    var historyLimit: Int = 0
    var scale: Float = 0f
    var shouldShowPerformance: Boolean = true
    var explanations: Explanations = Explanations.WhenAsked
    var shouldFormatNumbers: Boolean = false
    private var shouldShowColors: Boolean = true

    var allFavoritesCallback: ((List<HistoryDataClass>?) -> Unit)? = null

    // -1 = sempre  0 = quando pedidas   1 = nunca
    val withExplanations: Boolean
        get() {
            return explanations == Explanations.Always || explanations == Explanations.WhenAsked
        }

    val operationName: String = javaClass.simpleName

    val gson: Gson by lazy {
        val type: Type = object : TypeToken<SpannableStringBuilder>() {}.type
        GsonBuilder()
                .registerTypeAdapter(type, SpannableSerializer())
                .create()
    }

    private val reviewManager by lazy {
//        FakeReviewManager(context)
        ReviewManagerFactory.create(requireContext())
    }

    abstract var title: Int

    abstract var pageIndex: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        scale = resources.displayMetrics.density
        getBasePreferences()
        clearTemporaryResultsFromDB()
        registerScreenViewForAnalytics()
    }

    private fun registerScreenViewForAnalytics() {
        FirebaseAnalytics.getInstance(requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, operationName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, operationName)
        }
    }

    @StringRes
    abstract fun getHelpTextId(): Int?

    @StringRes
    abstract fun getHelpMenuTitleId(): Int?

    abstract fun getHistoryLayout(): LinearLayout?

    @MenuRes
    abstract fun optionsMenu(): Int?

    private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }
            }

    private fun handlePurchase(purchase: Purchase?) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.


        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase!!.purchaseToken)
                .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
            }
        }
    }


    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
    }


    private fun removeAds() {
        billingClient.queryPurchases("")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    launchSafeCoroutine {
                        queryPurchase()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                context?.showConfirmAlert(R.string.info, R.string.billing_disconnected) {
                    removeAds()
                }
            }
        })
    }

    private suspend fun queryPurchase() {
        val skuList = ArrayList<String>()
        skuList.add("remove_ads")
        val skuBuilder = SkuDetailsParams.newBuilder()
        skuBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        withContext(Dispatchers.IO) {
            billingClient.querySkuDetailsAsync(skuBuilder.build()) { billingResult, skuDetailsList ->
                // Process the result.
                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                skuDetailsList?.forEach { sku ->
                    val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(sku)
                            .build()
                    val responseCode = billingClient.launchBillingFlow(activity as Activity, flowParams).responseCode
                    print(responseCode)
                }

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        optionsMenu()?.let {
            inflater.inflate(it, menu)
        }
        inflater.inflate(R.menu.menu_sub_main, menu)
        getHelpMenuTitleId()?.let { menu.findItem(R.id.action_help)?.setTitle(it) }

        // TODO if has bought remove menu entries


    }


    open fun getAllFavorites() {
        context?.let { ctx ->
            launchSafeCoroutine {
                val list: List<HistoryDataClass>? = LocalDatabase.getInstance(ctx).historyDAO().getAllFavoritesForOperation(operationName)
                withContext(Dispatchers.Main) {
                    allFavoritesCallback?.invoke(list)
                }
            }
        }
    }

    open fun makeAllResultsFavorite() {
        context?.let { ctx ->
            launchSafeCoroutine {
                val num = LocalDatabase.getInstance(ctx).historyDAO().makeNonFavoriteFavorite(operationName)
                if (num > 0) {
                    withContext(Dispatchers.Main) {
                        getHistoryLayout()?.children?.forEach {
                            it.findViewById<View>(R.id.image_star)?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    private fun sendFeedbackRating() {
        var mp = MediaPlayer.create(context, R.raw.bling)
        mp?.setOnCompletionListener {
            mp?.reset()
            mp?.release()
            mp = null
        }
        mp?.start()
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { listener ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            } else {
                // There was some problem, log or handle the error code.
//                @ReviewErrorCode
//                val reviewErrorCode = (task.exception as TaskException).errorCode
                val t = Throwable(task.exception?.localizedMessage)
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_show_favorites -> getAllFavorites()
            R.id.action_save_all_to_db -> makeAllResultsFavorite()
            R.id.action_save_history_images -> MenuHelper.saveHistoryImages(activity as Activity)
            R.id.action_share_history -> MenuHelper.shareHistory(activity as Activity, withExplanations)
            R.id.action_share_history_images -> MenuHelper.shareHistoryImages(activity as Activity)
            R.id.action_clear_all_history -> clearResultHistory()
            R.id.action_help -> CreateCardView.withStringRes(getHistoryLayout(), getHelpTextId(), activity as Activity)
            R.id.action_about -> startActivity(Intent(activity, AboutActivity::class.java))
            R.id.action_settings -> activity?.openSettingsFragment()
            R.id.action_feedback_rating -> sendFeedbackRating()
            R.id.action_remove_ads2 -> removeAds()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this)
        clearTemporaryResultsFromDB()
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
    }

    enum class Explanations(private val value: String) {
        Never("1"),
        WhenAsked("0"),
        Always("-1");

        companion object {
            fun fromStringValue(value: String): Explanations {
                var ex = WhenAsked
                for (enumValue in values()) {
                    if (enumValue.value == value) {
                        ex = enumValue
                        break
                    }
                }
                return ex
            }
        }
    }

    open fun getBasePreferences() {
        val default: Int = resources.getInteger(R.integer.default_history_size)
        historyLimit = sharedPrefs.getString(getString(R.string.pref_key_history_size), default.toString())?.toInt() ?: default
        shouldShowPerformance = sharedPrefs.getBoolean(getString(R.string.pref_key_show_performance), true)
        val explStr = sharedPrefs.getString(getString(R.string.pref_key_show_explanation), "0") ?: "0"
        explanations = Explanations.fromStringValue(explStr)
        shouldShowColors = sharedPrefs.getBoolean(getString(R.string.pref_key_show_colors), true)
        shouldFormatNumbers = sharedPrefs.getBoolean(getString(R.string.pref_key_format_numbers), false)
    }

    @ColorRes
    fun getRandomFactorsColors(): List<Int> {
        var privateFColors: MutableList<Int> = resources.getIntArray(R.array.f_colors_xml).toMutableList()
        if (shouldShowColors) {
            privateFColors.shuffle() // randomizar as cores
        } else {
            privateFColors = MutableList(privateFColors.size) { privateFColors.last() } // just a list with always the same color
        }
        return privateFColors
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutIdForFragment(), container, false)

    @LayoutRes
    abstract fun getLayoutIdForFragment(): Int

    fun saveCardToDatabase(input: String?, data: String?, operation: String) {
        if (input == null || data == null) return
        context?.let { ctx ->
            launchSafeCoroutine {
                val history = HistoryDataClass(primaryKey = input, operation, content = data, favorite = false)
                LocalDatabase.getInstance(ctx).historyDAO().saveResult(history)
            }
        }
    }

    fun getFavoriteStarForCard(ssb: SpannableStringBuilder, input: String): TextviewStarBinding {
        val binding = TextviewStarBinding.inflate(layoutInflater)
        binding.textViewTop.text = ssb
        binding.textViewTop.setTag(R.id.texto, "texto")
        context?.let { ctx ->
            launchSafeCoroutine {
                val saved = LocalDatabase.getInstance(ctx).historyDAO().getFavoriteForKeyAndOp(key = input, operation = operationName) != null
                withContext(Dispatchers.Main) {
                    binding.imageStar.visibility = if (saved) View.VISIBLE else View.GONE
                    binding.imageStar.setOnClickListener {
                        TooltipManager.showTooltipOn(binding.imageStar, getString(R.string.result_is_favorite))
                    }
                }
            }
        }
        return binding
    }

    fun showFavoriteStarForInput(star: ImageView, input: String) {
        star.context?.let { ctx ->
            launchSafeCoroutine {
                val saved = LocalDatabase.getInstance(ctx).historyDAO().getFavoriteForKeyAndOp(key = input, operation = operationName) != null
                withContext(Dispatchers.Main) {
                    star.visibility = if (saved) View.VISIBLE else View.GONE
                    star.rotateYAnimation()
                    star.setOnClickListener {
                        TooltipManager.showTooltipOn(star, getString(R.string.result_is_favorite))
                    }
                }
            }
        }
    }


    private fun clearResultHistory() {
        MenuHelper.removeResultsFromLayout(activity as Activity)
        clearTemporaryResultsFromDB()
    }

    private fun clearTemporaryResultsFromDB() {
        context?.let {
            launchSafeCoroutine {
                LocalDatabase.getInstance(it).historyDAO().deleteNonFavoritesFromOperation(operationName)
            }
        }
    }

    fun BigInteger.conditionalFormat(): String {
        return if (shouldFormatNumbers) this.formatForLocale() else this.toString()
    }

    fun TextView.writePerformanceValue(startTime: Long) {
        if (shouldShowPerformance) {
            val formatter = DecimalFormat("#.###")
            val elapsed = context.getString(R.string.performance) + " " + formatter.format((System.nanoTime() - startTime) / 1_000_000_000.0) + "s"
            text = elapsed
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }

}
