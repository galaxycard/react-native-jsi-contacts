import { EventEmitter, Subscription } from "expo-modules-core";

import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { DeviceInfoType } from "./types";

const emitter = new EventEmitter(ReactNativeTurboUtilsModule);

export function addDeviceInfoChangedListener(
  listener: (event: DeviceInfoType) => void
): Subscription {
  return emitter.addListener("onDeviceInfoChanged", listener);
}

export default ReactNativeTurboUtilsModule;
