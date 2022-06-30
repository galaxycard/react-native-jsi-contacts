import { TurboModule, TurboModuleRegistry } from 'react-native';

import type { Contact } from './types';

export interface Spec extends TurboModule {
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
