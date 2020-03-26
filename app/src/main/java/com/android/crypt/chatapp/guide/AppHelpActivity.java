package com.android.crypt.chatapp.guide;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppHelpActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.help)
    TextView help;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_help);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.title_bar_help);
        setSupportActionBar(toolbar);

        initView();
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
                overridePendingTransition(R.anim.in_from_left,
                        R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        String helpText =
                "· 敬请期待\n个人开发，时间比较急，所以更新慢。下一版本回依次添加群聊中起他功能。安卓目前只能收发群消息，建群下一版本添加\n" +
                "· 聊天字体突然变大\n变大是起到强调的作用的\n1.只发单个字会变大\n2.短句子纯emoji变大\n3.短句子末尾加‘？’或‘！’变大\n" +
                "· 消息水印\n1.在聊天页面，如果你仔细看文字消息灰色背景，会发现一层很浅水印。这个水印内容对应你的账号\n2.对应规则就是电脑上面一排数字键盘，1234567890 对应!@#$%^&*()，~是结束。\n3.水印目的，当你的消息或群消息被截图或拍照，截图者账号就可以根据水印公开看出来。此外，没有水印的消息截图都是伪的\n"+
                "· 关于安卓推送\n开发者目前没能很好的解决安卓离线推送到达率的问题。虽然开发者尽可能集成了多家推送服务，并根据机型做了适配，但是由于安卓推送市场混乱，加之这是个新的app，无法获得微信一样的权限，所以离线推送到达率无法像微信高(在kill掉app的情况下)。如果对推送到达率有要求，请手动把此app加入白名单\n" +
                "· 消息如果解密失败\n1.如果你本地的私钥与服务器端的公钥不匹配，无法解密本地的消息。这个时候只要在app内导入正确的私钥即可\n2.如果密钥丢失，你可以重新计算密钥。但此时你的历史消息将作废无法被破解\n"+
                        "· 私钥会被破解吗\n在现有密码学理论下，无法破解。AirChat用了工业级对称/非对称加密算法，此类加密算法广泛应用于银行、金融等领域。若使用计算机暴力破解你的聊天内容需上亿年\n"+
                        "· 私信安全\n1.推荐使用系统键盘(第三方键盘会劫持你的消息并上传到他们的服务器)\n2.私聊的安全性远大于群聊。群聊中无论如何加密，只要入群就可以看到群消息。私聊消息是点对点的\n"+
                        "· 服务器可以查看用户聊天记录吗\n平台无法查看你的私聊消息，产品设计的目标就是这个。AirChat让用户自己保存解密私钥，私钥不泄漏，你的消息别人(包括平台自身)就无法破解。从密码学原理保证你的消息安全。换而言之，即使我们公布了服务器数据，别人依旧不知道你的私信内容。但是，群聊消息无法达到私聊的保密程度。如上一条所说，群聊无论如何加密，只要入群就可以接收到群消息\n"+
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t· 有事联系开发者contact.airchat@gmail.com";
        help.setText(helpText);
    }
}
