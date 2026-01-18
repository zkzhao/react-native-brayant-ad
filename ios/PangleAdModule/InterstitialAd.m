//
//  InterstitialAd.m
//  Zhiya
//

#import "InterstitialAd.h"
#import "PangleAdModule.h"
#import <BUAdSDK/BUAdSlot.h>
#import <BUAdSDK/BUNativeExpressFullscreenVideoAd.h>
#import <BUAdSDK/BUSize.h>

@interface InterstitialAd () <BUNativeExpressFullscreenVideoAdDelegate>

@property(nonatomic, strong) BUNativeExpressFullscreenVideoAd *interstitialAd;
@property(nonatomic, copy) InterstitialCompletionBlock completeBlock;
@property(nonatomic, assign) BOOL adLoaded;

@end

@implementation InterstitialAd

+ (instancetype)sharedInstance {
  static InterstitialAd *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[InterstitialAd alloc] init];
  });
  return instance;
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _adLoaded = NO;
  }
  return self;
}

- (void)loadAdWithSlotID:(NSString *)slotID {
  if (!slotID || slotID.length == 0) {
    NSLog(@"[Pangle] 插屏广告 SlotID 不能为空");
    return;
  }

  self.adLoaded = NO;
  self.interstitialAd = nil;

  BUSize *imgSize = [[BUSize alloc] init];
  imgSize.width = 1080;
  imgSize.height = 1920;

  BUAdSlot *slot = [[BUAdSlot alloc] init];
  slot.ID = slotID;
  slot.position = BUAdSlotPositionFullscreen;
  slot.imgSize = imgSize;

  NSLog(@"[Pangle] 开始加载插屏广告, SlotID: %@", slotID);

  BUNativeExpressFullscreenVideoAd *fullscreenVideoAd =
      [[BUNativeExpressFullscreenVideoAd alloc] initWithSlot:slot];
  fullscreenVideoAd.delegate = self;
  self.interstitialAd = fullscreenVideoAd;

  [fullscreenVideoAd loadAdData];
}

- (BOOL)isAdReady {
  return self.adLoaded && self.interstitialAd != nil;
}

- (void)showAdInRootViewController:(UIViewController *)rootVC
                        onComplete:(InterstitialCompletionBlock)completeBlock {
  self.completeBlock = completeBlock;

  if (!self.interstitialAd || !self.adLoaded) {
    NSError *error =
        [NSError errorWithDomain:@"com.pangle.interstitial"
                            code:1001
                        userInfo:@{NSLocalizedDescriptionKey : @"广告未加载"}];
    NSLog(@"[Pangle] 尝试展示插屏广告但广告未加载");
    if (completeBlock)
      completeBlock(NO, error);
    return;
  }

  if (!rootVC) {
    NSError *error = [NSError
        errorWithDomain:@"com.pangle.interstitial"
                   code:1002
               userInfo:@{
                 NSLocalizedDescriptionKey : @"rootViewController 不能为空"
               }];
    NSLog(@"[Pangle] rootViewController 为空，无法展示插屏广告");
    if (completeBlock)
      completeBlock(NO, error);
    return;
  }

  NSLog(@"[Pangle] 展示插屏广告");
  [self.interstitialAd showAdFromRootViewController:rootVC];
  self.adLoaded = NO;
}

- (void)removeAd {
  self.interstitialAd = nil;
  self.adLoaded = NO;
}

#pragma mark - BUNativeExpressFullscreenVideoAdDelegate

- (void)nativeExpressFullscreenVideoAdDidLoad:
    (BUNativeExpressFullscreenVideoAd *)ad {
  NSLog(@"[Pangle] 插屏广告加载成功");
  self.adLoaded = YES;
  if ([self.delegate
          respondsToSelector:@selector(interstitialAdDidLoadSuccess:)]) {
    [self.delegate interstitialAdDidLoadSuccess:ad];
  }
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleInterstitialAdLoaded"
                    object:nil];
}

- (void)nativeExpressFullscreenVideoAd:(BUNativeExpressFullscreenVideoAd *)ad
                  didLoadFailWithError:(NSError *)error {
  NSLog(@"[Pangle] 插屏广告加载失败: %@", error.localizedDescription);
  self.adLoaded = NO;
  if ([self.delegate
          respondsToSelector:@selector(interstitialAdDidLoadFail:error:)]) {
    [self.delegate interstitialAdDidLoadFail:ad error:error];
  }
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleInterstitialAdLoadFail"
                    object:@{@"error" : error.localizedDescription}];
}

- (void)nativeExpressFullscreenVideoAdDidVisible:
    (BUNativeExpressFullscreenVideoAd *)ad {
  NSLog(@"[Pangle] 插屏广告已展示");
  if ([self.delegate respondsToSelector:@selector(interstitialAdDidShow:)]) {
    [self.delegate interstitialAdDidShow:ad];
  }
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleInterstitialAdShowed"
                    object:nil];
}

- (void)nativeExpressFullscreenVideoAdDidClick:
    (BUNativeExpressFullscreenVideoAd *)ad {
  NSLog(@"[Pangle] 用户点击插屏广告");
  if ([self.delegate respondsToSelector:@selector(interstitialAdDidClick:)]) {
    [self.delegate interstitialAdDidClick:ad];
  }
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleInterstitialAdClicked"
                    object:nil];
}

- (void)nativeExpressFullscreenVideoAdDidClose:
    (BUNativeExpressFullscreenVideoAd *)ad {
  NSLog(@"[Pangle] 插屏广告关闭");
  [self removeAd];

  if (self.completeBlock) {
    self.completeBlock(YES, nil);
  }

  if ([self.delegate respondsToSelector:@selector(interstitialAdDidDismiss:)]) {
    [self.delegate interstitialAdDidDismiss:ad];
  }
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleInterstitialAdClosed"
                    object:nil];
}

- (void)nativeExpressFullscreenVideoAd:(BUNativeExpressFullscreenVideoAd *)ad
                      didFailWithError:(NSError *)error {
  NSLog(@"[Pangle] 插屏广告展示失败: %@", error.localizedDescription);
  [self removeAd];

  if (self.completeBlock) {
    self.completeBlock(NO, error);
  }
}

@end
