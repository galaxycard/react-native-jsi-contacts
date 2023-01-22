import { EventEmitter, Subscription } from "expo-modules-core";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contacts, DeviceInfoType, InstalledApps } from "./types";

export { Contacts, InstalledApps, DeviceInfoType };

export const {
  getDeviceData,
  getInstalledApps,
  getContacts,
  parseJwt,
  launchUrlInCCT,
  ...constants
} = ReactNativeTurboUtilsModule;

const DeviceModule = ReactNativeTurboUtilsModule;
const emitter = new EventEmitter(DeviceModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default constants;
