package com.christian;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.christian.base.BaseActivity;
import com.christian.base.BaseFragment;
import com.christian.gospel.GospelFragment;
import com.christian.home.HomeFragment;
import com.christian.me.MeFragment;
import com.christian.swipeback.SwipeBackHelper;
import com.christian.view.CustomViewPage;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

@ContentView(R.layout.navigation_act)
public class NavigationActivity extends BaseActivity {
    private static String TAG = NavigationActivity.class.getSimpleName();
    @ViewInject(R.id.navigation)
    private BottomNavigationView mBottomNavigationView;
    @ViewInject(R.id.content_view_page)
    private CustomViewPage mCustomViewPager;
    private long exitTime = 0;
    MenuItem mPrevMenuItem;
    private static final int DEFAULT_OFFSCREEN_PAGES = 0;
    private HomeFragment mHomeFragment;
    private GospelFragment mGospelFragment;
    private MeFragment mMeFragment;

    public enum ChristianTab {
        NAVIGATION_HOME, NAVIGATION_BOOK, NAVIGATION_MUSIC, NAVIGATION_ME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        SwipeBackHelper.getCurrentPage(this).setDisallowInterceptTouchEvent(true);
        initView();
        initListener();
        // Initialize the load
        if (savedInstanceState == null) {
            mCustomViewPager.setCurrentItem(ChristianTab.NAVIGATION_HOME.ordinal());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // To remain 4 tabs fragments
        mCustomViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);
//        NavigationViewHelper.disableShiftMode(mBottomNavigationView);
    }

    private void initView() {
    }

    private void initListener() {
        mBottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch ((item.getItemId())) {
                    case R.id.navigation_home:
                        if (mHomeFragment != null) {
                            mHomeFragment.scrollToTop();
                        }
                        break;
                    case R.id.navigation_gospel:
                        if (mGospelFragment != null) {
                            mGospelFragment.scrollToTop();
                        }
                        break;
                    case R.id.navigation_me:
                        mMeFragment.scrollToTop();
                        break;
                    default:
                        break;
                }
            }
        });
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mCustomViewPager.setCurrentItem(ChristianTab.NAVIGATION_HOME.ordinal());
                        return true;
                    case R.id.navigation_gospel:
                        mCustomViewPager.setCurrentItem(ChristianTab.NAVIGATION_BOOK.ordinal());
                        return true;
                    case R.id.navigation_me:
                        mCustomViewPager.setCurrentItem(ChristianTab.NAVIGATION_ME.ordinal());
                        return true;
                }
                return false;
            }

        });
        mCustomViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.i(TAG, "onPageScrolled: ");
            }

            @Override
            public void onPageSelected(int position) {
                if (mPrevMenuItem != null) {
                    mPrevMenuItem.setChecked(false);
                } else {
                    mBottomNavigationView.getMenu().getItem(ChristianTab.NAVIGATION_HOME.ordinal()).setChecked(false);
                }
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                mPrevMenuItem = mBottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                Log.i(TAG, "onPageScrollStateChanged: ");
            }
        });
        mCustomViewPager.setAdapter(new CustomFragmentPagerAdapter(getSupportFragmentManager()));
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
////        Toolbar toolbar = getActionBarToolbar();
//        toolbar.inflateMenu(R.menu.menu_share_and_more);
//        toolbar.setOnMenuItemClickListener(this);
//        return true;
//    }
//
//    @Override
//    public boolean onMenuItemClick(MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.menu_share:
//                return true;
//            case R.id.menu_more:
////                startActivity(new Intent(this, SearchActivity.class));
//                return true;
//        }
//        return false;
//    }
    private class CustomFragmentPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private int mCurrentPosition = -1;

        CustomFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            mHomeFragment = HomeFragment.newInstance(ChristianTab.NAVIGATION_HOME.ordinal());
            mGospelFragment = GospelFragment.newInstance();
            mMeFragment = MeFragment.newInstance();
            fragments.add(mHomeFragment);
            fragments.add(mGospelFragment);
            fragments.add(mMeFragment);
        }

//        @Override
//        public void setPrimaryItem(ViewGroup container, int position, Object object) {
//            super.setPrimaryItem(container, position, object);
//            if (position != mCurrentPosition) {
//                Fragment fragment = (Fragment) object;
//                CustomViewPage pager = (CustomViewPage) container;
//                if (fragment != null && fragment.getView() != null) {
//                    mCurrentPosition = position;
//                    pager.measureFragment(fragment.getView());
//                }
//            }
//        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - exitTime) < 2000) {
            super.onBackPressed();
        } else {
            Snackbar snackbar = Snackbar.make(mCustomViewPager, R.string.double_click_exit, Snackbar.LENGTH_SHORT);
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.white));
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
            snackbar.setAction("确定", null).show();
            exitTime = currentTime;
        }
    }
}
