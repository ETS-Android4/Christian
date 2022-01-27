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
package ren.qinc.markdowneditors.presenter

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christian.R
import com.christian.common.data.Result
import com.christian.common.data.Gospel
import com.christian.common.getDateAndCurrentTime
import com.christian.common.ui.editor.EditorViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import ren.qinc.markdowneditors.base.mvp.BasePresenter
import ren.qinc.markdowneditors.view.EditorActivity
import ren.qinc.markdowneditors.view.EditorFragment
import java.io.File
import java.util.*

/**
 * 编辑界面Presenter
 * Created by 沈钦赐 on 16/1/18.
 */
class EditorFragmentPresenter : BasePresenter<IEditorFragmentView?>() {
    //当前文件路径
    private val filePath: String? = null

    //当前本地文件名字(如果创建,则为"",可以和当前标题输入框的值不同)
    private var fileName: String? = null

    //时候为新创建文件
    private var isCreateFile = false

    /**
     * 加载当前文件
     */
    fun loadFile() {
        mCompositeSubscription.add(mDataManager.readFile(mDFile)
            .subscribe({ content: String? ->
                if (mvpView == null) return@subscribe
                mvpView!!.onReadSuccess(fileName!!, content!!)
            }) { throwable: Throwable ->
                callFailure(
                    -1,
                    throwable.message,
                    IEditorFragmentView.CALL_LOAOD_FILE
                )
            })
    }

    val mDFile: File
        get() = File(filePath, fileName)
    private var textChanged = false

    /**
     * 刷新保存图标的状态
     */
    fun refreshMenuIcon() {
        if (mvpView != null) return
        if (textChanged) mvpView!!.otherSuccess(IEditorFragmentView.CALL_NO_SAVE) else mvpView!!.otherSuccess(
            IEditorFragmentView.CALL_SAVE
        )
    }

    fun textChange() {
        textChanged = true
        if (mvpView != null) {
            mvpView!!.otherSuccess(IEditorFragmentView.CALL_NO_SAVE)
        }
    }

    /**
     * 保存当前内容
     *
     * @param name    the name
     * @param content the content
     */
    fun save(name: String, content: String?) {
        saveForExit(name, content, false)
    }

