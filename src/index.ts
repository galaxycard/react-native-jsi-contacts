import { EventEmitter, Subscription } from "expo-modules-core";
import { ColorValue } from "react-native/types";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contact, DeviceInfoType, InstalledApps, ModuleType } from "./types";

export { Contact, InstalledApps, DeviceInfoType };
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
}: ModuleType = ReactNativeTurboUtilsModule;

const launchUrlInCCT = (url: string, color?: ColorValue) =>
  launchCCTWithColor(url, color?.toString());

const emitter = new EventEmitter(ReactNativeTurboUtilsModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default constants;
