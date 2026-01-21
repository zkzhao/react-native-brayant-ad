//
//  BannerAdViewManager.m
//  react-native-brayant-ad
//
//  Created by Sisyphus on 2024-01-18
//  Copyright © 2024 Pangle. All rights reserved.
//

#import <React/RCTViewManager.h>
#import "BannerAd.h"

@interface BannerAdViewManager : RCTViewManager

@end

// BannerAd is currently Android-only
// This file is kept for potential future iOS support
// For now, BannerAdViewManager is not registered on iOS to avoid conflicts

@implementation BannerAdViewManager

// BannerAd currently only supports Android platform
// To avoid "Tried to register two views with the same name" error,
// we do not register this ViewManager on iOS
// Uncomment the line below if iOS support is added in the future
// RCT_EXPORT_MODULE(BrayantBannerAdViewManager)

- (UIView *)view {
  // BannerAd is managed by BannerAd singleton, not a direct view
  // This manager is mainly for prop handling
  UIView *containerView = [[UIView alloc] init];
  containerView.backgroundColor = [UIColor clearColor];
  return containerView;
}

#pragma mark - Properties

RCT_EXPORT_VIEW_PROPERTY(codeid, NSString)
RCT_EXPORT_VIEW_PROPERTY(adWidth, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(adHeight, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(visible, BOOL)

#pragma mark - Event Exports

- (NSArray<NSString *> *)customBubblingEventTypes {
  return @[
    @"onAdRenderSuccess",
    @"onAdError",
    @"onAdDismiss",
    @"onAdClick",
    @"onAdShow",
    @"onAdDislike"
  ];
}

@end
