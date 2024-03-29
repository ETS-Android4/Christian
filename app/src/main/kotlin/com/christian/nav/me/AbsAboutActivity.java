package com.christian.nav.me;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.christian.R;
import com.christian.multitype.Author;
import com.christian.multitype.AuthorViewBinder;
import com.christian.multitype.Card;
import com.christian.multitype.CardViewBinder;
import com.christian.multitype.Category;
import com.christian.multitype.CategoryViewBinder;
import com.christian.multitype.Contributor;
import com.christian.multitype.ImageLoader;
import com.christian.multitype.License;
import com.christian.multitype.LicenseViewBinder;
import com.christian.multitype.Line;
import com.christian.multitype.LineViewBinder;
import com.christian.multitype.OnContributorClickedListener;
import com.christian.multitype.OnRecommendationClickedListener;
import com.christian.multitype.Recommendation;
import com.christian.nav.HidingScrollListener;
import com.christian.swipe.SwipeBackActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author drakeet
 */
public abstract class AbsAboutActivity extends SwipeBackActivity implements EventListener<DocumentSnapshot> {

    public Toolbar toolbar;
    protected CollapsingToolbarLayout collapsingToolbar;
    private LinearLayout headerContentLayout;

    private List<Object> items;
    private MultiTypeAdapter adapter;
    private TextView slogan, version;
//    private FloatingActionMenu menuYellow;

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private RecyclerView recyclerView;
    private @Nullable
    ImageLoader imageLoader;
    private boolean initialized;
    private @Nullable
    OnRecommendationClickedListener onRecommendationClickedListener;
    private @Nullable
    OnContributorClickedListener onContributorClickedListener;
    private ListenerRegistration registration;
    private ArrayList snapshots = new ArrayList<DocumentSnapshot>();
    public DocumentReference meRef;


