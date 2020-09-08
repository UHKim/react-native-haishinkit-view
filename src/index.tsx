import React, { Component } from 'react';
import {
  findNodeHandle,
  UIManager,
  ViewPropTypes,
  requireNativeComponent,
  ViewProps,
} from 'react-native';
import PropTypes from 'prop-types';

type NativeEventObject = {
  nativeEvent: any;
};
type LiveViewProps = {
  streamUrl?: string;
  streamKey?: string;
  bitrate?: number;
  
  outputWidth?: number;
  outputHeight?: number;

  onViewStatus?: (detail: NativeEventObject) => any;
  onViewError?: (detail: NativeEventObject) => any;
  initBroadcastView?: () => void;

  startPublish?: () => void;
  stopPublish?: () => void;
  toggleCamera?: () => void;

  onDidMount?: () => void;
} & ViewProps;

export type RtmpStatusType = {
  code: string;
  description: string;
};

let HKViewNative = requireNativeComponent<LiveViewProps>('RNHaishinkitView');

class HaishinkitView extends Component<LiveViewProps> {
  static propTypes: any;

  constructor(props: LiveViewProps) {
    super(props);
  }

  componentDidMount() {
    setTimeout(() => {
      if (this.props.onDidMount) this.props.onDidMount();
    }, 0);
  }

  nativeViewCmd = (cmdName: string, cmdArgs?: any[]) => {
    try {
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this),
        UIManager.getViewManagerConfig('RNHaishinkitView').Commands[cmdName],
        cmdArgs
      );
    } catch (e) {
      console.error(e);
      return;
    }
  };

  _onViewStatus = (event: NativeEventObject) => {
    if (!this.props.onViewStatus) {
      return;
    }

    this.props.onViewStatus!(event.nativeEvent);
  };

  _onViewError = (event: NativeEventObject) => {
    if (!this.props.onViewError) {
      return;
    }
    this.props.onViewError!(event.nativeEvent);
  };

  startPublish = () => this.nativeViewCmd('startPublish');

  stopPublish = () => this.nativeViewCmd('stopPublish');

  toggleCamera = () => this.nativeViewCmd('toggleCamera');

  render() {
    return (
      <HKViewNative
        {...this.props}
        onViewStatus={this._onViewStatus}
        onViewError={this._onViewError}
      />
    );
  }
}

HaishinkitView.propTypes = {
  streamUrl: PropTypes.string,
  streamKey: PropTypes.string,
  bitrate: PropTypes.number,

  outputWidth: PropTypes.number,
  outputHeight: PropTypes.number,

  onViewStatus: PropTypes.func,
  onViewError: PropTypes.func,
  startPublish: PropTypes.func,
  stopPublish: PropTypes.func,
  toggleCamera: PropTypes.func,

  scaleX: PropTypes.number,
  scaleY: PropTypes.number,
  translateX: PropTypes.number,
  translateY: PropTypes.number,
  rotation: PropTypes.number,
  ...ViewPropTypes,
};

export default HaishinkitView;
