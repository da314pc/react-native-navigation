package com.reactnativenavigation.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reactnativenavigation.anim.TopBarAnimator;
import com.reactnativenavigation.anim.TopBarCollapseBehavior;
import com.reactnativenavigation.interfaces.ScrollEventListener;
import com.reactnativenavigation.parse.Button;
import com.reactnativenavigation.parse.Color;
import com.reactnativenavigation.parse.Fraction;
import com.reactnativenavigation.parse.Number;
import com.reactnativenavigation.parse.params.Bool;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@SuppressLint("ViewConstructor")
public class TopBar extends AppBarLayout implements ScrollEventListener.ScrollAwareView {
    private final Toolbar titleBar;
    private TitleBarButton.OnClickListener onClickListener;
    private final TopBarCollapseBehavior collapsingBehavior;
    private TopBarAnimator animator;
    private TopTabs topTabs;
    private StackLayout parentView;

    public TopBar(final Context context, TitleBarButton.OnClickListener onClickListener, StackLayout parentView) {
        super(context);
        this.onClickListener = onClickListener;
        collapsingBehavior = new TopBarCollapseBehavior(this);
        titleBar = new Toolbar(context);
        titleBar.getMenu();
        topTabs = new TopTabs(getContext());
        this.animator = new TopBarAnimator(this);
        this.parentView = parentView;
        addView(titleBar);
    }

    public void setTitle(String title) {
        titleBar.setTitle(title);
    }

    public String getTitle() {
        return titleBar.getTitle() != null ? titleBar.getTitle().toString() : "";
    }

    public void setTestId(String testId) {
        setTag(testId);
    }

    public void setTitleTextColor(Color color) {
        if (color.hasValue()) titleBar.setTitleTextColor(color.get());
    }

    public void setTitleFontSize(Fraction size) {
        TextView titleTextView = getTitleTextView();
        if (titleTextView != null && size.hasValue()) {
            titleTextView.setTextSize(size.get());
        }
    }

    public void setTitleTypeface(Typeface typeface) {
        TextView titleTextView = getTitleTextView();
        if (titleTextView != null) {
            titleTextView.setTypeface(typeface);
        }
    }

    public void setTopTabFontFamily(int tabIndex, Typeface fontFamily) {
        topTabs.setFontFamily(tabIndex, fontFamily);
    }

    public void applyTopTabsColors(Color selectedTabColor, Color unselectedTabColor) {
        topTabs.applyTopTabsColors(selectedTabColor, unselectedTabColor);
    }

    public void applyTopTabsFontSize(Number fontSize) {
        topTabs.applyTopTabsFontSize(fontSize);
    }

    public void setTopTabsVisible(boolean visible) {
        topTabs.setVisibility(this, visible);
    }

    public void setButtons(ArrayList<Button> leftButtons, ArrayList<Button> rightButtons) {
        setLeftButtons(leftButtons);
        setRightButtons(rightButtons);
    }

    public TextView getTitleTextView() {
        return findTextView(titleBar);
    }

    public void setBackgroundColor(Color color) {
        if (color.hasValue()) titleBar.setBackgroundColor(color.get());
    }

    @Nullable
    private TextView findTextView(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View view = root.getChildAt(i);
            if (view instanceof ViewGroup) {
                view = findTextView((ViewGroup) view);
            }
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }

    private void setLeftButtons(ArrayList<Button> leftButtons) {
        if (leftButtons == null) return;
        if (leftButtons.isEmpty()) {
            titleBar.setNavigationIcon(null);
            return;
        }

        if (leftButtons.size() > 1) {
            Log.w("RNN", "Use a custom TopBar to have more than one left button");
        }

        Button leftButton = leftButtons.get(0);
        setLeftButton(leftButton);
    }

    private void setLeftButton(final Button button) {
        TitleBarButton leftBarButton = new TitleBarButton(this.titleBar, button, onClickListener);
        leftBarButton.applyNavigationIcon(getContext());
    }

    private void setRightButtons(ArrayList<Button> rightButtons) {
        if (rightButtons == null || rightButtons.size() == 0) {
            return;
        }

        Menu menu = getTitleBar().getMenu();
        menu.clear();

        for (int i = 0; i < rightButtons.size(); i++) {
            Button button = rightButtons.get(i);
            TitleBarButton titleBarButton = new TitleBarButton(this.titleBar, button, onClickListener);
            titleBarButton.addToMenu(getContext(), menu);
        }
    }

    public Toolbar getTitleBar() {
        return titleBar;
    }

    public void initTopTabs(ViewPager viewPager) {
        topTabs = new TopTabs(getContext());
        topTabs.init(viewPager);
        addView(topTabs);
    }

    public void enableCollapse(ScrollEventListener scrollEventListener) {
        collapsingBehavior.enableCollapse(scrollEventListener);
    }

    public void disableCollapse() {
        collapsingBehavior.disableCollapse();
    }

    public void show(Bool animated) {
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        if (animated.isTrueOrUndefined()) {
            animator.show();
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    public void hide(Bool animated) {
        if (getVisibility() == View.GONE) {
            return;
        }
        if (animated.isTrueOrUndefined()) {
            animator.hide();
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            this.parentView.removeView(this);
        } else if (visibility == View.VISIBLE && this.getParent() == null) {
            this.parentView.addView(this, MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public void clear() {
        titleBar.setTitle(null);
        titleBar.setNavigationIcon(null);
        titleBar.getMenu().clear();
    }

    public void clearTopTabs() {
        topTabs.clear(this);
    }

    @VisibleForTesting()
    public TopTabs getTopTabs() {
        return topTabs;
    }

    @VisibleForTesting
    public void setAnimator(TopBarAnimator animator) {
        this.animator = animator;
    }
}