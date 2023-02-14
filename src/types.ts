export type InstalledApps = {
  apps: {
    system: boolean;
    name: string;
    package: string;
    install: number;
    update: number;
  }[];
  hash: string;
};

export type Contact = {
  id: number;
  name?: string;
  phones?: string[];
  emails?: string[];
  photo?: string;
};

export type DeviceInfoType = {
  accessPointName: string;
  airplaneMode: boolean;
  batteryLevel: number;
  batteryState: string;
  carrier: string;
  deviceName: string;
  fontScale: number;
  freeDiskStorage: number;
  hasHeadphones: boolean;
  hasLocation: boolean;
  lowPowerMode: boolean;
  maxMemory: number;
  pinOrFingerprintSet: boolean;
  totalDiskCapacity: number;
  usedMemory: number;
  wifiName: string;
};

export type ConstantType = {
  uniqueId: string;
  deviceId: string;
  bundleId: string;
  systemVersion: string;
  appVersion: string;
  buildNumber: string;
  appName: string;
  brand: string;
  model: string;
  manufacturer: string;
  screenWidth: number;
  screenHeight: number;
  screenDensity: number;
  installReferrer: string;
};

export type ModuleType = ConstantType & {
  getDeviceData: () => DeviceInfoType;
  getInstalledApps: () => InstalledApps;
  getContacts: () => Contact[];
  parseJwt: (authToken: string, key: string | null) => { [key: string]: any };
  launchUrlInCCT: (url: string, color?: string) => void;
};
