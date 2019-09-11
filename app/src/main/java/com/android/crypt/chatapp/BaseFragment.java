package com.android.crypt.chatapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypt.chatapp.R;


public class BaseFragment extends Fragment {

    public Toolbar toolbar;
    public TextView cosTitleTextView;
    public Dialog dialog;
    public OnFragmentInteractionListener mListener;


    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 如果集成的Fragment有ActionBar菜单，则设置该fragment拥有自己的actionItem
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        cosTitleTextView = (TextView) getActivity().findViewById(R.id.cos_text_title);
        cosTitleTextView.setVisibility(View.GONE);
        setHasOptionsMenu(true);
    }

    // ====================读写私有文件==========================
    /**
     * 根据键名在xml文件读取相应值
     *
     * @param fileName
     * @param key
     * @param defaultVal
     *            默认值
     * @return
     */
    public String getPrivateXml(String fileName, String key, String defaultVal) {
        try {
            SharedPreferences sp = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
            return sp.getString(key, defaultVal);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 在xml写入键值对
     *
     * @param fileName
     * @param key
     * @param val
     * @return
     */
    public boolean setPrivateXml(String fileName, String key, String val) {
        try {
            SharedPreferences sp = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString(key, val);
            e.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createDialog(String msg) {
        try {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_base,
                    null);
            LinearLayout dialogLL = (LinearLayout) view;
            TextView tv_msg = (TextView) dialogLL.findViewById(R.id.tv_msg);
            tv_msg.setText(msg);
            dialog = new Dialog(getActivity(), R.style.dialog_mass);
            Window win = dialog.getWindow();// 获取所在window
            android.view.WindowManager.LayoutParams params = win.getAttributes();// 获取LayoutParams
            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            win.setAttributes(params);// 设置生效
            dialog.setContentView(dialogLL);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 优化显示的toast
     * @param context
     * @param msg
     * @param imgRes
     * @param duration
     */
    public void makeToast(Context context, String msg, int imgRes, int duration){
        Looper.prepare();
        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.CENTER, 0, 100);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imgToast = new ImageView(getActivity());
        imgToast.setImageResource(imgRes);
        imgToast.setPadding(5,30,5,30);
        toastView.addView(imgToast, 0);
        toast.show();
        Looper.loop();
    }

    public Snackbar makeSnake(View view, String msg, int imgRes, int duration){
        Snackbar snackbar = Snackbar.make(view, msg, duration);
        Snackbar.SnackbarLayout snackView = (Snackbar.SnackbarLayout) snackbar.getView();
        ImageView imgSnack = new ImageView(getActivity());
        imgSnack.setImageResource(imgRes);
        imgSnack.setPadding(5,30,5,30);
        ((SnackbarContentLayout)snackView.getChildAt(0)).addView(imgSnack, 0);
        snackbar.show();
        return snackbar;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}