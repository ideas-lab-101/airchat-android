package com.android.crypt.chatapp.qrResult;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.WebView.WebActivity;
import com.android.crypt.chatapp.utility.Common.ClickUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QScanOtherResultActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.qr_value)
    EditText qrValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qscan_other_result);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_code_other_result);
        setSupportActionBar(toolbar);
        initGlobalData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理ActionBar的菜单
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initGlobalData() {
        Intent intent = getIntent();
        String keyValue = (String) intent.getSerializableExtra("qr_result");
        if (keyValue != null){
            Spannable spannable = new SpannableString(keyValue);
            final CharSequence text = TextUtils.concat(spannable, "\u200B");
            if (text instanceof Spannable) {
                int end = text.length();
                Spannable sp = (Spannable) text;
                URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                spannableStringBuilder.clearSpans();
                for (URLSpan urlSpan : urls) {
                    //拦截点击
                    InterceptUrlSpan interceptUrlSpan = new InterceptUrlSpan(urlSpan.getURL());
                    spannableStringBuilder.setSpan(interceptUrlSpan, sp.getSpanStart(urlSpan), sp.getSpanEnd(urlSpan), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                qrValue.setText(spannableStringBuilder);
            } else {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                spannableStringBuilder.clearSpans();
                qrValue.setText(spannableStringBuilder);
            }
        }
    }

    private class InterceptUrlSpan extends ClickableSpan {
        private String url;

        InterceptUrlSpan(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View arg0) {
            if (!ClickUtils.isFastClick()) {
                return;
            }
            //此处填写对应的点击跳转逻辑
            Intent intent = new Intent(QScanOtherResultActivity.this, WebActivity.class);
            intent.putExtra("url_value", url);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //自定义颜色和下划线
            ds.setColor(url.startsWith("tel:") ? Color.RED : Color.GREEN);
            ds.setUnderlineText(!url.startsWith("tel:"));
        }
    }
}
