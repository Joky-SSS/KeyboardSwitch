package com.jokysss.keyboardswitch;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
public class KeyboardHeightProvider extends PopupWindow {

    private KeyboardHeightObserver observer;
    private View popupView;
    private View parentView;
    private Point screenSize = new Point();
    private Rect rect = new Rect();

    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.popupView = LayoutInflater.from(activity).inflate(R.layout.popupwindow, null, false);
        setContentView(popupView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        parentView = activity.findViewById(android.R.id.content);
        setWidth(0);
        setHeight(LayoutParams.MATCH_PARENT);
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (popupView != null) {
                    handleOnGlobalLayout();
                }
            }
        });
    }

    public void start() {
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public void close() {
        this.observer = null;
        dismiss();
    }

    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }

    private void handleOnGlobalLayout() {
        popupView.getWindowVisibleDisplayFrame(rect);
        int keyboardHeight = screenSize.y - rect.bottom;
        if (keyboardHeight < 0) {
            keyboardHeight = 0;
            screenSize.y = rect.bottom;
        }
        notifyKeyboardHeightChanged(keyboardHeight);
    }

    private void notifyKeyboardHeightChanged(int height) {
        if (observer != null) {
            observer.onKeyboardHeightChanged(height);
        }
    }

    public interface KeyboardHeightObserver {
        void onKeyboardHeightChanged(int height);
    }
}
