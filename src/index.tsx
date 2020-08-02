import { NativeModules } from 'react-native';

type HaishinkitViewType = {
  multiply(a: number, b: number): Promise<number>;
};

const { HaishinkitView } = NativeModules;

export default HaishinkitView as HaishinkitViewType;
