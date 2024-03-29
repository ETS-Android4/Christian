package com.christian.nav.gospel

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.christian.R
import com.christian.common.data.Gospel
import com.christian.multitype.Card
import com.christian.nav.NavActivity
import com.christian.nav.me.AbsAboutActivity
import com.christian.util.ChristianUtil
import com.christian.util.filterImageUrlThroughDetailPageContent
import com.christian.util.restoreScrolledPositionOfDetailPage
import com.christian.view.showPopupMenu
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.lxj.xpopup.interfaces.OnSelectListener
import com.lxj.xpopup.util.XPopupUtils
import com.vincent.blurdialog.BlurDialog
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.about_page_main_activity.*
import kotlinx.android.synthetic.main.nav_activity.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.toast
import ren.qinc.markdowneditors.view.EditorActivity
import java.util.*

private fun NavActivity.initTbWithTitle(title: String) {
    sbl_nav.visibility = View.GONE

    /**
     * set up button
     */
    setSupportActionBar(tb_nav)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.app_name)
    tb_nav.setNavigationOnClickListener { finish() }
}

/**
 * The nav details page contains all the logic of the nav page.
 */
class NavDetailActivity : AbsAboutActivity(), AnkoLogger {

    private lateinit var dialog: BlurDialog
    private lateinit var userId: String
    private lateinit var gospelId: String
    private lateinit var gospelCategory: String
    private lateinit var gospelTime: String
    private lateinit var gospelTitle: String
    private lateinit var gospelContent: String
    private lateinit var gospelAuthor: String
    private lateinit var gospelChurch: String

    private lateinit var gospel: Gospel
    private var registration: ListenerRegistration? = null
    private val snapshots = ArrayList<DocumentSnapshot>()
    private var snapshot: DocumentSnapshot? = null

    var isMovingRight: Boolean = true // true不会崩溃，进入nav detail左滑的时候

    private var lastPosition = 0//位置
    private var lastOffset = 0//偏移量

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        meRef = firestore.collection(getString(R.string.gospels)).document(gospelId)
        startListening()

