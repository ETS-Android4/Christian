package com.christian.nav.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.christian.R
import com.christian.common.data.Gospel
import com.christian.data.Setting
import com.christian.databinding.NavItemGospelBinding
import com.christian.nav.*
import com.christian.util.ChristianUtil
import com.christian.view.ItemDecoration
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.lxj.xpopup.util.KeyboardUtils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_nav_rv.*
import kotlinx.android.synthetic.main.fragment_nav_rv.view.*
import kotlinx.android.synthetic.main.nav_activity.*
import org.jetbrains.anko.debug

open class HomeFragment : androidx.fragment.app.Fragment(), NavContract.INavFragment {

    lateinit var navChildFragmentPagerAdapter: NavChildFragmentPagerAdapter

    override fun onSaveInstanceState(outState: Bundle) {
    }
//
//    override fun onGospelSelected(gospel: DocumentSnapshot) {
//        // Go to the details page for the selected restaurant
//        gospelId = gospel.id
//        val intent = Intent(this@NavFragment.navActivity, NavDetailActivity::class.java)
//        intent.putExtra(toolbarTitle, gospel.data?.get("subtitle").toString())
//        startActivity(intent)
//    }

    lateinit var navActivity: NavActivity
    private lateinit var ctx: Context
    //    private lateinit var navAdapter: NavItemPresenter<*>
    lateinit var v: View
    var navId = -1
    lateinit var gospelId: String

    init {
        debug { "look at init times" }
    }

