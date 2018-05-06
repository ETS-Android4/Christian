package com.christian.nav

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import com.christian.BottomNavigationViewBehaviorExt
import com.christian.Injection
import com.christian.R
import com.christian.base.ActBase
import com.christian.data.Nav
import com.christian.helper.BottomNavigationViewHelper
import com.christian.navadapter.NavItemPresenter
import com.christian.swipe.SwipeBackHelper
import com.christian.view.GridItemDecoration
import com.christian.view.ItemDecoration
import kotlinx.android.synthetic.main.nav_activity.*
import org.jetbrains.anko.dip


/**
 * Home, Gospel, Communication, Me 4 TAB main entrance activity.
 * implementation of NavContract.View.
 */
open class NavActivity : ActBase(), NavContract.View {

    val SHOTRER_DURATION = 225L

    val LONGER_DURATION = 375L

    /**
     * presenter will be initialized when the NavPresenter is initialized
     */
    override lateinit var presenter: NavContract.Presenter

    lateinit var adapter: NavItemPresenter

    companion object {

        private const val HIDE_THRESHOLD = 0 //移动多少距离后显示隐藏

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.nav_activity)

        NavPresenter("", Injection.provideNavsRepository(applicationContext), this)

        presenter.start()

    }

    override fun initView(navs: List<Nav>) {

        startNav(0)

        initSbl()

        initTb("")

        initSrl()

        initFl()

        initRv(navs)

        initFAB()

        initBnv()

    }

    open fun initSbl() {

        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false)

        SwipeBackHelper.getCurrentPage(this).setDisallowInterceptTouchEvent(true)

    }

    private fun initSrl() {

        srl_nav.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))

        srl_nav.setOnRefreshListener { presenter.insertNav(0, true) }

    }

    open fun initFl() {

        fl_nav.background = ResourcesCompat.getDrawable(resources, R.color.default_background_nav, theme)

    }

    open fun initRv(navs: List<Nav>) {

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv_nav.addItemDecoration(ItemDecoration(resources.getDimension(R.dimen.search_margin_horizontal).toInt()))
            rv_nav.layoutManager = LinearLayoutManager(this)
        } else {
            rv_nav.addItemDecoration(GridItemDecoration(resources.getDimension(R.dimen.search_margin_horizontal).toInt()))
            rv_nav.layoutManager = GridLayoutManager(this, 2) // todo: should use the long side of the screen over the short side to decide span count
        }


        adapter = NavItemPresenter(navs)
        rv_nav.adapter = adapter

        rv_nav.addOnScrollListener(object : HidingScrollListener() {

            override fun onHide() {

                fab_nav.hide()

            }

            override fun onShow() {

                showFab(R.drawable.ic_keyboard_arrow_up_black_24dp)

            }

            override fun onTop() {

                showFab(R.drawable.ic_edit_black_24dp)

            }

        })

    }

    open fun initBnv() {

        BottomNavigationViewHelper.disableShiftMode(bnv_nav)

        // set behavior
        val params = CoordinatorLayout.LayoutParams(bnv_nav.layoutParams)
        params.gravity = Gravity.BOTTOM
        params.behavior = BottomNavigationViewBehaviorExt(this, null)
        bnv_nav.layoutParams = params

        setBnvListener()

    }

    open fun initFAB() {

        fab_nav.visibility = View.VISIBLE

        showFab(R.drawable.ic_edit_black_24dp)

        fab_nav.setOnClickListener(null)

    }

    override fun initTb(title: String) {

    }

    override fun setupSearchbar(searchHint: String) {
    }

    override fun startSwipeRefreshLayout() {
    }

    override fun stopSrl() {

        srl_nav.isRefreshing = false

    }

    override fun startPb() {

        pb_nav.visibility = View.VISIBLE

    }

    override fun stopPb() {

        pb_nav.visibility = View.GONE

    }

    override fun invalidateRv(navs: List<Nav>) {

        adapter.navs = navs

        adapter.notifyDataSetChanged()

    }

    override fun restoreRvPos() {
    }

    override fun showFab(drawableId: Int) {

        if (fab_nav.isVisible) {
            Log.i("fab", "hide")
            fab_nav.hide()
        }

        fab_nav.postDelayed({
            fab_nav.setImageDrawable(ResourcesCompat.getDrawable(resources, drawableId, theme))
            fab_nav.show()
            Log.i("fab", "show")
        }, SHOTRER_DURATION)

        when (drawableId) {

            R.drawable.ic_edit_black_24dp -> {

                fab_nav.setOnClickListener(null)

            }

            R.drawable.ic_keyboard_arrow_up_black_24dp -> {

                fab_nav.setOnClickListener { scrollRvToTop() }

            }

        }

    }

    override fun hideFloatingActionButton() {
    }

    override fun activeFloatingActionButton() {
    }

    private fun setBnvListener() {

        bnv_nav.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.navigation_home -> {

                    startNav(navId = 0)

                    return@OnNavigationItemSelectedListener true

                }

                R.id.navigation_gospel -> {

                    startNav(navId = 1)

                    return@OnNavigationItemSelectedListener true

                }

                R.id.navigation_chat -> {

                    startNav(navId = 2)

                    return@OnNavigationItemSelectedListener true

                }

                R.id.navigation_me -> {

                    startNav(navId = 3)

                    return@OnNavigationItemSelectedListener true

                }
            }

            false

        })

        bnv_nav.setOnNavigationItemReselectedListener { scrollRvToTop() }

    }

    /**
     * @param fabVisibility true presents showing floating action button, false otherwise.
     * @param navId quest parameter of nav page to get navs lists
     */
    private fun startNav(navId: Int) {

        if (navId == 0) {
            presenter.insertNav(navId)
        }

    }

    fun scrollRvToTop() {

        rv_nav.smoothScrollToPosition(dip(-1)) // 为了滚到顶

        abl_nav.setExpanded(true, true)

    }

    abstract inner class HidingScrollListener : RecyclerView.OnScrollListener() {

        private var scrolledDistance = 0 //移动的中距离

        private var controlsVisible = true //显示或隐藏


        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

            super.onScrolled(recyclerView, dx, dy)
            Log.i("rv", "-1 " + rv_nav.canScrollVertically(-1))
//            Log.i("rv", "1 " + rv_nav.canScrollVertically(1))

            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {//移动总距离大于规定距离 并且是显示状态就隐藏

                onHide()

                controlsVisible = false

                scrolledDistance = 0//归零

            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {

                onShow()

                controlsVisible = true

                scrolledDistance = 0

            }

            if (controlsVisible && dy > 0 || !controlsVisible && dy < 0) { //显示状态向上滑动 或 隐藏状态向下滑动 总距离增加

                scrolledDistance += dy

            }

            if (!rv_nav.canScrollVertically(-1) && dy < 0) { // 并且是向下滑动

                onTop()

            }

        }


        abstract fun onHide()

        abstract fun onShow()

        abstract fun onTop()

    }

}