        /*fab12.setOnClickListener {
            toEditorActivity()
        }
        fab22.setOnClickListener {
            deleteDocument()
        }

        if (auth.currentUser?.uid == userId) {
            menu_yellow.visibility = View.VISIBLE
        } else {
            menu_yellow.visibility = View.GONE
        }*/

//        fixAppBarLayoutElevation(header_layout)
    }

    private fun deleteDocument() {
        dialog = BlurDialog.Builder()
            .radius(10f) //dp
            .isCancelable(true)
            .isOutsideCancelable(true)
            .message("Do you want delete this document?")
            .positiveClick {
                dialog.dismiss()
                val editor = getSharedPreferences("mImg", MODE_PRIVATE).edit()
                editor.putString(gospelTitle, "")
                editor.apply()

                finish()
                firestore.collection(getString(R.string.gospels)).document(gospelTitle)
                    .delete()
                    .addOnSuccessListener {
                        debug { }
                    }
                    .addOnFailureListener { e ->
                        debug { e }
                    }
            }
            .negativeClick {
                dialog.dismiss()
            }
            .dismissListener { /*Toast.makeText(this, "I have no idea about it!", Toast.LENGTH_SHORT).show()*/ }
            .type(BlurDialog.TYPE_DELETE)
            .build(this)
        dialog.show()
    }

    private fun toEditorActivity() {
        val intent = Intent(this@NavDetailActivity, EditorActivity::class.java)
        intent.putExtra(ChristianUtil.DOCUMENT_GOSPEL_PATH, gospelId)
        startActivity(intent)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        menu.removeItem(R.id.menu_options_nav)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_nav_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.menu_share -> {
//                Snackbar.make(getString(R.string.toast_share)).show()
//                true
//            }
//            R.id.menu_favorite -> {
//                snackbar(getString(R.string.toast_favorite)).show()
//                true
//            }
//            R.id.menu_translate -> {
//                snackbar(getString(R.string.toast_translate)).show()
//                true
//            }
//            R.id.menu_read -> {
//                snackbar(getString(R.string.toast_read)).show()
//                true
//            }
            R.id.menu_options_nav_detail -> {
                val onSelectListener = object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        when(position) {
                            0 -> {
                                toast(getString(R.string.action_share))
                            }
                            1 -> {
                                toast(getString(R.string.action_favorite))
                            }
                            2 -> {
                                toEditorActivity()
                            }
                            3 -> {
                                deleteDocument()
                            }
                        }
                    }
                }
                if (auth.currentUser?.uid == userId) {
                    showPopupMenu(
                        findViewById(R.id.menu_options_nav_detail), this@NavDetailActivity, arrayOf(
                            getString(R.string.action_share),
                            getString(R.string.action_favorite),
                            getString(R.string.action_edit),
                            getString(R.string.action_delete),
                        ),
                        XPopupUtils.dp2px(this, -48f),
                        onSelectListener = onSelectListener
                    )
                } else {
                    showPopupMenu(
                        findViewById(R.id.menu_options_nav_detail), this@NavDetailActivity, arrayOf(
                            getString(R.string.action_share),
                            getString(R.string.action_favorite),
                        ),
                        XPopupUtils.dp2px(this, -48f),
                        onSelectListener = onSelectListener
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class NavDetailFragmentPagerAdapter(fm: FragmentManager) : NavActivity.NavFragmentPagerAdapter(
        fm,
    ) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    val gospelDetailFragment = GospelDetailFragment()
                    return gospelDetailFragment
                }
//                1 -> {
//                    val gospelReviewFragment = GospelReviewFragment()
//                    gospelReviewFragment.navId = position + 31
//                    return gospelReviewFragment
//                }
            }
            return super.getItem(position)
        }

        override fun getCount(): Int {
            return 1
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            when (position) {
                0 -> {
                    currentFragment = `object` as GospelDetailFragment
                }
//                1 -> {
//                    currentFragment = `object` as GospelReviewFragment
//                }
            }
            super.setPrimaryItem(container, position, `object`)
        }
    }

    override fun onEvent(documentSnapshots: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Snackbar.make(cl_nav, "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            return
        }

        if (documentSnapshots == null) {
            return
        }

        snapshot = documentSnapshots
        gospel = snapshot?.toObject(Gospel::class.java) ?: Gospel()

        if (items.isNotEmpty())
            items.clear()
        gospelCategory = gospel.classify
        gospelTitle = gospel.title
        gospelContent = gospel.content
//        items.add(Category(gospelTitle))
        items.add(Card(gospelContent))

        gospelAuthor = gospel.author
//        gospelChurch = gospel.church
        gospelTime = gospel.createTime
        userId = gospel.userId

        Glide.with(this)
                .load(filterImageUrlThroughDetailPageContent(gospelContent))
                .transform(BlurTransformation(225)) // Glide Blur
                .into(activity_detail_title_background)
        restoreScrolledPositionOfDetailPage(this, recyclerView, activity_detail_mask)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_detail
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {

        gospelId = intent.getStringExtra("gospelId")
            ?: ""
        gospelCategory = intent.getStringExtra(getString(R.string.category))
                ?: getString(R.string.uncategorized)
        gospelTitle = intent.getStringExtra(getString(R.string.name))
                ?: getString(R.string.no_title)
//        gospelContent = intent.getStringExtra(getString(R.string.content_lower_case))
//                ?: getString(R.string.no_content)
//        gospelAuthor = intent.getStringExtra(getString(R.string.author))
//                ?: getString(R.string.no_author)
//        gospelChurch = intent.getStringExtra(R.string.church_lower_case.toString())
//                ?: getString(R.string.no_church)
//        gospelTime = intent.getStringExtra(getString(R.string.time)) ?: getString(R.string.no_time)
        userId = intent.getStringExtra(getString(R.string.userId)) ?: ""

//        collapsingToolbar.subtitle = gospelAuthor
        collapsingToolbar.title = gospelTitle
    }
}
