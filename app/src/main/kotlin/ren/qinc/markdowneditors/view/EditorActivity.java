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

package ren.qinc.markdowneditors.view;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.christian.R;
import com.christian.util.ChristianUtil;
import com.christian.util.UtilsKt;
import com.christian.view.CustomViewPager;
import com.christian.view.ViewUtilKt;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.util.XPopupUtils;
import com.vincent.blurdialog.BlurDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import ren.qinc.markdowneditors.AppContext;
import ren.qinc.markdowneditors.base.BaseApplication;
import ren.qinc.markdowneditors.base.BaseToolbarActivity;
import ren.qinc.markdowneditors.event.RxEvent;
import ren.qinc.markdowneditors.event.RxEventBus;
import ren.qinc.markdowneditors.lib.ExpandableLinearLayout;
import ren.qinc.markdowneditors.presenter.IEditorActivityView;
import ren.qinc.markdowneditors.utils.Check;
import ren.qinc.markdowneditors.utils.FileUtils;
import ren.qinc.markdowneditors.utils.SystemBarUtils;
import ren.qinc.markdowneditors.utils.Toast;
import ren.qinc.markdowneditors.widget.TabIconView;

import static com.christian.nav.NavPresenterKt.RC_SIGN_IN;


public class EditorActivity extends BaseToolbarActivity implements IEditorActivityView, View.OnClickListener {
    public static final String SHARED_ELEMENT_NAME = "SHARED_ELEMENT_NAME";
    public static final String SHARED_ELEMENT_COLOR_NAME = "SHARED_ELEMENT_COLOR_NAME";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_Folder = "folder";
    public String mImg;

    private EditorFragment mEditorFragment;
    private EditorMarkdownFragment mEditorMarkdownFragment;

    private String mName;
    private String currentFilePath;

    @Bind(R.id.action_other_operate)
    protected ExpandableLinearLayout mExpandLayout;
    @Bind(R.id.pager)
    public CustomViewPager mViewPager;
    private TabIconView mTabIconView;
    private String documentGospelPath;
    private StorageReference metadataRef;
    private Task<Uri> downloadUrl;

    @Override
    public int getLayoutId() {
        return R.layout.activity_editor;
    }

    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        ViewCompat.setTransitionName(mViewPager, SHARED_ELEMENT_NAME);
//        ViewCompat.setTransitionName(mViewPager, SHARED_ELEMENT_COLOR_NAME);
//        mExpandLayout = (ExpandableLinearLayout) getLayoutInflater().inflate(R.layout.view_edit_operate, getAppBar(), false);
//        getAppBar().addView(mExpandLayout);

        getIntentData();
        mEditorFragment = EditorFragment.getInstance(documentGospelPath);
        mEditorMarkdownFragment = EditorMarkdownFragment.getInstance();

