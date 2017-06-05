package com.christian.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.christian.R;
import com.christian.adapter.HomeAdapter;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * author：Administrator on 2017/4/2 00:19
 * email：lanhuaguizha@gmail.com
 */

@ContentView(R.layout.fragment_home)
public class HomeFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @ViewInject(R.id.recycler_view)
    private RecyclerView recyclerView;
    @ViewInject(R.id.app_bar)
    private AppBarLayout appBarLayout;
    protected HomeAdapter adapter;
    protected RecyclerView.LayoutManager layoutManager;
    protected LayoutManagerType currentLayoutManagerType;
    private String[] dataSet;
    //    @ViewInject(R.id.toolbar_actionbar)
//    private Toolbar toolbar;
    @ViewInject(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.toolbar_actionbar)
    private Toolbar toolbar;
    private static final int SPAN_COUNT = 2;
    private static final int DATA_SET_COUNT = 12 * 2;
    private boolean added;
    private static final int TOP = 0;

    // For clicking the navigation menu to scroll the recycler view to the top when the menu is checked
    public void smoothScrollToPosition() {
        if (recyclerView != null)
            recyclerView.scrollToPosition(TOP);
        if (appBarLayout != null)
            appBarLayout.setExpanded(true, false);
    }

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * //     * @param param1 Parameter 1.
     * //     * @param param2 Parameter 2.
     *
     * @return A new instance of fragment PlusOneFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataSet();
    }

    // If Using xUtils 3 using onViewCreated to replace onCreateView
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
        initData();
    }

    private void initListener() {
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (navigation.getVisibility() != View.VISIBLE) {
////                    SnackbarUtils.dismissSnackbar();
//                    navigation.setVisibility(View.VISIBLE);
//                } else {
////                    SnackbarUtils.showSnackbar(v, getString(R.string.version));
//                    navigation.setVisibility(View.GONE);
//                }
//            }
//        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_share:
                        break;
                    case R.id.menu_more:
                        break;
                }
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        List<String> newDatas = new ArrayList<String>();
//                        for (int i = 0; i <5; i++) {
//                            int index = i + 1;
//                            newDatas.add("new item" + index);
//                        }
//                        adapter.addItem(newDatas);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 700);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initView();
        }
    }

    private void initView() {
        //Recycler View set adapter
        if (recyclerView != null) {
            adapter = new HomeAdapter(dataSet);
            recyclerView.setAdapter(adapter);
            currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
            setRecyclerViewLayoutManager(currentLayoutManagerType);
        }

//        if (bottomNavigationActivity != null && bottomNavigationActivity.getSupportActionBar() != null) {
//            bottomNavigationActivity.getSupportActionBar().setTitle(getString(R.string.title_home));
//        }
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_home));
            if (!added && false) {
                toolbar.inflateMenu(R.menu.menu_share_and_more);
                added = true;
            }

        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }
    }

    private void initData() {
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                layoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                layoutManager = new LinearLayoutManager(getActivity());
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                layoutManager = new LinearLayoutManager(getActivity());
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataSet() {
        dataSet = new String[DATA_SET_COUNT];
        for (int i = 0; i < DATA_SET_COUNT; i++) {
            dataSet[i] = getString(R.string.next_week) + i;
        }
    }

}
