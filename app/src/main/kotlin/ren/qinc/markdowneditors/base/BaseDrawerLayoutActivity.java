/*
 * Copyright 2016. SHENQINCI(沈钦赐)<dev@qinc.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ren.qinc.markdowneditors.base;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.christian.R;
import com.christian.common.CommonApp;
import com.google.android.material.navigation.NavigationView;

import butterknife.Bind;
import ren.qinc.markdowneditors.utils.SystemBarUtils;


/**
 * 带侧滑菜单的Activity
 * Created by 沈钦赐 on 16/1/15.
 */
public abstract class BaseDrawerLayoutActivity extends BaseToolbarActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.id_drawer_layout)
    protected DrawerLayout mDrawerLayout;
    @Bind(R.id.id_navigation_view)
    protected NavigationView mNavigationView;

    protected DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected NavigationView getNavigationView() {
        return mNavigationView;
    }

    @Override
    protected void init() {
        super.init();
        // 如果要用这个类,这两个东西不能为空
        if (mDrawerLayout == null || mNavigationView == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + ":要使用BaseDrawerLayoutActivity，必须在布局里面增加id为‘id_drawer’的DrawerLayout");
        }
        initDrawer();
    }

    protected void initStatusBar() {
        SystemBarUtils.tintStatusBarForDrawer(this, mDrawerLayout, getResources().getColor(R.color.colorPrimary));
    }

    @SuppressLint("ResourceType")
    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(this);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{{android.R.attr.state_checked, android.R.attr.state_enabled},
                        {android.R.attr.state_enabled},
                        {}},
                new int[]{CommonApp.color(R.color.colorPrimary), CommonApp.color(R.color.colorSecondaryText), 0xffDCDDDD});
        mNavigationView.setItemIconTintList(colorStateList);//设置图标的颜色变化
        mNavigationView.setItemTextColor(colorStateList);//设置item的颜色变化
        if (getDefaultMenuItemId() > 0)
            mNavigationView.setCheckedItem(getDefaultMenuItemId());//设置默认选择
    }

    /**
     * 默认选择的菜单id
     * Gets default menu item id.
     *
     * @return the default menu item id
     */
    @IdRes
    protected int getDefaultMenuItemId() {
        return -1;
    }

    @Override
    public void onBackPressed() {//返回按钮
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
