import React, { useEffect } from "react";
import { Text, View, PermissionsAndroid } from "react-native";
import * as ReactNativeTurboUtils from "react-native-turbo-utils";

export default function App() {
  ReactNativeTurboUtils.getContacts().then(res => console.log(res, "resssssss")).catch(e => console.log(e, "eeeeeeee"))


  console.log(ReactNativeTurboUtils.getConstants())
  
  return (
    <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
      <Text>Theme: </Text>
    </View>
  );
}
