package com.android.crypt.chatapp.finder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.contact.adapter.ContactListAdapter;
import com.android.crypt.chatapp.user.FriendCardActivity;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchLocalActivity extends BaseActivity  implements View.OnClickListener, XListView.OnItemClickListener {
    @BindView(R.id.quit_search)
    ImageButton quitSearch;
    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.start_search)
    Button startSearch;
    @BindView(R.id.clear_text)
    ImageButton clearText;
    @BindView(R.id.contactList_lv)
    XListView contactListLv;

    private ArrayList<CNPinyin<ContactModel>> resultList;
    private ArrayList<CNPinyin<ContactModel>> mListContact;
    private ContactListAdapter adapterFavor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_local);
        ButterKnife.bind(this);
        initView();
    }


    private void initView() {
        quitSearch.setOnClickListener(this);
        clearText.setOnClickListener(this);
        startSearch.setOnClickListener(this);
        contactListLv.setOnItemClickListener(this);
        contactListLv.setPullLoadEnable(false);
        contactListLv.setPullRefreshEnable(false);
        if (resultList == null){
            resultList = new ArrayList<>();
        }

        if (mListContact == null){
            mListContact = RunningData.getInstance().getContactList();
        }
        int heightLlistView = RunningData.getInstance().getAppShowHeight() - RunningData.getInstance().getActionBarHeight() - DensityUtil.dip2px(this, 50);
        adapterFavor = new ContactListAdapter(this, resultList, heightLlistView, DensityUtil.dip2px(this, 80), false);
        contactListLv.setAdapter(adapterFavor);
        inputValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                changeInputFrame(hasFocus);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.quit_search:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;

            case R.id.clear_text:
                inputValue.setText("");
                break;

            case R.id.start_search:
                startSearch();
                break;
        }
    }

    private void changeInputFrame(boolean hasFocus) {
        if (hasFocus) {
            int left = DensityUtil.dip2px(this, 15);
            int right = DensityUtil.dip2px(this, 75);
            inputValue.setPadding(left, 0, right, 0);
            inputValue.setGravity(Gravity.CENTER);

            inputValue.setText(inputValue.getText().toString());
            clearText.setVisibility(View.VISIBLE);
            startSearch.setVisibility(View.VISIBLE);
            inputValue.setSelection(inputValue.getText().length());

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
        } else {
            int left = DensityUtil.dip2px(this, 15);
            int right = DensityUtil.dip2px(this, 15);
            inputValue.setPadding(left, 0, right, 0);
            inputValue.setGravity(Gravity.CENTER);

            inputValue.setText(inputValue.getText().toString());
            clearText.setVisibility(View.GONE);
            startSearch.setVisibility(View.GONE);

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }

    private void startSearch() {
        inputValue.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        String value = inputValue.getText().toString();
        if (value.equals("")){
            return;
        }
        try{
            resultList.clear();
            for (int i = 0; i < mListContact.size() - 1; i++){
                String account = mListContact.get(i).data.account;
                String label = mListContact.get(i).data.label;
                String username = mListContact.get(i).data.username;

                if(account.contains(value) || label.contains(value) || username.contains(value)){
                    resultList.add(mListContact.get(i));
                }
            }
            resultList.add(mListContact.get(mListContact.size() - 1));

            adapterFavor.notifyDataSetChanged();
        }catch (Exception e){}
        Logger.d("结束 resultList = " + resultList);
        if (resultList.size() <= 1){
            makeSnake(contactListLv, "没有结果", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Logger.d("position" + position + " resultList.size()" + resultList.size());
        if (!ClickUtils.isFastClick()) {
            return;
        }
        position--;
        if (position >= 0 && position < resultList.size()) {
            ContactModel mMap = resultList.get(position).data;
            Intent intent = new Intent(this, FriendCardActivity.class);
            intent.putExtra("friendInfo", mMap);
            intent.putExtra("pushDirect", "message");
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }
}
