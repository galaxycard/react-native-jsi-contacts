import { EventEmitter, Subscription } from "expo-modules-core";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contact, DeviceInfoType, InstalledApps, ModuleType } from "./types";

export { Contact, InstalledApps, DeviceInfoType };
export { getDeviceData, getInstalledApps, getContacts };

const {
  getDeviceData,
  getInstalledApps,
  getContacts,
  ...constants
}: ModuleType = ReactNativeTurboUtilsModule;

const emitter = new EventEmitter(ReactNativeTurboUtilsModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default constants;
