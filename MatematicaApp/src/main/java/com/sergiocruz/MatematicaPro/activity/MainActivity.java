package com.sergiocruz.MatematicaPro.activity;

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
import android.view.MenuItem;
import android.view.View;

import com.sergiocruz.MatematicaPro.R;
import com.sergiocruz.MatematicaPro.fragment.DivisoresFragment;
import com.sergiocruz.MatematicaPro.fragment.FatorizarFragment;
import com.sergiocruz.MatematicaPro.fragment.HomeFragment;
import com.sergiocruz.MatematicaPro.fragment.MDCFragment;
import com.sergiocruz.MatematicaPro.fragment.MMCFragment;
import com.sergiocruz.MatematicaPro.fragment.MultiplosFragment;
import com.sergiocruz.MatematicaPro.fragment.PrimesTableFragment;
import com.sergiocruz.MatematicaPro.fragment.PrimorialFragment;

import static com.sergiocruz.MatematicaPro.helper.MenuHelper.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity {
    // tags used to attach the fragments
    private static final String[] FRAGMENT_TAGS = {"home", "mmc", "mdc", "fatorizar", "divisores", "primes_table", "multiplos", "primorial"};
    private static final Fragment[] FRAGMENTS = {new HomeFragment(), new MMCFragment(), new MDCFragment(), new FatorizarFragment(), new DivisoresFragment(), new PrimesTableFragment(), new MultiplosFragment(), new PrimorialFragment()};
    // index to identify current nav menu item
    public static int navItemIndex = 0;
    Fragment mContent;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;
    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

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
            loadFragment(0);
        } else if (savedInstanceState != null) {
            //Restore the fragment's instance
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        }
        verifyStoragePermissions(this);
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
    private void loadFragment(int item_index) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);

        navItemIndex = item_index;

        // selecting appropriate nav menu item
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
        navigationView.getMenu().getItem(navItemIndex).setActionView(R.layout.menu_dot);

        // set toolbar title
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAGS[navItemIndex]) != null) {
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
                Fragment fragment = FRAGMENTS[navItemIndex];
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, FRAGMENT_TAGS[navItemIndex]);
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
                        break;
                    case R.id.nav_mmc:
                        navItemIndex = 1;
                        break;
                    case R.id.nav_mdc:
                        navItemIndex = 2;
                        break;
                    case R.id.nav_fatorizar:
                        navItemIndex = 3;
                        break;
                    case R.id.nav_divisores:
                        navItemIndex = 4;
                        break;
                    case R.id.nav_prime_table:
                        navItemIndex = 5;
                        break;
                    case R.id.nav_multiplos:
                        navItemIndex = 6;
                        break;
                    case R.id.nav_primorial:
                        navItemIndex = 7;
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
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadFragment(navItemIndex);

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
        drawer.addDrawerListener(actionBarDrawerToggle);

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
                loadFragment(0);
                return;
            }
        }

        super.onBackPressed();
    }

    public void mmc(View view) {
        loadFragment(1);
    }

    public void mdc(View view) {
        loadFragment(2);
    }

    public void fatorizar(View view) {
        loadFragment(3);
    }

    public void divisores(View view) {
        loadFragment(4);
    }

    public void primes_table(View view) {
        loadFragment(5);
    }

    public void multiplos(View view) {
        loadFragment(6);
    }

    public void primorial(View view) {
        loadFragment(7);
    }

}
