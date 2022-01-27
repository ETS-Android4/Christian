package com.christian.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.christian.common.ui.editor.EditorViewModel
import java.text.SimpleDateFormat
import java.util.*

const val GOSPELS = "gospels"
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

/**
 * 获取当前日期和时间
 */
fun getDateAndCurrentTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}
//class CommonUtil {
//}