//
//  SplashAd.h
//  Zhiya
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class UIViewController;

@interface SplashAd : NSObject

+ (instancetype)sharedInstance;

- (void)loadAdWithSlotID:(NSString *)slotID;

- (BOOL)isAdReady;

- (void)showAdInRootViewController:(UIViewController *)rootVC
                        onComplete:(void(^)(BOOL completed, NSError * _Nullable error))completeBlock;

- (void)removeAd;

@end

NS_ASSUME_NONNULL_END
