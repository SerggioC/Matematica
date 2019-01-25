package com.sergiocruz.MatematicaPro.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.Editable
import android.text.TextUtils
import android.view.*
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.helper.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.openFolder_Snackbar
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.saveViewToImage
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.verifyStoragePermissions
import kotlinx.android.synthetic.main.fragment_primes_table.*
import java.lang.Long.toString
import java.text.DecimalFormat
import java.util.*

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 */

class PrimesTableFragment : BaseFragment(), OnCancelBackgroundTask, OnEditorActionDone,
    OnEditorActionError {


    private var BG_Operation: AsyncTask<Long, Double, ArrayList<String>> = LongOperation()
    var tableData: ArrayList<String>? = null
    internal var cv_width: Int = 0
    internal var height_dip: Int = 0
    var numMin: Long? = 0L
    var numMax: Long? = 50L
    internal var checkboxChecked: Boolean? = true
    internal var full_table: ArrayList<String>? = null
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // have a menu in this fragment
        setHasOptionsMenu(true)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context)
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_primes_table

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_primes_table, menu)
        inflater.inflate(R.menu.menu_sub_main, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        switchPrimos.setOnCheckedChangeListener { compoundButton, bool ->
            checkboxChecked = bool
            if (tableData != null) {
                if (checkboxChecked!!) {
                    historyGridView.adapter = object :
                        ArrayAdapter<String>(
                            activity!!,
                            R.layout.table_item,
                            R.id.tableItem,
                            full_table!!
                        ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getView(position, convertView, parent)
                            if (tableData!!.contains(full_table!![position])) { //Se o número for primo
                                (view as CardView).setCardBackgroundColor(Color.parseColor("#9769bc4d"))
                            } else {
                                (view as CardView).setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                            }
                            return view
                        }
                    }
                } else {
                    historyGridView.adapter =
                        ArrayAdapter(context, R.layout.table_item, R.id.tableItem, tableData)
                }
            }
        }

        cancelButton.setOnClickListener { displayCancelDialogBox(context!!, this) }
        createTableBtn.setOnClickListener { makePrimesTable() }
        btn_clear_min.setOnClickListener { min_pt.setText("") }
        btn_clear_max.setOnClickListener { max_pt.setText("") }

        min_pt.watchThis(this, this)
        max_pt.watchThis(this, this)
    }

    override fun onActionError() =
        showCustomToast(context, getString(R.string.highest_is_high), InfoLevel.WARNING)

    override fun onActionDone() = makePrimesTable()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        val childCount = historyGridView.childCount
        if (id == R.id.action_save_image_pt) {
            verifyStoragePermissions(activity as Activity)
            if (childCount > 0) {
                val img_path = saveViewToImage(historyGridView, 0, true)
                if (img_path != null) {
                    openFolder_Snackbar(activity!!, getString(R.string.image_saved))
                } else {
                    showCustomToast(context, getString(R.string.errorsavingimg), InfoLevel.ERROR)
                }
            } else {
                showCustomToast(context, getString(R.string.empty_table), InfoLevel.WARNING)
            }
        }
        if (id == R.id.action_share_history_image_pt) {
            verifyStoragePermissions(activity as Activity)
            if (childCount > 0) {
                val file_uris = ArrayList<Uri>(1)
                val img_path = saveViewToImage(historyGridView, 0, true)
                if (img_path != null) {
                    file_uris.add(Uri.parse(img_path))

                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT, getString(R.string.app_long_description) +
                                BuildConfig.VERSION_NAME + "\n"
                    )
                    sendIntent.type = "image/jpeg"
                    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, file_uris)
                    startActivity(
                        Intent.createChooser(
                            sendIntent,
                            resources.getString(R.string.app_name)
                        )
                    )

                } else {
                    showCustomToast(context, getString(R.string.errorsavingimg), InfoLevel.ERROR)
                }
            } else {
                showCustomToast(context, getString(R.string.empty_table), InfoLevel.WARNING)
            }
        }
        // Partilhar tabela como texto
        if (id == R.id.action_share_history) {
            if (tableData != null) {
                val primesString =
                    getString(R.string.table_between) + " " + numMin + " " + getString(R.string.and) + " " + numMax + ":\n" + tableData
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
            historyGridView.adapter = null
            tableData = null
            min_pt.text = Editable.Factory().newEditable("1")
            max_pt.text = Editable.Factory().newEditable("50")
        }
        if (id == R.id.action_about) {
            startActivity(Intent(context, AboutActivity::class.java))
        }
        if (id == R.id.action_settings) {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOperationCanceled(canceled: Boolean) {
        if (cancelAsyncTask(BG_Operation, context)) resetButtons()
    }

    private fun makePrimesTable() {
        val minString = min_pt.text.toString().replace("[^\\d]".toRegex(), "")
        val maxString = max_pt.text.toString().replace("[^\\d]".toRegex(), "")

        if (TextUtils.isEmpty(minString) || TextUtils.isEmpty(minString)) {
            showCustomToast(context, getString(R.string.fill_min_max), InfoLevel.WARNING)
            return
        }

        var minValue = minString.toLongOrNull(10)
        if (minValue == null) {
            showCustomToast(context, getString(R.string.lowest_is_high), InfoLevel.WARNING)
            return
        } else if (minValue < 1) {
            showCustomToast(context, getString(R.string.lowest_prime), InfoLevel.WARNING)
            min_pt.setText("1")
            numMin = 1L
            minValue = 1L
        }

        var maxValue = maxString.toLongOrNull(10)
        if (maxValue == null) {
            showCustomToast(context, getString(R.string.highest_is_high), InfoLevel.WARNING)
            return
        } else if (maxValue < 1) {
            showCustomToast(context, getString(R.string.lowest_prime), InfoLevel.WARNING)
            max_pt.setText("1")
            numMax = 1L
            maxValue = 1L
        }

        if (minValue > maxValue) {
            val swapp = minValue
            minValue = maxValue
            maxValue = swapp
            min_pt.setText(minValue.toString())
            max_pt.setText(maxValue.toString())
            makePrimesTable()
            return
        }

        BG_Operation = LongOperation().execute(minValue, maxValue)

    }

    private fun getPrimes(num_min: Int, num_max: Int): ArrayList<String> {
        val primes = ArrayList<String>()
        for (i in num_min..num_max) {
            var isPrime = true
            if (i % 2 == 0) {
                isPrime = false
            }
            if (isPrime) {
                var j = 3
                while (j < i) {
                    if (i % j == 0) {
                        isPrime = false
                        break
                    }
                    j += 2
                }
            }

            if (isPrime) {
                primes.add(Integer.toString(i))
            }

        }
        return primes
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAsyncTask(BG_Operation, context)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        //Quando altera a orientação do ecrã
        if (tableData != null) {
            val display = activity!!.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x  //int height = size.y;
            val min_num_length = tableData!![tableData!!.size - 1].length
            val scale = resources.displayMetrics.density
            val num_length = min_num_length * (18 * scale + 0.5f).toInt() + 8
            val num_columns = Math.round((width / num_length).toFloat())
            historyGridView.numColumns = num_columns
            val lr_dip = (4 * scale + 0.5f).toInt() * 2
            cv_width = width - lr_dip
        }
        hideKeyboard(activity as Activity)

    }

    private fun resetButtons() {
        progressBar.visibility = View.GONE
        cancelButton.visibility = View.GONE
        createTableBtn.isClickable = true
        createTableBtn.setText(R.string.gerar)
    }

    inner class LongOperation : AsyncTask<Long, Double, ArrayList<String>>() {
        private var startTime = System.nanoTime()

        public override fun onPreExecute() {
            createTableBtn.isClickable = false
            createTableBtn.text = getString(R.string.working)
            cancelButton.visibility = View.VISIBLE
            hideKeyboard(activity as Activity)
            cv_width = cardViewMain.width
            val scale = activity!!.resources.displayMetrics.density
            height_dip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(10, height_dip)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Long?): ArrayList<String> {
            val numMin = params[0] as Long
            val numMax = params[1] as Long

            full_table = ArrayList()

            for (i in numMin..numMax) {
                full_table!!.add(i.toString())
            }
            val primes = ArrayList<String>()
            var progress: Double
            var oldProgress = 0.0
            var min = numMin
            if (min == 1L) min = 2L
            if (min == 2L) {
                primes.add("2")
                min = 3L
            }

            for (i in min..numMax) {
                var isPrime = true
                if (i % 2 == 0L) isPrime = false
                if (isPrime) {
                    var j: Long = 3
                    while (j < i) {
                        if (i % j == 0L) {
                            isPrime = false
                            break
                        }
                        j += 2
                    }
                }
                if (isPrime) {
                    primes.add(toString(i))
                }
                progress = i.toDouble() / numMax.toDouble()
                if (progress - oldProgress > 0.05) {
                    publishProgress(progress)
                    oldProgress = progress
                }
                if (isCancelled) break
            }
            return primes
        }

        override fun onProgressUpdate(vararg values: Double?) {
            if (this@PrimesTableFragment.isVisible) {
                val progress: Double = values[0] ?: 0.0
                progressBar.layoutParams =
                    LinearLayout.LayoutParams(
                        Math.round(progress * cv_width).toInt(),
                        height_dip
                    )
            }
        }

        override fun onPostExecute(result: ArrayList<String>) {
            tableData = result
            if (this@PrimesTableFragment.isVisible) {
                val display = activity!!.windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val width = size.x  //int height = size.y;
                //int max_num_length = result.get(result.size() - 1).length();
                val max_num_length = numMax.toString().length
                val scale = resources.displayMetrics.density
                val num_length = max_num_length * (18 * scale + 0.5f).toInt() + 8
                val num_columns = Math.round((width / num_length).toFloat())
                historyGridView.numColumns = num_columns

                if (checkboxChecked!!) {
                    historyGridView.adapter = object :
                        ArrayAdapter<String>(
                            activity!!,
                            R.layout.table_item,
                            R.id.tableItem,
                            full_table!!
                        ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getView(position, convertView, parent)
                            view as CardView
                            if (tableData!!.contains(full_table!![position])) { // Se o número for primo
                                view.setCardBackgroundColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.gridcolor
                                    )
                                )
                            } else {
                                view.setCardBackgroundColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.white
                                    )
                                )
                            }
                            return view
                        }
                    }
                } else {
                    val primes_adapter =
                        ArrayAdapter(activity!!, R.layout.table_item, R.id.tableItem, result)
                    historyGridView.adapter = primes_adapter
                }
                numPrimesTextView.visibility = View.VISIBLE
                numPrimesTextView.text = "${getString(R.string.cardinal_primos)} ${result.size}"
                if (result.size == 0) {
                    showCustomToast(context, getString(R.string.no_primes_range))
                } else {
                    showCustomToast(context, getString(R.string.existem) + " " + result.size + " " + getString(R.string.primes_in_range))
                }
                showPerformance()
                resetButtons()
            }

        }

        private fun showPerformance() {
            val shouldShowPerformance = sharedPrefs.getBoolean("pref_show_performance", false)
            if (shouldShowPerformance) {
                val decimalFormatter = DecimalFormat("#.###")
                val elapsed =
                    " " + decimalFormatter.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
                performanceTextView.visibility = View.VISIBLE
                performanceTextView.text = getString(R.string.performance) + " " + elapsed
            } else {
                performanceTextView.visibility = View.GONE
            }
        }

        override fun onCancelled(parcial: ArrayList<String>) { //resultado parcial obtido após cancelar AsyncTask
            super.onCancelled(parcial)
            tableData = parcial
            if (this@PrimesTableFragment.isVisible && parcial.size > 0) {
                val display = activity!!.windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val width = size.x  //int height = size.y;
                val max_num_length = parcial[parcial.size - 1].length
                val scale = resources.displayMetrics.density
                val num_length = max_num_length * (18 * scale + 0.5f).toInt() + 8
                val num_columns = Math.round((width / num_length).toFloat())
                historyGridView.numColumns = num_columns

                if (checkboxChecked!!) {
                    historyGridView.adapter = object :
                        ArrayAdapter<String>(
                            activity!!,
                            R.layout.table_item,
                            R.id.tableItem,
                            full_table!!
                        ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getView(position, convertView, parent)
                            if (tableData!!.contains(full_table!![position])) { //Se o número for primo
                                (view as CardView).setCardBackgroundColor(Color.parseColor("#9769bc4d"))
                            } else {
                                (view as CardView).setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                            }
                            return view
                        }
                    }
                } else {
                    val primes_adapter =
                        ArrayAdapter(activity!!, R.layout.table_item, R.id.tableItem, parcial)
                    historyGridView.adapter = primes_adapter
                }
                Toast.makeText(
                    context,
                    getString(R.string.found) + " " + parcial.size + " " + getString(R.string.primes_in_range),
                    Toast.LENGTH_LONG
                ).show()
                numPrimesTextView.visibility = View.VISIBLE
                numPrimesTextView.text = "${getString(R.string.cardinal_primos)} (${parcial.size})"
                showPerformance()
                resetButtons()
            } else if (parcial.size == 0) {
                showCustomToast(context, getString(R.string.canceled_noprimes))
                historyGridView.adapter = null
                resetButtons()
            }

        }
    }
}
















