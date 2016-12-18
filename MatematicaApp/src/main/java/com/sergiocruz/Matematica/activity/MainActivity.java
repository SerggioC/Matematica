package com.sergiocruz.Matematica.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.fragment.DivisoresFragment;
import com.sergiocruz.Matematica.fragment.FatorizarFragment;
import com.sergiocruz.Matematica.fragment.HomeFragment;
import com.sergiocruz.Matematica.fragment.MDCFragment;
import com.sergiocruz.Matematica.fragment.MMCFragment;
import com.sergiocruz.Matematica.fragment.PrimesTableFragment;

public class MainActivity extends AppCompatActivity {

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_MMC = "mmc";
    private static final String TAG_MDC = "mdc";
    private static final String TAG_FATORIZAR = "fatorizar";
    private static final String TAG_DIVISORES = "divisores";
    private static final String TAG_PRIMES_TABLE = "primes_table";
    // index to identify current nav menu item
    public static int navItemIndex = 0;

    public static String CURRENT_TAG = TAG_HOME;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    Fragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Icones coloridos no menu de gaveta lateral
        navigationView.setItemIconTintList(null);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadFragment();
            navigationView.getMenu().getItem(navItemIndex).setActionView(R.layout.menu_dot);
        } else if (savedInstanceState != null) {
            //Restore the fragment's instance
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        //getSupportFragmentManager().putFragment(outState, "mContent", mContent);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                // photos
                MMCFragment MMCFragment = new MMCFragment();
                return MMCFragment;
            case 2:
                // movies fragment
                MDCFragment MDCFragment = new MDCFragment();
                return MDCFragment;
            case 3:
                // Fragment Fatorizar em números primos
                FatorizarFragment fatorizarFragment = new FatorizarFragment();
                return fatorizarFragment;
            case 4:
                // Fragment Divisores
                DivisoresFragment divisoresFragment = new DivisoresFragment();
                return divisoresFragment;
            case 5:
                // Fragment Tabela de números Primos
                PrimesTableFragment primesTableFragment = new PrimesTableFragment();
                return primesTableFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
        navigationView.getMenu().getItem(navItemIndex).setActionView(R.layout.menu_dot);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //remove dot in menu
                navigationView.getMenu().getItem(navItemIndex).setActionView(null);

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_mmc:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_MMC;
                        break;
                    case R.id.nav_mdc:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_MDC;
                        break;
                    case R.id.nav_fatorizar:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_FATORIZAR;
                        break;
                    case R.id.nav_divisores:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_DIVISORES;
                        break;
                    case R.id.nav_prime_table:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_PRIMES_TABLE;
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_about:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_send:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, SendMailActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        //remove dot from selected item menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
//        if (navItemIndex == 0) {
//            getMenuInflater().inflate(R.menu.main, menu);
//        }
/*
        // if fragment is fatorizar, load the menu
        if (navItemIndex == 3 || navItemIndex == 4) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_buy) {
//            Toast.makeText(getApplicationContext(), "Comprar versão PRO para remover anúncios", Toast.LENGTH_LONG).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void mmc(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 1;
        CURRENT_TAG = TAG_MMC;
        loadFragment();
    }

    public void mdc(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 2;
        CURRENT_TAG = TAG_MDC;
        loadFragment();
    }

    public void fatorizar(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 3;
        CURRENT_TAG = TAG_FATORIZAR;
        loadFragment();
    }

    public void divisores(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 4;
        CURRENT_TAG = TAG_DIVISORES;
        loadFragment();
    }

    public void primes_table(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 5;
        CURRENT_TAG = TAG_PRIMES_TABLE;
        loadFragment();
    }


}
