import { EventEmitter, Subscription } from "expo-modules-core";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contact, DeviceInfoType, InstalledApps } from "./types";

export { Contact, InstalledApps, DeviceInfoType };

const emitter = new EventEmitter(ReactNativeTurboUtilsModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default ReactNativeTurboUtilsModule;