        initViewPager();
        initTab();
        mExpandLayout.toggle();
    }

    private void initViewPager() {
        mViewPager.setAdapter(new EditFragmentAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                UtilsKt.setPagePosition(position);
                UtilsKt.setPagePositionOffset(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                //更新标题
                if (position == 0)
                    getToolbar().setTitle("");
                else if (mName != null)
                    getToolbar().setTitle(mName);

                //刷新渲染数据
                if (position == 1) {
                    RxEventBus.getInstance().send(new RxEvent(RxEvent.TYPE_REFRESH_NOTIFY));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void initStatusBar() {
        SystemBarUtils.tintStatusBar(this, getResources().getColor(R.color.colorPrimary));
    }

    private void initTab() {
        mTabIconView = (TabIconView) findViewById(R.id.tabIconView);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_list_bulleted, R.id.id_shortcut_list_bulleted, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_list_numbers, R.id.id_shortcut_format_numbers, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_insert_link, R.id.id_shortcut_insert_link, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_insert_photo, R.id.id_shortcut_insert_photo, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_console, R.id.id_shortcut_console, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_bold, R.id.id_shortcut_format_bold, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_italic, R.id.id_shortcut_format_italic, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_1, R.id.id_shortcut_format_header_1, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_2, R.id.id_shortcut_format_header_2, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_3, R.id.id_shortcut_format_header_3, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_quote, R.id.id_shortcut_format_quote, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_xml, R.id.id_shortcut_xml, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_minus, R.id.id_shortcut_minus, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_strikethrough, R.id.id_shortcut_format_strikethrough, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_grid, R.id.id_shortcut_grid, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_4, R.id.id_shortcut_format_header_4, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_5, R.id.id_shortcut_format_header_5, this);
        mTabIconView.addTab(R.drawable.ic_shortcut_format_header_6, R.id.id_shortcut_format_header_6, this);
    }

    @Override
    public void otherSuccess(int flag) {
    }

    @Override
    public void onFailure(int errorCode, String message, int flag) {
        switch (flag) {
            default:
                BaseApplication.showSnackbar(getWindow().getDecorView(), message);
                break;
        }
    }


    @Override
    public void showWait(String message, boolean canBack, int flag) {
        super.showWaitDialog(message, canBack);
    }

    @Override
    public void hideWait(int flag) {
        super.hideWaitDialog();
    }

    @Override
    public void onNameChange(@NonNull String name) {
        this.mName = name;
    }

    private final int SYSTEM_GALLERY = 1;

    @Override
    public void onClick(View v) {
        if (R.id.id_shortcut_insert_photo == v.getId()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);// Pick an item fromthe
            intent.setType("image/*");// 从所有图片中进行选择
            startActivityForResult(intent, SYSTEM_GALLERY);
            return;
        } else if (R.id.id_shortcut_insert_link == v.getId()) {
            //插入链接
            insertLink();
            return;
        } else if (R.id.id_shortcut_grid == v.getId()) {
            //插入表格
            insertTable();
            return;
        }
        //点击事件分发
        mEditorFragment.getPerformEditable().onClick(v);
    }

    private class EditFragmentAdapter extends FragmentPagerAdapter {

        public EditFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mEditorFragment;
            }
            return mEditorMarkdownFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void initData() {
    }

    private void getIntentData() {
        Intent intent = this.getIntent();
        int flags = intent.getFlags();
        documentGospelPath = intent.getStringExtra(ChristianUtil.DOCUMENT_GOSPEL_PATH);
        if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (intent.getAction() != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                if (SCHEME_FILE.equals(intent.getScheme())) {
                    //文件
                    String type = getIntent().getType();
                    // mImportingUri=file:///storage/emulated/0/Vlog.xml
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = intent.getData();

                    if (uri != null && SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
                        //这是一个文件
                        currentFilePath = FileUtils.uri2FilePath(getBaseContext(), uri);
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    protected String getTitleString() {
        return "";
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    private MenuItem mActionOtherOperate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_act, menu);
        mActionOtherOperate = menu.findItem(R.id.action_other_operate);
        if (mExpandLayout.isExpanded())
            //展开，设置向上箭头
            mActionOtherOperate.setIcon(R.drawable.ic_arrow_up);
        else
            mActionOtherOperate.setIcon(R.drawable.ic_arrow_down);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (mEditorFragment.onBackPressed()) {
                return true;
            }
        } else if (itemId == R.id.action_other_operate) {//展开和收缩
            if (!mExpandLayout.isExpanded())
                //没有展开，但是接下来就是展开，设置向上箭头
                mActionOtherOperate.setIcon(R.drawable.ic_arrow_up);
            else
                mActionOtherOperate.setIcon(R.drawable.ic_arrow_down);
            mExpandLayout.toggle();
            return true;
        } else if (itemId == R.id.menu_editor_options) {
            if (mViewPager.getCurrentItem() == 0) {
                ViewUtilKt.showPopupMenu(findViewById(R.id.menu_editor_options), this, new String[]{getString(R.string.action_share), getString(R.string.action_helper), getString(R.string.action_preview)}, XPopupUtils.dp2px(this, -48f), (position, text) -> {
                    switch (position) {
                        case 0:
//                            mViewPager.getCurr
                            break;
                        case 1:
                            CommonMarkdownActivity.startHelper(this);
                            break;
                        case 2:
                            mViewPager.setCurrentItem(1, true);
                            break;
                    }
                });
            } else if (mViewPager.getCurrentItem() == 1) {
                ViewUtilKt.showPopupMenu(findViewById(R.id.menu_editor_options), this, new String[]{getString(R.string.action_share), getString(R.string.action_helper), getString(R.string.action_edit)}, XPopupUtils.dp2px(this, -48f), (position, text) -> {
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            CommonMarkdownActivity.startHelper(this);
                            break;
                        case 2:
                            mViewPager.setCurrentItem(0, true);
                            break;
                    }
                });
            }
        }
     /*   else if (itemId == R.id.action_preview) {//预览
            mViewPager.setCurrentItem(1, true);
            return true;
        } else if (itemId == R.id.action_edit) {//编辑
            mViewPager.setCurrentItem(0, true);
            return true;
        } else if (itemId == R.id.action_helper) {
            CommonMarkdownActivity.startHelper(this);
            return true;
//            case R.id.action_setting://设置
//                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mExpandLayout.isExpanded())
                //没有展开，但是接下来就是展开，设置向上箭头
                mActionOtherOperate.setIcon(R.drawable.ic_arrow_up);
            else
                mActionOtherOperate.setIcon(R.drawable.ic_arrow_down);
            mExpandLayout.toggle();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mEditorFragment.onBackPressed()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        RxEventBus.getInstance().send(new RxEvent(RxEvent.TYPE_REFRESH_FOLDER));
        super.onPause();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == SYSTEM_GALLERY) {
            Uri uri = data.getData();
            String[] pojo = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.managedQuery(uri, pojo, null, null, null);
            if (cursor != null) {
//                    ContentResolver cr = this.getContentResolver();
                int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(colunm_index);
                //以上代码获取图片路径
                Uri.fromFile(new File(path));//Uri.decode(imageUri.toString())
//                mEditorFragment.getPerformEditable().perform(R.id.id_shortcut_insert_photo, Uri.fromFile(new File(path)));

                UtilsKt.requestStoragePermission(this, path);
            } else {
                Toast.showShort(this, "图片处理失败");
            }
        }

    }


    public void requestStoragePermissionAccepted(@NotNull String path) {

        BlurDialog dialog = ChristianUtil.showWaitingDialog(this);

        // Upload to firestore
        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(path);
        InputStream stream = null;
        try {
            // ToDo Permissions: Storage Permission
            stream = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        UploadTask uploadTask;
        if (stream != null) {
            uploadTask = storageRef.putStream(stream);
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads

                startSignInActivity();
                AppContext.showSnackbar(EditorActivity.this.mViewPager, getString(R.string.upload_error));
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                if (taskSnapshot.getMetadata() != null) {
                    metadataRef = taskSnapshot.getMetadata().getReference();
                    if (metadataRef != null) {
                        downloadUrl = metadataRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            mEditorFragment.getPerformEditable().perform(R.id.id_shortcut_insert_photo, uri);
                            dialog.dismiss();
                        });
                    }
                }

            });
        }
    }

    /**
     * 插入表格
     */
    private void insertTable() {
        View rootView = LayoutInflater.from(this).inflate(R.layout.view_common_input_table_view, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("插入表格")
                .setView(rootView)
                .show();

        TextInputLayout rowNumberHint = (TextInputLayout) rootView.findViewById(R.id.rowNumberHint);
        TextInputLayout columnNumberHint = (TextInputLayout) rootView.findViewById(R.id.columnNumberHint);
        EditText rowNumber = (EditText) rootView.findViewById(R.id.rowNumber);
        EditText columnNumber = (EditText) rootView.findViewById(R.id.columnNumber);


        rootView.findViewById(R.id.sure).setOnClickListener(v -> {
            String rowNumberStr = rowNumber.getText().toString().trim();
            String columnNumberStr = columnNumber.getText().toString().trim();

            if (Check.isEmpty(rowNumberStr)) {
                rowNumberHint.setError("不能为空");
                return;
            }
            if (Check.isEmpty(columnNumberStr)) {
                columnNumberHint.setError("不能为空");
                return;
            }


            if (rowNumberHint.isErrorEnabled())
                rowNumberHint.setErrorEnabled(false);
            if (columnNumberHint.isErrorEnabled())
                columnNumberHint.setErrorEnabled(false);

            mEditorFragment.getPerformEditable().perform(R.id.id_shortcut_grid, Integer.parseInt(rowNumberStr), Integer.parseInt(columnNumberStr));
            dialog.dismiss();
        });

        rootView.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 插入链接
     */
    private void insertLink() {
        View rootView = LayoutInflater.from(this).inflate(R.layout.view_common_input_link_view, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("插入链接")
                .setView(rootView)
                .show();

        TextInputLayout titleHint = (TextInputLayout) rootView.findViewById(R.id.inputNameHint);
        TextInputLayout linkHint = (TextInputLayout) rootView.findViewById(R.id.inputHint);
        EditText title = (EditText) rootView.findViewById(R.id.name);
        EditText link = (EditText) rootView.findViewById(R.id.text);


        rootView.findViewById(R.id.sure).setOnClickListener(v -> {
            String titleStr = title.getText().toString().trim();
            String linkStr = link.getText().toString().trim();

            if (Check.isEmpty(titleStr)) {
                titleHint.setError("不能为空");
                return;
            }
            if (Check.isEmpty(linkStr)) {
                linkHint.setError("不能为空");
                return;
            }

            if (titleHint.isErrorEnabled())
                titleHint.setErrorEnabled(false);
            if (linkHint.isErrorEnabled())
                linkHint.setErrorEnabled(false);

            mEditorFragment.getPerformEditable().perform(R.id.id_shortcut_insert_link, titleStr, linkStr);
            dialog.dismiss();
        });

        rootView.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    public void startSignInActivity() {
// Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
//                new AuthUI.IdpConfig.FacebookBuilder().build(),
//                new AuthUI.IdpConfig.TwitterBuilder().build());

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UtilsKt.dispatchTouchEvent( this, ev);
        return super.dispatchTouchEvent(ev);
    }

}
