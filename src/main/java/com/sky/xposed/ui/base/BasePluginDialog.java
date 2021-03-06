/*
 * Copyright (c) 2020 The sky Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sky.xposed.ui.base;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.sky.xposed.common.util.ToastUtil;
import com.sky.xposed.ui.R;
import com.sky.xposed.ui.UIAttribute;
import com.sky.xposed.ui.UIConstant;
import com.sky.xposed.ui.dialog.LoadingDialog;
import com.sky.xposed.ui.info.UAttributeSet;
import com.sky.xposed.ui.util.DisplayUtil;
import com.sky.xposed.ui.util.LayoutUtil;
import com.sky.xposed.ui.util.ViewUtil;
import com.sky.xposed.ui.view.PluginFrameLayout;
import com.sky.xposed.ui.view.TitleView;
import com.squareup.picasso.Picasso;

/**
 * Created by sky on 2019-06-06.
 */
public abstract class BasePluginDialog extends BaseDialogFragment implements BaseView {

    private TitleView mToolbar;
    private ImageButton mMoreButton;
    private PluginFrameLayout mPluginFrame;

    private LoadingDialog mLoadingDialog;

    private Callback mCallback;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {

        // ?????????????????????
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // ?????????View
        mPluginFrame = onCreatePluginFrame();

        // ??????
        mToolbar = mPluginFrame.getTitleView();

        // ???????????????View
        createView(mPluginFrame);

        return mPluginFrame;
    }

    protected PluginFrameLayout onCreatePluginFrame() {
        return createScrollPluginFrame();
    }

    @Override
    public void onStart() {

        // ??????Dialog???Window
        Window window = getDialog().getWindow();

        if (window != null) {
            // ??????????????????
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.width = DisplayUtil.DIP_330;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        super.onStart();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // ??????????????????
        setResult(Activity.RESULT_CANCELED, null);
    }

    public TitleView getTitleView() {
        return mToolbar;
    }

    public PluginFrameLayout getPluginFrame() {
        return mPluginFrame;
    }

    public void removeContentTopPadding() {
        LinearLayout linearLayout = mPluginFrame.getContentView();
        linearLayout.setPadding(0, 0, 0, linearLayout.getPaddingBottom());
    }

    public void addContentTopPadding() {
        LinearLayout linearLayout = mPluginFrame.getContentView();
        linearLayout.setPadding(0, linearLayout.getPaddingBottom(), 0, linearLayout.getPaddingBottom());
    }

    /**
     * ??????View,???View?????????frameView???
     * @param frameView
     */
    public abstract void createView(PluginFrameLayout frameView);

    @Override
    protected void initView(View view, Bundle args) {
        if (args != null) {
            final String title = args.getString(UIConstant.Key.TITLE);
            if (!TextUtils.isEmpty(title)) setTitle(title);
        }
    }

    /**
     * ????????????
     * @param title
     */
    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    /**
     * ????????????
     */
    public void showBack() {
        showBack(R.drawable.ic_action_clear);
    }

    /**
     * ????????????,????????????????????????
     * @param resId
     */
    public void showBack(int resId) {

        mToolbar.showBack();
        // ????????????
        mToolbar.setOnBackEventListener(view1 -> {
            if (onBackEvent()) {
                setResult(Activity.RESULT_CANCELED, null);
                dismiss();
            }
        });

        // ????????????
        Picasso.get()
                .load(getPluginResource(resId))
                .into(getTitleView().getBackView());
    }

    /**
     * ?????????????????????ImageButton
     * @param resId
     * @param listener
     */
    public void addMoreImageView(int resId, View.OnClickListener listener) {

        ImageButton iBtn = getTitleView().addMoreImageButton();
        iBtn.setOnClickListener(listener);

        // ????????????
        Picasso.get()
                .load(getPluginResource(resId))
                .into(iBtn);
    }

    /**
     * ??????????????????
     * @return
     */
    public boolean onBackEvent() {
        return true;
    }

    /**
     * ??????????????????
     */
    public void showMoreMenu() {

        mMoreButton = mToolbar.addMoreImageButton();
        mMoreButton.setOnClickListener(v -> {
            // ??????????????????
            final PopupMenu popupMenu = new PopupMenu(getContext(), mMoreButton, Gravity.RIGHT);
            onCreateMoreMenu(popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                onMoreItemSelected(item);
                return true;
            });
            popupMenu.show();
        });

        // ????????????
        Picasso.get()
                .load(getPluginResource(R.drawable.ic_action_more_vert))
                .into(mMoreButton);
    }

    /**
     * ???????????????????????????
     * @param menu
     */
    public void onCreateMoreMenu(Menu menu) {

    }

    /**
     * ????????????Button
     * @param text
     * @param listener
     */
    public void addEndButton(String text, View.OnClickListener listener) {

        int width = DisplayUtil.DIP_60;
        int height = DisplayUtil.DIP_36;

        LinearLayout.LayoutParams params = LayoutUtil.newLinearLayoutParams(width, height);
        params.rightMargin = DisplayUtil.DIP_15;
        params.topMargin = DisplayUtil.DIP_8;
        params.bottomMargin = params.topMargin;

        Button button = ViewUtil.newDialogButton(getContext(), text, v -> {
            if (listener != null) {
                listener.onClick(v);
            } else {
                if (onBackEvent()) {
                    setResult(Activity.RESULT_CANCELED, null);
                    dismiss();
                }
            }
        });
        button.setLayoutParams(params);

        LinearLayout linearLayout = getPluginFrame().getEndView();
        linearLayout.addView(button);
        ViewUtil.setVisibility(linearLayout, View.VISIBLE);
    }

    /**
     * ????????????????????????
     * @param item
     * @return
     */
    public boolean onMoreItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void showLoading() {
        showLoading("Loading...");
    }

    @Override
    public void showLoading(String text) {

        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            // ???????????????
            return;
        }

        mLoadingDialog = new LoadingDialog(getContext());
        mLoadingDialog.setTip(text);
        mLoadingDialog.show();
    }

    @Override
    public void cancelLoading() {

        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            // ???????????????
            return;
        }

        mLoadingDialog.dismiss();
        mLoadingDialog = null;
    }

    @Override
    public void showMessage(String msg) {
        ToastUtil.show(msg);
    }

    public void setResult(int resultCode, Bundle data) {
        if (mCallback != null) mCallback.onResult(resultCode, data);
    }

    public void show(Activity activity, String tag, Bundle args, Callback callback) {
        mCallback = callback;
        setArguments(args);
        show(activity.getFragmentManager(), tag);
    }

    public void show(Activity activity, Bundle args, Callback callback) {
        show(activity, this.getClass().getSimpleName(), args, callback);
    }

    public void show(Activity activity, Bundle args) {
        show(activity, args, null);
    }

    public void show(Activity activity, Callback callback) {
        show(activity, null, callback);
    }

    public void show(Activity activity) {
        show(activity, null, null);
    }


    public PluginFrameLayout createScrollPluginFrame() {
        return new PluginFrameLayout(getContext(), new UAttributeSet.Build()
                .putInt(UIAttribute.PluginFrame.style, PluginFrameLayout.Style.SCROLL)
                .build());
    }

    public PluginFrameLayout createLinePluginFrame() {
        return new PluginFrameLayout(getContext(), new UAttributeSet.Build()
                .putInt(UIAttribute.PluginFrame.style, PluginFrameLayout.Style.LINE)
                .build());
    }


    /**
     * Dialog????????????
     */
    public interface Callback {

        /**
         * ??????????????????
         * @param resultCode
         * @param data
         */
        void onResult(int resultCode, Bundle data);
    }
}
