package com.christian.nav

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.appcompat.widget.TooltipCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.christian.R
import com.christian.SettingsActivity
import com.christian.common.requestFocusWithKeyboard
import com.christian.common.showExitButton
import com.christian.nav.disciple.DiscipleFragment
import com.christian.nav.me.MeFragment
import com.christian.swipe.SwipeBackActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.kotlinpermissions.KotlinPermissions
import kotlinx.android.synthetic.main.fragment_nav_rv.*
import kotlinx.android.synthetic.main.nav_activity.*
import kotlinx.android.synthetic.main.nav_activity.view.*
import kotlinx.android.synthetic.main.nav_item_me_for_setting_static.*
import kotlinx.android.synthetic.main.nav_item_me_portrait.*
import kotlinx.android.synthetic.main.nav_item_me_portrait.iv_nav_item_small
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import ren.qinc.markdowneditors.view.EditorActivity
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

import android.content.Intent

import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import com.christian.nav.home.HomeFragment
import com.firebase.ui.auth.ErrorCodes
import com.github.anzewei.parallaxbacklayout.ParallaxHelper


/**
 * Home, Gospel, Communication, Me 4 TAB main entrance activity.
 * implementation of NavContract.View.
 */
open class NavActivity : SwipeBackActivity(), NavContract.INavActivity {

