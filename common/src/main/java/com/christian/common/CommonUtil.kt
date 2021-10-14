package com.christian.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/** 判断设置页面显不显示退出按钮 */
const val showExitButton = "showExitButton"

/**
 * 判断是不是夜间模式
 */
fun getDarkMode(): Boolean {
    return false
}

/**
 * 获取屏幕宽度
 */
fun getDisplayWidth(context: Context): Int {
    val dm = context.resources.displayMetrics
//    var density: Float = dm.density
//    val width: Int = dm.widthPixels
//    var height: Int = dm.heightPixels
    return dm.widthPixels
}

/**
 * 获取屏幕高度
 */
fun getDisplayHeight(context: Context): Int {
    val dm = context.resources.displayMetrics
    return dm.heightPixels
}

fun EditText.requestFocusWithKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}
//class CommonUtil {
//}