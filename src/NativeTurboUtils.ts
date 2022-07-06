import { TurboModule, TurboModuleRegistry } from 'react-native';

import type { Contact } from './types';

export interface Spec extends TurboModule {
  getConstants(): {
    uniqueId: string;
    deviceId: string;
    bundleId: string;
    systemVersion: string;
    appVersion: string;
    buildNumber: string;
    appName: string;
    brand: string;
    model: string;
    screenWidth: number;
    screenHeight: number;
    screenDensity: number;
    installReferrer: string;
  };
  getContacts(): Promise<{
    contacts: Contact[];
    hash: string;
  }>;
  getDeviceData(): {
    hasHeadphones: boolean;
    carrier: string;
    airplaneMode: boolean;
    batteryState: string;
    batteryLevel: number;
    lowPowerMode: boolean;
    pinOrFingerprintSet: boolean;
    fontScale: number;
    freeDiskStorage: number;
    totalDiskCapacity: number;
    maxMemory: number;
    usedMemory: number;
    hasLocation: boolean;
    wifiName: string;
    accessPointName: string;
  };
}

export default TurboModuleRegistry.getEnforcing<Spec>('TurboUtils');
