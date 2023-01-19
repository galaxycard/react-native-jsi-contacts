import { EventEmitter, Subscription } from "expo-modules-core";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contacts, InstalledApps, DeviceInfoType } from "./types";

export { Contacts, InstalledApps, DeviceInfoType };

const { getDeviceData, getInstalledApps, getContacts, parseJwt, ...constants } =
  ReactNativeTurboUtilsModule;

const DeviceModule = ReactNativeTurboUtilsModule;
const emitter = new EventEmitter(DeviceModule);

export { getDeviceData, getInstalledApps, getContacts, parseJwt };

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default constants;