    /**
     * It is the first place application code can run where the fragment is ready to be used
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        navActivity = context as NavActivity
        ctx = context
        debug { "onAttach" }
    }

    private var isInitView = false
    private var isVisibled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        isInitView = true
        isCanLoadData()

        return v
    }

    override fun onResume() {
        super.onResume()
        isVisibled = true
        isCanLoadData()

        when(isPageTop) {
            true -> navActivity.showFab()
            else -> navActivity.hideFab()
        }
//        From Editor Activity to Home Fragment hide keyboard
    }

    override fun onPause() {
        super.onPause()
        isVisibled = false

        navActivity.hideFab()
    }

    private fun isCanLoadData() {
        //所以条件是view初始化完成并且对用户可见
        if (isInitView && isVisibled) {
            initView()

            //防止重复加载数据
            isInitView = false
            isVisibled = false
        }
    }

    override fun initView() {
        debug { "nav fragment is $this and navId is $navId --initView" }
        mPosition = 0
        when (navId) {
            VIEW_ME -> {
                initPortrait()
            }
        }
        if (navId == VIEW_GOSPEL) {
            v.vp1_nav.visibility = View.VISIBLE
            v.srl_nav.visibility = View.GONE
            v.pb_nav.visibility = View.GONE
            initVp(navActivity.tabTitleList)
        } else {
            v.vp1_nav.visibility = View.GONE
            v.srl_nav.visibility = View.VISIBLE
            initSrl()
        }
        initRv()
    }

    private fun initSrl() {
    }

    private fun initPortrait() {
//        Glide.with(navActivity).load(R.drawable.me).into(navActivity.iv_nav_item_small)
    }

    private lateinit var navFragment: HomeFragment

    private var pageSelectedPosition: Int = -1

    private fun initVp(tabTitleList: ArrayList<String>) {
        vp1_nav.viewPager = navActivity.vp_nav
        navChildFragmentPagerAdapter = NavChildFragmentPagerAdapter(childFragmentManager, tabTitleList,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        v.vp1_nav.adapter = navChildFragmentPagerAdapter
        navActivity.tl_nav.setupWithViewPager(v.vp1_nav)//将TabLayout和ViewPager关联起来

        v.vp1_nav.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                pageSelectedPosition = position
                navFragment = navChildFragmentPagerAdapter.currentFragment
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }

    var isPageTop: Boolean = true

    var isPageBottom: Boolean = false

    private lateinit var gospelAdapter: FirestoreRecyclerAdapter<Gospel, NavItemView>
    private lateinit var meAdapter: FirestoreRecyclerAdapter<Setting, NavItemView>

    override fun onDestroyView() {
        super.onDestroyView()
        if (navId == VIEW_HOME)
            gospelAdapter.stopListening()
        if (navId == VIEW_ME && ::meAdapter.isInitialized)
            meAdapter.stopListening()
    }

    private fun initRv() {
        when (navId) {
            VIEW_HOME -> {
                v.fragment_nav_rv.addItemDecoration(ItemDecoration(top = ChristianUtil.dpToPx(8)))
                loadGospelsFromTabId(navId)
                gospelAdapter.startListening()
                v.fragment_nav_rv.adapter = gospelAdapter
            }
            VIEW_GOSPEL -> {
//                Gospel不在这里控制，在下面的id从4..69的地方控制
            }
            VIEW_DISCIPLE -> {
//                Disciple不在这里控制，有自己的DiscipleFragment
            }
            VIEW_ME -> {
//                v.fragment_nav_rv.addItemDecoration(ItemDecoration(resources.getDimension(R.dimen.search_margin_horizontal).toInt()))

//                meAdapter = firestoreRecyclerAdapter()
//                meAdapter.startListening()
                v.fragment_nav_rv.adapter = meAdapter
            }
            in 4..69 -> { // Gospel Page's Fragment's navId
                v.fragment_nav_rv.addItemDecoration(ItemDecoration(top = ChristianUtil.dpToPx(8)))

                loadGospelsFromTabId(navId)
            }
        }

        v.fragment_nav_rv.isVerticalScrollBarEnabled = false
        val onScrollListener = object : HidingScrollListener(v.fragment_nav_rv) {

            override fun onHide() {
//                hideFab()
                isPageTop = false
                isPageBottom = false
                navActivity.hideFab()
//                controlOverScroll(navActivity, navActivity.abl_nav, navActivity.verticalOffset)
                v.fragment_nav_rv.isVerticalScrollBarEnabled = true
                when (navActivity.vp_nav.currentItem) {
                    0 -> {
                        navActivity.hideFab()
                    }
                    1 -> {
//                        navActivity.hideFab()
                    }
                }
            }

            override fun onShow() {
//                navActivity.showFab(pageSelectedPosition)
                isPageTop = false
                isPageBottom = false
                navActivity.hideFab()
//                controlOverScroll(navActivity, navActivity.abl_nav, navActivity.verticalOffset)
                v.fragment_nav_rv.isVerticalScrollBarEnabled = true
                when (navActivity.vp_nav.currentItem) {
                    1 -> {
//                        navActivity.showFab()
                    }
                }
            }

            override fun onTop() {
                isPageTop = true
                navActivity.showFab()
                v.fragment_nav_rv.isVerticalScrollBarEnabled = false
            }

            override fun onBottom() {
                isPageBottom = true
                controlOverScroll(navActivity, navActivity.abl_nav, navActivity.verticalOffset)
                v.fragment_nav_rv.isVerticalScrollBarEnabled = false
            }
        }
        v.fragment_nav_rv.clearOnScrollListeners()
        v.fragment_nav_rv.addOnScrollListener(onScrollListener)

        val controller =
                AnimationUtils.loadLayoutAnimation(navActivity, R.anim.layout_animation_from_right)
        v.fragment_nav_rv.layoutAnimation = controller
    }

    /**
     * Loads gospels at different tab in Gospel Page
     */
    private fun loadGospelsFromTabId(navId: Int) {

        var query = navActivity.firestore.collection("gospels")
                .orderBy("createTime", Query.Direction.DESCENDING)

        when (navId) {
            4 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Gen")
            5 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Exo")
            6 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Lev")
            7 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Num")
            8 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Deu")
            9 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jos")
            10 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jug")
            11 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Rut")
            12 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Sa")
            13 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Sa")
            14 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Ki")
            15 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Ki")
            16 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Ch")
            17 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Ch")
            18 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Ezr")
            19 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Neh")
            20 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Est")
            21 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Job")
            22 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Psm")
            23 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Pro")
            24 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Ecc")
            25 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Son")
            26 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Isa")
            27 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jer")
            28 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Lam")
            29 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Eze")
            30 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Dan")
            31 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Hos")
            32 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Joe")
            33 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Amo")
            34 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Oba")
            35 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jon")
            36 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Mic")
            37 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Nah")
            38 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Hab")
            39 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Zep")
            40 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Hag")
            41 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Zec")
            42 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Mal")

            43 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Mat")
            44 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Mak")
            45 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Luk")
            46 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jhn")
            47 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Act")
            48 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Rom")
            49 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Co")
            50 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Co")
            51 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Gal")
            52 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Eph")
            53 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Phl")
            54 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Col")
            55 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Ts")
            56 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Ts")
            57 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Ti")
            58 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Ti")
            59 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Tit")
            60 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Mon")
            61 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Heb")
            62 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jas")
            63 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Pe")
            64 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Pe")
            65 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "1Jn")
            66 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "2Jn")
            67 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "3Jn")
            68 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Jud")
            69 -> query = navActivity.firestore.collection("gospels").orderBy("time", Query.Direction.DESCENDING).whereEqualTo(getString(R.string.desc), "Rev")

        }

        val options = FirestoreRecyclerOptions.Builder<Gospel>()
                //                        .setLifecycleOwner(this@NavFragment)
                .setQuery(query, Gospel::class.java)
                .build()
        gospelAdapter = object : FirestoreRecyclerAdapter<Gospel, NavItemView>(options) {
            @NonNull
            override fun onCreateViewHolder(@NonNull parent: ViewGroup,
                                            viewType: Int): NavItemView {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.nav_item_gospel, parent, false)
                val binding = NavItemGospelBinding.bind(view)
                return NavItemView(binding, navActivity)
            }

            override fun onBindViewHolder(@NonNull holder: NavItemView,
                                          position: Int,
                                          @NonNull model: Gospel) {
                applyViewHolderAnimation(holder)
                holder.bind(model)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                pb_nav?.visibility = View.GONE

                if (itemCount == 0) {
                } else {
                    fragment_nav_rv.scheduleLayoutAnimation()
                }
            }
        }
        gospelAdapter.startListening()
        v.fragment_nav_rv.adapter = gospelAdapter
    }

    /*private fun firestoreRecyclerAdapter(): FirestoreRecyclerAdapter<Setting, NavItemView> {
        val query = navActivity.firestore.collection("mes").orderBy("id", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Setting>()
                .setQuery(query, Setting::class.java)
//                .setLifecycleOwner(navActivity)
                .build()

        return object : FirestoreRecyclerAdapter<Setting, NavItemView>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavItemView {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.nav_item_me, parent, false)
                val binding = NavItemMeBinding.bind(view)
                return NavItemView(binding, navActivity)
            }

            override fun onBindViewHolder(holder: NavItemView, position: Int, model: Setting) {
                holder.bind(model)
                if (position >= 2) {
                    holder.containerView.visibility = View.GONE
                }
            }

            override fun onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
//                mEmptyListMessage.setVisibility(if (itemCount == 0) View.VISIBLE else View.GONE)
                pb_nav.visibility = View.GONE

                if (itemCount == 0) {
                } else {
                    fragment_nav_rv.scheduleLayoutAnimation()
                }
            }
        }
    }*/

    fun showToast(message: String) {
        Toast.makeText(navActivity, message, Toast.LENGTH_SHORT).show();
    }

    override fun showFab() {
//        navActivity.showFAB()
//        if (navId == 1 && cv_nav_frag.visibility == View.GONE) {
//            cv_nav_frag.visibility = View.VISIBLE
//            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.abc_fade_in)
//            cv_nav_frag.startAnimation(fadeIn)
//        }
    }

    override fun hideFab() {
//        navActivity.hideFab()
//        if (navId == 1 && cv_nav_frag.visibility == View.VISIBLE) {
//            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.abc_fade_out)
//            cv_nav_frag.startAnimation(fadeOut)
//            cv_nav_frag.visibility = View.GONE
//        }
    }

