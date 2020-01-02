package com.christian.nav

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.christian.R
import com.christian.data.Disciple
import com.christian.data.Gospel
import com.christian.data.MeBean
import com.christian.data.Setting
import com.christian.nav.gospel.NavDetailActivity
import com.christian.nav.me.AboutActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_editor.view.*
import kotlinx.android.synthetic.main.nav_item_gospel.*
import kotlinx.android.synthetic.main.nav_item_me.*
import org.jetbrains.anko.AnkoLogger

/**
 * NavItemView/NavItemHolder is view logic of nav items.
 */

open class NavItemView(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer, AnkoLogger {

    init {
//        containerView.login_nav_item.setOnClickListener {
//        }

//        when (adapterPosition) {
//            1 -> {
//                itemView.setOnClickListener {
//                    if (isOn) {
//                        itemView.findViewById<Switch>(R.id.switch_nav_item_small).isChecked = false
//                        isOn = false
//                    } else {
//                        itemView.findViewById<Switch>(R.id.switch_nav_item_small).isChecked = true
//                        isOn = true
//                    }
//                }
//            }
//            else -> {
//                itemView.setOnClickListener {
//                    val i = Intent(itemView.context, NavDetailActivity::class.java)
//                    i.putExtra(toolbarTitle, presenter.getTitle(adapterPosition))
//                    itemView.context.startActivity(i)
//                }
//            }
//        }
//        itemView.findViewById<AppCompatImageButton>(R.id.ib_nav_item).setOnClickListener { v: View -> showPopupMenu(v) }

    }

    fun initView() {

//        cv_nav_item.radius = 0f
//        cv_nav_item.foreground = null
//        tv_subtitle_nav_item.visibility = View.GONE
//        tv_title_nav_item.visibility = View.GONE
//        tv_detail_nav_item.textColor = ResourcesCompat.getColor(itemView.resources, R.color.text_color_primary, itemView.context.theme)
//        tv_detail_nav_item.textSize = 18f
//        tv_detail_nav_item.maxLines = Integer.MAX_VALUE
    }

    fun animateItemView(itemView: View) {

        val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.up_from_bottom)

        itemView.startAnimation(animation)

    }

    fun clearItemAnimation(itemView: View) {
        itemView.clearAnimation()
    }

    fun bind(gospel: MeBean) {
        if (gospel.img.isNotBlank()) {
            iv_nav_item.visibility = View.VISIBLE
            Glide.with(containerView.context).load(gospel.img).into(iv_nav_item)
        } else {
            iv_nav_item.visibility = View.GONE
        }
        tv_title_nav_item.text = gospel.desc
        tv_subtitle_nav_item.text = gospel.name
        tv_detail_nav_item.text = gospel.content
        textView.text = gospel.author + "·" + gospel.church + "·" + gospel.time
//        textView2.text = gospel.church
//        textView3.text = gospel.time
        containerView.setOnClickListener {
            startGospelDetailActivity(gospel)
        }
        tv_title_nav_item.setOnClickListener {
            //            gospelId = gospel.id
            startGospelDetailActivity(gospel)
        }
    }

    private fun startGospelDetailActivity(gospel: MeBean) {
        val intent = Intent(containerView.context, NavDetailActivity::class.java)
        intent.putExtra(containerView.context.getString(R.string.category), gospel.desc)
        intent.putExtra(containerView.context.getString(R.string.name), gospel.name)
        intent.putExtra(containerView.context.getString(R.string.content_lower_case), gospel.content)
        intent.putExtra(containerView.context.getString(R.string.author), gospel.author)
        intent.putExtra(containerView.context.getString(R.string.church_lower_case), gospel.church)
        intent.putExtra(containerView.context.getString(R.string.time), gospel.time)

        intent.putExtra(toolbarTitle, gospel.name)
        containerView.context.startActivity(intent)
    }

    private var isOn = false
    fun bind(setting: Setting) {
        when (adapterPosition) {
            0 -> {
                containerView.setOnClickListener {
                    if (isOn) {
                        containerView.findViewById<Switch>(R.id.switch_nav_item_small).isChecked = false
                        // 恢复应用默认皮肤
//                        Aesthetic.config {
//                            activityTheme(R.style.Christian)
//                            isDark(false)
//                            textColorPrimary(res = R.color.text_color_primary)
//                            textColorSecondary(res = R.color.text_color_secondary)
//                            attribute(R.attr.my_custom_attr, res = R.color.default_background_nav)
//                            attribute(R.attr.my_custom_attr2, res = R.color.white)
//                        }
                        isOn = false
                    } else {
                        containerView.findViewById<Switch>(R.id.switch_nav_item_small).isChecked = true
                        // 夜间模式
//                        Aesthetic.config {
//                            activityTheme(R.style.ChristianDark)
//                            isDark(true)
//                            textColorPrimary(res = android.R.color.primary_text_dark)
//                            textColorSecondary(res = android.R.color.secondary_text_dark)
//                            attribute(R.attr.my_custom_attr, res = R.color.text_color_primary)
//                            attribute(R.attr.my_custom_attr2, res = R.color.background_material_dark)
//                        }
                        isOn = true
                    }
                }
            }
            4 -> {
                containerView.setOnClickListener {
                    // 老的跳转设置移到了NavActivity页的options menu

                    val i = Intent(containerView.context, AboutActivity::class.java)
                    i.putExtra(toolbarTitle, getTitle(setting, adapterPosition))
                    containerView.context.startActivity(i)
                }
            }
            else -> {
                containerView.setOnClickListener {
                    val i = Intent(containerView.context, NavDetailActivity::class.java)
                    i.putExtra(toolbarTitle, getTitle(setting, adapterPosition))
                    containerView.context.startActivity(i)
                }
            }
        }

        if (adapterPosition == 0) {
            val sharedPreferences = containerView.context.getSharedPreferences("christian", Activity.MODE_PRIVATE)
            val string = sharedPreferences.getString("sunrise", "") ?: ""
            val string1 = sharedPreferences.getString("sunset", "") ?: ""
            switch_nav_item_small.visibility = View.VISIBLE

            if (string.isNotEmpty() && string.isNotEmpty()) {
                val sunriseString = string.substring(11, 19)
                val sunsetString = string1.substring(11, 19)
                switch_nav_item_small.text = String.format(containerView.context.getString(R.string.sunrise_sunset), sunriseString, sunsetString)
//                switch_nav_item_small.visibility = View.VISIBLE
            } else {
                switch_nav_item_small.text = containerView.context.getString(R.string.no_location_service)
//                switch_nav_item_small.visibility = View.GONE
            }
        }
        tv_nav_item_small.text = setting.name
        tv2_nav_item_small.text = setting.desc
        val url = setting.url
        Glide.with(containerView.context).load(generateUrlId(url)).into(iv_nav_item_small)
    }

    private fun getTitle(setting: Setting, pos: Int): String {

//        return when (navId) {
//            VIEW_HOME, VIEW_GOSPEL, VIEW_DISCIPLE -> {
//                snapshots[pos].data?.get("subtitle").toString()
//            }
//            VIEW_ME -> {
        return when (pos) {
            0 -> {
                containerView.context.getString(R.string.me)
            }
            in 1..5 -> {
                setting.name
            }
            else -> ""
        }
//            }
//            else -> {
//                return ""
//            }
    }

    fun bind(disciple: Disciple) {
        tv_title_nav_item.text = disciple.id
        tv_subtitle_nav_item.text = disciple.name
    }

}