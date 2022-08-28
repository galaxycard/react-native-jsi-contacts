import {
  AndroidConfig,
  ConfigPlugin,
  createRunOncePlugin,
  withAndroidManifest,
  withAppBuildGradle,
  withGradleProperties,
  withMainApplication,
  withPlugins,
  withProjectBuildGradle,
} from '@expo/config-plugins';

const { addMetaDataItemToMainApplication, getMainApplicationOrThrow } =
  AndroidConfig.Manifest;

const withHeaderInterceptor: ConfigPlugin = (config) => {
  return withMainApplication(config, async (config) => {
    // Add an OkHttpClientFactory
    // This factory will include our Device Headers interceptor
    // to the NetworkModule of RN
    config.modResults.contents = config.modResults.contents.replace(
      /public class MainApplication(.*) {$/m,
      `import in.galaxycard.android.utils.DeviceHeadersInterceptor;
import com.facebook.react.modules.network.OkHttpClientFactory;
import com.facebook.react.modules.network.NetworkingModule;
import com.facebook.react.modules.network.OkHttpClientProvider;
import okhttp3.OkHttpClient;
import com.bugsnag.android.Bugsnag;

public class MainApplication$1, OkHttpClientFactory {
  @Override
  public OkHttpClient createNewNetworkModuleClient() {
    OkHttpClient.Builder builder = OkHttpClientProvider.createClientBuilder(this);
    builder
      .addNetworkInterceptor(new DeviceHeadersInterceptor(this));
    return builder.build();
  }
  `
    );
    config.modResults.contents = config.modResults.contents.replace(
      'super.onCreate();',
      `$&
    OkHttpClientProvider.setOkHttpClientFactory(this);`
    );
    config.modResults.contents = config.modResults.contents.replace(
      'SoLoader.init(this, /* native exopackage */ false);',
      `$&

    SoLoader.loadLibrary("bugsnag-ndk");
    SoLoader.loadLibrary("bugsnag-plugin-android-anr");

    Bugsnag.start(this);`
    );

    return config;
  });
};

const withBugsnagPlugin: ConfigPlugin = (config) => {
  return withProjectBuildGradle(config, async (config) => {
    config.modResults.contents = config.modResults.contents.replace(
      /classpath\('de.undercouch:gradle-download-task.*?$/m,
      `$&
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:7.+")`
    );
    return config;
  });
};

const withBugsnag: ConfigPlugin = (config) => {
  return withAppBuildGradle(config, async (config) => {
    config.modResults.contents = config.modResults.contents.replace(
      'apply plugin: "com.android.application"',
      `$&
apply plugin: "com.bugsnag.android.gradle"`
    );
    config.modResults.contents = config.modResults.contents.replace(
      /^apply from: .*react.gradle.*$/m,
      `$&

bugsnag {
    uploadReactNativeMappings = true
}`
    );
    config.modResults.contents = config.modResults.contents.replace(
      /dependencies {/,
      `$&
    implementation 'com.google.mlkit:barcode-scanning:17.0.2'
    implementation 'com.facebook.android:facebook-core:12.0.1'
    `
    );
    return config;
  });
};

const withMetadata: ConfigPlugin<{
  bugsnag: { apiKey: string };
  facebook: { appId: string };
}> = (
  config,
  props: { bugsnag: { apiKey: string }; facebook: { appId: string } }
) => {
  return withAndroidManifest(config, async (config) => {
    const mainApplication = getMainApplicationOrThrow(config.modResults);
    addMetaDataItemToMainApplication(
      mainApplication,
      'com.facebook.sdk.ApplicationId',
      props.facebook.appId
    );
    addMetaDataItemToMainApplication(
      mainApplication,
      'com.bugsnag.android.API_KEY',
      props.bugsnag.apiKey
    );
    return config;
  });
};

const withProguard: ConfigPlugin = (config) => {
  return withGradleProperties(config, async (config) => {
    config.modResults.push({
      type: 'property',
      key: 'android.kotlinVersion',
      value: '1.6.0',
    });
    config.modResults = config.modResults.map((item) => {
      // if (item.type === 'property' && item.key === 'expo.webp.animated') {
      //   item.value = 'true';
      // }
      if (item.type === 'property' && item.key === 'newArchEnabled') {
        item.value = 'true';
      }
      return item;
    });
    return config;
  });
};

const withGalaxyCardUtils: ConfigPlugin<{
  bugsnag: { apiKey: string };
  facebook: { appId: string };
}> = (
  config,
  props: { bugsnag: { apiKey: string }; facebook: { appId: string } }
) => {
  return withPlugins(config, [
    withHeaderInterceptor,
    withBugsnagPlugin,
    withBugsnag,
    [withMetadata, props],
    withProguard,
  ]);
};

const pak = require('@galaxycard/react-native-turbo-utils/package.json');
export default createRunOncePlugin(withGalaxyCardUtils, pak.name, pak.version);
