package com.sergiocruz.MatematicaPro.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.activity.AboutActivity
import com.sergiocruz.MatematicaPro.activity.SettingsActivity
import com.sergiocruz.MatematicaPro.helper.MenuHelper.*
import com.sergiocruz.MatematicaPro.helper.OnCancelBackgroundTask
import com.sergiocruz.MatematicaPro.helper.displayCancelDialogBox
import kotlinx.android.synthetic.main.fragment_primes_table.*
import java.lang.Long.parseLong
import java.lang.Long.toString
import java.text.DecimalFormat
import java.util.*

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.fragment
 * Created by Sergio on 11/11/2016 16:31
 */

class PrimesTableFragment : Fragment(), OnCancelBackgroundTask {

    private var BG_Operation: AsyncTask<Long, Double, ArrayList<String>> = LongOperation()
    var tableData: ArrayList<String>? = null
    internal var cv_width: Int = 0
    internal var height_dip: Int = 0
    var num_min: Long = 0L
    var num_max: Long = 50L
    internal var checkboxChecked: Boolean? = true
    internal var full_table: ArrayList<String>? = null
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // have a menu in this fragment
        setHasOptionsMenu(true)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_primes_table, menu)
        inflater.inflate(R.menu.menu_sub_main, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_primes_table, container, false)

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
                    val primes_adapter =
                        ArrayAdapter(activity!!, R.layout.table_item, R.id.tableItem, tableData!!)
                    historyGridView.adapter = primes_adapter
                }
            }
        }

        cancelTask.setOnClickListener { displayCancelDialogBox(this.context!!, this) }

        createTableBtn.setOnClickListener { makePrimesTable(view) }

        btn_clear_min.setOnClickListener { min_pt.setText("") }

        btn_clear_max.setOnClickListener { max_pt.setText("") }

        min_pt.addTextChangedListener(object : TextWatcher {
            var num1: Long? = null
            var oldnum1: String? = null

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                oldnum1 = s.toString()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) return

                try {
                    // Tentar converter o string para long
                    num1 = parseLong(s.toString())
                } catch (e: Exception) {
                    min_pt.setText(oldnum1)
                    min_pt.setSelection(min_pt.text.length) //Colocar o cursor no final do texto

                    showCenterToast(R.string.lowest_is_high)
                }

            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        max_pt.addTextChangedListener(object : TextWatcher {
            var num2: Long? = null
            var oldnum2: String? = null

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                oldnum2 = s.toString()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) return

                try {
                    // Tentar converter o string para long
                    num2 = parseLong(s.toString())
                } catch (e: Exception) {
                    max_pt.setText(oldnum2)
                    max_pt.setSelection(max_pt.text.length) //Colocar o cursor no final do texto
                    showCenterToast(R.string.highest_is_high)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        max_pt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) makePrimesTable(view)
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        val childCount = historyGridView.childCount
        if (id == R.id.action_save_image_pt) {
            verifyStoragePermissions(activity)
            if (childCount > 0) {
                val img_path = saveViewToImage(historyGridView, 0, true)
                if (img_path != null) {
                    openFolder_Snackbar(activity!!, getString(R.string.image_saved))
                } else {
                    val thetoast =
                        Toast.makeText(context, R.string.errorsavingimg, Toast.LENGTH_SHORT)
                    thetoast.setGravity(Gravity.CENTER, 0, 0)
                    thetoast.show()
                }
            } else {
                val theToast = Toast.makeText(context, R.string.empty_table, Toast.LENGTH_SHORT)
                theToast.setGravity(Gravity.CENTER, 0, 0)
                theToast.show()
            }
        }
        if (id == R.id.action_share_history_image_pt) {
            verifyStoragePermissions(activity)
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
                    val theToast =
                        Toast.makeText(context, R.string.errorsavingimg, Toast.LENGTH_SHORT)
                    theToast.setGravity(Gravity.CENTER, 0, 0)
                    theToast.show()
                }
            } else {
                val thetoast = Toast.makeText(context, R.string.empty_table, Toast.LENGTH_SHORT)
                thetoast.setGravity(Gravity.CENTER, 0, 0)
                thetoast.show()

            }
        }
        // Partilhar tabela como texto
        if (id == R.id.action_share_history) {
            if (tableData != null) {
                val primes_string =
                    getString(R.string.table_between) + " " + num_min + " " + getString(R.string.and) + " " + num_max + ":\n" + tableData
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.app_long_description) +
                            BuildConfig.VERSION_NAME + "\n" + primes_string
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            } else {
                val theToast = Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT)
                theToast.setGravity(Gravity.CENTER, 0, 0)
                theToast.show()
            }
        }

        if (id == R.id.action_clear_all_history) {
            historyGridView.adapter = null
            tableData = null
            num_min = 0L
            num_max = 50L
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
        cancelAsyncTask()
    }

    private fun makePrimesTable(view: View) {
        val min_string = min_pt.text.toString().replace("[^\\d]".toRegex(), "")
        val max_string = max_pt.text.toString().replace("[^\\d]".toRegex(), "")

        if (TextUtils.isEmpty(min_string) || TextUtils.isEmpty(min_string)) {
            showCenterToast(R.string.fill_min_max)
            return
        }

        try {
            // Tentar converter o string do valor mínimo para Long 2^63-1
            num_min = parseLong(min_string)
            if (num_min < 1L) {
                showCenterToast(R.string.lowest_prime)
                min_pt.setText("1")
                num_min = 1L
            }
        } catch (e: Exception) {
            showCenterToast(R.string.lowest_is_high)
            return
        }

        try {
            // Tentar converter o string do valor mínimo para Long 2^63-1
            num_max = parseLong(max_string)
            if (num_max < 1L) {
                showCenterToast(R.string.lowest_prime)
                max_pt.setText("1")
                num_max = 2L
            }
        } catch (e: Exception) {
            showCenterToast(R.string.highest_is_high)
            return
        }


        if (num_min > num_max) {
            val swapp = num_min
            num_min = num_max
            num_max = swapp
            min_pt.setText(num_min.toString())
            max_pt.setText(num_max.toString())
            makePrimesTable(view)
        }

        BG_Operation = LongOperation().execute(num_min, num_max)

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

        if (BG_Operation.status == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true)
            showCenterToast(R.string.canceled_op)
        }
    }


    private fun cancelAsyncTask() {
        if (BG_Operation.status == AsyncTask.Status.RUNNING) {
            BG_Operation.cancel(true)
            showCenterToast(R.string.canceled_op)
            resetButtons()
        }
    }

    private fun showCenterToast(@StringRes resId: Int) {
        val theToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT)
        theToast.setGravity(Gravity.CENTER, 0, 0)
        theToast.show()
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
        hideKeyboard()
    }

    fun hideKeyboard() {
        //Hide the keyboard
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    private fun resetButtons() {
        progressBar.visibility = View.GONE
        cancelTask.visibility = View.GONE
        createTableBtn.isClickable = true
        createTableBtn.setText(R.string.gerar)
    }

    inner class LongOperation : AsyncTask<Long, Double, ArrayList<String>>() {
        private var startTime = System.nanoTime()

        public override fun onPreExecute() {
            createTableBtn.isClickable = false
            createTableBtn.text = getString(R.string.working)
            cancelTask.visibility = View.VISIBLE
            hideKeyboard()
            cv_width = cardViewMain.width
            val scale = activity!!.resources.displayMetrics.density
            height_dip = (4 * scale + 0.5f).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(10, height_dip)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Long?): ArrayList<String> {
            num_min = params[0] as Long
            num_max = params[1] as Long

            full_table = ArrayList()

            for (i in num_min..num_max) {
                full_table!!.add(i.toString())
            }
            val primes = ArrayList<String>()
            var progress: Double
            var oldProgress = 0.0
            var min = num_min
            if (min == 1L) min = 2L
            if (min == 2L) {
                primes.add("2")
                min = 3L
            }

            for (i in min..num_max) {
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
                progress = i.toDouble() / num_max.toDouble()
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
                val max_num_length = num_max.toString().length
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
                                view.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.gridcolor))
                            } else {
                                view.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.white))
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
                    showCenterToast(R.string.no_primes_range)
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.existem) + " " + result.size + " " + getString(R.string.primes_in_range),
                        Toast.LENGTH_LONG
                    ).show()
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
                numPrimesTextView.text = getString(R.string.cardinal_primos) + " (" + parcial.size +
                        ")"
                showPerformance()
                resetButtons()
            } else if (parcial.size == 0) {
                showCenterToast(R.string.canceled_noprimes)
                historyGridView.adapter = null
                resetButtons()
            }

        }
    }
}
















