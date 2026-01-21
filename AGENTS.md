# 语言规范

- 所有对话和文档都使用中文

# Agent Guidelines for react-native-brayant-ad

This file provides guidelines for AI agents working on this React Native ad SDK library.

## Build & Test Commands

```bash
# Type checking
pnpm typecheck          # Run TypeScript compiler without emit
tsc --noEmit

# Linting
pnpm lint              # Run ESLint on all JS/TS/TSX files
eslint "**/*.{js,ts,tsx}"

# Testing
pnpm test               # Run all Jest tests
jest                    # Alternative command
jest src/__tests__/index.test.tsx  # Run single test file

# Build
pnpm prepare            # Build library with react-native-builder-bob
pnpm clean              # Remove build artifacts

# Development
pnpm example            # Run workspace example app
```

## Code Style Guidelines

### Formatting (Prettier)

- **Single quotes**: `'string'` not `"string"`
- **Tab width**: 2 spaces (no tabs)
- **Trailing comma**: ES5 compatible
- **Quote props**: consistent
- Config in package.json under `prettier` and `eslintConfig.rules.prettier/prettier`

### TypeScript Configuration

- **Strict mode enabled**: All type checking rules active
- **verbatimModuleSyntax**: Use explicit `import type` for type-only imports
- **noUncheckedIndexedAccess**: Objects may be undefined on index access
- **noUnusedLocals/Parameters**: All locals/params must be used
- Module system: `esnext`, target: `esnext`, jsx: `react`

### Import Patterns

```typescript
// Relative imports with explicit paths
import { init } from './dy/api/AdManager';

// Type-only imports (verbatimeModuleSyntax requires this)
import type { ViewStyle } from 'react-native';

// Destructured native module imports
const { AdManager } = NativeModules;

// Named exports preferred for utilities
export { init, loadFeedAd, requestPermission };

// Default exports for main module components
export default FeedAdView;
```

### Type Definitions

```typescript
// Interfaces for exported props (public API)
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

// Types for internal use
type FeedInfo = {
  appid: string;
  codeid: string;
  adWidth?: string;
};

// Enums for constants
export enum AD_EVENT_TYPE {
  onAdError = 'onAdError',
  onAdLoaded = 'onAdLoaded',
  onAdClick = 'onAdClick',
  onAdClose = 'onAdClose',
}

// Type aliases with utility types
type ViewProps = Omit<DrawFeedAdProps, 'appid'>;
type ListenerCache = {
  [K in AD_EVENT_TYPE]: EventSubscription | undefined;
};
```

### Naming Conventions

- **Components**: PascalCase (`FeedAdView`, `DrawFeedAd`)
- **Functions/Variables**: camelCase (`dyLoadSplashAd`, `loadFeedAd`)
- **Constants**: camelCase (`listenerCache`) or UPPER_SNAKE_CASE (rare)
- **Types/Interfaces**: PascalCase (`FeedAdProps`, `AD_EVENT_TYPE`)
- **File names**: PascalCase for components, camelCase for utilities

### Error Handling

```typescript
// LINKING_ERROR pattern for unlinked native modules
const LINKING_ERROR =
  `The package 'react-native-view' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Check for linked module before usage
const Component =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<FeedAdProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

// Console logging for event debugging
console.log('SplashAd event type ', type);
console.log('SplashAd event ', event);

// Conditional function calls
onAdError && onAdError(e.nativeEvent);
```

### React Component Patterns

```typescript
// Functional components only
const FeedAdView = (props: FeedAdProps) => {
  const { codeid, style, adWidth = 375, visible = true, ... } = props;

  // Hooks usage
  const [closed, setClosed] = useState(false);
  const [height, setHeight] = useState(0);

  // Early returns
  if (!visible || closed) return null;

  return (
    <FeedAdComponent
      codeid={codeid}
      style={{ width: adWidth, height, ...style }}
      onAdError={(e: any) => { onAdError && onAdError(e.nativeEvent); }}
    />
  );
};

