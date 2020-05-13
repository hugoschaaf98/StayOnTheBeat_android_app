package fr.damansoviet.stayonthebeat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.view.View;

import androidx.core.app.ActivityCompat;

import java.util.concurrent.atomic.AtomicInteger;

import fr.damansoviet.stayonthebeat.ui.ControlActivity;


public class Utils {
    private ControlActivity controlActivity;

    /*** Misc utils ***/

    /**
     * Get the current activity from a given context
     * @param context the current context
     * @return the Activity object corresponding to the given context
     */
    public static Activity getActivity(Context context)
    {
        if(context == null) return null;
        else if(context instanceof ContextWrapper) {
            if(context instanceof Activity) {
                return (Activity) context;
            }
            else {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }
        return null;
    }

    //*** safe View ID generation ***//

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static int generateId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * Generate a value suitable for use in setId(int) since View.generateViewId() isn't
     * available before JellyBean
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    @SuppressLint("NewApi")
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return generateId();
        }
        else {
            return View.generateViewId();
        }
    }





}
