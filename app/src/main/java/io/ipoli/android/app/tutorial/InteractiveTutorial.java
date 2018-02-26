package io.ipoli.android.app.tutorial;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.wooplr.spotlight.target.Target;

import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.LocalStorage;

import static io.ipoli.android.MainActivity.getContext;

/**
 * Created by Client-9 on 6/17/2017.
 */

public class InteractiveTutorial {
    @Inject
    LocalStorage localStorage;
    private int text_color=R.color.md_red_50;
    private int desc_color=R.color.md_white;
    private int circle_color=R.color.color3;
    private int target_circle_color=R.color.colorPrimaryDark;
    private int dim_color=R.color.md_black;

    public void showTutorials(List<TapTarget> tapTargets, Activity activity,String preference) {
        App.getAppComponent(activity.getApplicationContext()).inject(this);

            new TapTargetSequence(activity)
                    .targets(tapTargets
                    ).start();


    }

    public TapTarget createTutorialForView( View view, Activity activity, String title, String description) {
        Typeface typeface=Typeface.createFromAsset(activity.getAssets(), "fonts/Yekan.ttf");
        TapTarget t = TapTarget.forView(view, title, description)
                // All options below are optional
                .outerCircleColor(target_circle_color)      // Specify a color for the outer circle
                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(circle_color)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.md_white)      // Specify the color of the title text
                .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.mdtp_white)  // Specify the color of the description text
                .textColor(R.color.md_white)            // Specify a color for both the title and description text
                .textTypeface(typeface)  // Specify a typeface for the text
                .dimColor(R.color.md_black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(false)                   // Whether to tint the target view's color
                .transparentTarget(true)
                // Specify whether the target is transparent (displays the content underneath)
//                        .icon(Drawable)                     // Specify a custom drawable to draw as the target
                .targetRadius(60);

        return t;


        // Specify the target radius (in dp)


    }

    public TapTarget createTutorialForRect( Rect view, Activity activity, String title, String description) {
        Typeface typeface=Typeface.createFromAsset(activity.getAssets(), "fonts/Yekan.ttf");
        TapTarget t = TapTarget.forBounds(view, title, description)
                // All options below are optional
                .outerCircleColor(circle_color)      // Specify a color for the outer circle
                .outerCircleAlpha(0.9f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(target_circle_color)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(text_color)      // Specify the color of the title text
                .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                .descriptionTextColor(desc_color)  // Specify the color of the description text
                .textColor(text_color)            // Specify a color for both the title and description text
                .textTypeface(typeface)  // Specify a typeface for the text
                .dimColor(dim_color)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(false)                   // Whether to tint the target view's color
                .transparentTarget(true)
                // Specify whether the target is transparent (displays the content underneath)
//                        .icon(Drawable)                     // Specify a custom drawable to draw as the target
                .targetRadius(60)
                ;

        return t;

    }

    public TapTarget createTutorialForNav(Toolbar toolbar, Activity activity, String title, String description) {
        Typeface typeface=Typeface.createFromAsset(activity.getAssets(), "fonts/Yekan.ttf");
        TapTarget t = TapTarget.forToolbarNavigationIcon(toolbar, title, description)
                // All options below are optional
                .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.colorPrimaryDark)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.md_white)      // Specify the color of the title text
                .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.mdtp_white)  // Specify the color of the description text
                .textColor(R.color.md_white)            // Specify a color for both the title and description text
                .textTypeface(typeface)  // Specify a typeface for the text
                .dimColor(R.color.md_black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(false)                   // Whether to tint the target view's color
                .transparentTarget(true)
                // Specify whether the target is transparent (displays the content underneath)
//                        .icon(Drawable)                     // Specify a custom drawable to draw as the target
                .targetRadius(60);

        return t;

    }
    public TapTarget createTutorialForMenuItem(Toolbar toolbar,int item, Activity activity, String title, String description) {
        Typeface typeface=Typeface.createFromAsset(activity.getAssets(), "fonts/Yekan.ttf");
        TapTarget t = TapTarget.forToolbarMenuItem(toolbar,item, title, description)
                // All options below are optional
                .outerCircleColor(R.color.colorPrimaryDark)      // Specify a color for the outer circle
                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.color4)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.md_white)      // Specify the color of the title text
                .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.mdtp_white)  // Specify the color of the description text
                .textColor(R.color.md_white)            // Specify a color for both the title and description text
                .textTypeface(typeface)  // Specify a typeface for the text
                .dimColor(R.color.md_black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(false)                   // Whether to tint the target view's color
                .transparentTarget(true)
                // Specify whether the target is transparent (displays the content underneath)
//                        .icon(Drawable)                     // Specify a custom drawable to draw as the target
                .targetRadius(60)
                ;

        return t;

    }
}
