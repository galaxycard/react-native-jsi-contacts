import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { Contacts, InstalledApps } from "./types";

export function getDeviceData() {
  return ReactNativeTurboUtilsModule.getDeviceData();
}

export function getConstants() {
  return ReactNativeTurboUtilsModule.getConstants();
}

export async function getInstalledApps(): Promise<InstalledApps> {
  return await ReactNativeTurboUtilsModule.getInstalledApps();
}

export async function getContacts(): Promise<Contacts> {
  return ReactNativeTurboUtilsModule.getContacts();
}

export function parseJwt(jwt: string, key: string | null) {
  return ReactNativeTurboUtilsModule.parseJwt(jwt, key);
}
