/**
 * @Author: 马海
 * @createdTime: 2024-05-2024/5/18 13:39
 * @description: description
 */
import React, { useCallback, useMemo } from 'react';
import {
  type ViewStyle,
  UIManager,
  StyleSheet,
  NativeModules,
  Platform,
  requireNativeComponent,
} from 'react-native';
const { DrawFeedAdModule } = NativeModules;

const LINKING_ERROR =
  `The package 'react-native-brayant-ad' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ComponentName = 'DrawFeedAdViewManager';

type DrawFeedAdProps = {
  codeid: string;
  appid: string;
  visible?: boolean;
  style?: ViewStyle;
  onAdError?: Function;
  onAdShow?: Function;
  onAdClick?: Function;
};

type ViewProps = Omit<DrawFeedAdProps, 'appid'>;

export const loadDrawFeedAd = (info: { appid: string; codeid: string }) => {
  DrawFeedAdModule.loadDrawFeedAd(info);
};

// Define native component at module level to avoid duplicate registration
const DrawFeedAdNativeComponent = UIManager.getViewManagerConfig(ComponentName) != null
  ? requireNativeComponent<ViewProps>(ComponentName)
  : undefined;

export const DrawFeedView = (props: DrawFeedAdProps) => {
  const {
    codeid,
    onAdError,
    onAdShow,
    onAdClick,
    visible = true,
    style,
  } = props;

  const styleObj = useMemo(() => style || styles.container, [style]);

  if (!visible) return null;

  if (!DrawFeedAdNativeComponent) {
    throw new Error(LINKING_ERROR);
  }

  // Stable callbacks using useCallback to prevent re-renders
  const handleError = useCallback((e: any) => {
    console.log('onAdError DrawFeed', e.nativeEvent);
    onAdError?.(e.nativeEvent);
  }, [onAdError]);

  const handleClick = useCallback((e: any) => {
    console.log('onAdClick DrawFeed', e.nativeEvent);
    onAdClick?.(e.nativeEvent);
  }, [onAdClick]);

  const handleShow = useCallback((e: any) => {
    console.log('onAdShow DrawFeed', e.nativeEvent);
    onAdShow?.(e.nativeEvent);
  }, [onAdShow]);

  return (
    <DrawFeedAdNativeComponent
      codeid={codeid}
      onAdError={handleError}
      onAdClick={handleClick}
      onAdShow={handleShow}
      style={styleObj}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    width: '100%',
  },
});

export default React.memo(DrawFeedView, (prevProps, nextProps) => {
  // Custom comparison function for React.memo
  // Only re-render if visible changes or key props change
  return (
    prevProps.codeid === nextProps.codeid &&
    prevProps.visible === nextProps.visible
  );
});
