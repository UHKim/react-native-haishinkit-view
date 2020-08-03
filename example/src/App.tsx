import * as React from 'react';
import { StyleSheet, View } from 'react-native';
import HaishinkitView from 'react-native-haishinkit-view';

export default function App() {
  React.useEffect(() => {}, []);

  return (
    <View style={styles.container}>
      <HaishinkitView />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
