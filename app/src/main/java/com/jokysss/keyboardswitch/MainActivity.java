package com.jokysss.keyboardswitch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements KeyboardHeightProvider.KeyboardHeightObserver {
    LinearLayout bottom;
    LinearLayout content;
    LinearLayout inputBg;
    ImageView mSwitch;
    TextView mInfo;
    private int keyboradH = -1;
    private KeyboardHeightProvider keyboardHeightProvider;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottom = findViewById(R.id.bottom);
        content = findViewById(R.id.content);
        inputBg = findViewById(R.id.inputbg);
        mSwitch = findViewById(R.id.iv_switch);
        mInfo = findViewById(R.id.tv_info);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboardHeightProvider = new KeyboardHeightProvider(this);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottom.getVisibility() == View.VISIBLE) {
                    bottom.setVisibility(View.GONE);
                } else {
                    imm.hideSoftInputFromWindow(bottom.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    bottom.setVisibility(View.VISIBLE);
                }
            }
        });
        mSwitch.post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        inputBg.setVisibility(View.GONE);
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
    }

    @Override
    public void onKeyboardHeightChanged(int height) {
        Log.i("Main", "onKeyboardHeightChanged in pixels: " + height);
        if (keyboradH == -1 && height > 0) {
            keyboradH = height;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inputBg.getLayoutParams();
            params.height = keyboradH;
        }

        if (height > 0) {
            bottom.setVisibility(View.GONE);
            inputBg.setVisibility(View.VISIBLE);
        } else {
            inputBg.setVisibility(View.GONE);
        }
        mInfo.setText("keyboard height:"+height);
    }
}
