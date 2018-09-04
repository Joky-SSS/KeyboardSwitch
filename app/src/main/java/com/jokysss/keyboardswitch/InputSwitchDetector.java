package com.jokysss.keyboardswitch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;


public class InputSwitchDetector {

    private static final String SHARE_PREFERENCE_NAME = "emotioninputdetector";
    private static final String SHARE_PREFERENCE_TAG = "soft_input_height";

    private Activity mActivity;
    private InputMethodManager mInputManager;
    private SharedPreferences sp;
    private EditText mEditText;
    private View mContentView;
    private Map<View, View> viewPair = new HashMap<>();
    private static int navigationBarH = -1;
    private InputSwitchDetector() {}

    public static InputSwitchDetector with(Activity activity) {
        InputSwitchDetector emotionInputDetector = new InputSwitchDetector();
        emotionInputDetector.mActivity = activity;
        emotionInputDetector.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        emotionInputDetector.sp = activity.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return emotionInputDetector;
    }

    public InputSwitchDetector bindToContent(View contentView) {
        mContentView = contentView;
        return this;
    }

    public InputSwitchDetector bindToEditText(EditText editText) {
        mEditText = editText;
        mEditText.requestFocus();
        mEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && isBottomShown()) {
                Object longClickObj = mEditText.getTag(R.id.long_click_tag);
                if (longClickObj == null || !(boolean) longClickObj) {
                    resizeContent(getCurrentBottom(), false, true);
                    hideBottomLayout();
                }
                mEditText.setTag(R.id.long_click_tag, false);
            }
            return false;
        });
        mEditText.setOnLongClickListener(v -> {
            mEditText.setTag(R.id.long_click_tag, true);
            return false;
        });
        return this;
    }

    public InputSwitchDetector bind(View switchView, View functionView) {
        viewPair.put(switchView, functionView);
        return this;
    }

    public boolean backPress() {
        if (isBottomShown()) {
            hideBottomLayout();
            return true;
        }
        return false;
    }


    private void showEmotionLayout(View switchView) {
        hideBottomLayout();
        View functionView = viewPair.get(switchView);
        if (functionView != null)
            functionView.setVisibility(View.VISIBLE);
    }

    private void hideBottomLayout() {
        for (View view : viewPair.values()) {
            if (view.isShown())
                view.setVisibility(View.GONE);
        }
    }

    private boolean isBottomShown() {
        for (View view : viewPair.values()) {
            if (view.isShown())
                return true;
        }
        return false;
    }

    private View getCurrentBottom() {
        for (View view : viewPair.values()) {
            if (view.isShown())
                return view;
        }
        return null;
    }

    private void resizeContent(View functionView, boolean emotionShow, boolean inputShow) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        int emotionH = functionView == null ? 0 : functionView.getLayoutParams().height;
        int inputH = getSupportSoftInputHeight();
        int contentH = mContentView.getHeight();
        if (emotionShow && inputH > 0) {
            params.height = contentH + inputH - emotionH;
            params.weight = 0.0F;
            unlockContentHeightDelayed(false);
        } else if (inputShow && isBottomShown()) {
            inputH = sp.getInt(SHARE_PREFERENCE_TAG, 0);
            inputH = 0;
            params.weight = 0F;
            if (inputH == 0) {
                params.height = contentH;
                unlockContentHeightDelayed(true);
            } else {
                params.height = contentH - inputH + emotionH;
                unlockContentHeightDelayed(false);
            }
        }
    }

    private void unlockContentHeightDelayed(boolean requestlayout) {
        mContentView.postDelayed(() -> {
            ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
            ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).height = 0;
            if (requestlayout)
                mContentView.requestLayout();
        }, 100);
    }

    private void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        Rect o = new Rect();
        int height = mActivity.getWindow().getDecorView().getRootView().getBottom();
        int softInputHeight = height - r.bottom;
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            // When SDK Level >= 21 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
        //            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        //        }
        if (softInputHeight < 0) {
            softInputHeight = 0;
        }
        if (softInputHeight > 0) {
            int naviBarHeight = getNavigationBarHeight();
            if (softInputHeight == naviBarHeight)
                softInputHeight = 0;
        }
        if (softInputHeight > 0) {
            sp.edit().putInt(SHARE_PREFERENCE_TAG, softInputHeight).apply();
        }
        return softInputHeight;
    }

    public int getNavigationBarHeight() {
        if (navigationBarH == -1) {
            Resources resources = mActivity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            //获取NavigationBar的高度
            navigationBarH = resources.getDimensionPixelSize(resourceId);
        }
        return navigationBarH;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    public void onSwitchClick(View switchView) {
        View functionView = viewPair.get(switchView);
        if (functionView == null)
            return;
        if (functionView.isShown()) {
            functionView.setVisibility(View.GONE);
        } else {
            if (isSoftInputShown()) {
                resizeContent(viewPair.get(switchView), true, false);
                hideSoftInput();
                showEmotionLayout(switchView);
            } else {
                showEmotionLayout(switchView);
            }
        }
    }
}
