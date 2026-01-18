//
//  ExpressNativeAd.h
//  Zhiya
//

#import <BUAdSDK/BUNativeExpressAdManager.h>
#import <BUAdSDK/BUNativeExpressAdView.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ExpressNativeAdDelegate <NSObject>
@optional
- (void)expressAdDidLoad;
- (void)expressAdDidFailWithError:(NSError *)error;
- (void)expressAdDidShow;
- (void)expressAdDidClick;
- (void)expressAdDidClose;
@end

@interface ExpressNativeAd : NSObject

+ (instancetype)sharedInstance;

@property(nonatomic, weak, nullable) id<ExpressNativeAdDelegate> delegate;
@property(nonatomic, strong, readonly, nullable)
    BUNativeExpressAdView *expressAdView;

- (void)loadAdWithSlotID:(NSString *)slotID
                   width:(CGFloat)width
                  height:(CGFloat)height;
- (BOOL)isAdReady;
- (void)registerContainerView:(UIView *)containerView;

@end

NS_ASSUME_NONNULL_END
