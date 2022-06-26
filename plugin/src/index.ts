import { ConfigPlugin, withMainApplication } from '@expo/config-plugins';

const withHeaderInterceptor: ConfigPlugin = (config) => {
  return withMainApplication(config, async (config) => {
    // Add an OkHttpClientFactory
    // This factory will include our Device Headers interceptor
    // to the NetworkModule of RN
    config.modResults.contents = config.modResults.contents.replace(
      'public class MainApplication extends Application implements ReactApplication {',
      `import in.galaxycard.android.utils.DeviceHeadersInterceptor;
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
  `
    );
    return config;
  });
};

export default withHeaderInterceptor;
