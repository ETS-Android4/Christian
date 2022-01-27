package eightbitlab.com.blurview;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur;

public class ViewUtilKt {
    public static final void makeViewBlur(BlurViewExtendsVerticalRecyclerView viewVertical, ViewGroup parent, Window window, boolean var3) {
        View var10000 = window.getDecorView();
        Drawable windowBackground = var10000.getBackground();
        float radius = 25.0F;
        viewVertical.setupWith(parent).setFrameClearDrawable(windowBackground).setBlurAlgorithm((BlurAlgorithm) (new SupportRenderScriptBlur(parent.getContext()))).setBlurRadius(radius).setHasFixedTransformationMatrix(var3);
    }
}
