#import <React/RCTBridgeModule.h>
#import <Foundation/Foundation.h>

#import <React/RCTViewManager.h>
#import <React/RCTComponent.h>
#import <React/RCTConvert.h>
#import "RNHaishinkitView-Swift.h"

@import RNHaishinkitView;

@interface RNHaishinkitViewManager : RCTViewManager

@property (strong) RNHaishinkitView* bView;

@end

@implementation RNHaishinkitViewManager

RCT_EXPORT_MODULE(RNHaishinkitView)

RCT_EXPORT_VIEW_PROPERTY(streamUrl, NSString)
RCT_EXPORT_VIEW_PROPERTY(streamKey, NSString)

RCT_EXPORT_VIEW_PROPERTY(outputWidth, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(outputHeight, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(bitrate, NSNumber)

RCT_EXPORT_VIEW_PROPERTY(onViewStatus, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onViewError, RCTBubblingEventBlock)

RCT_EXPORT_METHOD(initBroadcastView:options){
  [_bView initBroadcastView];
}
RCT_EXPORT_METHOD(configRtmpStream:options){
  [_bView configRtmpStream];
}

RCT_EXPORT_METHOD(startPublish:options){
  [_bView startPublish];
}
RCT_EXPORT_METHOD(stopPublish:options){
  [_bView stopPublish];
}
RCT_EXPORT_METHOD(toggleCamera:options){
  [_bView toggleCamera];
  
}

- (UIView *) view
{
  if (_bView ==nil) _bView =[[RNHaishinkitView alloc] init];
  return _bView ;
}

@end
