package com.sergiocruz.MatematicaPro.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.allPermissionsGranted
import com.sergiocruz.MatematicaPro.fragment.*
import com.sergiocruz.MatematicaPro.getRuntimePermissions
import com.sergiocruz.MatematicaPro.helper.openSettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var activityTitles: Array<String>
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mHandler = Handler()

        // load toolbar titles from string resources
        activityTitles = resources.getStringArray(R.array.nav_item_activity_titles)

        // initializing navigation menu
        setUpNavigationView()

        if (allPermissionsGranted(this).not()) getRuntimePermissions(this)

        when (savedInstanceState) {
            null -> loadFragment(HomeFragment())
            else -> return
        }

    }

    interface PermissionResultInterface {
        fun onPermissionResult(permitted: Boolean)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResultInterface?.onPermissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private fun loadFragment(fragment: BaseFragment) {
        //remove dot in menu
        nav_view.menu.getItem(getCurrentFragmentIndex()).actionView = null

        // selecting appropriate nav menu item
        nav_view.menu.getItem(fragment.pageIndex).isChecked = true
        nav_view.menu.getItem(fragment.pageIndex).setActionView(R.layout.menu_dot)

        // set toolbar title
        toolbarTitle.setText(fragment.title)

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer_layout
        if (supportFragmentManager.fragments.getOrNull(0)?.tag == fragment::class.java.simpleName) {
            drawer_layout.closeDrawers()
            return
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        mHandler.post {
            // update the main content by replacing fragments
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.frame, fragment, fragment::class.java.simpleName)
            fragmentTransaction.commitAllowingStateLoss()
        }

        //Closing drawer_layout on item click
        drawer_layout.closeDrawers()

        // refresh toolbar menu
        invalidateOptionsMenu()
    }

    private fun getCurrentFragmentIndex(): Int {
        val fragmentList = supportFragmentManager.fragments
        val stackSize = fragmentList.size
        return (fragmentList.getOrNull(stackSize - 1) as? BaseFragment)?.pageIndex ?: 0
    }


    private fun goToSettingsFragment() {
        nav_view.menu.getItem(getCurrentFragmentIndex()).actionView = null
        nav_view.menu.getItem(SettingsFragment.index).isChecked = true
        nav_view.menu.getItem(SettingsFragment.index).setActionView(R.layout.menu_dot)
        openSettingsFragment()
        drawer_layout.closeDrawers()
    }

    private fun setUpNavigationView() {

        // Icones coloridos no menu de gaveta lateral
        nav_view.itemIconTintList = null

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        // This method will trigger on item Click of navigation menu
        nav_view.setNavigationItemSelectedListener { menuItem ->

            //remove dot in menu
            nav_view.menu.getItem(getCurrentFragmentIndex()).actionView = null

            when (menuItem.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.nav_primality -> loadFragment(PrimalityFragment())
                R.id.nav_mmc -> loadFragment(MMCFragment())
                R.id.nav_mdc -> loadFragment(MDCFragment())
                R.id.nav_fatorizar -> loadFragment(FatorizarFragment())
                R.id.nav_divisores -> loadFragment(DivisoresFragment())
                R.id.nav_prime_table -> loadFragment(PrimesTableFragment())
                R.id.nav_multiplos -> loadFragment(MultiplosFragment())
                R.id.nav_primorial -> loadFragment(PrimorialFragment())
                R.id.nav_settings -> goToSettingsFragment()
                R.id.nav_about -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.nav_send -> startActivity(Intent(this@MainActivity, SendMailActivity::class.java))
                else -> loadFragment(HomeFragment())
            }

            //Checking if the item is in checked state or not, if not make it in checked state
            menuItem.isChecked = menuItem.isChecked.not()

            true
        }

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.openDrawer, R.string.closeDrawer)

        //Setting the actionbarToggle to drawer_layout layout
        drawer_layout.addDrawerListener(actionBarDrawerToggle)

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawers()
            return
        }

        //remove dot from selected item menu
        nav_view.menu.getItem(getCurrentFragmentIndex()).actionView = null

        // checking if user is on other navigation menu
        // rather than home
        if (getCurrentFragmentIndex() != 0) {
            loadFragment(HomeFragment())
            return
        }

        super.onBackPressed()
    }

    fun primality(view: View) = loadFragment(PrimalityFragment())

    fun mmc(view: View) = loadFragment(MMCFragment())

    fun mdc(view: View) = loadFragment(MDCFragment())

    fun fatorizar(view: View) = loadFragment(FatorizarFragment())

    fun divisores(view: View) = loadFragment(DivisoresFragment())

    fun primesTable(view: View) = loadFragment(PrimesTableFragment())

    fun multiplos(view: View) = loadFragment(MultiplosFragment())

    fun primorial(view: View) = loadFragment(PrimorialFragment())

    companion object {
        var permissionResultInterface: PermissionResultInterface? = null
    }

}

