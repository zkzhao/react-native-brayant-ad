import { NativeModules, NativeEventEmitter } from 'react-native';
import type { EventSubscription } from 'react-native';
const { RewardVideoModule } = NativeModules;
export enum AD_EVENT_TYPE {
  onAdError = 'onAdError', // 广告加载失败监听
  onAdLoaded = 'onAdLoaded', // 广告加载成功监听
  onAdClick = 'onAdClick', // 广告被点击监听
  onAdClose = 'onAdClose', // 广告关闭监听
}

type ListenerCache = {
  [K in AD_EVENT_TYPE]: EventSubscription | undefined;
};

type rewardInfo = {
  codeid: string;
};

export default function (info: rewardInfo) {
  const eventEmitter = new NativeEventEmitter(RewardVideoModule);
  // Per-instance listener cache to avoid conflicts with multiple ads
  const listenerCache: ListenerCache = {} as ListenerCache;
  let result = RewardVideoModule.startAd(info);
  return {
    result,
    subscribe: (type: AD_EVENT_TYPE, callback: (event: any) => void) => {
      // Remove previous listener for this type in this instance only
      if (listenerCache[type]) {
        listenerCache[type]?.remove();
      }
      return (listenerCache[type] = eventEmitter.addListener(
        'RewardVideo-' + type,
        (event: any) => {
          callback(event);
        }
      ));
    },
    // Provide cleanup method
    cleanup: () => {
      Object.values(listenerCache).forEach((subscription) => {
        subscription?.remove();
      });
      Object.keys(listenerCache).forEach((key) => {
        delete listenerCache[key as AD_EVENT_TYPE];
      });
    },
  };
}
