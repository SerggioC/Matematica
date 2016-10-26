package com.sergiocruz.Matematica.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sergiocruz.Matematica.R;
import com.sergiocruz.Matematica.fragment.DivisoresFragment;
import com.sergiocruz.Matematica.fragment.FatorizarFragment;
import com.sergiocruz.Matematica.fragment.HomeFragment;
import com.sergiocruz.Matematica.fragment.MoviesFragment;
import com.sergiocruz.Matematica.fragment.PhotosFragment;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
//    private ImageView imgNavHeaderBg, imgProfile;
//    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    // urls to load navigation header background image
    // and profile image
//    private static final String urlNavHeaderBg = "http://api.androidhive.info/images/nav-menu-header-bg.jpg";
//    private static final String urlProfileImg = "https://lh3.googleusercontent.com/eCtE_G34M9ygdkmOpYvCag1vBARCmZwnVS6rS5t4JLzJ6QgQSBquM0nuTsCpLhYbKljoyS-txg";

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    public static int navItemIndexOld = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_MMC = "mmc";
    private static final String TAG_MDC = "mdc";
    private static final String TAG_FATORIZAR = "fatorizar";
    private static final String TAG_DIVISORES = "divisores";
    private static final String TAG_NDIVISORES = "ndivisores";
    private static final String TAG_PRIMOS_SI = "primos_si";
    public static String CURRENT_TAG = TAG_HOME;

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
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Icones coloridos no menu de gaveta lateral
        navigationView.setItemIconTintList(null);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
//        txtName = (TextView) navHeader.findViewById(R.id.name);
//        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
//        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
//        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SendMailActivity.class));
//                Snackbar.make(view, "Send me an e-mail :)", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        // load nav menu header data
       // loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadFragment();
            navigationView.getMenu().getItem(navItemIndex).setActionView(R.layout.menu_dot);
        }
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
//        txtName.setText("Matemática dos Números Naturais");
//        txtName.setTextColor(getResources().getColor(R.color.topDrawerTextColor));
//        txtWebsite.setText("www.sergio.cruz.com\nsergiocrz@gmail.com");
//        //txtWebsite.setTextColor(Color.parseColor("#000000"));
//        txtWebsite.setTextColor(getResources().getColor(R.color.topDrawerTextColor));
        /*

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);
*/

        // showing dot next to home label
        navigationView.getMenu().getItem(0).setActionView(R.layout.menu_dot);
        //logMe(this, "debug", "Loading NavHeader" );
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

            // show or hide the fab button
            toggleFab();
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
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

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
                PhotosFragment photosFragment = new PhotosFragment();
                return photosFragment;
            case 2:
                // movies fragment
                MoviesFragment moviesFragment = new MoviesFragment();
                return moviesFragment;
            case 3:
                // Fragment Fatorizar em números primos
                FatorizarFragment fatorizarFragment = new FatorizarFragment();
                return fatorizarFragment;
            case 4:
                // Fragment Divisores
                DivisoresFragment divisoresFragment = new DivisoresFragment();
                return divisoresFragment;
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

    public final static void logMe(Object obj, String level, String str) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        final String ltag = "Sergio >>> " + str + " :";
        final String msg = " Class: " + obj.getClass().getSimpleName() +
                " / Method: " + ste[3].getMethodName() +
                " / Invoked by: " + ste[4].getMethodName();
        switch (level) {
            case "verbose": Log.v(ltag, msg); break;
            case "debug": Log.d(ltag, msg);  break;
            case "info": Log.i(ltag, msg); break;
            case "warn": Log.w(ltag, msg); break;
            case "error": Log.e(ltag, msg);  break;
            case "assert": Log.wtf(ltag, msg); break;
            default: Log.w(ltag, msg); break;
        }
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
                        logMe(this, "debug", "GOING TO TAG_HOME" );
                        break;
                    case R.id.nav_mmc:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_MMC;
                        logMe(this, "debug", "GOING To MMC" );
                        break;
                    case R.id.nav_mdc:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_MDC;
                        logMe(this, "debug", "GOING TO TAG_MDC" );
                        break;
                    case R.id.nav_fatorizar:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_FATORIZAR;
                        logMe(this, "debug", "GOING TO TAG_FATORIZAR" );
                        break;
                    case R.id.nav_divisores:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_DIVISORES;
                        logMe(this, "debug", "GOING TO TAG_DIVISORES" );
                        break;
                    case R.id.nav_num_divisors:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_NDIVISORES;
                        logMe(this, "debug", "GOING TO TAG_NDIVISORES" );
                        break;
                    case R.id.nav_primos_si:
                        navItemIndex = 6;
                        CURRENT_TAG = TAG_PRIMOS_SI;
                        logMe(this, "debug", "GOING TO TAG_PRIMOS_SI" );
                        break;
                    case R.id.nav_about:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        logMe(this, "debug", "GOING TO nav_about" );

                        return true;
                    case R.id.nav_send:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, SendMailActivity.class));
                        drawer.closeDrawers();
                        logMe(this, "debug", "GOING TO nav_send" );
                        return true;
                    case R.id.nav_share:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, ShareActivity.class));
                        drawer.closeDrawers();
                        logMe(this, "debug", "GOING TO nav_share" );
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
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        // when fragment is notifications, load the menu created for notifications
        if (navItemIndex == 3) {
            getMenuInflater().inflate(R.menu.notifications, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
            return true;
        }

        // user is in notifications fragment
        // and selected 'Mark all as Read'
        if (id == R.id.action_mark_all_read) {
            Toast.makeText(getApplicationContext(), "All notifications marked as read!", Toast.LENGTH_LONG).show();
        }

        // user is in notifications fragment
        // and selected 'Clear All'
        if (id == R.id.action_clear_notifications) {
            Toast.makeText(getApplicationContext(), "Clear all notifications!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    // show or hide the fab
    private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
    }

    public void divisores(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 4;
        CURRENT_TAG = TAG_DIVISORES;
        loadFragment();
    }
    public void fatorizar(View view) {
        //remove dot in menu
        navigationView.getMenu().getItem(navItemIndex).setActionView(null);
        navItemIndex = 3;
        CURRENT_TAG = TAG_FATORIZAR;
        loadFragment();
    }

}
