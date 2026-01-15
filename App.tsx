import React, {useCallback, useMemo, useRef, useState} from 'react';
import {
  BackHandler,
  Image,
  SafeAreaView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import WebView, {WebViewNavigation} from 'react-native-webview';

// Suppress RN warning banner in Debug builds (safe across RN versions)
if (__DEV__) {
  // Old RN versions
  // @ts-ignore
  console.disableYellowBox = true;

  // Newer RN versions (if available)
  try {
    // Dynamic require so it won't crash if LogBox isn't exported
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const RN = require('react-native');
    if (RN?.LogBox?.ignoreAllLogs) {
      RN.LogBox.ignoreAllLogs(true);
    } else if (RN?.YellowBox?.ignoreWarnings) {
      RN.YellowBox.ignoreWarnings(['']);
    }
  } catch (e) {
    // ignore
  }
}

type RootStackParamList = {
  Home: undefined;
  Recharge: {initialUrl: string};
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const TAG = 'SpecmaticRecharge';
const log = (msg: string, extra?: any) => {
  if (extra !== undefined) console.log(`[${TAG}] ${msg}`, extra);
  else console.log(`[${TAG}] ${msg}`);
};

const DEFAULT_URL = 'http://localhost:8080';

/**
 * Central place for all testIDs.
 */
const UI = {
  // App level
  appNavContainer: 'app.nav.container',

  // Home screen
  homeScreen: 'home.screen',
  homeHero: 'home.hero',
  homeLogo: 'home.logo.jio',

  homeCard: 'home.card',
  homeRechargeBtn: 'home.btn.recharge',
  homeRechargeBtnText: 'home.btn.recharge.text',

  // Recharge screen
  rechargeScreen: 'recharge.screen',
  rechargeWebView: 'recharge.webview',
};

function HomeScreen({navigation}: any) {
  const onPressRecharge = useCallback(() => {
    log('Home -> Recharge pressed', {url: DEFAULT_URL});
    navigation.navigate('Recharge', {initialUrl: DEFAULT_URL});
  }, [navigation]);

  return (
    <SafeAreaView
      style={styles.container}
      testID={UI.homeScreen}
      accessibilityLabel={UI.homeScreen}>
      {/* ✅ Logo hero */}
      <View
        style={styles.hero}
        testID={UI.homeHero}
        accessibilityLabel={UI.homeHero}>
        <Image
          source={require('./assets/images/jio-logo.jpg')}
          style={styles.logo}
          resizeMode="contain"
          testID={UI.homeLogo}
          accessibilityLabel={UI.homeLogo}
        />
      </View>

      <View
        style={styles.card}
        testID={UI.homeCard}
        accessibilityLabel={UI.homeCard}>
        {/* ✅ URL Label + Text removed */}

        <TouchableOpacity
          style={styles.primaryBtn}
          onPress={onPressRecharge}
          testID={UI.homeRechargeBtn}
          accessibilityLabel={UI.homeRechargeBtn}
          accessibilityRole="button"
          accessibilityHint="Opens WebView for recharge flow">
          <Text
            style={styles.primaryBtnText}
            testID={UI.homeRechargeBtnText}
            accessibilityLabel={UI.homeRechargeBtnText}>
            Recharge phone number
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

function RechargeScreen({navigation, route}: any) {
  const initialUrl = route?.params?.initialUrl ?? DEFAULT_URL;
  const webRef = useRef<WebView>(null);
  const [canGoBack, setCanGoBack] = useState(false);

  const injectedJS = useMemo(
    () => `
      (function() {
        window.ReactNativeWebView && window.ReactNativeWebView.postMessage("WebView injected JS loaded");
        true;
      })();
    `,
    [],
  );

  const onNavStateChange = useCallback((navState: WebViewNavigation) => {
    setCanGoBack(navState.canGoBack);
    log('WebView nav state', {
      url: navState.url,
      canGoBack: navState.canGoBack,
      loading: navState.loading,
      title: navState.title,
    });
  }, []);

  React.useEffect(() => {
    const sub = BackHandler.addEventListener('hardwareBackPress', () => {
      if (canGoBack) {
        log('Hardware back -> WebView.goBack()');
        webRef.current?.goBack();
        return true;
      }
      log('Hardware back -> popToTop()');
      navigation.popToTop();
      return true;
    });
    return () => sub.remove();
  }, [canGoBack, navigation]);

  return (
    <SafeAreaView
      style={styles.webContainer}
      testID={UI.rechargeScreen}
      accessibilityLabel={UI.rechargeScreen}>
      {/* ✅ Buttons removed: Back to Home + Web Back */}

      <WebView
        ref={webRef}
        source={{uri: initialUrl}}
        injectedJavaScript={injectedJS}
        javaScriptEnabled
        domStorageEnabled
        onLoadStart={() => log('WebView load start')}
        onLoadEnd={() => log('WebView load end')}
        onError={(e) => log('WebView error', e.nativeEvent)}
        onHttpError={(e) => log('WebView http error', e.nativeEvent)}
        onNavigationStateChange={onNavStateChange}
        onMessage={(event) => log('WebView message', event.nativeEvent.data)}
        startInLoadingState
        testID={UI.rechargeWebView}
        accessibilityLabel={UI.rechargeWebView}
      />
    </SafeAreaView>
  );
}

export default function App() {
  log('App started');
  return (
    <NavigationContainer
      testID={UI.appNavContainer}
      accessibilityLabel={UI.appNavContainer}>
      <Stack.Navigator>
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{headerShown: false}}
        />
        <Stack.Screen
          name="Recharge"
          component={RechargeScreen}
          options={{title: 'Recharge'}}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1, padding: 16},

  hero: {
    flex: 7,                 // ✅ takes ~70% of screen
    padding: 16,
    borderRadius: 16,
    backgroundColor: '#fff',
    marginBottom: 16,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  logo: {
    width: '100%',
    height: '100%',
    borderRadius: 16,
  },

  card: {
    padding: 16,
    borderRadius: 16,
    backgroundColor: '#f4f4f4',
  },

  primaryBtn: {
    padding: 14,
    borderRadius: 12,
    backgroundColor: '#1f6feb',
    alignItems: 'center',
  },
  primaryBtnText: {color: '#fff', fontSize: 16, fontWeight: '700'},

  webContainer: {flex: 1, backgroundColor: '#fff'},
});
