import React, { useState } from 'react';
import { Platform, requireNativeComponent, UIManager } from 'react-native';
import type { ViewStyle } from 'react-native';
const ComponentName = 'BannerAdViewManager';

export interface BannerAdProps {
  codeid: string;
  style?: ViewStyle;
  adWidth?: number;
  adHeight?: number;
  visible?: boolean;
  onAdRenderSuccess?: Function;
  onAdError?: Function;
  onAdDismiss?: Function;
  onAdClick?: Function;
  onAdShow?: Function;
  onAdDislike?: Function;
}

const LINKING_ERROR =
  `The package 'react-native-view' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const BannerAdView = (props: BannerAdProps) => {
  const {
    codeid,
    style,
    adWidth = 320,
    adHeight = 50,
    onAdRenderSuccess,
    onAdError,
    onAdDismiss,
    onAdClick,
    onAdShow,
    onAdDislike,
    visible = true,
  } = props;

  const [dismissed, setDismissed] = useState(false);
  const [height, setHeight] = useState(adHeight);

  if (!visible || dismissed) return null;

  const BannerAdComponent =
    UIManager.getViewManagerConfig(ComponentName) != null
      ? requireNativeComponent<BannerAdProps>(ComponentName)
      : () => {
          throw new Error(LINKING_ERROR);
        };

  return (
    <BannerAdComponent
      codeid={codeid}
      adWidth={adWidth}
      adHeight={height}
      style={{ width: adWidth, height, ...style }}
      onAdError={(e: any) => {
        onAdError && onAdError(e.nativeEvent);
      }}
      onAdClick={(e: any) => {
        onAdClick && onAdClick(e.nativeEvent);
      }}
      onAdDismiss={(e: any) => {
        setDismissed(true);
        onAdDismiss && onAdDismiss(e.nativeEvent);
      }}
      onAdShow={(e: any) => {
        onAdShow && onAdShow(e.nativeEvent);
      }}
      onAdRenderSuccess={(e: any) => {
        if (e.nativeEvent.height) {
          setHeight(e.nativeEvent.height + 10);
          onAdRenderSuccess && onAdRenderSuccess(e.nativeEvent);
        }
      }}
      onAdDislike={(e: any) => {
        setDismissed(true);
        onAdDislike && onAdDislike(e.nativeEvent);
      }}
    />
  );
};

export default BannerAdView;
