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
        String helpText = "这个页面是暂时的，接下来的某个版本会移除\n开发者来哔哔几句\n❖ 这个很重要\n为了你的消息受到AirChat全方位的安全保护，推荐你使用系统键盘(防止第三方键盘消息劫持)\n❖ 关于安卓推送\n开发者目前没能很好的解决安卓离线推送到达率的问题。虽然开发者尽可能集成了多家推送服务，并根据机型做了适配，但是由于安卓推送市场混乱，加之这是个新的app，无法获得微信一样的权限，所以离线推送到达率无法像微信高(在kill掉app的情况下)。如果对推送到达率有要求，请手动把此app加入白名单\n❖ 消息解密失败怎么办\n1.如果你本地的私钥与服务器端的公钥不匹配，无法解密本地的消息。这个时候只要在app内导入正确的私钥即可\n2.如果密钥丢失，你可以重新计算密钥。但此时你的历史消息将作废无法被破解\n❖ 私钥会被破解吗\n在现有密码学理论下，无法破解。AirChat加密算法底层基于openssl，混用了高强度（128位）RSA和AES加密算法。目前计算机可以暴力破解密钥约70位，所以当下它是安全的。当然，随着计算机越算越快，它总有一天不安全，但那时候我们肯定换了更安全的加密算法\n❖ 私钥泄漏\n重新算一个即可\n❖ 关于为什么要写这个app\n国内的其他社交app不重视用户隐私。有朋友想找一款可以保护聊天信息的app。市面上其他产品不能达到要求。于是自己开发了一款，当作公益项目发布给有需求的人\n❖ 与其他产品区别\n平台无法查看你的聊天消息。AirChat让用户自己保存解密私钥，私钥不泄漏，你的消息别人(包括平台自身)就无法破解。从密码学原理保证你的消息安全。换而言之，即使我们公布了服务器数据，别人依旧不知道你的私信内容\n❖ 关于产品细节\n采用高强度非对称加密算法，当下现有计算力是无法暴力破解密钥密文。用户注册时会生成专属的公私密钥对，其中公钥上传，私钥用户自己保存。其次，根据用户私钥生成AES加密key，用户本地信息的加密保存。详细请见后续博客，会给出详细的产品细节\n❖ 关于端对端加密\n用户与服务器之间传输是端对端加密的。本来考虑使用Diffle-Human算法生成传输key，但是精力有限后续再改。目前采用固定key加密传输。但是不用担心安全性，因为本地的key采用带返回值的宏和局部函数动态生成，逆向破解此key很困难。再者，即使被破解，第三方也只能看到你加密后的聊天内容，没有你的私钥第三方依旧无法知晓你的消息(这也是平台无法知晓你聊天内容的原因)。升级使用Diffle-Human仅仅是技术人员对最好技术的追求而已\n❖ 关于源码发布\n后续考虑开源源代码，接受评析产品安全性\n❖ 关于用户人群\n使用这个产品需要一定的门槛，但尤其适用于金融人士、科研人士、管理人士等等一系列有聊天隐私需求的人\n❖ 关于产品后续\n开发者独立开发iOS、安卓客户端和服务器部分全部代码，由于精力有限，更新可能会慢\n❖ 关于语音听写\n试了一下子弹短信的语音转文字，感觉科大讯飞的服务不错。我也加了一个，用户可以自愿使用\n❖ 关于群聊\n在计划中，但是个人精力有限，更新会慢\n❖ 关于第三方\n目前IM服务作者本人所写，聊天数据存在自己服务器。用户发送的文件数据，保存在七牛服务器中\n❖ 关于费用\n公益产品写给有需要的人。希望这是个长寿的App\n\n\n\n\n\n\n\n\n\n\n\n\n\n❖ 有事联系开发者703351566@qq.com";
        help.setText(helpText);
    }
}
