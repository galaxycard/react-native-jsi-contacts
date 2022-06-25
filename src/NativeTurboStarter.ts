// @ts-ignore
import { TurboModule, TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getContacts(): Promise<Object>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('TurboStarter');