// StyleSheet.create for component styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    width: '100%',
  },
});
```

### Native Module Integration Patterns

```typescript
// Access native modules
const { AdManager } = NativeModules;
const { SplashAd } = NativeModules;

// Event emitter pattern for native events
const eventEmitter = new NativeEventEmitter(SplashAd);
let result = SplashAd.loadSplashAd({ codeid, anim });

return {
  result, // Promise for operation completion
  subscribe: (type: AD_EVENT_TYPE, callback: (event: any) => void) => {
    if (listenerCache[type]) {
      listenerCache[type]?.remove();
    }
    return (listenerCache[type] = eventEmitter.addListener(
      'SplashAd-' + type,
      (event: any) => {
        callback(event);
      }
    ));
  },
};

// Native view components
const ComponentName = 'FeedAdViewManager';
const FeedAdComponent = requireNativeComponent<FeedAdProps>(ComponentName);

// Platform-specific logic
if (Platform.OS === 'android') {
  return AdManager.loadDrawFeedAd(info);
}
```

### File Organization

```
src/
├── index.tsx                    # Main entry point, all public exports
├── dy/
│   ├── api/                     # API functions for ad types
│   │   ├── AdManager.ts        # Core ad manager (init, permission)
│   │   ├── SplashAd.ts         # Splash ads
│   │   ├── RewardVideo.ts      # Reward video ads
│   │   └── FullScreenVideo.ts  # Full-screen video ads
│   └── component/              # React native components
│       ├── FeedAd.tsx          # Feed ad view component
│       └── DrawFeedAd.tsx      # Draw feed ad view component
└── __tests__/                   # Test files
    └── index.test.tsx
```

## Testing

- **Test location**: `src/__tests__/` directory
- **File naming**: `*.test.tsx` or `*.test.ts`
- **Jest config**: preset: `react-native`, modulePathIgnorePatterns excludes example/node_modules and lib/
- **Module path ignore patterns**: `<rootDir>/example/node_modules`, `<rootDir>/lib/`
- Run single test: `jest path/to/test.file.tsx`

## Development Workflow

### Quick Verification After Code Changes

```bash
# 1. Rebuild the library (required after any changes to src/)
pnpm prepare

# 2. Start Metro bundler (in a new terminal)
cd example && pnpm start
# or from root directory:
pnpm example start

# 3. Run the app in another terminal
# Android:
cd example && pnpm android

# iOS:
cd example && pnpm ios
```

### First-Time Setup

```bash
# Android: Ensure emulator or device is connected
adb devices

# iOS: Install pods (first time or after iOS code changes)
cd ios && pod install
```

### Common Workflow

```bash
# Modify code → Rebuild → Reload app
pnpm prepare
# Press R (iOS) or RR (Android) in simulator/emulator to reload
```

### Debugging

```bash
# View logs
# Android:
adb logcat | grep BrayantAd

# iOS:
# View logs in Xcode console
```

### File Locations

- **Example code**: `example/src/App.tsx`, `example/src/DrawFeedViewDemo.tsx`
- **Library code**: `src/` directory
- **Important**: Always run `pnpm prepare` after modifying library code

## Important Notes

1. **Never suppress type errors** with `as any`, `@ts-ignore`, or `@ts-expect-error`
2. **Always check for linked native modules** before usage with the LINKING_ERROR pattern
3. **Native events are prefixed** with module name (e.g., 'SplashAd-onAdError')
4. **Listener cache management** prevents duplicate listeners for same event type
5. **Platform checks** required for platform-specific features (DrawFeedAd: Android only)
6. **Default values** for optional props (e.g., `adWidth = 375`, `visible = true`)
7. **Event callbacks receive native events** via `e.nativeEvent` wrapper
8. **StyleSheet.create** for component styles, inline styles for dynamic values
