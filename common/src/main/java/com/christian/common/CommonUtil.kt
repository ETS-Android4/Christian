package com.christian.common

import android.content.Context


fun getDisplayWidth(context: Context): Int {
    val dm = context.resources.displayMetrics
    var density: Float = dm.density
    val width: Int = dm.widthPixels
    var height: Int = dm.heightPixels
    return width
}

fun getDisplayHeight(context: Context): Int {
    val dm = context.resources.displayMetrics
    return dm.heightPixels
}
//class CommonUtil {
//}