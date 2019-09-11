package com.android.crypt.chatapp.WebView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.activity_main_webview)
    WebView mWebview;
    @BindView(R.id.quit_web)
    ImageButton quitWeb;
    @BindView(R.id.url_input_value)
    EditText urlInputValue;
    @BindView(R.id.start_search)
    Button startSearch;
    @BindView(R.id.clear_text)
    ImageButton clearText;
    @BindView(R.id.progress_line)
    View progressLine;



    private ViewGroup.LayoutParams lp;
    private int curWidth = 30;
    private int allWidth = 500;
    private Timer sendSmsTimer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        initView();
    }


    private void initView() {
        quitWeb.setOnClickListener(this);
        clearText.setOnClickListener(this);
        startSearch.setOnClickListener(this);
        lp = progressLine.getLayoutParams();
        allWidth = RunningData.getInstance().getScreenWidth();

        Intent intent = getIntent();
        String urlVaule = (String) intent.getSerializableExtra("url_value");
        // Force links and redirects to open in the WebView instead of in a browser
        mWebview.setWebViewClient(new WebViewClient());
        // Enable Javascript
        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (urlVaule.startsWith("http://") || urlVaule.startsWith("https://") || urlVaule.startsWith("www.") || urlVaule.startsWith("ftp://")) {
            urlInputValue.setText(urlVaule);
        } else {
            String urlStart = "https://m.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=";
            String urlEnd = "&rsv_pq=80c7023000194588&rsv_t=e923%2BJi6SI0Q7Kl1q1%2BUl%2Bq7jbwN0GzkIghhykTbOJO0WX2%2BnR6iKTwQWLI&rsv_enter=1&inputT=7275&rsv_sug3=20&rsv_sug1=19&rsv_sug2=0&rsv_sug4=7642";
            urlVaule = urlStart + urlVaule + urlEnd;
            urlInputValue.setText(urlVaule);
        }

        // REMOTE RESOURCE
        mWebview.loadUrl(urlVaule);
        mWebview.setWebViewClient(new MyWebViewClient());

        mWebview.getSettings().setSupportZoom(true);
        mWebview.getSettings().setBuiltInZoomControls(true);

        // 如果页面中链接，如果希望点击链接继续在当前browser中响应，
        // 而不是新开Android的系统browser中响应该链接，必须覆盖webview的WebViewClient对象
        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlInputValue.setText(url);
                startLoadWeb();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                finishLoadWeb();
            }
        });

        urlInputValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 获得焦点
                    changeInputFrame(hasFocus);
                } else {
                    changeInputFrame(hasFocus);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quit_web:
                if (mWebview.canGoBack()) {
                    mWebview.goBack();
                } else {
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
                break;

            case R.id.clear_text:
                urlInputValue.setText("");
                break;

            case R.id.start_search:
                startSearch();
                break;
        }
    }


    private void changeInputFrame(boolean hasFocus) {
        if (hasFocus) {
            urlInputValue.requestFocus();
            int left = DensityUtil.dip2px(this, 15);
            int right = DensityUtil.dip2px(this, 75);
            urlInputValue.setPadding(left, 0, right, 0);
            urlInputValue.setGravity(Gravity.CENTER);

            urlInputValue.setText(urlInputValue.getText().toString());
            clearText.setVisibility(View.VISIBLE);
            startSearch.setVisibility(View.VISIBLE);
            urlInputValue.setSelection(urlInputValue.getText().length());
        } else {
            urlInputValue.clearFocus();
            int left = DensityUtil.dip2px(this, 15);
            int right = DensityUtil.dip2px(this, 15);
            urlInputValue.setPadding(left, 0, right, 0);
            urlInputValue.setGravity(Gravity.CENTER);

            urlInputValue.setText(urlInputValue.getText().toString());
            clearText.setVisibility(View.GONE);
            startSearch.setVisibility(View.GONE);
        }

    }

    private void startSearch() {
        String urlVaule = urlInputValue.getText().toString();
        if (urlVaule.startsWith("http://") || urlVaule.startsWith("https://")|| urlVaule.startsWith("www.") || urlVaule.startsWith("ftp://")) {
            urlInputValue.setText(urlVaule);
        } else {
            String urlStart = "https://m.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=";
            String urlEnd = "&rsv_pq=80c7023000194588&rsv_t=e923%2BJi6SI0Q7Kl1q1%2BUl%2Bq7jbwN0GzkIghhykTbOJO0WX2%2BnR6iKTwQWLI&rsv_enter=1&inputT=7275&rsv_sug3=20&rsv_sug1=19&rsv_sug2=0&rsv_sug4=7642";
            urlVaule = urlStart + urlVaule + urlEnd;
            urlInputValue.setText(urlVaule);
        }

        Logger.d("urlVaule = " + urlVaule);
        urlInputValue.clearFocus();
        mWebview.loadUrl(urlVaule);

    }

    private void startLoadWeb(){
        progressLine.setVisibility(View.VISIBLE);
        lp.width = DensityUtil.dip2px(this, curWidth);
        lp.height = DensityUtil.dip2px(this, 2);
        progressLine.setLayoutParams(lp);

        curWidth = curWidth + 5;
        if (sendSmsTimer == null){
            sendSmsTimer = new Timer();
            sendSmsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (curWidth >= allWidth){
                        sendSmsTimer.cancel();
                        sendSmsTimer = null;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lp.width = DensityUtil.dip2px(WebActivity.this, curWidth);
                            lp.height = DensityUtil.dip2px(WebActivity.this, 2);
                            progressLine.setLayoutParams(lp);
                        }
                    });
                }
            },100, 100);
        }
    }

    private void finishLoadWeb() {
        sendSmsTimer.cancel();
        sendSmsTimer = null;
        curWidth = 30;
        lp.width = DensityUtil.dip2px(this, allWidth);
        lp.height = DensityUtil.dip2px(this, 2);
        progressLine.setLayoutParams(lp);

        Timer hideTimer = new Timer();
        hideTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressLine.setVisibility(View.GONE);
                    }
                });
            }
        },100);
    }

}
