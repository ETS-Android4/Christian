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
package com.christian.common

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.github.anzewei.parallaxbacklayout.ParallaxHelper

/**
 * 业务无关的Application基类
 * Created by 沈钦赐 on 16/21/25.
 */
class CommonApp : Application() {
//    private var refWatcher: RefWatcher? = null
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        resource = applicationContext.resources
        MultiDex.install(this)
        // CrashHandler.getInstance().get().init(this);
        registerActivityLifecycleCallbacks(ParallaxHelper.getInstance())
//        if (hasMemoryLeak()) {
//            refWatcher = LeakCanary.install(this) //预定义的 RefWatcher，同时也会启用一个 ActivityRefWatcher
//        }
//        if (hasCrashLog()) {
//            CrashWoodpecker.fly().to(this) //崩溃异常捕获
//        }
        initNightMode()
    }

    private fun initNightMode() {
//        boolean b1 = (getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)==2;
        val b: Boolean = getNightModeSP(this)
        AppCompatDelegate.setDefaultNightMode(if (b) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

//    protected abstract fun hasMemoryLeak(): Boolean
//    protected abstract fun hasCrashLog(): Boolean
    override fun onTerminate() {
        super.onTerminate()
//        AppManager.getAppManager().AppExit(this)
    }


    companion object {
        var resource: Resources? = null
        lateinit var context: Context

//        @JvmStatic
//        fun getRefWatcher(context: Context?): RefWatcher? {
//            if (context == null) {
//                return null
//            }
//            val application = context.applicationContext as CommonApp
//            return if (application.hasMemoryLeak()) {
//                application.refWatcher
//            } else null
//        }

        /**
         * 根据资源返回String值
         *
         * @param id 资源id
         * @return String
         */
        @JvmStatic
        fun string(id: Int): String {
            return resource!!.getString(id)
        }

        /**
         * 根据资源返回color值
         *
         * @param id 资源id
         * @return int类型的color
         */
        @JvmStatic
        fun color(id: Int): Int {
            return resource!!.getColor(id)
        }

        /**
         * 根据资源翻译Drawable值
         *
         * @param id 资源id
         * @return Drawable
         */
        fun drawable(id: Int): Drawable {
            return resource!!.getDrawable(id)
        }

        fun getNightModeSP(context: Context): Boolean {
            return context.getSharedPreferences("switchNightModeIsOn", MODE_PRIVATE)
                .getBoolean("isOn", false)
        }
    }
}