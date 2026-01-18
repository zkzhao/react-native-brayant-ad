//
//  PAGSDKService.h
//  Zhiya
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^PangleInitializationBlock)(BOOL success, NSError * _Nullable error);

@interface PAGSDKService : NSObject

+ (instancetype)sharedService;

- (void)initializeSDKWithAppID:(NSString *)appID
                     completion:(nullable PangleInitializationBlock)completion;

- (BOOL)isInitialized;

- (NSString *)SDKVersion;

@end

NS_ASSUME_NONNULL_END
