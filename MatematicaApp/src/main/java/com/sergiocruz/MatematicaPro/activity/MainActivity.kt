package com.sergiocruz.MatematicaPro.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.fragment.*
import com.sergiocruz.MatematicaPro.helper.MenuHelper.Companion.verifyStoragePermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity() {
    // toolbar titles respected to selected nav menu item
    private lateinit var activityTitles: Array<String>
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mHandler = Handler()

        // load toolbar titles from string resources
        activityTitles = resources.getStringArray(R.array.nav_item_activity_titles)

        // initializing navigation menu
        setUpNavigationView()

        if (savedInstanceState == null) {
            loadFragment(0)
        } else {
            return
        }
        verifyStoragePermissions(this)
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private fun loadFragment(index: Int) {
        //remove dot in menu
        nav_view.menu.getItem(navItemIndex).actionView = null

        navItemIndex = index

        // selecting appropriate nav menu item
        nav_view.menu.getItem(index).isChecked = true
        nav_view.menu.getItem(index).setActionView(R.layout.menu_dot)

        // set toolbar title
        supportActionBar?.title = activityTitles[index]

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer_layout
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAGS[index]) != null) {
            drawer_layout.closeDrawers()
            return
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app

        mHandler.post {
            // update the main content by replacing fragments
            val fragment = FRAGMENTS[index]
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            fragmentTransaction.replace(R.id.frame, fragment, FRAGMENT_TAGS[index])
            fragmentTransaction.commitAllowingStateLoss()
        }


        //Closing drawer_layout on item click
        drawer_layout.closeDrawers()

        // refresh toolbar menu
        invalidateOptionsMenu()
    }


    private fun setUpNavigationView() {

        // Icones coloridos no menu de gaveta lateral
        nav_view.itemIconTintList = null

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        // This method will trigger on item Click of navigation menu
        nav_view.setNavigationItemSelectedListener { menuItem ->

            //remove dot in menu
            nav_view.menu.getItem(navItemIndex).actionView = null

            //Check to see which item was being clicked and perform appropriate action
            when (menuItem.itemId) {
                //Replacing the main content with ContentFragment Which is our Inbox View;
                R.id.home -> navItemIndex = 0
                R.id.nav_primality -> navItemIndex = 1
                R.id.nav_mmc -> navItemIndex = 2
                R.id.nav_mdc -> navItemIndex = 3
                R.id.nav_fatorizar -> navItemIndex = 4
                R.id.nav_divisores -> navItemIndex = 5
                R.id.nav_prime_table -> navItemIndex = 6
                R.id.nav_multiplos -> navItemIndex = 7
                R.id.nav_primorial -> navItemIndex = 8
                R.id.nav_settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    drawer_layout.closeDrawers()
                }
                R.id.nav_about -> {
                    // launch new intent instead of loading fragment
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                    drawer_layout.closeDrawers()
                }
                R.id.nav_send -> {
                    // launch new intent instead of loading fragment
                    startActivity(Intent(this@MainActivity, SendMailActivity::class.java))
                    drawer_layout.closeDrawers()
                }
                else -> navItemIndex = 0
            }

            //Checking if the item is in checked state or not, if not make it in checked state
            menuItem.isChecked = menuItem.isChecked.not()

            loadFragment(navItemIndex)

            true
        }

        val actionBarDrawerToggle =
            ActionBarDrawerToggle(
                this,
                drawer_layout, toolbar, R.string.openDrawer, R.string.closeDrawer
            )

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
        nav_view.menu.getItem(navItemIndex).actionView = null

        // checking if user is on other navigation menu
        // rather than home
        if (navItemIndex != 0) {
            loadFragment(0)
            return
        }

        super.onBackPressed()
    }

    fun primality(view: View) {
        loadFragment(1)
    }

    fun mmc(view: View) {
        loadFragment(2)
    }

    fun mdc(view: View) {
        loadFragment(3)
    }

    fun fatorizar(view: View) {
        loadFragment(4)
    }

    fun divisores(view: View) {
        loadFragment(5)
    }

    fun primesTable(view: View) {
        loadFragment(6)
    }

    fun multiplos(view: View) {
        loadFragment(7)
    }

    fun primorial(view: View) {
        loadFragment(8)
    }

    companion object {
        // tags used to attach the fragments
        private val FRAGMENT_TAGS = arrayOf(
            "home",
            "primality",
            "mmc",
            "mdc",
            "fatorizar",
            "divisores",
            "primesTable",
            "multiplos",
            "primorial"
        )
        private val FRAGMENTS = arrayOf(
            HomeFragment(),
            PrimalityFragment(),
            MMCFragment(),
            MDCFragment(),
            FatorizarFragment(),
            DivisoresFragment(),
            PrimesTableFragment(),
            MultiplosFragment(),
            PrimorialFragment()
        )
        // index to identify current nav menu item
        var navItemIndex = 0
    }

}
