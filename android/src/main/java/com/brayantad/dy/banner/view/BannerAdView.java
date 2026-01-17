package com.brayantad.dy.banner.view;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.brayantad.R;
import com.brayantad.dy.DyADCore;
import com.brayantad.utils.Utils;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class BannerAdView extends RelativeLayout {
  // Banner广告
  private static final String TAG = "BannerAd";

  private Activity mContext;
  private ReactContext reactContext;
  private String _codeid = "";
  private AdSlot adSlot;
  private TTNativeExpressAd mBannerAd;

  private int _expectedWidth = 320; // 默认宽度 dp
  private int _expectedHeight = 50; // 默认高度 dp

  public BannerAdView(ReactContext context) {
    super(context);
    mContext = context.getCurrentActivity();
    reactContext = context;
    // 开始展开
    inflate(context, R.layout.feed_view, this);

    // 这个函数很关键，不然不能触发再次渲染，让 view 在 RN 里渲染成功!!
    Utils.setupLayoutHack(this);
  }

  public void setWidth(int width) {
    Log.d(TAG, "setCodeId = " + _codeid + ", setWidth:" + width);
    _expectedWidth = width;
    showAd();
  }

  public void setHeight(int height) {
    Log.d(TAG, "setCodeId = " + _codeid + ", setHeight:" + height);
    _expectedHeight = height;
    showAd();
  }

  public void setCodeId(String codeId) {
    Log.d(TAG, "setCodeId: " + codeId + ", _expectedWidth:" + _expectedWidth);
    _codeid = codeId;
    showAd();
  }

  public void showAd() {
    Log.d(TAG, "showAd: width:" + _expectedWidth + " height:" + _expectedHeight + " codeid:" + _codeid);

    // 显示广告
    if (_expectedWidth <= 0 || _expectedHeight <= 0 || _codeid.isEmpty()) {
      // 广告宽高未设置或 code id 未设置，停止显示广告
      return;
    }

    // 在UI线程加载广告
    runOnUiThread(
      () -> {
        loadBannerAd();
      }
    );
  }

  // 显示Banner广告
  public void loadBannerAd() {
    if (DyADCore.TTAdSdk == null) {
      Log.e(TAG, "TTAdSdk 还没初始化");
      return;
    }

    // 如果已有广告，先销毁
    if (mBannerAd != null) {
      mBannerAd.destroy();
    }

    // 创建广告请求参数AdSlot
    adSlot =
      new AdSlot.Builder()
        .setCodeId(_codeid) // 广告位id
        .setSupportDeepLink(true)
        .setAdCount(1) // 请求数量设置为1
        .setExpressViewAcceptedSize(_expectedWidth, _expectedHeight) // 期望模板广告view的size,单位dp
        .build();

    // 请求广告
    final BannerAdView _this = this;
    DyADCore.TTAdSdk.loadBannerExpressAd(
      adSlot,
      new TTAdNative.NativeExpressAdListener() {

        @Override
        public void onError(int code, String message) {
          message =
            "Banner ad error: " + code + ", " + message;
          Log.e(TAG, message);
          onAdError(message);
        }

        @Override
        public void onNativeExpressAdLoad(java.util.List<TTNativeExpressAd> ads) {
          Log.d(TAG, "onNativeExpressAdLoad: Banner ad loaded!");
          if (ads == null || ads.isEmpty()) {
            onAdError("Banner ad loaded but no content");
            return;
          }

          mBannerAd = ads.get(0);
          _showBannerAd(mBannerAd);
        }
      }
    );
  }

  // 显示广告
  private void _showBannerAd(final TTNativeExpressAd ad) {
    mContext.runOnUiThread(
      () -> {
        bindAdListener(ad);
        ad.render();
      }
    );
  }

  // 绑定Banner express ================================
  private final void bindAdListener(TTNativeExpressAd ad) {
    final RelativeLayout mExpressContainer = findViewById(R.id.feed_container);
    ad.setExpressInteractionListener(
      new TTNativeExpressAd.ExpressAdInteractionListener() {

        @Override
        public void onAdClicked(View view, int type) {
          Log.d(TAG, "Banner ad clicked");
          onAdClick();
        }

        @Override
        public void onAdShow(View view, int type) {
          Log.d(TAG, "Banner onAdShow");
          onAdShow();
        }

        @Override
        public void onAdDismiss() {
          Log.d(TAG, "Banner onAdDismiss");
          onAdDismiss();
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
          Log.e(TAG, "Banner render fail: " + code + ", " + msg);
          onAdError("渲染失败: " + msg);
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
          Log.d(TAG, "Banner onRenderSuccess: " + width + ", " + height);
          // 在渲染成功回调时展示广告
          RelativeLayout mExpressContainer = findViewById(R.id.feed_container);
          if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
            mExpressContainer.addView(view);
          }
          onAdRenderSuccess((int) width, (int) height);
        }
      }
    );
    // dislike设置
    bindDislike(ad);
  }

  /**
   * 设置广告的不喜欢
   *
   * @param ad
   */
  private void bindDislike(TTNativeExpressAd ad) {
    // 使用默认个性化模板中默认dislike弹出样式
    ad.setDislikeCallback(
      mContext,
      new TTAdDislike.DislikeInteractionCallback() {

        @Override
        public void onShow() {}

        @Override
        public void onSelected(int position, String value, boolean enforce) {
          Log.d(TAG, "Banner dislike selected: " + value);
          // 用户选择不喜欢原因后，移除广告展示
          RelativeLayout mExpressContainer = findViewById(R.id.feed_container);
          if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
          }
          onAdDislike(value);
        }

        @Override
        public void onCancel() {
          Log.d(TAG, "Banner dislike cancel");
        }
      }
    );
  }

  // 外部事件..
  public void onAdError(String message) {
    WritableMap event = Arguments.createMap();
    event.putString("message", message);
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdError", event);
  }

  public void onAdClick() {
    WritableMap event = Arguments.createMap();
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdClick", event);
  }

  public void onAdShow() {
    WritableMap event = Arguments.createMap();
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdShow", event);
  }

  public void onAdDismiss() {
    WritableMap event = Arguments.createMap();
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdDismiss", event);
  }

  public void onAdRenderSuccess(int width, int height) {
    WritableMap event = Arguments.createMap();
    event.putInt("width", width);
    event.putInt("height", height);
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdRenderSuccess", event);
  }

  public void onAdDislike(String reason) {
    WritableMap event = Arguments.createMap();
    event.putString("reason", reason);
    reactContext
      .getJSModule(RCTEventEmitter.class)
      .receiveEvent(getId(), "onAdDislike", event);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // View被移除时销毁广告
    if (mBannerAd != null) {
      mBannerAd.destroy();
      mBannerAd = null;
    }
  }
}
