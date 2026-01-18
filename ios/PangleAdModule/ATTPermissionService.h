//
//  ATTPermissionService.h
//  Zhiya
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ATTAuthorizationStatus) {
    ATTAuthorizationStatusNotDetermined = 0,
    ATTAuthorizationStatusRestricted = 1,
    ATTAuthorizationStatusDenied = 2,
    ATTAuthorizationStatusAuthorized = 3,
};

@interface ATTPermissionService : NSObject <RCTBridgeModule>

+ (instancetype)sharedService;

/**
 * 获取当前 ATT 授权状态
 */
- (ATTAuthorizationStatus)currentStatus;

/**
 * 请求用户授权追踪
 * @param completion 授权完成回调 (granted: 是否授权)
 */
- (void)requestAuthorizationWithCompletion:(void(^)(BOOL granted))completion;

@end

NS_ASSUME_NONNULL_END
