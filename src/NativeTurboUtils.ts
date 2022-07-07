import { TurboModule, TurboModuleRegistry } from 'react-native';

interface Constants {
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
}

interface DeviceData {
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
}

interface Contact {
  name?: string;
  phones?: string[];
  emails?: string[];
  photo?: string;
}

export interface Spec extends TurboModule, Constants {
  getConstants(): Constants;
  getContacts(): Promise<{
    contacts: Contact[];
    hash: string;
  }>;
  getDeviceData(): DeviceData;
}

export default TurboModuleRegistry.getEnforcing<Spec>('TurboUtils');
