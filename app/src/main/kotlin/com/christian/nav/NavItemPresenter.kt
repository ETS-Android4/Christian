package com.christian.nav

/**
 * Created by christian on 19-5-31.
 */

import android.util.Log
import com.christian.R
import org.jetbrains.anko.AnkoLogger

/**
 * NavItemPresenter/Adapter is business logic of nav items.
 */
abstract class NavItemPresenter : AnkoLogger {


    /* private fun applyViewHolderAnimation(holder: NavItemView) {
         debug { "mPosition$mPosition" }
         if (holder.adapterPosition > mPosition) {
             navItemView.animateItemView(holder.itemView)
         } else {
             navItemView.clearItemAnimation(holder.itemView)
         }
         mPosition = holder.adapterPosition
     }*/
}

fun generateUrlId(url: String?): Int {
    return when (url) {
        "R.drawable.me" -> R.drawable.me
        "ic_brightness_medium_black_24dp" -> R.drawable.ic_brightness_medium_black_24dp
        "ic_history_black_24dp" -> R.drawable.ic_history_black_24dp
        "ic_library_books_black_24dp" -> R.drawable.ic_library_books_black_24dp
        "ic_crop_free_black_24dp" -> R.drawable.ic_crop_free_black_24dp
        "ic_wallpaper_black_24dp" -> R.drawable.ic_wallpaper_black_24dp
        "R.drawable.ic_settings_black_24dp" -> R.drawable.ic_settings_black_24dp
        "R.drawable.ic_assignment_black_24dp" -> R.drawable.ic_assignment_black_24dp
        "R.drawable.ic_contact_mail_black_24dp" -> R.drawable.ic_contact_mail_black_24dp
        "R.drawable.ic_contact_phone_black_24dp" -> R.drawable.ic_contact_phone_black_24dp
        "ic_star_black_24dp" -> R.drawable.ic_star_black_24dp
        "ic_group_add_black_24dp" -> R.drawable.ic_group_add_black_24dp
        else -> {
            return 0
        }
    }
}

fun generateUrlIdNightMode(url: String?): Int {
    return when (url) {
        "R.drawable.me" -> R.drawable.me
        "ic_brightness_medium_black_24dp" -> R.drawable.ic_brightness_medium_black_24dp
        "ic_history_black_24dp" -> R.drawable.ic_history_black_24dp_night_mode
        "ic_library_books_black_24dp" -> R.drawable.ic_library_books_black_24dp
        "ic_crop_free_black_24dp" -> R.drawable.ic_crop_free_black_24dp
        "ic_wallpaper_black_24dp" -> R.drawable.ic_wallpaper_black_24dp
        "R.drawable.ic_settings_black_24dp" -> R.drawable.ic_settings_black_24dp
        "R.drawable.ic_assignment_black_24dp" -> R.drawable.ic_assignment_black_24dp
        "R.drawable.ic_contact_mail_black_24dp" -> R.drawable.ic_contact_mail_black_24dp
        "R.drawable.ic_contact_phone_black_24dp" -> R.drawable.ic_contact_phone_black_24dp
        "ic_star_black_24dp" -> R.drawable.ic_star_black_24dp_night_mode
        "ic_group_add_black_24dp" -> R.drawable.ic_group_add_black_24dp
        else -> {
            return 0
        }
    }
}

/**
 * item animation
 */
//private var loadNextPage: Boolean = true
var mPosition = 0
fun applyViewHolderAnimation(holder: NavItemView) {
//    if (loadNextPage)
    Log.d("NavItemPresenter", "adapterPosition: ${holder.adapterPosition}")
    Log.d("NavItemPresenter", "mPosition: $mPosition")
    if (holder.adapterPosition >= mPosition) {
        holder.animateItemView(holder.itemView)
    } else {
        holder.clearItemAnimation(holder.itemView)
    }
    mPosition = holder.adapterPosition
}