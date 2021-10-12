package com.christian.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


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

fun EditText.requestFocusWithKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}
//class CommonUtil {
//}