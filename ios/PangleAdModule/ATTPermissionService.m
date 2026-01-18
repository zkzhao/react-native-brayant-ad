//
//  ATTPermissionService.m
//  Zhiya
//

#import "ATTPermissionService.h"
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

@implementation ATTPermissionService

+ (instancetype)sharedService {
    static ATTPermissionService *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[ATTPermissionService alloc] init];
    });
    return instance;
}

- (ATTAuthorizationStatus)currentStatus {
    if (@available(iOS 14.0, *)) {
        return (ATTAuthorizationStatus)[ATTrackingManager trackingAuthorizationStatus];
    }
    return ATTAuthorizationStatusAuthorized;
}

- (void)requestAuthorizationWithCompletion:(void(^)(BOOL granted))completion {
    if (@available(iOS 14.0, *)) {
        ATTrackingManagerAuthorizationStatus status = [ATTrackingManager trackingAuthorizationStatus];
        
        if (status == ATTrackingManagerAuthorizationStatusAuthorized) {
            if (completion) completion(YES);
            return;
        }
        
        if (status == ATTrackingManagerAuthorizationStatusNotDetermined) {
            [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
                BOOL granted = (status == ATTrackingManagerAuthorizationStatusAuthorized);
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (completion) completion(granted);
                });
            }];
        } else {
            // 用户已拒绝或受限
            if (completion) completion(NO);
        }
    } else {
        // iOS 14 以下默认授权
        if (completion) completion(YES);
    }
}

@end
