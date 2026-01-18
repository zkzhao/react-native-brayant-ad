# Draft: iOS Pangle SDK Bridge Implementation

## Context

### Current State

**iOS (PangleAdModule)** - Uses **BUAdSDK** (Chinese Market SDK):

- ✅ `PangleAdModule` - Main bridge module (RCTEventEmitter)
- ✅ `PAGSDKService` - SDK initialization service (singleton)
- ✅ `SplashAd` - Splash ads implemented (singleton)
- ✅ `InterstitialAd` - Interstitial ads implemented (singleton)
- ✅ `BannerAd` - Banner ads implemented (singleton with auto-refresh)
- ✅ `ExpressNativeAd` - Express native ads implemented (singleton)
- ✅ `ATTPermissionService` - ATT permission handling

**Android (Fully Implemented)**:

- ✅ All ad types: Splash, RewardVideo, FullScreenVideo, FeedAd, DrawFeedAd, BannerAd
- ✅ Core: `DyADCore.java`, `AdManager.java`
- ✅ All view managers

**JavaScript API**:

- ✅ `src/index.tsx` - Main exports
- ✅ All ad type APIs with Promise-based + event subscription pattern
- ✅ Components: FeedAdView, DrawFeedView, BannerAdView

## Research Findings

### Official Pangle SDK Documentation

**Feed Ads (Native Ads - PAGLNativeAd)**:

- API: `PAGLNativeAd.loadAdWithSlotID:request:completionHandler:`
- Returns: `PAGLNativeAd` with `PAGLMaterialMeta` (ad data)
- Delegate: `PAGLNativeAdDelegate` (adDidShow, adDidClick, adDidDismiss)
- **Critical**: Must call `registerContainer:withClickableViews:` before displaying
- Uses `PAGNativeRequest` for configuration

**Banner Ads (PAGBannerAd)**:

- API: `PAGBannerAd.loadAdWithSlotID:request:completionHandler:`
- Returns: `bannerView` (UIView) to add to hierarchy
- Delegate: `PAGBannerAdDelegate` (adDidShow, adDidClick, adDidDismiss)
- Supports fixed sizes: 300x250dp, 320x50dp
- Supports adaptive banners (anchored & inline)
- Need to set `rootViewController`

**Splash Ads (App Open Ads - PAGLAppOpenAd)**:

- API: `PAGLAppOpenAd.loadAdWithSlotID:request:completionHandler:`
- Returns: `PAGLAppOpenAd` object
- Delegate: `PAGLAppOpenAdDelegate` (adDidShow, adDidClick, adDidDismiss)
- Call `presentFromRootViewController:` to show
- **Critical**: Must set `appLogoImage` during SDK initialization

### CRITICAL DISCOVERY: SDK Version Mismatch

**Current iOS Code**: Uses **BUAdSDK** (Chinese market SDK)

- Prefix: `BU*` (e.g., `BUNativeExpressAdView`, `BUAdSlot`)
- Framework: `<BUAdSDK/...>`

**Fetched Documentation**: Uses **PAG** prefix (International market SDK)

- Prefix: `PAG*` (e.g., `PAGLNativeAd`, `PAGBannerAd`)
- Framework: Not specified in docs (assumed `<PangleSDK/...>`)

## User Requirements (Confirmed)

### Ad Types to Implement on iOS

- ✅ FeedAd (信息流广告) - Need to implement
- ✅ Banner ads - Complete/fix existing
- ✅ Splash ads (开屏广告) - Complete/fix existing

### Goal

- ✅ Complete iOS parity with Android implementation
- ✅ Reuse existing PangleAdModule architecture

### Documentation

- ✅ User needs Pangle SDK official iOS documentation (provided from pangleglobal.com)

## Questions to Clarify

### SDK Version Decision

- **User Choice**: Chinese Market (BUAdSDK) - "国内版本"
- **Reasoning**: Continue with existing code base, targeting China mainland users

### Code Reuse Strategy

- **User Choice**: Modify existing modules
- **Reasoning**: Keep existing BUAdSDK patterns for compatibility

### Architecture Decision

- **User Choice**: Match Android as much as possible
- **Android Pattern**: FeedAdView.java (custom view) + FeedAdViewManager.java (view manager)
- **iOS Implementation**: Similar pattern - FeedAdView + FeedAdViewManager

## Decisions

### Architecture Approach

