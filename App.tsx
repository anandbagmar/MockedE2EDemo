import React, { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  Modal,
  Platform,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  Pressable,
  TouchableOpacity,
  View,
  Keyboard,
  type ViewProps,
} from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import WebView from 'react-native-webview';
import { requireNativeComponent } from 'react-native';

if (__DEV__) {
  // @ts-ignore
  console.disableYellowBox = true;

  try {
    const RN = require('react-native');
    if (RN?.LogBox?.ignoreAllLogs) {
      RN.LogBox.ignoreAllLogs(true);
    } else if (RN?.YellowBox?.ignoreWarnings) {
      RN.YellowBox.ignoreWarnings(['']);
    }
  } catch {
    // ignore
  }
}

type PlannerGuest = {
  login: { uuid: string };
  name: { title: string; first: string; last: string };
  email: string;
  phone: string;
  cell: string;
  picture: { large: string; medium: string; thumbnail: string };
  location: { city: string; country: string };
};

type FlowVariant = 'original' | 'alternate';

type FlowRouteParams = {
  variant: FlowVariant;
};

type RootStackParamList = {
  Home: undefined;
  Recharge: undefined;
  Planner: FlowRouteParams;
  NativeJourney: FlowRouteParams;
  NativeHybrid: FlowRouteParams;
  GuestLookup: FlowRouteParams;
  WebChecklist: FlowRouteParams;
  Summary: FlowRouteParams;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const UI = {
  homeScreen: 'home.screen',
  homeScroll: 'home.scroll',
  homeHero: 'home.hero',
  homeHeroImage: 'home.hero.image',
  homeHeroEyebrow: 'home.hero.eyebrow',
  homeHeroTitle: 'home.hero.title',
  homeHeroBody: 'home.hero.body',
  homeWorkflowPanel: 'home.panel.workflows',
  homeVersion: 'home.version',
  homeRechargeButton: 'home.button.recharge',
  homePlannerButton: 'home.button.planner',
  plannerModeModal: 'planner.mode.modal',
  plannerModePanel: 'planner.mode.panel',
  plannerModeOriginalButton: 'planner.mode.button.original',
  plannerModeAlternateButton: 'planner.mode.button.alternate',
  plannerModeCancelButton: 'planner.mode.button.cancel',
  plannerScreen: 'planner.screen',
  plannerScroll: 'planner.scroll',
  plannerTipBanner: 'planner.tipBanner',
  plannerTipTitle: 'planner.tipBanner.title',
  plannerTipText: 'planner.tipBanner.text',
  plannerNextNativeButton: 'planner.button.next.native',
  rechargeScreen: 'recharge.screen',
  rechargeFrame: 'recharge.frame',
  rechargeWebView: 'recharge.webview',
  guestLookupScreen: 'guestLookup.screen',
  guestLookupScroll: 'guestLookup.scroll',
  guestLookupPanel: 'guestLookup.panel',
  guestLookupInputLabel: 'guestLookup.label.count',
  guestLookupInput: 'guestLookup.input.count',
  guestLookupHelper: 'guestLookup.helper.count',
  guestLookupFetchButton: 'guestLookup.button.fetch',
  guestLookupLoader: 'guestLookup.loader',
  guestLookupLoaderText: 'guestLookup.loader.text',
  guestLookupResults: 'guestLookup.results',
  guestLookupCards: 'guestLookup.cards',
  guestLookupCardsList: 'guestLookup.cards.list',
  guestLookupResultsNextButton: 'guestLookup.button.next',
  nativeJourneyScreen: 'nativeJourney.screen',
  nativeJourneyScroll: 'nativeJourney.scroll',
  nativeJourneyPanel: 'nativeJourney.panel',
  nativeJourneyView: 'nativeJourney.nativeView',
  nativeJourneyContinue: 'nativeJourney.button.continue',
  nativeJourneyModeNote: 'nativeJourney.mode.native',
  nativeHybridScreen: 'nativeHybrid.screen',
  nativeHybridView: 'nativeHybrid.nativeView',
  nativeHybridContinue: 'nativeHybrid.button.continue',
  nativeHybridModeNote: 'nativeHybrid.mode.hybrid',
  webChecklistScreen: 'webChecklist.screen',
  webChecklistTop: 'webChecklist.top',
  webChecklistLoading: 'webChecklist.loading',
  webChecklistReady: 'webChecklist.ready',
  webChecklistFrame: 'webChecklist.frame',
  webChecklistWebView: 'webChecklist.webview',
  webChecklistContinue: 'webChecklist.button.continue',
  summaryScreen: 'summary.screen',
  summaryScroll: 'summary.scroll',
  summaryCard: 'summary.card',
  summaryEyebrow: 'summary.eyebrow',
  summaryTitle: 'summary.title',
  summaryBody: 'summary.body',
  summaryRestartButton: 'summary.button.restart',
};

const getHighlightId = (index: number) => `home.highlight.${index + 1}`;
const getSectionTitleId = (scope: string, part: 'eyebrow' | 'title' | 'body') =>
  `${scope}.${part}`;
const getAgendaId = (
  index: number,
  part: 'card' | 'index' | 'title' | 'body',
) => `planner.agenda.${index + 1}.${part}`;
const getProfileId = (
  index: number,
  part: 'card' | 'avatar' | 'name' | 'email' | 'location' | 'phone',
) => `guestLookup.profile.${index + 1}.${part}`;
const getRecapId = (index: number) => `summary.recap.${index + 1}`;
const nativePlatformName = Platform.select({ android: 'AndroidX', ios: 'Swift' }) ?? 'Native';
const nativePlatformSuffix = Platform.select({ android: 'androidx', ios: 'swift' }) ?? 'native';
const reactNativeModeLabel = 'React Native View';
const nativeViewModeLabel = `${nativePlatformName} Native View`;
const hybridViewModeLabel = `${nativePlatformName} Hybrid Native Views`;
const APP_VERSION = 'Version 1.1';

const FEATURE_HIGHLIGHTS = [
  'Create a small event agenda in native screens',
  'Review vertically scrollable activity cards',
  'Fetch attendee profiles from a real API',
  'Validate input and show dismissible error dialogs',
  'Complete one lightweight interaction inside a webview',
];

const AGENDA_STEPS = [
  {
    title: 'Welcome Wall',
    subtitle: 'Guests arrive, scan the day plan, and pick their pace.',
  },
  {
    title: 'Snack Voting',
    subtitle: 'The team chooses between coffee, juices, and street food.',
  },
  {
    title: 'Icebreaker Sprint',
    subtitle: 'Each group shares one app idea they would build in a weekend.',
  },
  {
    title: 'Mini Demos',
    subtitle: 'Product, QA, and automation folks each show a tiny win.',
  },
  {
    title: 'Wrap-up Notes',
    subtitle: 'Attendees leave with contacts and follow-up actions.',
  },
  {
    title: 'Photo Wall',
    subtitle:
      'A slow stroll past a few snapshots from the day before everyone moves on.',
  },
  {
    title: 'Team Kudos',
    subtitle:
      'A calm moment for a couple of shout-outs and small celebrations.',
  },
  {
    title: 'Exit Snacks',
    subtitle:
      'A final friendly stop for refreshments, quick goodbyes, and the last scroll of the page.',
  },
];

const ALTERNATE_AGENDA_STEPS = [
  {
    title: 'Welcome Wall',
    subtitle:
      'Guests arrive, scan the updated day plan, and settle into a relaxed pace.',
  },
  {
    title: 'Snack Voting',
    subtitle: 'The team picks between coffee, juices, and neighborhood snacks.',
  },
  {
    title: 'Icebreaker Sprint',
    subtitle:
      'Each group shares one app idea they would happily prototype over a weekend.',
  },
  {
    title: 'Mini Demos',
    subtitle: 'Product, QA, and automation folks each highlight one tiny win.',
  },
  {
    title: 'Wrap-up Notes',
    subtitle:
      'Attendees leave with fresh contacts and a few follow-up actions.',
  },
  {
    title: 'Photo Wall',
    subtitle:
      'A longer, slower pass where guests can browse snapshots from the day and tap into the memories.',
  },
  {
    title: 'Team Kudos',
    subtitle:
      'A final stretch for shout-outs, quick thank-yous, and a couple of small wins from the event.',
  },
  {
    title: 'Exit Snacks',
    subtitle:
      'One last scroll-friendly stop with refreshments and a quiet finish before everyone heads out.',
  },
];

const NativeJourneyView = requireNativeComponent<ViewProps>('NativeJourneyView');
const NativeHybridView = requireNativeComponent<ViewProps>('NativeHybridView');

const CHECKLIST_HTML = `
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        margin: 0;
        font-family: -apple-system, BlinkMacSystemFont, sans-serif;
        background: linear-gradient(180deg, #fff8e7 0%, #f4f9ff 100%);
        color: #17324d;
      }
      .wrap {
        padding: 24px 18px 40px;
      }
      .card {
        background: #ffffff;
        border-radius: 18px;
        padding: 18px;
        box-shadow: 0 10px 30px rgba(23, 50, 77, 0.08);
      }
      h1 {
        margin: 0 0 10px;
        font-size: 28px;
      }
      p {
        margin: 0 0 16px;
        line-height: 1.45;
      }
      button {
        width: 100%;
        border: 0;
        border-radius: 14px;
        padding: 14px 16px;
        font-size: 16px;
        font-weight: 700;
        color: #ffffff;
        background: #ef6c3d;
      }
      .done {
        margin-top: 16px;
        display: none;
        padding: 14px;
        border-radius: 14px;
        background: #e7f7ee;
        color: #1b6b42;
        font-weight: 600;
      }
    </style>
  </head>
  <body>
    <div class="wrap">
      <div class="card">
        <h1>Quick Web Check-in</h1>
        <p>Tap the button below to confirm that the venue checklist was reviewed.</p>
        <button id="confirmButton">Mark checklist as ready</button>
        <div id="doneMessage" class="done">Checklist confirmed. You can return to the app flow.</div>
      </div>
    </div>
    <script>
      const button = document.getElementById('confirmButton');
      const doneMessage = document.getElementById('doneMessage');
      button.addEventListener('click', function () {
        doneMessage.style.display = 'block';
        button.textContent = 'Ready';
        button.disabled = true;
        button.style.background = '#1b6b42';
        window.ReactNativeWebView && window.ReactNativeWebView.postMessage('checklist-complete');
      });
    </script>
  </body>
</html>
`;

function PrimaryButton({
  label,
  onPress,
  disabled,
  testID,
}: {
  label: string;
  onPress: () => void;
  disabled?: boolean;
  testID?: string;
}) {
  return (
    <TouchableOpacity
      accessible
      accessibilityLabel={testID ?? label}
      accessibilityRole="button"
      disabled={disabled}
      onPress={onPress}
      style={[styles.primaryButton, disabled && styles.primaryButtonDisabled]}
      testID={testID}
    >
      <Text style={styles.primaryButtonText}>
        {label}
      </Text>
    </TouchableOpacity>
  );
}

function SecondaryButton({
  label,
  onPress,
  testID,
}: {
  label: string;
  onPress: () => void;
  testID?: string;
}) {
  return (
    <TouchableOpacity
      accessible
      accessibilityLabel={testID ?? label}
      accessibilityRole="button"
      onPress={onPress}
      style={styles.secondaryButton}
      testID={testID}
    >
      <Text style={styles.secondaryButtonText}>
        {label}
      </Text>
    </TouchableOpacity>
  );
}

function SectionTitle({
  eyebrow,
  title,
  body,
  testID,
}: {
  eyebrow: string;
  title: string;
  body: string;
  testID: string;
}) {
  return (
    <View
      accessibilityLabel={testID}
      style={styles.sectionTitleWrap}
      testID={testID}
    >
      <Text
        accessibilityLabel={getSectionTitleId(testID, 'eyebrow')}
        style={styles.eyebrow}
        testID={getSectionTitleId(testID, 'eyebrow')}
      >
        {eyebrow}
      </Text>
      <Text
        accessibilityLabel={getSectionTitleId(testID, 'title')}
        style={styles.sectionTitle}
        testID={getSectionTitleId(testID, 'title')}
      >
        {title}
      </Text>
      <Text
        accessibilityLabel={getSectionTitleId(testID, 'body')}
        style={styles.sectionBody}
        testID={getSectionTitleId(testID, 'body')}
      >
        {body}
      </Text>
    </View>
  );
}

function ScreenModeNote({
  label,
  testID,
}: {
  label: string;
  testID: string;
}) {
  return (
    <View
      accessibilityLabel={testID}
      style={styles.modeNote}
      testID={testID}
    >
      <Text style={styles.modeNoteText}>{label}</Text>
    </View>
  );
}

function HomeScreen({ navigation }: any) {
  const [isPlannerModeVisible, setIsPlannerModeVisible] = useState(false);

  const openPlannerModeChooser = useCallback(() => {
    setIsPlannerModeVisible(true);
  }, []);

  const closePlannerModeChooser = useCallback(() => {
    setIsPlannerModeVisible(false);
  }, []);

  const startPlannerFlow = useCallback(
    (variant: FlowVariant) => {
      setIsPlannerModeVisible(false);
      navigation.navigate('Planner', { variant });
    },
    [navigation],
  );

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.homeScreen} testID={UI.homeScreen}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff8e7" />
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label="Recharge Phone Number"
          onPress={() => navigation.navigate('Recharge')}
          testID={UI.homeRechargeButton}
        />
        <View style={styles.actionSpacer} />
        <PrimaryButton
          label="Community Meeting Planner"
          onPress={openPlannerModeChooser}
          testID={UI.homePlannerButton}
        />
      </View>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        testID={UI.homeScroll}
      >
        <View
          accessibilityLabel={UI.homeHero}
          style={styles.heroCard}
          testID={UI.homeHero}
        >
          <Image
            source={require('./assets/images/jio-logo.jpg')}
            style={styles.heroLogo}
            testID={UI.homeHeroImage}
            resizeMode="cover"
          />
          <View style={styles.heroOverlay}>
            <Text style={styles.heroEyebrow} testID={UI.homeHeroEyebrow}>
              Cross-platform Demo
            </Text>
            <Text style={styles.heroTitle} testID={UI.homeHeroTitle}>
              App Automation Playground
            </Text>
            <Text style={styles.heroBody} testID={UI.homeHeroBody}>
              A small native workflow for Android and iOS with scrolling,
              validation, API data, and a webview step.
            </Text>
          </View>
        </View>

        <View
          accessibilityLabel={UI.homeWorkflowPanel}
          style={styles.panel}
          testID={UI.homeWorkflowPanel}
        >
          <ScreenModeNote label={reactNativeModeLabel} testID="home.mode.native" />
          <SectionTitle
            eyebrow="What It Shows"
            title="Choose a demo workflow"
            body="The original recharge experience is still available, and the meetup planner adds a richer native plus web journey."
            testID="home.section.workflows"
          />

          {FEATURE_HIGHLIGHTS.map((item, index) => (
            <View
              accessibilityLabel={getHighlightId(index)}
              key={item}
              style={styles.bulletRow}
              testID={getHighlightId(index)}
            >
              <View style={styles.bulletDot} />
              <Text
                style={styles.bulletText}
                testID={`${getHighlightId(index)}.text`}
              >
                {item}
              </Text>
            </View>
          ))}
        </View>

        <Text style={styles.versionText} testID={UI.homeVersion}>
          {APP_VERSION}
        </Text>
      </ScrollView>

      <Modal
        animationType="fade"
        transparent
        visible={isPlannerModeVisible}
        onRequestClose={closePlannerModeChooser}
      >
        <View style={styles.modalBackdrop} testID={UI.plannerModeModal}>
          <View style={styles.modalCard} testID={UI.plannerModePanel}>
            <Text style={styles.modalEyebrow}>Community Meeting Planner</Text>
            <Text style={styles.modalTitle}>Choose the planner flow</Text>
            <Text style={styles.modalBody}>
              The alternate flow keeps the overall journey the same, but adds
              subtle visual differences for screenshot and visual testing.
            </Text>

            <PrimaryButton
              label="Open Original Flow"
              onPress={() => startPlannerFlow('original')}
              testID={UI.plannerModeOriginalButton}
            />
            <View style={styles.actionSpacer} />
            <PrimaryButton
              label="Open Alternate Flow"
              onPress={() => startPlannerFlow('alternate')}
              testID={UI.plannerModeAlternateButton}
            />
            <View style={styles.actionSpacer} />
            <SecondaryButton
              label="Cancel"
              onPress={closePlannerModeChooser}
              testID={UI.plannerModeCancelButton}
            />
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

function RechargeScreen() {
  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.rechargeScreen} testID={UI.rechargeScreen}>
      <View style={styles.screenHeader}>
        <ScreenModeNote label="Web view" testID="recharge.mode.web" />
      </View>
      <View
        accessibilityLabel={UI.rechargeFrame}
        style={styles.webviewFrame}
        testID={UI.rechargeFrame}
      >
        <WebView
          source={{ uri: 'http://localhost:8080' }}
          javaScriptEnabled
          domStorageEnabled
          startInLoadingState
          webviewDebuggingEnabled
          accessibilityLabel={UI.rechargeWebView}
          testID={UI.rechargeWebView}
        />
      </View>
    </SafeAreaView>
  );
}

function NativeJourneyScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.nativeJourneyScreen} testID={UI.nativeJourneyScreen}>
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label="Next: Hybrid Native Views"
          onPress={() => navigation.push('NativeHybrid', { variant })}
          testID={UI.nativeJourneyContinue}
        />
      </View>
      <ScrollView
        contentContainerStyle={[
          styles.scrollContent,
          isAlternate && styles.scrollContentAlternate,
        ]}
        showsVerticalScrollIndicator={false}
        testID={UI.nativeJourneyScroll}
      >
        <View style={styles.screenHeader}>
          <ScreenModeNote
            label={nativeViewModeLabel}
            testID={`${UI.nativeJourneyModeNote}.${nativePlatformSuffix}`}
          />
        </View>

        <SectionTitle
          eyebrow="Step 2A"
          title={
            isAlternate
              ? `${nativePlatformName} native detail with extra breathing room`
              : `A longer ${nativePlatformName} native screen`
          }
          body={
            `This scrollable screen is rendered entirely with ${nativePlatformName} platform views before the workflow continues.`
          }
          testID="nativeJourney.section.intro"
        />

        <NativeJourneyView
          accessibilityLabel={UI.nativeJourneyView}
          testID={UI.nativeJourneyView}
          style={styles.nativeJourneyView}
        />
      </ScrollView>
    </SafeAreaView>
  );
}

function NativeHybridScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.nativeHybridScreen} testID={UI.nativeHybridScreen}>
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label="Next: Load Guest Profiles"
          onPress={() => navigation.navigate('GuestLookup', { variant })}
          testID={UI.nativeHybridContinue}
        />
      </View>
      <View style={styles.nativeHybridShell}>
        <View style={styles.screenHeader}>
          <ScreenModeNote
            label={hybridViewModeLabel}
            testID={`${UI.nativeHybridModeNote}.${nativePlatformSuffix}`}
          />
        </View>

        <SectionTitle
          eyebrow="Step 2B"
          title={
            isAlternate
              ? `${nativePlatformName} hybrid native views before profiles`
              : `A compact ${nativePlatformName} hybrid native views`
          }
          body="This screen combines React Native layout with a fixed native component, and it does not scroll."
          testID="nativeHybrid.section.intro"
        />

        <NativeHybridView
          accessibilityLabel={UI.nativeHybridView}
          testID={UI.nativeHybridView}
          style={styles.nativeHybridView}
        />
      </View>
    </SafeAreaView>
  );
}

function PlannerScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';
  const agendaSteps = isAlternate ? ALTERNATE_AGENDA_STEPS : AGENDA_STEPS;

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.plannerScreen} testID={UI.plannerScreen}>
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label="Next: Native Detail"
          onPress={() => navigation.push('NativeJourney', { variant })}
          testID={UI.plannerNextNativeButton}
        />
      </View>
      <ScrollView
        contentContainerStyle={[
          styles.scrollContent,
          isAlternate && styles.scrollContentAlternate,
        ]}
        showsVerticalScrollIndicator={false}
        style={styles.plannerScroll}
        testID={UI.plannerScroll}
      >
        <SectionTitle
          eyebrow="Step 1"
          title={
            isAlternate ? 'Build the event atmosphere' : 'Build the event mood'
          }
          body={
            isAlternate
              ? 'This version keeps the same screen structure, but introduces subtle layout and typography changes for visual comparison.'
              : 'This screen is intentionally scrollable so the demo includes a natural vertical swipe interaction.'
          }
          testID="planner.section.intro"
        />

        <ScreenModeNote label={reactNativeModeLabel} testID="planner.mode.native" />

        {agendaSteps.map((step, index) => (
          <View
            accessibilityLabel={getAgendaId(index, 'card')}
            key={step.title}
            style={[
              styles.agendaCard,
              isAlternate && styles.agendaCardAlternate,
            ]}
            testID={getAgendaId(index, 'card')}
          >
            <Text
              style={[
                styles.agendaIndex,
                isAlternate && styles.agendaIndexAlternate,
              ]}
              testID={getAgendaId(index, 'index')}
            >
              0{index + 1}
            </Text>
            <Text
              style={[
                styles.agendaTitle,
                isAlternate && styles.agendaTitleAlternate,
              ]}
              testID={getAgendaId(index, 'title')}
            >
              {step.title}
            </Text>
            <Text
              style={[
                styles.agendaSubtitle,
                isAlternate && styles.agendaSubtitleAlternate,
              ]}
              testID={getAgendaId(index, 'body')}
            >
              {step.subtitle}
            </Text>
          </View>
        ))}

        <View
          style={[styles.tipBanner, isAlternate && styles.tipBannerAlternate]}
          testID={UI.plannerTipBanner}
        >
          <Text
            style={[styles.tipTitle, isAlternate && styles.tipTitleAlternate]}
            testID={UI.plannerTipTitle}
          >
            Demo Tip
          </Text>
          <Text
            style={[styles.tipText, isAlternate && styles.tipTextAlternate]}
            testID={UI.plannerTipText}
          >
            {isAlternate
              ? 'Scan the refreshed cards, then move ahead to load attendee profiles from the API.'
              : 'Scroll through the agenda, then move forward to fetch attendee profiles from the API.'}
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function GuestLookupScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';
  const [guestCount, setGuestCount] = useState('');
  const [guests, setGuests] = useState<PlannerGuest[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [activeAlert, setActiveAlert] = useState<{
    title: string;
    message: string;
  } | null>(null);

  const dismissActiveAlert = useCallback(() => {
    setActiveAlert(null);
  }, []);

  const validateGuestCount = useCallback((value: string) => {
    const trimmedValue = value.trim();

    if (!/^\d+$/.test(trimmedValue)) {
      return 'Please enter a whole number from 1 to 15.';
    }

    const parsedValue = Number(trimmedValue);

    if (parsedValue < 1 || parsedValue > 15) {
      return 'Please enter a number between 1 and 15.';
    }

    return null;
  }, []);

  const loadGuests = useCallback(async () => {
    Keyboard.dismiss();
    const validationMessage = validateGuestCount(guestCount);

    if (validationMessage) {
      setActiveAlert({
        title: 'Invalid guest count',
        message: validationMessage,
      });
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(
        `https://randomuser.me/api/?results=${guestCount.trim()}`,
      );

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }

      const payload = await response.json();
      setGuests(Array.isArray(payload?.results) ? payload.results : []);
    } catch {
      setActiveAlert({
        title: 'Unable to load profiles',
        message: 'Please try again in a moment.',
      });
    } finally {
      setIsLoading(false);
    }
  }, [guestCount, validateGuestCount]);

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.guestLookupScreen} testID={UI.guestLookupScreen}>
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label={isLoading ? 'Loading Profiles...' : 'Load Profiles'}
          onPress={loadGuests}
          disabled={isLoading}
          testID={UI.guestLookupFetchButton}
        />
        <View style={styles.actionSpacer} />
        <PrimaryButton
          label="Next: Open Web Checklist"
          onPress={() => navigation.navigate('WebChecklist', { variant })}
          disabled={guests.length === 0}
          testID={UI.guestLookupResultsNextButton}
        />
      </View>
      <Modal
        animationType="fade"
        transparent
        visible={activeAlert !== null}
        onRequestClose={dismissActiveAlert}
      >
        <Pressable
          accessibilityLabel="guestLookup.alert.backdrop"
          style={styles.alertBackdrop}
          testID="guestLookup.alert.backdrop"
          onPress={dismissActiveAlert}
        >
          <Pressable
            accessibilityLabel="guestLookup.alert.card"
            style={styles.alertCard}
            testID="guestLookup.alert.card"
            onPress={() => undefined}
          >
            <Text style={styles.alertTitle} testID="guestLookup.alert.title">
              {activeAlert?.title}
            </Text>
            <Text style={styles.alertBody} testID="guestLookup.alert.body">
              {activeAlert?.message}
            </Text>
            <SecondaryButton
              label="Ok"
              onPress={dismissActiveAlert}
              testID="guestLookup.alert.ok"
            />
          </Pressable>
        </Pressable>
      </Modal>
      <ScrollView
        contentContainerStyle={[
          styles.scrollContent,
          isAlternate && styles.scrollContentAlternate,
        ]}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        testID={UI.guestLookupScroll}
        >
        <ScreenModeNote label={reactNativeModeLabel} testID="guestLookup.mode.native" />
        <SectionTitle
          eyebrow="Step 2"
          title={
            isAlternate ? 'Fetch attendee profiles' : 'Fetch guest profiles'
          }
          body={
            isAlternate
              ? 'Enter how many sample attendees you want to load. The validation remains the same, with a few styling and copy changes.'
              : 'Enter how many sample attendees you want to load. The screen validates the input before calling the API.'
          }
          testID="guestLookup.section.intro"
        />

        <View
          style={[styles.panel, isAlternate && styles.panelAlternate]}
          testID={UI.guestLookupPanel}
        >
          <Text
            style={[
              styles.inputLabel,
              isAlternate && styles.inputLabelAlternate,
            ]}
            testID={UI.guestLookupInputLabel}
          >
            {isAlternate ? 'Attendee count' : 'Number of guests'}
          </Text>
          <TextInput
            accessibilityLabel={UI.guestLookupInput}
            keyboardType="number-pad"
            maxLength={2}
            onChangeText={setGuestCount}
            placeholder="Enter 1 to 15"
            placeholderTextColor="#7f8b97"
            style={[styles.input, isAlternate && styles.inputAlternate]}
            testID={UI.guestLookupInput}
            value={guestCount}
          />
          <Text
            style={[
              styles.helperText,
              isAlternate && styles.helperTextAlternate,
            ]}
            testID={UI.guestLookupHelper}
          >
            {isAlternate
              ? 'Use digits only. Values from 1 to 15 are accepted.'
              : 'Only numeric values from 1 to 15 are accepted.'}
          </Text>
        </View>

        {isLoading ? (
          <View style={styles.loaderWrap} testID={UI.guestLookupLoader}>
            <ActivityIndicator size="large" color="#ef6c3d" />
            <Text style={styles.loaderText} testID={UI.guestLookupLoaderText}>
              {isAlternate
                ? 'Fetching attendee profiles...'
                : 'Fetching guest profiles...'}
            </Text>
          </View>
        ) : null}

        {guests.length > 0 ? (
          <View testID={UI.guestLookupResults}>
            <SectionTitle
              eyebrow="Results"
              title={`Loaded ${guests.length} sample ${
                isAlternate ? 'attendee' : 'guest'
              }${guests.length > 1 ? 's' : ''}`}
              body={
                isAlternate
                  ? 'Each card below still comes from the live API response, with small presentation changes.'
                  : 'Each card below is rendered from the live API response.'
              }
              testID="guestLookup.section.results"
            />

            <View
              accessibilityLabel={UI.guestLookupCards}
              testID={UI.guestLookupCards}
            >
              <View
                accessibilityLabel={UI.guestLookupCardsList}
                collapsable={false}
                style={styles.profileCards}
                testID={UI.guestLookupCardsList}
              >
                {guests.map((guest, index) => (
                  <View
                    key={guest.login.uuid}
                    style={[
                      styles.profileCard,
                      isAlternate && styles.profileCardAlternate,
                    ]}
                    testID={getProfileId(index, 'card')}
                  >
                    <Image
                      source={{ uri: guest.picture.large }}
                      style={[styles.avatar, isAlternate && styles.avatarAlternate]}
                      testID={getProfileId(index, 'avatar')}
                    />
                    <View
                      style={styles.profileContent}
                      testID={`guestLookup.profile.${index + 1}.content`}
                    >
                      <Text
                        style={[
                          styles.profileName,
                          isAlternate && styles.profileNameAlternate,
                        ]}
                        testID={getProfileId(index, 'name')}
                      >
                        {guest.name.title} {guest.name.first} {guest.name.last}
                      </Text>
                      <Text
                        style={[
                          styles.profileMeta,
                          isAlternate && styles.profileMetaAlternate,
                        ]}
                        testID={getProfileId(index, 'email')}
                      >
                        {guest.email}
                      </Text>
                      <Text
                        style={[
                          styles.profileMeta,
                          isAlternate && styles.profileMetaAlternate,
                        ]}
                        testID={getProfileId(index, 'location')}
                      >
                        {guest.location.city}, {guest.location.country}
                      </Text>
                      <Text
                        style={[
                          styles.profileMeta,
                          isAlternate && styles.profileMetaAlternate,
                        ]}
                        testID={getProfileId(index, 'phone')}
                      >
                        Phone: {guest.phone} | Cell: {guest.cell}
                      </Text>
                    </View>
                  </View>
                ))}
              </View>
            </View>

          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function WebChecklistScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';
  const [checklistCompleted, setChecklistCompleted] = useState(false);
  const [webViewReady, setWebViewReady] = useState(false);

  const onMessage = useCallback((event: any) => {
    if (event?.nativeEvent?.data === 'checklist-complete') {
      setChecklistCompleted(true);
    }
  }, []);

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.webChecklistScreen} testID={UI.webChecklistScreen}>
      <View style={styles.webScreenTop} testID={UI.webChecklistTop}>
        <ScreenModeNote label="Hybrid view" testID="webChecklist.mode.hybrid" />
        <SectionTitle
          eyebrow="Step 3"
          title={
            isAlternate ? 'Finish the web checklist' : 'Finish one webview task'
          }
          body={
            isAlternate
              ? 'The checklist still runs inside a webview, while the surrounding native screen has small presentation changes.'
              : 'The checklist below runs inside a webview. Tap the button in the web content to unlock the final screen.'
            }
          testID="webChecklist.section.intro"
        />
        <View
          accessibilityLabel={UI.webChecklistReady}
          style={styles.webChecklistReadyBadge}
          testID={UI.webChecklistReady}
        >
          <Text style={styles.webChecklistReadyText}>Web checklist ready</Text>
        </View>
      </View>

      <View style={styles.screenTopActions}>
        <PrimaryButton
          label={
            checklistCompleted
              ? 'Complete Workflow'
              : 'Complete the web checklist first'
          }
          onPress={() => navigation.navigate('Summary', { variant })}
          disabled={!checklistCompleted}
          testID={UI.webChecklistContinue}
        />
      </View>

      <View style={styles.webviewFrame} testID={UI.webChecklistFrame}>
        <WebView
          source={{ html: CHECKLIST_HTML }}
          onLoadEnd={() => setWebViewReady(true)}
          onLoadStart={() => setWebViewReady(false)}
          onMessage={onMessage}
          javaScriptEnabled
          domStorageEnabled
          startInLoadingState
          webviewDebuggingEnabled
          accessibilityLabel={UI.webChecklistWebView}
          testID={UI.webChecklistWebView}
        />
        {!webViewReady ? (
          <View style={styles.webChecklistLoading} testID={UI.webChecklistLoading}>
            <ActivityIndicator size="small" color="#ef6c3d" />
            <Text style={styles.webChecklistLoadingText}>Loading web checklist...</Text>
          </View>
        ) : null}
      </View>
    </SafeAreaView>
  );
}

