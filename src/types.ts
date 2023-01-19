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

export type Contacts = {
  contacts: {
    id: number;
    name?: string;
    phones?: string[];
    emails?: string[];
    photo?: string;
  }[];
  hash: string;
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
