/**
 * React Native 0.73 兼容配置
 *
 * 这个配置文件用于在React Native 0.73项目中使用本库
 * 将此文件复制到你的项目根目录
 */

module.exports = {
  project: {
    android: {
      sourceDir: 'android',
      // 关键：使用相对路径指向node_modules中的库
      modulePath: 'node_modules/react-native-brayant-ad/android',
    },
  },
  dependencies: {
    'react-native-brayant-ad': {
      platforms: {
        android: {
          // 指向node_modules中的android目录
          sourceDir: 'node_modules/react-native-brayant-ad/android',
          // 导入语句
          packageImportPath: "import BrayantAd from 'react-native-brayant-ad';",
          // 包实例化
          packageInstance: 'new com.brayantad.BrayantAdPackage()',
          // 构建类型
          buildTypes: ['debug', 'release'],
        },
      },
    },
  },
};