function SummaryScreen({ navigation, route }: any) {
  const variant: FlowVariant = route.params?.variant ?? 'original';
  const isAlternate = variant === 'alternate';
  const recapItems = useMemo(
    () => [
      'Native stack navigation across multiple screens',
      'Vertical scrolling in the agenda and results experience',
      'User input validation with an Ok-dismiss alert dialog',
      'Live API rendering from randomuser.me',
      'A simple webview interaction built into the flow',
    ],
    [],
  );

  return (
    <SafeAreaView style={styles.screen} accessibilityLabel={UI.summaryScreen} testID={UI.summaryScreen}>
      <View style={styles.screenTopActions}>
        <PrimaryButton
          label="Start Again"
          onPress={() => navigation.popToTop()}
          testID={UI.summaryRestartButton}
        />
      </View>
      <ScrollView
        contentContainerStyle={[
          styles.scrollContent,
          isAlternate && styles.scrollContentAlternate,
        ]}
        showsVerticalScrollIndicator={false}
        testID={UI.summaryScroll}
        >
        <ScreenModeNote label={reactNativeModeLabel} testID="summary.mode.native" />
        <View
          style={[
            styles.summaryCard,
            isAlternate && styles.summaryCardAlternate,
          ]}
          testID={UI.summaryCard}
        >
          <Text
            style={[
              styles.summaryEyebrow,
              isAlternate && styles.summaryEyebrowAlternate,
            ]}
            testID={UI.summaryEyebrow}
          >
            Workflow Complete
          </Text>
          <Text
            style={[
              styles.summaryTitle,
              isAlternate && styles.summaryTitleAlternate,
            ]}
            testID={UI.summaryTitle}
          >
            {isAlternate
              ? 'Alternate demo ready for Android and iOS'
              : 'Demo ready for Android and iOS'}
          </Text>
          <Text
            style={[
              styles.summaryBody,
              isAlternate && styles.summaryBodyAlternate,
            ]}
            testID={UI.summaryBody}
          >
            {isAlternate
              ? 'This flow keeps the same journey while introducing intentional visual differences for comparison testing.'
              : 'This flow is intentionally simple, visually clean, and easy to use in presentations or automation demos.'}
          </Text>
        </View>

        {recapItems.map((item, index) => (
          <View
            key={item}
            style={[styles.bulletRow, isAlternate && styles.bulletRowAlternate]}
            testID={getRecapId(index)}
          >
            <View
              style={[
                styles.bulletDot,
                isAlternate && styles.bulletDotAlternate,
              ]}
            />
            <Text
              style={[
                styles.bulletText,
                isAlternate && styles.bulletTextAlternate,
              ]}
              testID={`${getRecapId(index)}.text`}
            >
              {item}
            </Text>
          </View>
        ))}

      </ScrollView>
    </SafeAreaView>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator
          screenOptions={{
            headerStyle: { backgroundColor: '#fff8e7' },
            headerShadowVisible: false,
            headerTintColor: '#17324d',
            headerTitleStyle: { fontWeight: '700' },
            contentStyle: { backgroundColor: '#fff8e7' },
          }}
        >
          <Stack.Screen
            name="Home"
            component={HomeScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Recharge"
            component={RechargeScreen}
            options={{ title: 'Recharge' }}
          />
          <Stack.Screen
            name="Planner"
            component={PlannerScreen}
            options={{ title: 'Meeting Planner' }}
          />
          <Stack.Screen
            name="NativeJourney"
            component={NativeJourneyScreen}
            options={{ title: 'Native Detail' }}
          />
          <Stack.Screen
            name="NativeHybrid"
            component={NativeHybridScreen}
            options={{ title: 'Native Blend' }}
          />
          <Stack.Screen
            name="GuestLookup"
            component={GuestLookupScreen}
            options={{ title: 'Guest Profiles' }}
          />
          <Stack.Screen
            name="WebChecklist"
            component={WebChecklistScreen}
            options={{ title: 'Venue Checklist' }}
          />
          <Stack.Screen
            name="Summary"
            component={SummaryScreen}
            options={{ title: 'Demo Summary' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#fff8e7',
  },
  screenTopActions: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 12,
  },
  scrollContent: {
    padding: 20,
    paddingBottom: 32,
  },
  plannerScroll: {
    flex: 1,
  },
  screenHeader: {
    marginBottom: 12,
  },
  modeNote: {
    alignSelf: 'flex-start',
    backgroundColor: '#fff0df',
    borderColor: '#f5c7a8',
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginBottom: 14,
  },
  modeNoteText: {
    color: '#8a3f18',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
  },
  heroCard: {
    height: 280,
    borderRadius: 28,
    overflow: 'hidden',
    marginBottom: 20,
    backgroundColor: '#17324d',
  },
  heroLogo: {
    width: '100%',
    height: '100%',
  },
  heroOverlay: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    padding: 20,
    backgroundColor: 'rgba(23, 50, 77, 0.78)',
  },
  heroEyebrow: {
    color: '#ffd166',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1.1,
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  heroTitle: {
    color: '#ffffff',
    fontSize: 28,
    fontWeight: '800',
    marginBottom: 10,
  },
  heroBody: {
    color: '#f4f7fb',
    fontSize: 15,
    lineHeight: 22,
  },
  panel: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 20,
    marginBottom: 20,
  },
  sectionTitleWrap: {
    marginBottom: 18,
  },
  eyebrow: {
    color: '#ef6c3d',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1,
    marginBottom: 6,
    textTransform: 'uppercase',
  },
  sectionTitle: {
    color: '#17324d',
    fontSize: 28,
    fontWeight: '800',
    marginBottom: 8,
  },
  sectionBody: {
    color: '#4d6378',
    fontSize: 15,
    lineHeight: 22,
  },
  bulletRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  bulletDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#ef6c3d',
    marginTop: 6,
    marginRight: 12,
  },
  bulletText: {
    flex: 1,
    color: '#17324d',
    fontSize: 15,
    lineHeight: 22,
  },
  primaryButton: {
    backgroundColor: '#ef6c3d',
    borderRadius: 18,
    paddingVertical: 16,
    paddingHorizontal: 20,
    alignItems: 'center',
  },
  primaryButtonDisabled: {
    backgroundColor: '#e4b09c',
  },
  primaryButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '800',
  },
  secondaryButton: {
    borderWidth: 1,
    borderColor: '#d6dee6',
    borderRadius: 18,
    paddingVertical: 16,
    paddingHorizontal: 20,
    alignItems: 'center',
    backgroundColor: '#ffffff',
  },
  secondaryButtonText: {
    color: '#17324d',
    fontSize: 16,
    fontWeight: '700',
  },
  actionSpacer: {
    height: 12,
  },
  versionText: {
    marginTop: 20,
    color: '#66788a',
    fontSize: 14,
    fontWeight: '600',
    textAlign: 'center',
  },
  agendaCard: {
    backgroundColor: '#ffffff',
    borderRadius: 22,
    padding: 20,
    marginBottom: 16,
  },
  agendaIndex: {
    color: '#ef6c3d',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1.1,
    marginBottom: 8,
  },
  agendaTitle: {
    color: '#17324d',
    fontSize: 20,
    fontWeight: '800',
    marginBottom: 8,
  },
  agendaSubtitle: {
    color: '#4d6378',
    fontSize: 15,
    lineHeight: 22,
  },
  scrollContentAlternate: {
    paddingTop: 24,
    paddingBottom: 40,
  },
  agendaCardAlternate: {
    borderWidth: 1,
    borderColor: '#b7d4c9',
    paddingVertical: 22,
    paddingHorizontal: 24,
    marginLeft: 8,
  },
  agendaIndexAlternate: {
    color: '#0f766e',
    fontSize: 13,
    letterSpacing: 1.4,
  },
  agendaTitleAlternate: {
    color: '#124559',
    fontSize: 22,
    fontStyle: 'italic',
  },
  agendaSubtitleAlternate: {
    color: '#516b76',
    fontSize: 16,
  },
  tipBanner: {
    backgroundColor: '#17324d',
    borderRadius: 22,
    padding: 20,
    marginBottom: 20,
  },
  tipTitle: {
    color: '#ffd166',
    fontSize: 16,
    fontWeight: '800',
    marginBottom: 8,
  },
  tipText: {
    color: '#f4f7fb',
    fontSize: 15,
    lineHeight: 22,
  },
  tipBannerAlternate: {
    backgroundColor: '#e3f5ef',
    borderWidth: 1,
    borderColor: '#9ac8ba',
  },
  tipTitleAlternate: {
    color: '#0f766e',
    fontSize: 17,
  },
  tipTextAlternate: {
    color: '#295769',
  },
  inputLabel: {
    color: '#17324d',
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d6dee6',
    backgroundColor: '#fdfdfd',
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 14,
    color: '#17324d',
    fontSize: 18,
    marginBottom: 8,
  },
  helperText: {
    color: '#66788a',
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 16,
  },
  panelAlternate: {
    borderWidth: 1,
    borderColor: '#d9e7df',
    paddingTop: 24,
  },
  inputLabelAlternate: {
    color: '#0f766e',
    fontSize: 17,
    fontStyle: 'italic',
  },
  inputAlternate: {
    borderColor: '#b6d3c8',
    backgroundColor: '#fbfefd',
  },
  helperTextAlternate: {
    color: '#5b7a72',
  },
  loaderWrap: {
    paddingVertical: 20,
    alignItems: 'center',
  },
  loaderText: {
    marginTop: 10,
    color: '#4d6378',
    fontSize: 14,
  },
  profileCard: {
    flexDirection: 'row',
    backgroundColor: '#ffffff',
    borderRadius: 22,
    padding: 16,
    marginBottom: 14,
  },
  profileCards: {
    width: '100%',
  },
  profileCardAlternate: {
    borderWidth: 1,
    borderColor: '#d2e3db',
    padding: 18,
    marginLeft: 6,
  },
  avatar: {
    width: 74,
    height: 74,
    borderRadius: 37,
    marginRight: 14,
    backgroundColor: '#e8edf2',
  },
  avatarAlternate: {
    width: 78,
    height: 78,
    borderRadius: 22,
  },
  profileContent: {
    flex: 1,
  },
  profileName: {
    color: '#17324d',
    fontSize: 17,
    fontWeight: '800',
    marginBottom: 6,
  },
  profileMeta: {
    color: '#4d6378',
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 3,
  },
  profileNameAlternate: {
    color: '#124559',
    fontStyle: 'italic',
  },
  profileMetaAlternate: {
    color: '#5f727c',
    fontSize: 14,
  },
  webScreenTop: {
    paddingHorizontal: 20,
    paddingTop: 20,
  },
  webviewFrame: {
    flex: 1,
    marginHorizontal: 20,
    marginBottom: 16,
    borderRadius: 24,
    overflow: 'hidden',
    backgroundColor: '#ffffff',
  },
  webChecklistLoading: {
    ...StyleSheet.absoluteFill,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.88)',
  },
  webChecklistLoadingText: {
    color: '#4d6378',
    fontSize: 13,
    fontWeight: '600',
  },
  webChecklistReadyText: {
    color: '#0f766e',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
  },
  webChecklistReadyBadge: {
    alignSelf: 'flex-start',
    backgroundColor: '#e2fbf4',
    borderColor: '#9be1cd',
    borderRadius: 999,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  nativeJourneyView: {
    minHeight: 1120,
    marginTop: 12,
    marginBottom: 16,
    borderRadius: 24,
    overflow: 'hidden',
    backgroundColor: '#ffffff',
  },
  nativeHybridShell: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 24,
  },
  nativeHybridView: {
    flexGrow: 0,
    height: 320,
    marginBottom: 16,
    borderRadius: 24,
    overflow: 'hidden',
    backgroundColor: '#ffffff',
  },
  summaryCard: {
    backgroundColor: '#17324d',
    borderRadius: 28,
    padding: 24,
    marginBottom: 20,
  },
  summaryEyebrow: {
    color: '#ffd166',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1,
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  summaryTitle: {
    color: '#ffffff',
    fontSize: 28,
    fontWeight: '800',
    marginBottom: 10,
  },
  summaryBody: {
    color: '#eef5fb',
    fontSize: 15,
    lineHeight: 22,
  },
  summaryCardAlternate: {
    backgroundColor: '#e3f5ef',
    borderWidth: 1,
    borderColor: '#9ac8ba',
  },
  summaryEyebrowAlternate: {
    color: '#0f766e',
  },
  summaryTitleAlternate: {
    color: '#124559',
    fontStyle: 'italic',
    fontSize: 30,
  },
  summaryBodyAlternate: {
    color: '#376071',
  },
  bulletRowAlternate: {
    marginLeft: 8,
  },
  bulletDotAlternate: {
    backgroundColor: '#0f766e',
    width: 12,
    height: 12,
    borderRadius: 3,
  },
  bulletTextAlternate: {
    color: '#295769',
    fontSize: 16,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(23, 50, 77, 0.45)',
    justifyContent: 'center',
    padding: 20,
  },
  modalCard: {
    backgroundColor: '#ffffff',
    borderRadius: 28,
    padding: 24,
  },
  alertBackdrop: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: 'rgba(23, 50, 77, 0.45)',
    justifyContent: 'center',
    padding: 20,
  },
  alertCard: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 24,
    width: '100%',
  },
  alertTitle: {
    color: '#17324d',
    fontSize: 22,
    fontWeight: '800',
    marginBottom: 10,
  },
  alertBody: {
    color: '#4d6378',
    fontSize: 15,
    lineHeight: 22,
    marginBottom: 20,
  },
  modalEyebrow: {
    color: '#ef6c3d',
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1.1,
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  modalTitle: {
    color: '#17324d',
    fontSize: 26,
    fontWeight: '800',
    marginBottom: 10,
  },
  modalBody: {
    color: '#4d6378',
    fontSize: 15,
    lineHeight: 22,
    marginBottom: 20,
  },
});