- **iOS View Manager**: Yes, follow Android pattern with FeedAdViewManager
- **Event Pattern**: Use NSNotificationCenter (consistent with existing PangleAdModule)
- **Singleton Pattern**: Use singleton for ad managers (consistent with BannerAd, ExpressNativeAd)
- **FeedAd Implementation**: Create FeedAdView.m + FeedAdViewManager.m (matching Android: FeedAdView.java + FeedAdViewManager.java)
- **ExpressNativeAd as Base**: ExpressNativeAd serves as底层 ad manager for FeedAd (reusable)
- **SDK Version**: Continue with BUAdSDK (Chinese market/国内版本)

### Technical Decisions

- **Framework**: `<BUAdSDK/BUNativeExpressAdManager.h>` (already in project)
- **Event Naming**: `PangleFeedAd*` pattern for FeedAd events (consistent with existing)
- **View Component**: Create `FeedAdView.m` as iOS native view (subclass of UIView)
- **Manager Component**: Create `FeedAdViewManager.m` as RN bridge (subclass of RCTViewManager)

### Verification Strategy

- **User Priority**: 中优先级 - 稳步修复，确保代码质量
- **Test Infrastructure**: 无测试框架（手动验证）
- **QA Approach**: 每个任务包含详细的 manual verification steps

## Scope

### IN Scope (What to implement)

- ✅ **FeedAd (信息流广告)**:
  - NEW: `ios/PangleAdModule/FeedAdViewManager.m` - RN view manager (createViewInstance, setCodeId, setAdWidth)
  - NEW: `ios/PangleAdModule/FeedAdView.m` - Native view component (UIView with ExpressNativeAd integration)
  - MODIFY: `ios/PangleAdModule/PangleAdModule.m` (add loadFeedAd, registerFeedAdContainer methods)
  - VERIFY: `src/index.tsx` (FeedAd export exists and matches)
  - VERIFY: `src/dy/component/FeedAd.tsx` (FeedAdView component references FeedAdViewManager)
  - BASE ON: `ios/PangleAdModule/ExpressNativeAd.m` (use as ad manager, not modify)

- ✅ **BannerAds (verify and fix if needed)**:
  - REVIEW: `ios/PangleAdModule/BannerAd.m` implementation
  - REVIEW: `ios/PangleAdModule/BannerAd.h` interface
  - VERIFY: PangleAdModule exports Banner methods
  - ENSURE: Proper BUAdSDK delegate patterns
  - ENSURE: Event emission via NSNotificationCenter

- ✅ **SplashAds (verify and fix if needed)**:
  - REVIEW: `ios/PangleAdModule/SplashAd.m` implementation
  - REVIEW: `ios/PangleAdModule/SplashAd.h` interface
  - VERIFY: PangleAdModule exports Splash methods
  - ENSURE: Proper BUAdSDK delegate patterns
  - ENSURE: Event emission via NSNotificationCenter

### OUT Scope (Explicitly excluded)

- ❌ RewardVideo (激励视频) - Not requested
- ❌ FullScreenVideo (全屏视频) - Not requested
- ❌ DrawFeedAd (视频刷信息流) - Not requested (Android only)
- ❌ Migration to PAG SDK - Continue with BUAdSDK (国内版本)
- ❌ Create BannerAdViewManager - Not requested (keep existing BannerAd.m pattern)

### Guardrails (Must NOT do)

- ❌ DO NOT modify ExpressNativeAd.m (it works correctly, reuse as base)
- ❌ DO NOT change existing event naming conventions
- ❌ DO NOT switch to PAG SDK (maintain BUAdSDK)
- ❌ DO NOT modify Android code
- ❌ DO NOT change JavaScript API (maintain compatibility)
- ❌ DO NOT create RewardVideo/FullScreenVideo implementations (out of scope)

### Code Reuse Strategy

- **Pattern**: Follow existing ExpressNativeAd.m singleton pattern
- **Event System**: Use NSNotificationCenter (PangleFeedAdLoaded, PangleFeedAdLoadFail, PangleFeedAdRenderSuccess, PangleFeedAdClicked, PangleFeedAdClosed)
- **View Manager Pattern**: Follow Android's FeedAdViewManager.java:
  - `getName()`: return @"FeedAdViewManager"
  - `createViewInstance()`: return new FeedAdView(themedReactContext)
  - `@ReactProp(name = "codeid")`: setCodeId
  - `@ReactProp(name = "adWidth")`: setAdWidth
  - `getExportedCustomBubblingEventTypeConstants()`: define event mapping
- **Native View Pattern**: Follow Android's FeedAdView.java:
  - Init: ExpressNativeAd singleton call to load ad
  - Props: codeid, adWidth, visible
  - Events: onAdError, onAdLayout, onAdClick, onAdClose
  - Container: Use ExpressNativeAd.registerContainerView: to register
- **PangleAdModule Pattern**: Follow existing module export pattern with RCT_EXPORT_METHOD
