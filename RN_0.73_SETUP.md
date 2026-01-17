# React Native 0.73 使用说明

## 环境要求

| 依赖                        | 版本要求 |
| --------------------------- | -------- |
| React Native                | 0.73.x   |
| Android Gradle Plugin (AGP) | 8.1.x    |
| Gradle                      | 8.4      |
| Java                        | 17       |
| Android SDK                 | 34       |
| minSdkVersion               | 24       |

## 在RN 0.73项目中使用

### 1. 安装依赖

```bash
cd your-react-native-project
npm install react-native-brayant-ad --save
```

### 2. 配置 android/build.gradle

确保你的 `android/build.gradle` 包含以下仓库配置：

```groovy
buildscript {
  ext {
    buildToolsVersion = "34.0.0"
    minSdkVersion = 24
    compileSdkVersion = 34
    targetSdkVersion = 34
    ndkVersion = "26.1.10909125"
  }
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.1.4")
    classpath("com.facebook.react:react-native-gradle-plugin:0.73.6")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
    // 穿山甲SDK仓库
    maven {
      url 'https://artifact.bytedance.com/repository/pangle'
    }
  }
}
```

### 3. 配置 android/gradle.properties

```properties
# 支持New Architecture（新架构）
newArchEnabled=false

# AndroidX配置
android.useAndroidX=true
android.enableJetifier=true

# JVM内存配置
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

### 4. 配置 gradle/wrapper/gradle-wrapper.properties

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### 5. 配置 react-native.config.js

在项目根目录创建 `react-native.config.js`：

```javascript
module.exports = {
  dependencies: {
    'react-native-brayant-ad': {
      platforms: {
        android: {
          sourceDir: '../node_modules/react-native-brayant-ad/android',
          packageImportPath: "import BrayantAd from 'react-native-brayant-ad';",
          packageInstance: 'new com.brayantad.BrayantAdPackage()',
        },
      },
    },
  },
};
```

### 6. 配置 AndroidManifest.xml

确保 `android/app/src/main/AndroidManifest.xml` 包含必要的权限和Provider配置：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <!--必要权限-->
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

  <!--可选权限-->
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
  <uses-permission android:name="android.permission.WAKE_LOCK" />

  <application>
    <!--TTFileProvider 用于下载类广告-->
    <provider
      android:name="com.bytedance.sdk.openadsdk.TTFileProvider"
      android:authorities="${applicationId}.TTFileProvider"
      android:exported="false"
      android:grantUriPermissions="true">
      <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
    </provider>

    <!--TTMultiProvider SDK 7.3.0+必需-->
    <provider
      android:name="com.bytedance.sdk.openadsdk.multipro.TTMultiProvider"
      android:authorities="${applicationId}.TTMultiProvider"
      android:exported="false" />
  </application>
</manifest>
```

### 7. 添加 file_paths.xml

在 `android/app/src/main/res/xml/` 目录下创建 `file_paths.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="tt_external_root" path="." />
    <external-path name="tt_external_download" path="Download" />
    <external-files-path name="tt_external_files_download" path="Download" />
    <files-path name="tt_internal_file_download" path="Download" />
    <cache-path name="tt_internal_cache_download" path="Download" />
</paths>
```

### 8. 使用示例

```tsx
import React, { useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { init, BannerAdView } from 'react-native-brayant-ad';

function App() {
  useEffect(() => {
    // 初始化穿山甲SDK
    init({
      appid: '你的穿山甲AppID',
      app: '应用名称',
    });
  }, []);

  return (
    <View style={styles.container}>
      {/* Banner广告 */}
      <BannerAdView
        codeid={'你的Banner广告位ID'}
        adWidth={320}
        adHeight={50}
        visible={true}
        onAdRenderSuccess={(data) => {
          console.log('Banner渲染成功', data);
        }}
        onAdError={(err) => {
          console.log('Banner加载失败', err);
        }}
        onAdClick={(val) => {
          console.log('Banner被点击', val);
        }}
        onAdDismiss={(val) => {
          console.log('Banner关闭', val);
        }}
        onAdShow={(val) => {
          console.log('Banner展示', val);
        }}
        onAdDislike={(val) => {
          console.log('用户不感兴趣', val);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
```

## API参考

### BannerAdView 组件

| 属性                | 类型      | 必填 | 默认值 | 说明         |
| ------------------- | --------- | ---- | ------ | ------------ |
| `codeid`            | string    | 是   | -      | 广告位ID     |
| `adWidth`           | number    | 否   | 320    | 宽度(dp)     |
| `adHeight`          | number    | 否   | 50     | 高度(dp)     |
| `visible`           | boolean   | 否   | true   | 是否显示     |
| `style`             | ViewStyle | 否   | -      | 样式         |
| `onAdRenderSuccess` | Function  | 否   | -      | 渲染成功回调 |
| `onAdError`         | Function  | 否   | -      | 加载失败回调 |
| `onAdClick`         | Function  | 否   | -      | 点击回调     |
| `onAdDismiss`       | Function  | 否   | -      | 关闭回调     |
| `onAdShow`          | Function  | 否   | -      | 展示回调     |
| `onAdDislike`       | Function  | 否   | -      | 不感兴趣回调 |

### 其他API

```tsx
import {
  init, // 初始化SDK
  loadFeedAd, // 预加载信息流广告
  requestPermission, // 申请权限
  FeedAdView, // 信息流广告组件
  DrawFeedView, // Draw广告组件
  BannerAdView, // Banner广告组件
  dyLoadSplashAd, // 开屏广告
  startRewardVideo, // 激励视频
  startFullScreenVideo, // 全屏视频
} from 'react-native-brayant-ad';
```

## 常见问题

### 1. minSdkVersion错误

如果遇到 `minSdkVersion cannot be smaller than 24` 错误：

**解决方案**：在 `android/build.gradle` 中设置：

```groovy
ext {
  minSdkVersion = 24  // RN 0.73 默认就是24
}
```

### 2. Provider配置缺失

确保 `TTMultiProvider` 已配置，否则会影响广告收益。

### 3. okhttp依赖缺失

SDK需要okhttp依赖，如果遇到相关错误，确保 `android/build.gradle` 包含：

```groovy
dependencies {
  implementation 'com.squareup.okhttp3:okhttp:3.12.1'
}
```

### 4. 编译时内存不足

增加Gradle内存：

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

## 验证安装

运行以下命令验证库是否正确安装：

```bash
# 同步依赖
cd android && ./gradlew :dependencies --configuration implementation

# 查找 react-native-brayant-ad
# 应该能看到 com.pangle.cn:ads-sdk-pro
```

## 技术支持

如有问题，请检查：

1. 穿山甲广告位是否已创建并审核通过
2. AppID是否正确
3. 网络权限是否已配置
4. 是否在主线程调用init