    /**
     * 保存当前内容并退出
     *
     * @param name    the name
     * @param content the content
     * @param exit    the exit
     */
    fun saveForExit(name: String, content: String?, exit: Boolean) {
        if (TextUtils.isEmpty(name)) {
            callFailure(-1, "名字不能为空", IEditorFragmentView.CALL_SAVE)
            return
        }
        if (content == null) return

        //上一次文件名为空
        if (TextUtils.isEmpty(fileName)) {
            //新建文件
            if (isCreateFile) {
                //新创建文件，但是文件已经存在了
                val file = File(filePath, "$name.md")
                if (!file.isDirectory && file.exists()) {
                    callFailure(-1, "文件已经存在", IEditorFragmentView.CALL_SAVE)
                    return
                }
            }
            fileName = name
        }
        if (!fileName!!.endsWith(".md") &&
            !fileName!!.endsWith(".markdown") &&
            !fileName!!.endsWith(".mdown")
        ) {
            fileName = "$fileName.md"
        }
        val editorFragment = mvpView as EditorFragment

        val activity = (mvpView as EditorFragment?)!!.activity as EditorActivity?

        val gospel = Gospel(
            title = editorFragment.mName.text.toString().trim { it <= ' ' },
            classify = editorFragment.et_editor_topic?.text.toString().trim { it <= ' ' },
            content = editorFragment.mContent?.text.toString().trim { it <= ' ' },
            createTime = getDateAndCurrentTime(),
            author = FirebaseAuth.getInstance().currentUser?.email ?: FirebaseAuth.getInstance().currentUser?.uid.toString(),
        )
        val editorViewModel = ViewModelProvider(editorFragment)[EditorViewModel::class.java]
        if (editorFragment.mName.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            if (editorFragment.mContent.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                editorViewModel.viewModelScope.launch {
                    when (editorViewModel.saveGospel(gospel)) {
                        is Result.Success -> {
                            isCreateFile = false
                            textChanged = false
                            if (!rename(name)) {
                                callFailure(-1, "重命名失败", IEditorFragmentView.CALL_SAVE)
                            }
                            if (mvpView != null) {
                                if (exit) mvpView!!.otherSuccess(IEditorFragmentView.CALL_EXIT) else mvpView!!.otherSuccess(
                                    IEditorFragmentView.CALL_SAVE
                                )
                            }
                        }
                        is Result.Error -> {
                            callFailure(
                                -1,
                                editorFragment.getString(R.string.please_sign_in),
                                IEditorFragmentView.CALL_SAVE
                            )
                            activity?.startSignInActivity()
                        }
                        else -> {}
                    }
                }
            } else {
                Snackbar.make(
                    editorFragment.mName,
                    editorFragment.getString(R.string.content_empty),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            Snackbar.make(
                editorFragment.mContent,
                editorFragment.getString(R.string.title_empty),
                Snackbar.LENGTH_SHORT
            ).show()
        }

//        }

//        mDataManager.saveFile(getMDFile(), content).subscribe(success -> {
//            if (success) {
//                isCreateFile = false;
//                textChanged = false;
//                if (!rename(name)) {
//                    callFailure(-1, "重命名失败", IEditorFragmentView.CALL_SAVE);
//                    return;
//                }
//                if (getMvpView() != null) {
//                    if (exit)
//                        getMvpView().otherSuccess(IEditorFragmentView.CALL_EXIT);
//                    else
//                        getMvpView().otherSuccess(IEditorFragmentView.CALL_SAVE);
//                }
//            } else {
//                callFailure(-1, "保存失败", IEditorFragmentView.CALL_SAVE);
//            }
//        }, throwable -> {
//            callFailure(-1, "保存失败", IEditorFragmentView.CALL_SAVE);
//        });
    }

    fun getDocument(editorFragment: EditorFragment) {
        val editorViewModel = ViewModelProvider(editorFragment)[EditorViewModel::class.java]
        editorViewModel.viewModelScope.launch {
//            editorViewModel.getWriting()
        }
        val documentReference = editorFragment.firebaseFirestore.collection(
            editorFragment.getString(
                R.string.gospels
            )
        ).document(editorFragment.documentGospelPath)
        documentReference.get().addOnCompleteListener(editorFragment.requireActivity()) { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.data)
                        editorFragment.et_editor_topic.setText(document[editorFragment.getString(R.string.desc)] as CharSequence?)
                        editorFragment.mName.setText(document[editorFragment.getString(R.string.name)] as CharSequence?)
                        editorFragment.mAuthor.setText(document[editorFragment.getString(R.string.author)] as CharSequence?)
                        //                            editorFragment.mChurch.setText((CharSequence) document.get(editorFragment.getString(R.string.church_lower_case)));
                        editorFragment.mContent.setText(document[editorFragment.getString(R.string.content_lower_case)] as CharSequence?)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        }
    }

    private fun rename(newName: String): Boolean {
        val end = fileName!!.lastIndexOf(".")
        val name = fileName!!.substring(0, end)
        if (newName == name) return true
        val suffix = fileName!!.substring(end, fileName!!.length)
        if (suffix.endsWith(".md") ||
            suffix.endsWith(".markdown") ||
            suffix.endsWith(".mdown")
        ) {
            //重命名
            val oldFile = mDFile
            val newPath = File(filePath, newName + suffix)
            if (oldFile.absolutePath == newPath.absolutePath) return true
            fileName = newPath.name
            return if (newPath.exists()) false else oldFile.renameTo(newPath)
        }
        return false
    }

    val isSave: Boolean
        get() = !textChanged

    companion object {
        private val TAG = EditorFragmentPresenter::class.java.simpleName
    }
}