    protected abstract void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version);

    protected void onItemsCreated(@NonNull List<Object> items) {
//        UtilsKt.recordScrolledPositionOfDetailPage(this, recyclerView);
    }

    protected void onTitleViewCreated(@NonNull CollapsingToolbarLayout collapsingToolbar) {
    }

    public void setImageLoader(@NonNull ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        if (initialized) {
            adapter.notifyDataSetChanged();
        }
    }

    public @Nullable
    ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    public final void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public final void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public final void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }


    public abstract class DoubleClickListener implements View.OnClickListener {
        private static final long DOUBLE_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
                onDoubleClick(v);
            }
            lastClickTime = currentTimeMillis;
        }

        public abstract void onDoubleClick(View v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        toolbar = findViewById(com.christian.R.id.toolbar);
        ImageView icon = findViewById(com.christian.R.id.icon);
        slogan = findViewById(com.christian.R.id.slogan);
        version = findViewById(com.christian.R.id.version);
        collapsingToolbar = findViewById(com.christian.R.id.collapsing_toolbar);
        headerContentLayout = findViewById(com.christian.R.id.header_content_layout);
//        menuYellow = findViewById(R.id.menu_yellow);

        onTitleViewCreated(collapsingToolbar);
        onCreateHeader(icon, slogan, version);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        onApplyPresetAttrs();
        recyclerView = findViewById(com.christian.R.id.list);

        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.addOnScrollListener(new HidingScrollListener(recyclerView) {
            @Override
            public void onHide() {
//                menuYellow.hideMenu(true);
                recyclerView.setVerticalScrollBarEnabled(true);
            }

            @Override
            public void onShow() {
//                menuYellow.showMenu(true);
                recyclerView.setVerticalScrollBarEnabled(true);
            }

            @Override
            public void onTop() {
                recyclerView.setVerticalScrollBarEnabled(false);
            }

            @Override
            public void onBottom() {
                recyclerView.setVerticalScrollBarEnabled(false);
            }
        });


        toolbar.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

    }

    protected int getLayoutId() {
        return com.christian.R.layout.about_page_main_activity;
    }

    public void startListening() {
        if (registration == null) {
            registration = meRef.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (registration != null)
            registration.remove();
        registration = null;

        snapshots.clear();
        adapter.notifyDataSetChanged();
    }


    @Override
    @SuppressWarnings("deprecation")
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        adapter = new MultiTypeAdapter();
        adapter.register(Category.class, new CategoryViewBinder());
        adapter.register(Card.class, new CardViewBinder());
        adapter.register(Author.class, new AuthorViewBinder());
        adapter.register(Line.class, new LineViewBinder());
        adapter.register(Contributor.class, new ContributorViewBinder(this));
        adapter.register(License.class, new LicenseViewBinder());
        adapter.register(Recommendation.class, new RecommendationViewBinder(this));

        items = new ArrayList<>();
        onItemsCreated(items);
        adapter.setItems(items);
        adapter.setHasStableIds(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(adapter));
        recyclerView.setAdapter(adapter);
        initialized = true;
    }

    private void onApplyPresetAttrs() {
        final TypedArray a = obtainStyledAttributes(com.christian.R.styleable.AbsAboutActivity);
        Drawable headerBackground = a.getDrawable(com.christian.R.styleable.AbsAboutActivity_aboutPageHeaderBackground);
        if (headerBackground != null) {
            setHeaderBackground(headerBackground);
        }
        Drawable headerContentScrim = a.getDrawable(com.christian.R.styleable.AbsAboutActivity_aboutPageHeaderContentScrim);
        if (headerContentScrim != null) {
            setHeaderContentScrim(headerContentScrim);
        }
        @ColorInt
        int headerTextColor = a.getColor(com.christian.R.styleable.AbsAboutActivity_aboutPageHeaderTextColor, -1);
        if (headerTextColor != -1) {
            setHeaderTextColor(headerTextColor);
        }
        Drawable navigationIcon = a.getDrawable(com.christian.R.styleable.AbsAboutActivity_aboutPageNavigationIcon);
        if (navigationIcon != null) {
            setNavigationIcon(navigationIcon);
        }
        a.recycle();
    }

    /**
     * Use {@link #setHeaderBackground(int)} instead.
     *
     * @param resId The resource id of header background
     */
    @Deprecated
    public void setHeaderBackgroundResource(@DrawableRes int resId) {
        setHeaderBackground(resId);
    }

    private void setHeaderBackground(@DrawableRes int resId) {
        setHeaderBackground(ContextCompat.getDrawable(this, resId));
    }

    private void setHeaderBackground(@NonNull Drawable drawable) {
        ViewCompat.setBackground(headerContentLayout, drawable);
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     */
    public void setHeaderContentScrim(@NonNull Drawable drawable) {
        collapsingToolbar.setContentScrim(drawable);
    }

    public void setHeaderContentScrim(@DrawableRes int resId) {
        setHeaderContentScrim(ContextCompat.getDrawable(this, resId));
    }

    public void setHeaderTextColor(@ColorInt int color) {
        collapsingToolbar.setCollapsedTitleTextColor(color);
        slogan.setTextColor(color);
        version.setTextColor(color);
    }

    /**
     * Set the icon to use for the toolbar's navigation button.
     *
     * @param resId Resource ID of a drawable to set
     */
    public void setNavigationIcon(@DrawableRes int resId) {
        toolbar.setNavigationIcon(resId);
    }

    public void setNavigationIcon(@NonNull Drawable drawable) {
        toolbar.setNavigationIcon(drawable);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//        if (menuItem.getItemId() == android.R.id.home) {
//            onBackPressed();
//        }
//        return super.onOptionsItemSelected(menuItem);
//    }

    @Override
    public void setTitle(@NonNull CharSequence title) {
        collapsingToolbar.setTitle(title);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public CollapsingToolbarLayout getCollapsingToolbar() {
        return collapsingToolbar;
    }

    public List<Object> getItems() {
        return items;
    }

    public MultiTypeAdapter getAdapter() {
        return adapter;
    }

    public TextView getSloganTextView() {
        return slogan;
    }

    public TextView getVersionTextView() {
        return version;
    }

    public void setOnRecommendationClickedListener(@Nullable OnRecommendationClickedListener listener) {
        this.onRecommendationClickedListener = listener;
    }

    public @Nullable
    OnRecommendationClickedListener getOnRecommendationClickedListener() {
        return onRecommendationClickedListener;
    }

    public void setOnContributorClickedListener(@Nullable OnContributorClickedListener listener) {
        this.onContributorClickedListener = listener;
    }

    public @Nullable
    OnContributorClickedListener getOnContributorClickedListener() {
        return onContributorClickedListener;
    }
}

