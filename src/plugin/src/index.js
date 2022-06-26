import { createRunOncePlugin, withMainApplication, withPlugins, withProjectBuildGradle, } from '@expo/config-plugins';
const withHeaderInterceptor = (config) => {
    return withMainApplication(config, async (config) => {
        // Add an OkHttpClientFactory
        // This factory will include our Device Headers interceptor
        // to the NetworkModule of RN
        config.modResults.contents = config.modResults.contents.replace('public class MainApplication extends Application implements ReactApplication {', `import in.galaxycard.android.utils.DeviceHeadersInterceptor;
import com.facebook.react.modules.network.OkHttpClientFactory;
import com.facebook.react.modules.network.NetworkingModule;
import com.facebook.react.modules.network.OkHttpClientProvider;
import okhttp3.OkHttpClient;

public class MainApplication extends Application implements ReactApplication, OkHttpClientFactory {
  @Override
  protected OkHttpClient createNewNetworkModuleClient() {
    OkHttpClient.Builder builder = OkHttpClientProvider.createClientBuilder(this);
    builder
      .addNetworkInterceptor(new DeviceHeadersInterceptor(this));
    return builder.build();
  }
  `);
        return config;
    });
};
const withKotlinGradlePlugin = (config) => {
    return withProjectBuildGradle(config, async (config) => {
        config.modResults.contents = config.modResults.contents.replace("classpath('de.undercouch:gradle-download-task:4.1.2')", "classpath('de.undercouch:gradle-download-task:4.1.2')\n" +
            'classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"');
        return config;
    });
};
const withGalaxyCardUtils = (config) => {
    return withPlugins(config, [[withHeaderInterceptor, withKotlinGradlePlugin]]);
};
const pak = require('../../package.json');
export default createRunOncePlugin(withGalaxyCardUtils, pak.name, pak.version);
