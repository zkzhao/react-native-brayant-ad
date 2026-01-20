package com.brayantad.dy.banner.view;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    Log.d(TAG, "[DEBUG] BannerAdView constructor - inflating feed_view.xml");
    inflate(context, R.layout.feed_view, this);
    Utils.setupLayoutHack(this);

    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT,
      _expectedHeight
    );
    setLayoutParams(params);

    Log.d(TAG, "[DEBUG] BannerAdView constructor completed - LayoutParams set (width=MATCH_PARENT, height=" + _expectedHeight + ")");
  }

  public void setWidth(int width) {
    Log.d(TAG, "[DEBUG] setWidth called - codeid=" + _codeid + ", width=" + width + ", current height=" + _expectedHeight);
    _expectedWidth = width;
    showAd();
  }

  public void setHeight(int height) {
    Log.d(TAG, "[DEBUG] setHeight called - codeid=" + _codeid + ", height=" + height + ", current width=" + _expectedWidth);
    _expectedHeight = height;

    ViewGroup.LayoutParams params = getLayoutParams();
    if (params != null) {
      params.height = height;
      setLayoutParams(params);
      Log.d(TAG, "[DEBUG] setHeight - LayoutParams updated to height=" + height);
    }

    showAd();
  }

  public void setCodeId(String codeId) {
    Log.d(TAG, "[DEBUG] setCodeId called - codeid=" + codeId + ", _expectedWidth=" + _expectedWidth + ", _expectedHeight=" + _expectedHeight);
    _codeid = codeId;
    showAd();
  }

  /**
   * 设置可见性
   * @param visible true: 可见，false: 不可见
   */
  public void setVisibility(boolean visible) {
    Log.d(TAG, "[DEBUG] setVisibility called - visible=" + visible + ", current codeid=" + _codeid + ", SDK initialized=" + (DyADCore.TTAdSdk != null));
    if (visible) {
      super.setVisibility(View.VISIBLE);
      // 可见时尝试加载广告
      showAd();
    } else {
      super.setVisibility(View.INVISIBLE);
    }
  }

  public void showAd() {
    Log.d(TAG, "[DEBUG] showAd called - width=" + _expectedWidth + ", height=" + _expectedHeight + ", codeid=" + _codeid + ", SDK initialized=" + (DyADCore.TTAdSdk != null));

    // 显示广告
    if (_expectedWidth <= 0 || _expectedHeight <= 0 || _codeid.isEmpty()) {
      Log.w(TAG, "[DEBUG] showAd aborted - width=" + _expectedWidth + " (must >0), height=" + _expectedHeight + " (must >0), codeid=" + _codeid + " (must not be empty)");
      // 广告宽高未设置或 code id 未设置，停止显示广告
      return;
    }

    // 在UI线程加载广告
    runOnUiThread(
      () -> {
        Log.d(TAG, "[DEBUG] showAd - calling loadBannerAd on UI thread");
        loadBannerAd();
      }
    );
  }

  // 显示Banner广告
  public void loadBannerAd() {
    Log.d(TAG, "[DEBUG] loadBannerAd called - SDK initialized=" + (DyADCore.TTAdSdk != null) + ", codeid=" + _codeid);

    if (DyADCore.TTAdSdk == null) {
      Log.e(TAG, "[DEBUG] loadBannerAd aborted - TTAdSdk not initialized yet");
      return;
    }

    // 如果已有广告，先销毁
    if (mBannerAd != null) {
      Log.d(TAG, "[DEBUG] loadBannerAd - destroying previous ad");
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

    Log.d(TAG, "[DEBUG] loadBannerAd - requesting ad with AdSlot: codeid=" + _codeid + ", width=" + _expectedWidth + ", height=" + _expectedHeight);

    // 请求广告
    final BannerAdView _this = this;
    DyADCore.TTAdSdk.loadBannerExpressAd(
      adSlot,
      new TTAdNative.NativeExpressAdListener() {

        @Override
        public void onError(int code, String message) {
          Log.e(TAG, "[DEBUG] onError - code=" + code + ", message=" + message);
          message =
            "Banner ad error: " + code + ", " + message;
          Log.e(TAG, message);
          onAdError(message);
        }

        @Override
        public void onNativeExpressAdLoad(java.util.List<TTNativeExpressAd> ads) {
          Log.d(TAG, "[DEBUG] onNativeExpressAdLoad - ads received=" + (ads != null ? ads.size() : "null"));
          if (ads == null || ads.isEmpty()) {
            Log.e(TAG, "[DEBUG] onNativeExpressAdLoad - ads is null or empty");
            onAdError("Banner ad loaded but no content");
            return;
          }

          mBannerAd = ads.get(0);
          Log.d(TAG, "[DEBUG] onNativeExpressAdLoad - calling _showBannerAd");
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
          BannerAdView.this.onAdShow();
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
          Log.e(TAG, "Banner render fail: " + code + ", " + msg);
          onAdError("渲染失败: " + msg);
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
          Log.d(TAG, "[DEBUG] onRenderSuccess - adView width=" + width + ", adView height=" + height);

          RelativeLayout mExpressContainer = findViewById(R.id.feed_container);

          if (mExpressContainer == null) {
            Log.e(TAG, "[DEBUG] onRenderSuccess - feed_container is null!");
            onAdError("feed_container not found");
            return;
          }

          Log.d(TAG, "[DEBUG] onRenderSuccess - feed_container found, removing old views");
          mExpressContainer.removeAllViews();

          RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
          );
          mExpressContainer.addView(view, params);

          Log.d(TAG, "[DEBUG] onRenderSuccess - adView added to feed_container");

          RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams) mExpressContainer.getLayoutParams();
          if (containerParams != null) {
            containerParams.height = (int) height;
            mExpressContainer.setLayoutParams(containerParams);
            Log.d(TAG, "[DEBUG] onRenderSuccess - feed_container LayoutParams updated to height=" + (int) height);
          }

          RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) BannerAdView.this.getLayoutParams();
          if (viewParams != null) {
            viewParams.height = (int) height;
            BannerAdView.this.setLayoutParams(viewParams);
            Log.d(TAG, "[DEBUG] onRenderSuccess - BannerAdView LayoutParams updated to height=" + (int) height);
          }

          Log.d(TAG, "[DEBUG] onRenderSuccess - BannerAdView size: width=" + BannerAdView.this.getWidth() + ", height=" + BannerAdView.this.getHeight());
          Log.d(TAG, "[DEBUG] onRenderSuccess - feed_container size: width=" + mExpressContainer.getWidth() + ", height=" + mExpressContainer.getHeight());

          view.setVisibility(View.VISIBLE);
          mExpressContainer.setVisibility(View.VISIBLE);
          BannerAdView.this.setVisibility(View.VISIBLE);

          mExpressContainer.requestLayout();
          BannerAdView.this.requestLayout();

          Log.d(TAG, "[DEBUG] onRenderSuccess - layout requested, sending onAdRenderSuccess event");

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
