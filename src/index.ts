import { EventEmitter, Subscription } from "expo-modules-core";
import { ColorValue } from "react-native/types";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contacts, DeviceInfoType, InstalledApps } from "./types";

export { Contacts, InstalledApps, DeviceInfoType };
export {
  getDeviceData,
  getInstalledApps,
  getContacts,
  parseJwt,
  launchUrlInCCT,
};

const {
  getDeviceData,
  getInstalledApps,
  getContacts,
  parseJwt,
  launchUrlInCCT: launchCCTWithColor,
  ...constants
} = ReactNativeTurboUtilsModule;

const launchUrlInCCT = (url: string, color?: ColorValue) =>
  launchCCTWithColor(url, color?.toString());

const DeviceModule = ReactNativeTurboUtilsModule;
const emitter = new EventEmitter(DeviceModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default constants;
