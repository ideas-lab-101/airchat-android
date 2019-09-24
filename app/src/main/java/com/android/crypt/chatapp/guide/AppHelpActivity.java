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
        String helpText = "\t\t· 这个很重要\n\t\t推荐使用系统键盘(第三方键盘会劫持你的消息并上传到他们的服务器)\n\t\t· 关于安卓推送\n\t\t开发者目前没能很好的解决安卓离线推送到达率的问题。虽然开发者尽可能集成了多家推送服务，并根据机型做了适配，但是由于安卓推送市场混乱，加之这是个新的app，无法获得微信一样的权限，所以离线推送到达率无法像微信高(在kill掉app的情况下)。如果对推送到达率有要求，请手动把此app加入白名单\n\t\t· 消息解密失败\n\t\t1.如果你本地的私钥与服务器端的公钥不匹配，无法解密本地的消息。这个时候只要在app内导入正确的私钥即可\n\t\t2.如果密钥丢失，你可以重新计算密钥。但此时你的历史消息将作废无法被破解\n\t\t· 私钥会被破解吗\n\t\t在现有密码学理论下，无法破解。AirChat用了工业级对称/非对称加密算法，此类加密算法广泛应用于银行、金融等领域。若使用计算机暴力破解你的聊天内容需上亿年\n\t\t· 服务器可以查看用户聊天记录吗\n\t\t平台无法查看你的聊天消息，产品设计的目标就是这个。AirChat让用户自己保存解密私钥，私钥不泄漏，你的消息别人(包括平台自身)就无法破解。从密码学原理保证你的消息安全。换而言之，即使我们公布了服务器数据，别人依旧不知道你的私信内容\n\n\t\t--------\n\n\t\t· 当前版本缺点\n\t\t1. 当前私钥和本地消息解密绑在一起，本地私钥丢失，本地历史消息都无法破解(所以如果你希望销毁本地历史消息，更换私钥即可)\n\t\t2. 私钥泄漏的话，会对历史消息的安全性造成影响(如果没有更换过私钥)\n\t\t3. 群聊\n\t\t针对1和2，可以采用动态密钥、定期更换密钥很好的解决。群聊功能比较吃服务器资源，实现难度不大。以上功能一般不影响通信的安全，除非在极端条件下。等作者年底考完研究生试后立刻就能更新完成。感谢支持\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t· 有事联系开发者contact.airchat@gmail.com";
        help.setText(helpText);
    }
}
