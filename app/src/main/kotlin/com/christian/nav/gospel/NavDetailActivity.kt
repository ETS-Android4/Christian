package com.christian.nav.gospel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.christian.R
import com.christian.data.MeBean
import com.christian.multitype.Card
import com.christian.nav.NavActivity
import com.christian.nav.me.AbsAboutActivity
import com.christian.util.ChristianUtil
import com.christian.util.fixAppBarLayoutElevation
import com.christian.util.restoreScrolledPositionOfDetailPage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.vincent.blurdialog.BlurDialog
import kotlinx.android.synthetic.main.about_page_main_activity.*
import kotlinx.android.synthetic.main.nav_activity.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
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
    private lateinit var gospelCategory: String
    private lateinit var gospelTime: String
    private lateinit var gospelTitle: String
    private lateinit var gospelContent: String
    private lateinit var gospelAuthor: String
    private lateinit var gospelChurch: String

    private lateinit var meBean: MeBean
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
        meRef = firestore.collection(getString(R.string.gospels)).document(gospelTitle)
        startListening()

        fab12.setOnClickListener {
            val intent = Intent(this@NavDetailActivity, EditorActivity::class.java)
            intent.putExtra(ChristianUtil.DOCUMENT_GOSPEL_PATH, gospelTitle)
            startActivity(intent)
        }
        fab22.setOnClickListener {
            dialog = BlurDialog.Builder()
                    .radius(10f) //dp
                    .isCancelable(true)
                    .isOutsideCancelable(true)
                    .message("Do you want delete this document?")
                    .positiveClick {
                        dialog.dismiss()
                        val editor = getSharedPreferences("mImg", Context.MODE_PRIVATE).edit()
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

        if (auth.currentUser?.uid == userId) {
            menu_yellow.visibility = View.VISIBLE
        } else {
            menu_yellow.visibility = View.GONE
        }

//        fixAppBarLayoutElevation(header_layout)
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
            R.id.menu_share -> {
//                Snackbar.make(getString(R.string.toast_share)).show()
                true
            }
            R.id.menu_favorite -> {
//                snackbar(getString(R.string.toast_favorite)).show()
                true
            }
            R.id.menu_translate -> {
//                snackbar(getString(R.string.toast_translate)).show()
                true
            }
//            R.id.menu_read -> {
//                snackbar(getString(R.string.toast_read)).show()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class NavDetailFragmentPagerAdapter(fm: FragmentManager) : NavActivity.NavFragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    val gospelDetailFragment = NavDetailFragment()
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
                    currentFragment = `object` as NavDetailFragment
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
        meBean = snapshot?.toObject(MeBean::class.java) ?: MeBean()

        if (items.isNotEmpty())
            items.clear()
        gospelCategory = meBean.desc
        gospelTitle = meBean.name
        gospelContent = meBean.content
        items.add(Card(gospelContent))

        gospelAuthor = meBean.author
        gospelChurch = meBean.church
        gospelTime = meBean.time
        userId = meBean.userId

        restoreScrolledPositionOfDetailPage(this, recyclerView, activity_detail_mask)
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {

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
        collapsingToolbar.title = gospelCategory
    }
}
