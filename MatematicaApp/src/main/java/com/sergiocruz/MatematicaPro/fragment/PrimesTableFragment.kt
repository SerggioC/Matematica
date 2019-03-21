package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.adapter.TableAdapter
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.InfoLevel.ERROR
import com.sergiocruz.MatematicaPro.helper.InfoLevel.WARNING
import com.sergiocruz.MatematicaPro.helper.MenuHelper.checkPermissionsWithCallback
import com.sergiocruz.MatematicaPro.helper.MenuHelper.openFolderSnackBar
import com.sergiocruz.MatematicaPro.helper.MenuHelper.saveViewToImage
import kotlinx.android.synthetic.main.fragment_primes_table.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.DecimalFormat

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 */

class PrimesTableFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActions {

    companion object {
        // isProbablePrime function returns a prime with probability = 1 - (1/2)^certainty
        private const val certainty = 100
    }

    private var asyncTask: AsyncTask<Long, Float, ResultWrapper> = LongOperation()
    private var heightDp: Int = 0
    private var minValue: Long? = 0L
    private var maxValue: Long? = 50L
    private var bruteForceMode: Boolean = true
    private lateinit var tableAdapter: TableAdapter
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var progressBarParams: ConstraintLayout.LayoutParams
    private lateinit var tableData: ResultWrapper

    override fun getHelpTextId(): Int? = null

    override fun getHelpMenuTitleId(): Int? = null

