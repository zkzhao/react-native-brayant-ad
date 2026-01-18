//
//  ExpressNativeAd.m
//  Zhiya
//

#import "ExpressNativeAd.h"
#import "PangleAdModule.h"
#import <BUAdSDK/BUAdSlot.h>
#import <BUAdSDK/BUNativeExpressAdManager.h>
#import <BUAdSDK/BUNativeExpressAdView.h>

@interface ExpressNativeAd () <BUNativeExpressAdViewDelegate>

@property(nonatomic, strong) BUNativeExpressAdManager *expressAdManager;
@property(nonatomic, strong) BUNativeExpressAdView *expressAdView;
@property(nonatomic, strong) UIView *containerView;
@property(nonatomic, assign) BOOL adLoaded;
@property(nonatomic, assign) BOOL adRendered;

@end

@implementation ExpressNativeAd

+ (instancetype)sharedInstance {
  static ExpressNativeAd *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[ExpressNativeAd alloc] init];
  });
  return instance;
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _adLoaded = NO;
    _adRendered = NO;
  }
  return self;
}

- (void)loadAdWithSlotID:(NSString *)slotID
                    width:(CGFloat)width
                   height:(CGFloat)height {
  if (!slotID || slotID.length == 0) {
    return;
  }

  self.adLoaded = NO;
  self.adRendered = NO;
  self.expressAdView = nil;

  CGFloat screenWidth = [UIScreen mainScreen].bounds.size.width;
  CGFloat finalWidth = width > 0 ? width : screenWidth;

  CGSize adSize = CGSizeMake(finalWidth, height);

  BUAdSlot *slot = [[BUAdSlot alloc] init];
  slot.ID = slotID;
  slot.AdType = BUAdSlotAdTypeFeed;
  slot.imgSize = [BUSize sizeBy:BUProposalSize_Feed228_150];

  self.expressAdManager =
      [[BUNativeExpressAdManager alloc] initWithSlot:slot adSize:adSize];
  self.expressAdManager.delegate = self;

  [self.expressAdManager loadAdDataWithCount:1];
}

- (BOOL)isAdReady {
  return self.adLoaded && self.expressAdView != nil && self.adRendered;
}

- (void)registerContainerView:(UIView *)containerView {
  if (!self.expressAdView || !containerView) {
    return;
  }

  self.containerView = containerView;

  dispatch_async(dispatch_get_main_queue(), ^{
    [containerView addSubview:self.expressAdView];
  });
}

#pragma mark - BUNativeExpressAdViewDelegate

- (void)nativeExpressAdSuccessToLoad:
            (BUNativeExpressAdManager *)nativeExpressAdManager
                               views:
                                   (NSArray<__kindof BUNativeExpressAdView *> *)
                                       views {
  if (views.count > 0) {
    self.expressAdView = views.firstObject;
    self.expressAdView.rootViewController =
        [UIApplication sharedApplication].keyWindow.rootViewController;

    [self.expressAdView render];

    self.adLoaded = YES;

    [[NSNotificationCenter defaultCenter]
        postNotificationName:@"PangleExpressNativeAdLoaded"
                      object:nil];

    if ([self.delegate respondsToSelector:@selector(expressAdDidLoad)]) {
      [self.delegate expressAdDidLoad];
    }
  }
}

- (void)nativeExpressAdFailToLoad:
            (BUNativeExpressAdManager *)nativeExpressAdManager
                            error:(NSError *)error {
  self.adLoaded = NO;
  self.adRendered = NO;

  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleExpressNativeAdLoadFail"
                    object:@{@"error" : error.localizedDescription}];

  if ([self.delegate
          respondsToSelector:@selector(expressAdDidFailWithError:)]) {
    [self.delegate expressAdDidFailWithError:error];
  }
}

- (void)nativeExpressAdViewRenderSuccess:
    (BUNativeExpressAdView *)nativeExpressAdView {
  self.adRendered = YES;

  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleExpressNativeAdRenderSuccess"
                    object:nil];

  if ([self.delegate respondsToSelector:@selector(expressAdDidShow)]) {
    [self.delegate expressAdDidShow];
  }
}

- (void)nativeExpressAdViewRenderFail:
            (BUNativeExpressAdView *)nativeExpressAdView
                                error:(NSError *)error {
  self.adLoaded = NO;
  self.adRendered = NO;

  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleExpressNativeAdLoadFail"
                    object:@{@"error" : error.localizedDescription}];

  if ([self.delegate
          respondsToSelector:@selector(expressAdDidFailWithError:)]) {
    [self.delegate expressAdDidFailWithError:error];
  }
}

- (void)nativeExpressAdViewDidClick:
    (BUNativeExpressAdView *)nativeExpressAdView {
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleExpressNativeAdClicked"
                    object:nil];

  if ([self.delegate respondsToSelector:@selector(expressAdDidClick)]) {
    [self.delegate expressAdDidClick];
  }
}

- (void)nativeExpressAdView:(BUNativeExpressAdView *)nativeExpressAdView
      dislikeWithReason:(NSArray<BUDislikeWords *> *)filterwords {
  [[NSNotificationCenter defaultCenter]
      postNotificationName:@"PangleExpressNativeAdClosed"
                    object:nil];

  [nativeExpressAdView removeFromSuperview];
  self.expressAdView = nil;
  self.adLoaded = NO;
  self.adRendered = NO;

  if ([self.delegate respondsToSelector:@selector(expressAdDidClose)]) {
    [self.delegate expressAdDidClose];
  }
}

@end
