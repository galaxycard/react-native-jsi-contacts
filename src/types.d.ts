export interface Constants {
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

export interface DeviceData {
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

export interface Contact {
  name?: string;
  phones?: string[];
  emails?: string[];
  photo?: string;
}