    override fun getHistoryLayout(): LinearLayout? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        getBasePreferences()
        getSharedPreferences()
        writeCalcMode()
    }

    private fun getSharedPreferences() {
        bruteForceMode = sharedPrefs.getBoolean(
            getString(R.string.pref_key_brute_force),
            resources.getBoolean(R.bool.pref_default_brute_force)
        )
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_primes_table

    override fun loadOptionsMenus() = listOf(R.menu.menu_primes_table, R.menu.menu_sub_main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getSharedPreferences()
        writeCalcMode()
        heightDp = (4 * scale + 0.5f).toInt()

        switchPrimos.setOnCheckedChangeListener { _, isChecked ->
            if (::tableAdapter.isInitialized)
                tableAdapter.reloadAdapter(isChecked)
        }

        cancelButton.setOnClickListener {
            if (asyncTask.status == AsyncTask.Status.RUNNING || status == OperationStatus.Running) {
                displayCancelDialogBox(context!!, this)
            }
        }
        createTableBtn.setOnClickListener { makePrimesTable() }
        btn_clear_min.setOnClickListener { min_pt.setText("") }
        btn_clear_max.setOnClickListener { max_pt.setText("") }
        min_pt.watchThis(this)
        max_pt.watchThis(this)
    }

    private fun writeCalcMode() {
        calcMode.setText(
            if (bruteForceMode)
                R.string.pref_title_brute_force
            else
                R.string.pref_title_probabilistic
        )
    }

    override fun onActionDone() = makePrimesTable()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        val childCount = historyGridRecyclerView.childCount
        if (id == R.id.action_save_image_pt) {
            if (childCount > 0) {
                checkPermissionsWithCallback(activity as Activity) {
                    val imgPath = saveViewToImage(historyGridRecyclerView, 0, true)
                    if (imgPath != null) {
                        openFolderSnackBar(activity!!, getString(R.string.image_saved))
                    } else {
                        showCustomToast(this.context, getString(R.string.errorsavingimg), ERROR)
                    }
                }
            } else {
                showCustomToast(context, getString(R.string.empty_table), WARNING)
            }
        }
        if (id == R.id.action_share_history_image_pt) {
            if (childCount > 0) {
                checkPermissionsWithCallback(activity as Activity) {
                    val fileUris = ArrayList<Uri>(1)
                    val imgPath = saveViewToImage(historyGridRecyclerView, 0, true)
                    if (imgPath != null) {
                        fileUris.add(Uri.parse(imgPath))
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                        sendIntent.putExtra(
                            Intent.EXTRA_TEXT, getString(R.string.app_long_description) +
                                    BuildConfig.VERSION_NAME + "\n"
                        )
                        sendIntent.type = "image/jpeg"
                        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
                        startActivity(
                            Intent.createChooser(
                                sendIntent,
                                resources.getString(R.string.app_name)
                            )
                        )

                    } else {
                        showCustomToast(context, getString(R.string.errorsavingimg), ERROR)
                    }
                }
            } else {
                showCustomToast(context, getString(R.string.empty_table), WARNING)
            }
        }
        // Partilhar tabela como texto
        if (id == R.id.action_share_history) {
            if (tableData?.primesTable?.isNotEmpty()) {
                val primesString =
                    "${getString(R.string.table_between)} $minValue ${getString(R.string.and)} $maxValue:\n${tableData.primesTable}"
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.app_long_description) +
                            BuildConfig.VERSION_NAME + "\n" + primesString
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            } else {
                showCustomToast(context, getString(R.string.no_data))
            }
        }

        if (id == R.id.action_clear_all_history) {
            historyGridRecyclerView.adapter = null
            min_pt.text = Editable.Factory().newEditable("1")
            max_pt.text = Editable.Factory().newEditable("50")
            numPrimesTextView.visibility = GONE
            performanceTextView.visibility = GONE
        }
        if (id == R.id.action_about) {
            startActivity(Intent(context, AboutActivity::class.java))
        }
        if (id == R.id.action_settings) {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
        return true
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(asyncTask, context)) resetButtons()
        if (status == OperationStatus.Running) {
            status = OperationStatus.Canceled
        }
    }

    private fun makePrimesTable() {
        hideKeyboard(activity)
        val minString = min_pt.text.toString().replace("[^\\d]".toRegex(), "")
        val maxString = max_pt.text.toString().replace("[^\\d]".toRegex(), "")

        if (TextUtils.isEmpty(minString)) {
            min_pt.apply {
                requestFocus()
                error = getString(R.string.fill_this)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            showKeyboard(activity)
            return
        }
        if (TextUtils.isEmpty(maxString)) {
            max_pt.apply {
                requestFocus()
                error = getString(R.string.fill_this)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            showKeyboard(activity)
            return
        }

        minValue = minString.toLongOrNull(10)
        if (minValue == null) {
            min_pt.apply {
                requestFocus()
                error = getString(R.string.numero_alto)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            return
        } else if (minValue!! < 1) {
            min_pt.setText("1")
            minValue = 1
        }

        maxValue = maxString.toLongOrNull(10)
        if (maxValue == null) {
            max_pt.apply {
                requestFocus()
                error = getString(R.string.numero_alto)
                postDelayed({ error = null }, clearErrorDelayMillis)
            }
            return
        } else if (maxValue!! < 1) {
            max_pt.setText("1")
            maxValue = 1
        }

        if (minValue!! > maxValue!!) {
            val swap = minValue
            minValue = maxValue
            maxValue = swap
            min_pt.setText(minValue.toString())
            max_pt.setText(maxValue.toString())
            makePrimesTable()
            return
        }

        lockButtons()
        if (bruteForceMode) {
            setUpAdapter(maxValue!!)
            asyncTask = LongOperation().execute(minValue, maxValue)
        } else {
            status = OperationStatus.Running
            // launch coroutine in the Default thread
            GlobalScope.launch(Dispatchers.Default) {
                val startTime = System.currentTimeMillis()
                val result: ResultWrapper = probabilisticMode(minValue!!, maxValue!!)
                withContext(Dispatchers.Main) {
                    showPerformance(startTime)
                    setUpAdapter(maxValue!!)
                    tableAdapter.swap(result.fullTable, result.primesTable, switchPrimos.isChecked)
                    numPrimesTextView.visibility = VISIBLE
                    numPrimesTextView.text =
                        "${getString(R.string.cardinal_primos)} ${result.primesTable.size}"
                    resetButtons()
                }
                tableData = result
                status = OperationStatus.Done
            }
        }
    }

    /** Data class to wrap the result of the async calculation
     * containing primesTable and fullTable */
    data class ResultWrapper(
        val fullTable: MutableMap<Int, Pair<String, Boolean>>,
        val primesTable: MutableMap<Int, Pair<String, Boolean>>
    )

    private suspend fun probabilisticMode(minValue: Long, maxValue: Long): ResultWrapper {
        val fullTable = mutableMapOf<Int, Pair<String, Boolean>>()
        val primesOnlyTable = mutableMapOf<Int, Pair<String, Boolean>>()
        var indexPrimes = 0
        var progress: Float
        var lastProgress = 0.0f
        for ((index, i) in (minValue..maxValue).withIndex()) {
            fullTable[index] = Pair(i.toString(), false)
            val currentVal: BigInteger = BigInteger.valueOf(i)
            if (currentVal.isProbablePrime(certainty)) {
                primesOnlyTable[indexPrimes] = Pair(i.toString(), true)
                indexPrimes++
                fullTable[index] = Pair(i.toString(), true)
            }

            if (status is OperationStatus.Canceled) {
                status = OperationStatus.Idle
                return ResultWrapper(fullTable, primesOnlyTable)
            }

            progress = i.toFloat() / maxValue.toFloat()
            if (progress - lastProgress > 0.05) {
                updateProgress(progress)
                lastProgress = progress
            }
        }
        return ResultWrapper(fullTable, primesOnlyTable)
    }

    private suspend fun updateProgress(progress: Float) {
        if (this@PrimesTableFragment.isVisible) {
            withContext(Dispatchers.Main) {
                progressBarParams.width = Math.round(progress * cardViewMain.width)
                progressBar.layoutParams = progressBarParams
            }
        }
    }


    private var status: OperationStatus = OperationStatus.Idle

    /**status of the background calculations */
    sealed class OperationStatus {
        object Running : OperationStatus()
        object Canceled : OperationStatus()
        object Done : OperationStatus()
        object Idle : OperationStatus()
    }

    /** Slower method even though it doesn't check all numbers.*/
    private fun probabilisticMode2(minValue: Long, maxValue: Long): ResultWrapper {

        val fullTable = mutableMapOf<Int, Pair<String, Boolean>>()
        val primesOnlyTable = mutableMapOf<Int, Pair<String, Boolean>>()

        var index = 0
        var indexPrimes = 0
        var tracker = minValue
        var nextTracker = tracker

        if (BigInteger.valueOf(minValue).isProbablePrime(certainty)) {
            fullTable[index] = Pair(minValue.toString(), true)
            index++
            primesOnlyTable[indexPrimes] = Pair(minValue.toString(), true)
            indexPrimes++
        }
        val start = BigInteger.valueOf(minValue).nextProbablePrime().toLong()
        for (i in (minValue + index)..(start - 1)) {
            fullTable[index] = Pair(i.toString(), false)
            index++
        }

        while (tracker <= maxValue) {
            tracker = BigInteger.valueOf(tracker).nextProbablePrime().toLong()
            if (tracker >= maxValue) {
                for (i in nextTracker..maxValue) {
                    fullTable[index] = Pair(i.toString(), false)
                    index++
                }
                break
            }

            fullTable[index] = Pair(tracker.toString(), true)
            index++
            primesOnlyTable[indexPrimes] = Pair(tracker.toString(), true)
            indexPrimes++
            nextTracker = BigInteger.valueOf(tracker).nextProbablePrime().toLong()
            if (nextTracker >= maxValue) {
                for (i in (tracker + 1)..maxValue) {
                    fullTable[index] = Pair(i.toString(), false)
                    index++
                }
                break
            }
            for (i in (tracker + 1)..(nextTracker - 1)) {
                fullTable[index] = Pair(i.toString(), false)
                index++
            }
            fullTable[index] = Pair(nextTracker.toString(), true)
            primesOnlyTable[indexPrimes] = Pair(nextTracker.toString(), true)

            if (status == OperationStatus.Canceled) {
                status = OperationStatus.Idle
                return ResultWrapper(fullTable, primesOnlyTable)
            }
        }
        return ResultWrapper(fullTable, primesOnlyTable)
    }

    private fun setUpAdapter(maxValue: Long) {
        tableAdapter = TableAdapter()
        tableAdapter.setHasStableIds(true)
        historyGridRecyclerView.adapter = tableAdapter
        historyGridRecyclerView.setHasFixedSize(true)
        layoutManager = GridLayoutManager(context, getNumColumns(maxValue))
        historyGridRecyclerView.layoutManager = layoutManager
    }

    private fun getNumColumns(maxValue: Long): Int {
        val display = activity?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        val width = size.x  //int height = size.y;
        val numMaxLength = maxValue.toString().length
        val scale = resources.displayMetrics.density
        val numLength = numMaxLength * (18 * scale + 0.5f).toInt() + 8
        return Math.round((width / numLength).toFloat())
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(asyncTask, context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Quando altera a orientação do ecrã
        if (tableData?.fullTable?.isNotEmpty()) {
            layoutManager.spanCount = getNumColumns(maxValue!!)
        }
        hideKeyboard(activity)
    }

    private fun lockButtons() {
        createTableBtn.isClickable = false
        createTableBtn.text = getString(R.string.working)
        cancelButton.visibility = VISIBLE
        progressBarParams = progressBar.layoutParams as ConstraintLayout.LayoutParams
        progressBarParams.width = 1
        progressBar.layoutParams = progressBarParams
        progressBar.visibility = VISIBLE
        hideKeyboard(activity as Activity)
        numPrimesTextView.visibility = GONE
        performanceTextView.visibility = GONE
    }

    private fun resetButtons() {
        progressBar.visibility = GONE
        cancelButton.visibility = GONE
        createTableBtn.isClickable = true
        createTableBtn.setText(R.string.gerar)
    }

    inner class LongOperation : AsyncTask<Long, Float, ResultWrapper>() {

        private var startTime: Long = System.currentTimeMillis()

        override fun doInBackground(vararg params: Long?): ResultWrapper {
            var min = params[0] as Long
            val max = params[1] as Long

            var index = 0
            var indexPrimes = 0
            val fullTable = mutableMapOf<Int, Pair<String, Boolean>>()
            val primesTable = mutableMapOf<Int, Pair<String, Boolean>>()

            var progress: Float
            var oldProgress = 0.0f
            if (min == 1L) {
                min = 2L
                fullTable[index] = Pair("1", false)
                index++
            }

            if (min == 2L) {
                fullTable[index] = Pair("2", true)
                primesTable[indexPrimes] = Pair("2", true)
                index++
                indexPrimes++
                min = 3
            }

            for (i in min..max) {
                var isPrime = true
                if (i % 2 == 0L) isPrime = false
                if (isPrime) {
                    var j = 3
                    while (j < i) {
                        if (i % j == 0L) {
                            isPrime = false
                            break
                        }
                        j += 2
                    }
                }
                if (isPrime) {
                    fullTable[index] = Pair(i.toString(), true)
                    primesTable[indexPrimes] = Pair(i.toString(), true)
                    index++
                    indexPrimes++
                } else {
                    fullTable[index] = Pair(i.toString(), false)
                    index++
                }

                progress = i.toFloat() / max.toFloat()
                if (progress - oldProgress > 0.05) {
                    publishProgress(progress)
                    oldProgress = progress
                }
                if (isCancelled) break
            }
            return ResultWrapper(fullTable, primesTable)
        }

        override fun onProgressUpdate(vararg values: Float?) {
            if (this@PrimesTableFragment.isVisible) {
                val progress: Float = values[0] ?: 0.0f
                progressBarParams.width = Math.round(progress * cardViewMain.width).toInt()
                progressBar.layoutParams = progressBarParams
            }
        }

        override fun onPostExecute(result: ResultWrapper) {

            if (this@PrimesTableFragment.isVisible) {

                tableAdapter.swap(result.fullTable, result.primesTable, switchPrimos.isChecked)

                numPrimesTextView.visibility = VISIBLE
                numPrimesTextView.text =
                    "${getString(R.string.cardinal_primos)} ${result.primesTable.size}"
                showPerformance(startTime)
                resetButtons()
                tableData = result
            }
        }

        override fun onCancelled(parcial: ResultWrapper) { //resultado parcial obtido após cancelar AsyncTask
            super.onCancelled(parcial)

            if (this@PrimesTableFragment.isVisible && parcial.fullTable.isNotEmpty()) {

                tableAdapter.swap(parcial.fullTable, parcial.primesTable, switchPrimos.isChecked)

                numPrimesTextView.visibility = VISIBLE
                numPrimesTextView.text =
                    "${getString(R.string.cardinal_primos)} (${parcial.primesTable.size})"
                showPerformance(startTime)
                resetButtons()
            } else if (parcial.primesTable.isEmpty()) {
                showCustomToast(context, getString(R.string.canceled_noprimes))
                historyGridRecyclerView.adapter = null
                resetButtons()
            }
            tableData = parcial
        }

    }

    private fun showPerformance(startTime: Long) {
        if (shouldShowPerformance) {
            ConstraintSet().apply {
                clone(cardViewMain)
                connect(
                    R.id.performanceTextView,
                    ConstraintSet.TOP,
                    R.id.numPrimesTextView,
                    ConstraintSet.TOP,
                    0
                )
                connect(
                    R.id.performanceTextView,
                    ConstraintSet.BOTTOM,
                    R.id.numPrimesTextView,
                    ConstraintSet.BOTTOM,
                    0
                )
                setVisibility(R.id.performanceTextView, VISIBLE)
                applyTo(cardViewMain)
            }

            val elapsed =
                " " + DecimalFormat("#.###").format((System.currentTimeMillis() - startTime) / 1000.0) + "s"
            performanceTextView.text = getString(R.string.performance) + " " + elapsed
        } else {
            performanceTextView.visibility = GONE
        }
    }

}