    private lateinit var sharedPreferences: SharedPreferences
    private var isOn = false

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        // Successfully signed in
        // Successfully signed in
        val response = result.idpResponse
        handleSignInResponse(result.resultCode, response)
    }

    private fun handleSignInResponse(resultCode: Int, @Nullable response: IdpResponse?) {
        // Successfully signed in
        if (resultCode == RESULT_OK) {
            invalidateSignInUI()
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled)
                return
            }
            if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection)
                return
            }
            if (response.error?.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
//                val intent = Intent(
//                    this,
//                    AnonymousUpgradeActivity::class.java
//                ).putExtra(ExtraConstants.IDP_RESPONSE, response)
//                startActivity(intent)
            }
            if (response.error?.errorCode == ErrorCodes.ERROR_USER_DISABLED) {
                showSnackbar(R.string.account_disabled)
                return
            }
            showSnackbar(R.string.unknown_error)
        }
    }

    private fun showSnackbar(@StringRes errorMessageRes: Int) {
        Snackbar.make(cl_nav, errorMessageRes, Snackbar.LENGTH_LONG).show()
//        Snackbar.make(mBinding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show()
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        if (sharedPreferences.getBoolean("isOn", false)) {
//            // 恢复应用默认皮肤
//            shouldEnableDarkMode(SettingsActivity.DarkModeConfig.NO)
//            isOn = false
//            sharedPreferences.edit { putBoolean("isOn", isOn) }
//        } else {
//            // 夜间模式
//            shouldEnableDarkMode(SettingsActivity.DarkModeConfig.YES)
//            isOn = true
//            sharedPreferences.edit { putBoolean("isOn", isOn) }
//        }
//        recreate()
//    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuItemSearch = menu.findItem(R.id.menu_search)
        menuItemSetting = menu.findItem(R.id.menu_setting)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                val i = Intent(this, SettingsActivity::class.java)
                i.putExtra(showExitButton, auth.currentUser != null)
                startActivityForResult(i, RC_SIGN_IN)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    lateinit var menuItemSetting: MenuItem
    private lateinit var menuItemSearch: MenuItem
    private var customTime = 0L
    val SHOTRER_DURATION = 225L

    val LONGER_DURATION = 375L

    companion object {
        class StaticHandler(navActivity: NavActivity) : Handler() {
            private val navActivityWeakReference = WeakReference<NavActivity>(navActivity)
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_SET_TOOLBAR_EXPANDED -> {
                        navActivityWeakReference.get()?.info { "setTabLayoutExpanded--${msg.arg1}" }
                        navActivityWeakReference.get()?.let { initAppBarLayout(it, msg.arg1) }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
    }

    private lateinit var mStaticHandler: StaticHandler
    var pageSelectedPosition = 0

    /**
     * presenter will be initialized when the NavPresenter is initialized
     */
    override lateinit var presenter: NavContract.IPresenter
    var verticalOffset = -1
    lateinit var navFragmentPagerAdapter: NavFragmentPagerAdapter

    open val viewPagerOnPageChangeListener =
        object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                bnv_nav.menu.getItem(position).isChecked = true

                mStaticHandler.removeMessages(MESSAGE_SET_TOOLBAR_EXPANDED)
                val msg = Message()
                msg.what = MESSAGE_SET_TOOLBAR_EXPANDED
                msg.arg1 = position
                info { "setTabLayoutExpanded---$position" }
                mStaticHandler.sendMessageDelayed(msg, 0)
//            setTabLayoutExpanded(this@NavActivity, position)

                pageSelectedPosition = position

                pageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                info { "onPageScrollStateChanged: $state" }
                /* if (state == 2) {
     //                fab_nav.hide()
                 } else if (state == 0) {
     //                    fab_nav.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_black_24dp, theme))
     //                    if (showOrHideLogicExecute) {
     //                        showFAB()
     //                    }
                 }*/
            }
        }

    private fun initTl(position: Int) {
        when (position) {
            VIEW_HOME -> {
                tabTitleList = arrayListOf(
                    getString(R.string.tab_recommendation),
//                    getString(R.string.tab_sermon),
//                    getString(R.string.tab_sentiment),
//                    getString(R.string.tab_technology),
                )
//                tl_nav.tabMode = TabLayout.MODE_SCROLLABLE
            }
            VIEW_GOSPEL -> {
                /*tabTitleList = arrayListOf(
                    getString(R.string._Gen),
                    getString(R.string._Exo),
                    getString(R.string._Lev),
                    getString(R.string._Num),
                    getString(R.string._Deu),
                    getString(R.string._Jos),
                    getString(R.string._Jug),
                    getString(R.string._Rut),
                    getString(R.string._1Sa),
                    getString(R.string._2Sa),
                    getString(R.string._1Ki),
                    getString(R.string._2Ki),
                    getString(R.string._1Ch),
                    getString(R.string._2Ch),
                    getString(R.string._Ezr),
                    getString(R.string._Neh),
                    getString(R.string._Est),
                    getString(R.string._Job),
                    getString(R.string._Psm),
                    getString(R.string._Pro),
                    getString(R.string._Ecc),
                    getString(R.string._Son),
                    getString(R.string._Isa),
                    getString(R.string._Jer),
                    getString(R.string._Lam),
                    getString(R.string._Eze),
                    getString(R.string._Dan),
                    getString(R.string._Hos),
                    getString(R.string._Joe),
                    getString(R.string._Amo),
                    getString(R.string._Oba),
                    getString(R.string._Jon),
                    getString(R.string._Mic),
                    getString(R.string._Nah),
                    getString(R.string._Hab),
                    getString(R.string._Zep),
                    getString(R.string._Hag),
                    getString(R.string._Zec),
                    getString(R.string._Mal),

                    getString(R.string._Mat),
                    getString(R.string._Mak),
                    getString(R.string._Luk),
                    getString(R.string._Jhn),
                    getString(R.string._Act),
                    getString(R.string._Rom),
                    getString(R.string._1Co),
                    getString(R.string._2Co),
                    getString(R.string._Gal),
                    getString(R.string._Eph),
                    getString(R.string._Phl),
                    getString(R.string._Col),
                    getString(R.string._1Ts),
                    getString(R.string._2Ts),
                    getString(R.string._1Ti),
                    getString(R.string._2Ti),
                    getString(R.string._Tit),
                    getString(R.string._Phm),
                    getString(R.string._Heb),
                    getString(R.string._Jas),
                    getString(R.string._1Pe),
                    getString(R.string._2Pe),
                    getString(R.string._1Jn),
                    getString(R.string._2Jn),
                    getString(R.string._3Jn),
                    getString(R.string._Jud),
                    getString(R.string._Rev)
                )
                tl_nav.tabMode = TabLayout.MODE_SCROLLABLE*/
            }
            VIEW_DISCIPLE -> {
                /*tabTitleList = arrayListOf(
                    getString(R.string.tab_messages),
                    getString(R.string.tab_qa),
                    getString(R.string.tab_marriage),
                )
                tl_nav.tabMode = TabLayout.MODE_SCROLLABLE*/
            }
            VIEW_ME -> {
                /*tabTitleList = arrayListOf(
                    getString(R.string.tab_my_reading),
                    getString(R.string.tab_my_communication),
                )
                tl_nav.tabMode = TabLayout.MODE_FIXED*/
                initPortrait()
            }

        }
//        tl_nav.removeAllTabs()
//        for (tabTitle in tabTitleList) {
//            tl_nav.newTab().setText(tabTitle).let { tl_nav.addTab(it) }
//        }
    }

    private fun pageSelected(position: Int) {
//        pageSelected这是一个重要方法，initTl这里进行TabLayout的UI初始化工作
        initTl(position)

        when (position) {
            0 -> {
                tb_nav.title = getString(R.string.title_home)

                if (::menuItemSearch.isInitialized) menuItemSearch.isVisible = true
                if (::menuItemSetting.isInitialized) menuItemSetting.isVisible = false

                //        为了在Home到Disciple的时候FAB有一个显示到消失再到显示的过程
                /*if (::navFragmentPagerAdapter.isInitialized && navFragmentPagerAdapter.isCurrentFragmentIn() && navFragmentPagerAdapter.currentFragment is HomeFragment) {
                    val homeFragment = navFragmentPagerAdapter.currentFragment as HomeFragment
                    when (homeFragment.isPageTop) {
                        false -> hideFab()
                        true -> showFab()
                    }
                }*/
                activity_nav_fab.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_edit_black_24dp,
                        theme
                    )
                )
//                fab_nav.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorAccent,theme))
//                if (verticalOffset != -tb_nav.height && !fab_nav.isVisible)
//                    fab_nav.show()
                TooltipCompat.setTooltipText(activity_nav_fab, getString(R.string.action_edit))
                activity_nav_fab.setOnClickListener {
                    startActivity(Intent(this@NavActivity, EditorActivity::class.java))
                }
            }
            1 -> {
                tb_nav.title = getString(R.string.title_gospel)

                if (::menuItemSearch.isInitialized) menuItemSearch.isVisible = true
                if (::menuItemSetting.isInitialized) menuItemSetting.isVisible = false

                hideFab()
//                activity_nav_fab.setImageDrawable(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.ic_baseline_wifi_protected_setup_24,
//                        theme
//                    )
//                null
//                )
//                fab_nav.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorAccent,theme))
                /*if (verticalOffset > -tb_nav.height)
                    fab_nav.hide()*/
                TooltipCompat.setTooltipText(activity_nav_fab, "Filter")

                activity_nav_fab.setOnClickListener {
                    startActivity(
                        Intent(
                            this@NavActivity,
                            ren.qinc.markdowneditors.view.MainActivity::class.java
                        )
                    )
                }
            }
            2 -> {
                tb_nav.title = getString(R.string.title_chat)

                if (::menuItemSearch.isInitialized) menuItemSearch.isVisible = true
                if (::menuItemSetting.isInitialized) menuItemSetting.isVisible = false

                //        为了在Home到Disciple的时候FAB有一个显示到消失再到显示的过程
                /*showFab()*/

                activity_nav_fab.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_baseline_send_24,
                        theme
                    )
                )
//                fab_nav.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorAccent,theme))
                TooltipCompat.setTooltipText(activity_nav_fab, getString(R.string.send))
                activity_nav_fab.setOnClickListener {
                    if (::navFragmentPagerAdapter.isInitialized) {
                        val discipleFragment =
                            navFragmentPagerAdapter.currentFragment as DiscipleFragment
                        if (discipleFragment.binding.messageEditText.text.isEmpty()) {
                            abl_nav.setExpanded(false, true)
//                            snackbar(getString(R.string.content_empty)).show()
                            discipleFragment.binding.messageEditText.requestFocusWithKeyboard()
                        } else {
                            discipleFragment.sendMessage()
                        }
                    }
                }
            }
            3 -> {
                tb_nav.title = getString(R.string.me)

                if (::menuItemSearch.isInitialized) menuItemSearch.isVisible = false
                if (::menuItemSetting.isInitialized) menuItemSetting.isVisible = true
                invalidateSignInUI()

                hideFab()

//                activity_nav_fab.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_help_24, theme))
//                fab_nav.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorAccentRed,theme))
                TooltipCompat.setTooltipText(activity_nav_fab, getString(R.string.help))
                activity_nav_fab.setOnClickListener {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mStaticHandler = StaticHandler(this@NavActivity)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_activity)
        NavPresenter(initFragmentIndex, this)
        presenter.init(whichActivity = NAV_ACTIVITY)
        sharedPreferences = getSharedPreferences(
            switchNightModeIsOn, Activity.MODE_PRIVATE
        )
    }

    override fun initView() {
        initAbl()
        initTb()
        initPortrait()
        initVp()
        initFab()
        initBv()
        initBnv()
        // 自动夜间模式
//        sunriseSunset()
        ParallaxHelper.disableParallaxBack(this)
    }

    private fun sunriseSunset() {
        getLatitudeLongitudePermissions()
    }

    private fun getLatitudeLongitudePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            KotlinPermissions.with(this) // where this is an FragmentActivity instance
                .permissions(INTERNET, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .onAccepted { permissions ->
                    //List of accepted permissions
                    if (permissions.contains(ACCESS_FINE_LOCATION))
                        bindNavService()
                }
                .onDenied { permissions ->
                    //List of denied permissions
                }
                .onForeverDenied { permissions ->
                    //List of forever denied permissions
                }
                .ask()
        } else {
            bindNavService()
        }
    }

    private fun initAbl() {
//        makeViewBlurExtendsAppBarLayout(abl_nav, cl_nav, window)
        tb_nav.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View) {
                scrollRvToTop(this@NavActivity)
            }
        })
    }

    internal lateinit var tabTitleList: ArrayList<String>
    open fun initTb() {
        setSupportActionBar(tb_nav)
        sbl_nav.visibility = View.GONE


//        UITools.elasticPadding(tl_nav, 500);
//        OverScrollDecoratorHelper.setUpStaticOverScroll(tl_nav, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        HorizontalOverScrollBounceEffectDecorator(object : IOverScrollDecoratorAdapter {
            override fun isInAbsoluteEnd(): Boolean {
                return !tl_nav.canScrollDown()
            }

            override fun isInAbsoluteStart(): Boolean {
                return !tl_nav.canScrollUp()
            }

            override fun getView(): View {
                return tl_nav
            }

        })
        info { "圣经初始化" }
    }

    private fun initPortrait() {
        applyMarqueeEffect(intro)
        sign_in.setOnClickListener {
            signIn()
        }
        sign_out.setOnClickListener {

        }
    }

    open fun initVp() {
        navFragmentPagerAdapter = NavFragmentPagerAdapter(supportFragmentManager)
        vp_nav.offscreenPageLimit = 3
        vp_nav.adapter = navFragmentPagerAdapter
        vp_nav.addOnPageChangeListener(viewPagerOnPageChangeListener)
    }

    open fun initFab() {
//        fab_nav.visibility = View.GONE

//        fab_nav.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_black_24dp, theme))
//        fab_nav.setOnClickListener {
//            ViewModelProviders.of(this).get(NavViewModel::class.java).loadImage()
//        }
    }

    open fun initBv() {

        makeViewBlur(bv_nav, cl_nav, window)

        val params = CoordinatorLayout.LayoutParams(bv_nav.layoutParams)
        params.gravity = Gravity.BOTTOM
        params.behavior = BottomNavigationViewBehavior(this, null)
        bv_nav.layoutParams = params
    }

    /*open fun showFAB() {
        fab_nav.show()
    }*/
