package eightbitlab.com.blurview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import com.christian.R
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur
import com.google.android.material.appbar.AppBarLayout
import eightbitlab.com.blurview.BlurController
import eightbitlab.com.blurview.NoOpController
import eightbitlab.com.blurview.BlockingBlurController
import eightbitlab.com.blurview.BlurViewExtendsAppBarLayout
import eightbitlab.com.blurview.BlurViewFacade

/**
 * FrameLayout that blurs its underlying content.
 * Can have children and draw them over blurred background.
 */
class BlurViewExtendsAppBarLayout : AppBarLayout {
    private var blurController: BlurController = NoOpController()

    @ColorInt
    private var overlayColor = 0

    constructor(context: Context?) : super(context!!) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BlurView, defStyleAttr, 0)
        overlayColor =
            a.getColor(R.styleable.BlurView_blurOverlayColor, BlockingBlurController.TRANSPARENT)
        a.recycle()
    }

    override fun draw(canvas: Canvas) {
        val shouldDraw = blurController.draw(canvas)
        if (shouldDraw) {
            super.draw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blurController.updateBlurViewSize()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        blurController.setBlurAutoUpdate(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isHardwareAccelerated) {
            Log.e(TAG, "BlurView can't be used in not hardware-accelerated window!")
        } else {
            blurController.setBlurAutoUpdate(true)
        }
    }

    /**
     * @param rootView root to start blur from.
     * Can be Activity's root content layout (android.R.id.content)
     * or (preferably) some of your layouts. The lower amount of Views are in the root, the better for performance.
     * @return [BlurViewExtendsAppBarLayout] to setup needed params.
     */
    fun setupWith(rootView: ViewGroup): BlurViewFacade {
        val blurController: BlurController = BlockingBlurController(this, rootView, overlayColor)
        this.blurController.destroy()
        this.blurController = blurController
        return blurController
    }
    // Setters duplicated to be able to conveniently change these settings outside of setupWith chain
    /**
     * @see BlurViewFacade.setBlurRadius
     */
    fun setBlurRadius(radius: Float): BlurViewFacade {
        return blurController.setBlurRadius(radius)
    }

    /**
     * @see BlurViewFacade.setOverlayColor
     */
    fun setOverlayColor(@ColorInt overlayColor: Int): BlurViewFacade {
        this.overlayColor = overlayColor
        return blurController.setOverlayColor(overlayColor)
    }

    /**
     * @see BlurViewFacade.setBlurAutoUpdate
     */
    fun setBlurAutoUpdate(enabled: Boolean): BlurViewFacade {
        return blurController.setBlurAutoUpdate(enabled)
    }

    /**
     * @see BlurViewFacade.setBlurEnabled
     */
    fun setBlurEnabled(enabled: Boolean): BlurViewFacade {
        return blurController.setBlurEnabled(enabled)
    }

    companion object {
        private val TAG = BlurViewExtendsAppBarLayout::class.java.simpleName
    }
}

fun makeViewBlurExtendsAppBarLayout(blurViewExtendsAppBarLayout: BlurViewExtendsAppBarLayout, parent: ViewGroup, window: Window, boolean: Boolean = false) {
    val windowBackground = window.decorView.background
    val radius = 25f
    blurViewExtendsAppBarLayout.setupWith(parent)
        .setFrameClearDrawable(windowBackground)
        .setBlurAlgorithm(SupportRenderScriptBlur(parent.context))
        .setBlurRadius(radius)
        .setHasFixedTransformationMatrix(boolean)
}