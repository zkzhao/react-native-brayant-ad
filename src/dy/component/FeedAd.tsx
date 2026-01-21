/**
 * @Author: 马海
 * @createdTime: 2024-05-2024/5/20 22:00
 * @description: description
 */
import React, { useState, useCallback, useRef, useEffect, useMemo } from 'react';
import { Platform, requireNativeComponent, UIManager } from 'react-native';
import type { ViewStyle } from 'react-native';

const ComponentName = 'FeedAdViewManager';

export interface FeedAdProps {
  codeid: string;
  style?: ViewStyle;
  adWidth?: number;
  visible?: boolean;
  onAdLayout?: Function;
  onAdError?: Function;
  onAdClose?: Function;
  onAdClick?: Function;
}

const LINKING_ERROR =
  `The package 'react-native-brayant-ad' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Define native component at module level to avoid duplicate registration
const FeedAdNativeComponent = UIManager.getViewManagerConfig(ComponentName) != null
  ? requireNativeComponent<FeedAdProps>(ComponentName)
  : undefined;

const FeedAdView = (props: FeedAdProps) => {
  const {
    codeid,
    style,
    adWidth = 375,
    onAdLayout,
    onAdError,
    onAdClose,
    onAdClick,
    visible = true,
  } = props;

  const [closed, setClosed] = useState(false);
  const [height, setHeight] = useState(0);

  // Use ref to track if height has been set to prevent unnecessary re-renders
  const heightInitialized = useRef(false);

  // Reset state when visible changes from false to true to allow re-display
  useEffect(() => {
    if (visible) {
      setClosed(false);
      heightInitialized.current = false;
      setHeight(0);
    }
  }, [visible]);

  // FeedAd是否显示，外部和内部均可控制，外部visible、内部closed
  if (!visible || closed) return null;

  if (!FeedAdNativeComponent) {
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

  const handleClose = useCallback((e: any) => {
    setClosed(true);
    onAdClose?.(e.nativeEvent);
  }, [onAdClose]);

  const handleLayout = useCallback((e: any) => {
    const newHeight = e.nativeEvent.height;
    if (newHeight && !heightInitialized.current) {
      setHeight(newHeight + 10);
      heightInitialized.current = true;
    }
    onAdLayout?.(e.nativeEvent);
  }, [onAdLayout]);

  return (
    <FeedAdNativeComponent
      codeid={codeid}
      // 里面素材的宽度，减30是有些情况下，里面素材过宽贴边显示不全
      adWidth={adWidth - 30}
      // 为了不影响广告宽度占满屏幕的情况，style的width可单独控制
      style={containerStyle}
      onAdError={handleError}
      onAdClick={handleClick}
      onAdClose={handleClose}
      onAdLayout={handleLayout}
    />
  );
};

export default React.memo(FeedAdView, (prevProps, nextProps) => {
  // Custom comparison function for React.memo
  // Only re-render if visible changes or key props change
  return (
    prevProps.codeid === nextProps.codeid &&
    prevProps.visible === nextProps.visible &&
    prevProps.adWidth === nextProps.adWidth
  );
});
