import React, { useState, useCallback, useRef, useEffect, useMemo } from 'react';
import { Platform, requireNativeComponent, UIManager } from 'react-native';
import type { ViewStyle } from 'react-native';

// BannerAd currently only supports Android platform
const ComponentName = Platform.select({
  android: 'BrayantBannerAdViewManager',
  ios: undefined,
}) as string | undefined;

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
  `The package 'react-native-brayant-ad' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n' +
  '\nNote: BannerAd is currently only supported on Android platform.';

// Define native component at module level to avoid duplicate registration
const BannerAdNativeComponent = ComponentName && UIManager.getViewManagerConfig(ComponentName) != null
  ? requireNativeComponent<BannerAdProps>(ComponentName)
  : undefined;

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

  // Use ref to track if height has been set to prevent unnecessary re-renders
  const heightInitialized = useRef(false);

  // Reset state when visible changes from false to true to allow re-display
  useEffect(() => {
    if (visible) {
      setDismissed(false);
      heightInitialized.current = false;
      setHeight(adHeight);
    }
  }, [visible, adHeight]);

  // BannerAd is only supported on Android
  if (Platform.OS !== 'android') {
    return null;
  }

  if (!visible || dismissed) return null;

  if (!BannerAdNativeComponent) {
    throw new Error(LINKING_ERROR);
  }

  // Use useMemo to cache style object and prevent unnecessary re-renders
  const containerStyle = useMemo(() => ({
    width: adWidth,
    height,
    ...style,
  }), [adWidth, height, style]);

  // Stable callbacks using useCallback to prevent re-renders
  const handleError = useCallback((e: any) => {
    onAdError?.(e.nativeEvent);
  }, [onAdError]);

  const handleClick = useCallback((e: any) => {
    onAdClick?.(e.nativeEvent);
  }, [onAdClick]);

  const handleDismiss = useCallback((e: any) => {
    setDismissed(true);
    onAdDismiss?.(e.nativeEvent);
  }, [onAdDismiss]);

  const handleShow = useCallback((e: any) => {
    onAdShow?.(e.nativeEvent);
  }, [onAdShow]);

  const handleRenderSuccess = useCallback((e: any) => {
    const newHeight = e.nativeEvent.height;
    if (newHeight && !heightInitialized.current) {
      setHeight(newHeight + 10);
      heightInitialized.current = true;
    }
    onAdRenderSuccess?.(e.nativeEvent);
  }, [onAdRenderSuccess]);

  const handleDislike = useCallback((e: any) => {
    setDismissed(true);
    onAdDislike?.(e.nativeEvent);
  }, [onAdDislike]);

  return (
    <BannerAdNativeComponent
      codeid={codeid}
      adWidth={adWidth}
      adHeight={height}
      style={containerStyle}
      onAdError={handleError}
      onAdClick={handleClick}
      onAdDismiss={handleDismiss}
      onAdShow={handleShow}
      onAdRenderSuccess={handleRenderSuccess}
      onAdDislike={handleDislike}
    />
  );
};

export default React.memo(BannerAdView, (prevProps, nextProps) => {
  // Custom comparison function for React.memo
  // Only re-render if visible changes or key props change
  return (
    prevProps.codeid === nextProps.codeid &&
    prevProps.visible === nextProps.visible &&
    prevProps.adWidth === nextProps.adWidth &&
    prevProps.adHeight === nextProps.adHeight
  );
});

