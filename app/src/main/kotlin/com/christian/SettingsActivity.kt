package com.christian

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.christian.common.showExitButton
import com.christian.nav.me.AboutActivity
import com.christian.nav.shouldEnableDarkMode
import com.christian.nav.switchNightModeIsOn
import com.christian.nav.toolbarTitle
import com.christian.swipe.SwipeBackActivity
import com.christian.util.fixToolbarElevation
import com.christian.util.setToolbarAsUp
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.nav_item_me_for_setting_static.*
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : SwipeBackActivity() {
    private var isOn = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setToolbarAsUp(this, settings_toolbar, getString(R.string.settings))
        fixToolbarElevation(settings_abl)

        sharedPreferences = getSharedPreferences(switchNightModeIsOn
                , Activity.MODE_PRIVATE)

        clear_cache.setOnClickListener {
            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val packageURI = Uri.parse("package:" + "com.christian")
            intent = intent.setData(packageURI)
            startActivity(intent)
        }

        about_us.setOnClickListener {
            val i = Intent(this, AboutActivity::class.java)
            i.putExtra(toolbarTitle, getString(R.string.about))
            startActivity(i)
        }

        dark_mode.setOnClickListener {
            if (sharedPreferences.getBoolean("isOn", false)) {
                switch_nav_item_small.isChecked = false
                // 恢复应用默认皮肤
                shouldEnableDarkMode(DarkModeConfig.NO)
                isOn = false
                sharedPreferences.edit { putBoolean("isOn", isOn) }
            } else {
                switch_nav_item_small.isChecked = true
                // 夜间模式
                shouldEnableDarkMode(DarkModeConfig.YES)
                isOn = true
                sharedPreferences.edit { putBoolean("isOn", isOn) }
            }
        }

        if (intent.getBooleanExtra(showExitButton, false)) {
            exit_settings_activity.visibility = View.VISIBLE
        } else {
            exit_settings_activity.visibility = View.GONE
        }
        exit_settings_activity.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
//                        Snackbar.make("Sign out successful").show()
//                        invalidateSignInUI()
                        exit_settings_activity.visibility = View.GONE
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        switch_nav_item_small.isChecked = sharedPreferences.getBoolean("isOn", false)
    }

    enum class DarkModeConfig {
        YES,
        NO,
        FOLLOW_SYSTEM
    }

}