//
//    private fun runLayoutAnimation(recyclerView: androidx.recyclerview.widget.RecyclerView) {
//        val animation = AnimationUtils.loadLayoutAnimation(recyclerView.context, R.anim.layout_animation_from_right)
//        recyclerView.layoutAnimation = animation
//        recyclerView.gospelAdapter?.notifyDataSetChanged()
//        recyclerView.scheduleLayoutAnimation()
//    }

    class NavChildFragmentPagerAdapter(
        fm: FragmentManager,
        private val tabTitleList: ArrayList<String>,
        behaviorResumeOnlyCurrentFragment: Int
    ) : androidx.fragment.app.FragmentStatePagerAdapter(fm, behaviorResumeOnlyCurrentFragment) {

        lateinit var currentFragment: HomeFragment

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            val navFragment = HomeFragment()
            navFragment.navId = position + 4
            return navFragment
        }

        override fun getCount(): Int {
            return tabTitleList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitleList[position]
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            currentFragment = `object` as HomeFragment
            super.setPrimaryItem(container, position, `object`)
        }
    }

    fun scrollChildRVToTop() {
        if ((navActivity.navFragmentPagerAdapter.currentFragment as HomeFragment)::navChildFragmentPagerAdapter.isInitialized) {
            (navActivity.navFragmentPagerAdapter.currentFragment as HomeFragment).navChildFragmentPagerAdapter.currentFragment.fragment_nav_rv.smoothScrollToPosition(0) // 为了滚到顶
        }
    }
}