/*解决暗黑模式重启无首页标题*/
    override fun onResume() {
        super.onResume()
        if (pageSelectedPosition == 0) tb_nav.title = getString(R.string.title_home)
    }

    open fun initBnv() {
//        disableShiftMode(bnv_nav)
        /** 切换夜间模式重启的时候，需要通过设置currentItem来设置标题等一系列参数（但是无效，只能采用下一行的方式） */
//        vp_nav.currentItem = 0
        viewPagerOnPageChangeListener.onPageSelected(initFragmentIndex)
        bnv_nav.bnv_nav.setOnNavigationItemSelectedListener {
            val itemPosition = (presenter as NavPresenter).generateNavId(it.itemId)
            debug { "generateNavId$itemPosition" }
            vp_nav.currentItem = itemPosition
            true
        }
        bnv_nav.setOnNavigationItemReselectedListener {
            scrollRvToTop(this@NavActivity)
//            scrollRvToTop(this@NavActivity, navFragment.rv_nav)
        }
    }

    override fun showFab() {
        activity_nav_fab.show()
    }

    override fun hideFab() {
        activity_nav_fab.hide()
    }

    private fun slExpand() {
        if (!sbl_nav.isExpanded) {
            sbl_nav.expand(true, true)
        }
    }

    private fun slCollapse() {
        if (sbl_nav.isExpanded) {
            sbl_nav.collapse(true)
        }
        if (sbl_nav.isFadedOut) {
            sbl_nav.fadeIn()
        }
    }

    /**
     * Immersive reading, swipe hidden.
     */
    class BottomNavigationViewBehavior(context: Context?, attrs: AttributeSet?) :
        androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<View>(context, attrs) {
        override fun onLayoutChild(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            layoutDirection: Int
        ): Boolean {
            (child.layoutParams as CoordinatorLayout.LayoutParams).topMargin =
                parent.measuredHeight.minus(child.measuredHeight)
            return super.onLayoutChild(parent, child, layoutDirection)
        }

        override fun layoutDependsOn(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is AppBarLayout
        }

        override fun onDependentViewChanged(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            val top =
                ((dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior).topAndBottomOffset
            Log.i("dd", top.toString())
            //因为BottomNavigation的滑动与ToolBar是反向的，所以取-top值
            child.translationY = (-top).toFloat()
            return false
        }
    }

    /**
     * Locate nav detail FAB
     */
    class BottomNavigationViewBehaviorDetail(context: Context?, attrs: AttributeSet?) :
        androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<View>(context, attrs) {
        override fun onLayoutChild(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            layoutDirection: Int
        ): Boolean {
            (child.layoutParams as CoordinatorLayout.LayoutParams).topMargin =
                parent.measuredHeight.minus(child.measuredHeight)
            return super.onLayoutChild(parent, child, layoutDirection)
        }

        override fun layoutDependsOn(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is AppBarLayout
        }

        override fun onDependentViewChanged(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            child.translationY = 2000f
            return false
        }
    }

    // presenter solve login in and login out logic
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            info { "response: ${response?.idpToken}" }
            invalidateSignInUI()
            /*if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                invalidateSignInUI()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                val snackbar = snackbar(getString(R.string.sign_in_failed))
                snackbar.show()
            }*/
        }
    }

    fun invalidateSignInUI() {
        val user = auth.currentUser
        info { "user: $user" }
        if (user != null) {
            tb_nav.title = ""
            sign_in.visibility = View.GONE
            sign_out.visibility = View.GONE
//            fab_nav.show()
            portrait.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            intro.visibility = View.VISIBLE
            name.text = user.displayName
            intro.text = user.email
            Glide.with(this).load(user.photoUrl).into(iv_nav_item_small)
            info { "user.photoUrl: ${user.photoUrl}, user.displayName: ${user.displayName}, user.email: ${user.email}" }

            portrait_nav.isClickable = true
            portrait_nav.isFocusable = true
            portrait_nav.isFocusableInTouchMode = true
        } else {
            tb_nav.title = getString(R.string.title_me)
            sign_in.visibility = View.VISIBLE
            sign_out.visibility = View.GONE
//            fab_nav.hide()
            portrait.visibility = View.GONE
            name.visibility = View.GONE
            intro.visibility = View.GONE

            portrait_nav.isClickable = false
            portrait_nav.isFocusable = false
            portrait_nav.isFocusableInTouchMode = false
        }
        // ...
    }

    open class NavFragmentPagerAdapter(
        fm: FragmentManager,
    ) :
        androidx.fragment.app.FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        lateinit var currentFragment: Fragment
        fun isCurrentFragmentIn() = ::currentFragment.isInitialized

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    val homeFragment = HomeFragment()
                    homeFragment.navId = position
                    return homeFragment
                }
                1 -> {
                    val gospelFragment = GospelFragment()
                    gospelFragment.navId = position
                    return gospelFragment
                }
                2 -> {
                    return DiscipleFragment()
                }
                3 -> {
                    return MeFragment()
                }
            }
            val navFragment = GospelFragment()
            navFragment.navId = position
            return navFragment
        }

        override fun getCount(): Int {
            return 4
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            when (position) {
                0 -> {
                    currentFragment = `object` as HomeFragment
                }
                1 -> {
                    currentFragment = `object` as GospelFragment
                }
                2 -> {
                    currentFragment = `object` as DiscipleFragment
                }
                3 -> {
                    currentFragment = `object` as MeFragment
                }
            }
            super.setPrimaryItem(container, position, `object`)
        }
    }

    val appBarLayoutOnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appBarLayoutOnOffsetChangedListener(this@NavActivity, appBarLayout, verticalOffset)
            this@NavActivity.verticalOffset = verticalOffset
        }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.AppTheme)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    fun snackbar(s: String): Snackbar {
        val snackbar = Snackbar.make(cl_nav_2, s, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            snackbarView.elevation = dip(3).toFloat()
        }
        // Snackbar
//        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
//        params.anchorId = R.id.bnv_nav
//        params.width = CoordinatorLayout.LayoutParams.MATCH_PARENT
//        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL// 相对锚点的位置
//        params.anchorGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL // 锚点的位置
//        snackbarView.layoutParams = params
        return snackbar
    }

    open fun scrollRvToTop(navActivity: NavActivity) {
        try {
            if (::navFragmentPagerAdapter.isInitialized)
                when (vp_nav.currentItem) {
                    0, 1, 2 -> {
                        navActivity.navFragmentPagerAdapter.currentFragment.fragment_nav_rv.smoothScrollToPosition(
                            0
                        ) // 为了滚到顶

                        (navActivity.navFragmentPagerAdapter.currentFragment as GospelFragment).scrollChildRVToTop()
                    }
                    3 -> {
                        navActivity.navFragmentPagerAdapter.currentFragment.fragment_nav_rv.smoothScrollToPosition(
                            0
                        ) // 为了滚到顶

                        (navActivity.navFragmentPagerAdapter.currentFragment as MeFragment).scrollChildRVToTop()
                    }
                }
            {

            }
            navActivity.abl_nav.setExpanded(true, true)
        } catch (e: Exception) {
        }
    }


    abstract class DoubleClickListener : View.OnClickListener {

        companion object {
            const val DOUBLE_TIME = 1000
            var lastClickTime = 0L
        }

        override fun onClick(v: View) {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
                onDoubleClick(v)
            }
            lastClickTime = currentTimeMillis
        }

        abstract fun onDoubleClick(v: View)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindNavService()
    }

    private lateinit var navService: NavService
    private var isBind: Boolean = false

    /**
     * 用于获取NavService
     */
    val navServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val navBinder = service as NavService.NavBinder
            navService = navBinder.navService
            isBind = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBind = false
        }
    }


    fun bindNavService() {
        val intent = Intent(this, NavService::class.java)
        bindService(intent, navServiceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindNavService() {
        if (isBind) {
            unbindService(navServiceConnection);
            isBind = false;
        }
    }


    override fun onBackPressed() {
        //没有东西可以返回了，剩下软件退出逻辑
        //没有东西可以返回了，剩下软件退出逻辑
        if (abs(customTime - System.currentTimeMillis()) < 2000) {
            finish()
        } else { // 提示用户退出
            customTime = System.currentTimeMillis()
            snackbar(getString(R.string.double_click_exit)).show()
        }
    }
}
