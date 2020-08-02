#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(HaishinkitView, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

@end
