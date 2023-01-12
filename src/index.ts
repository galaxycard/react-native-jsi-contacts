
import ReactNativeTurboUtilsModule from "./ReactNativeTurboUtilsModule";
import { PermissionResponse } from 'expo-modules-core';

interface InstalledApps {
    apps: {
      system: boolean;
      name: string;
      package: string;
      install: number;
      update: number;
    }[];
    hash: string;
}

type Contacts = {
    contacts: {
        id: number;
        name?: string;
        phones?: string[];
        emails?: string[];
        photo?: string;
    }[],
    hash: string;
}

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

export async function testDummy(): Promise<String> {
    return ReactNativeTurboUtilsModule.testDummy();
}