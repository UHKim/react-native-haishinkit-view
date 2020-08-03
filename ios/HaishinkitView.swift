import UIKit
import AVFoundation
import HaishinKit
import VideoToolbox

@objc class HaishinkitView: HKView, RTMPStreamDelegate {
  private static let maxRetryCount: Int = 5
  
  private var rtmpConnection = RTMPConnection()
  private var rtmpStream: RTMPStream!
  private var sharedObject: RTMPSharedObject!
  private var currentEffect: VideoEffect?
  private var currentPosition: AVCaptureDevice.Position = .back
  private var retryCount: Int = 0
      
  @objc var streamUrl: String?;
  @objc var streamKey: String?;
  
  @objc var outputWidth: NSNumber! = 540;
  @objc var outputHeight: NSNumber! = 960;
  @objc var fpsFrame: NSNumber! = 30;
  
  @objc var bitrate: NSNumber! = 1600 * 1000 as NSNumber;

  @objc var broadcast = false;
  
  @objc var onViewStatus: RCTBubblingEventBlock?;
  @objc var onViewError: RCTBubblingEventBlock?;

  @objc var usingFront: Bool = false

  var rtmpDelegate: RTMPStreamDelegate!;

  override init(frame:CGRect) {
    super.init(frame: frame);
    configRtmpStream();
    initBroadcastView();
  }
  
  deinit {
    rtmpConnection.removeEventListener(.rtmpStatus, selector: #selector(rtmpStatusHandler), observer: self)
    rtmpConnection.removeEventListener(.ioError, selector: #selector(rtmpErrorHandler), observer: self)
    
    rtmpStream.close()
    rtmpStream.dispose();
  }
  
  required init(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  
  func didPublishInsufficientBW(_ stream: RTMPStream, withConnection: RTMPConnection) {
    if (onViewStatus != nil) {onViewStatus!(["status": "INSUFFICIENT_BW", "fps": stream.currentFPS] )}
  }
  
  func didPublishSufficientBW(_ stream: RTMPStream, withConnection: RTMPConnection) {
    if (onViewStatus != nil) {onViewStatus!(["status": "SUFFICIENT_BW", "fps": stream.currentFPS])}
  }
  
  func clear() {
    if (onViewStatus != nil) {onViewError!(["name": "clear"])}
  }
  

  @objc(initBroadcastView)
  func initBroadcastView() -> Void{
    print("initBroadcastView START")
    self.videoGravity = AVLayerVideoGravity.resizeAspectFill
    
    NotificationCenter.default.addObserver(self, selector: #selector(didEnterBackground(_:)), name: UIApplication.didEnterBackgroundNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(didBecomeActive(_:)), name: UIApplication.didBecomeActiveNotification, object: nil)
    
    self.attachStream(rtmpStream)
    if (onViewStatus != nil) {onViewStatus!(["status": "INIT_COMPLETE" ])}
    print("initBroadcastView END")
  }
  
  @objc(configRtmpStream)
  func configRtmpStream() -> Void{
    print("configRtmpStream START")
    rtmpStream = RTMPStream(connection: rtmpConnection);

    rtmpStream.captureSettings = [
      .fps: fpsFrame ?? 30, // FPS
      .sessionPreset: AVCaptureSession.Preset.iFrame1280x720, // input video width/height
        // .isVideoMirrored: false,
        .continuousAutofocus: true, // use camera autofocus mode
        .continuousExposure: true, //  use camera exposure mode
        .preferredVideoStabilizationMode: AVCaptureVideoStabilizationMode.auto
    ]
    rtmpStream.audioSettings = [
        .muted: false, // mute audio
        .bitrate: 32 * 1000,
    ]
    rtmpStream.videoSettings = [
      .width: outputWidth ??  540, // video output width
        .height: outputHeight ??  960, // video output height
        .bitrate: bitrate ?? 1600000, // video output bitrate
        .profileLevel: kVTProfileLevel_H264_High_AutoLevel, // H264 Profile require "import VideoToolbox"
        .maxKeyFrameIntervalDuration: 2, // key frame / sec
    ]
    
    rtmpStream.attachAudio(AVCaptureDevice.default(for: AVMediaType.audio), automaticallyConfiguresApplicationAudioSession: false) { error in
        print(error)
    }
    toggleCamera()
    
    rtmpStream.delegate = self;
    
    print("configRtmpStream END")
  }
  
  
  @objc(toggleCamera)
  func toggleCamera() -> Void{
    usingFront = !usingFront
    rtmpStream.attachCamera(DeviceUtil.device(withPosition: usingFront ? .front : .back)) { error in
        print(error)
      
    }
  }

  
  @objc(startPublish)
  func startPublish() -> Void{
    if ((streamUrl) != nil){
    rtmpConnection.addEventListener(.rtmpStatus, selector: #selector(rtmpStatusHandler), observer: self)
    rtmpConnection.addEventListener(.ioError, selector: #selector(rtmpErrorHandler), observer: self)
    rtmpConnection.connect(streamUrl!)
    }
    
  }
  
  
  @objc(stopPublish)
  func stopPublish() -> Void{
    if (rtmpConnection.connected == true){
      rtmpConnection.close()
      rtmpConnection.removeEventListener(.rtmpStatus, selector: #selector(rtmpStatusHandler), observer: self)
      rtmpConnection.removeEventListener(.ioError, selector: #selector(rtmpErrorHandler), observer: self)
    }
  }
  
  @objc(rtmpErrorHandler:)
  func rtmpErrorHandler(notification: Notification){
    if (onViewStatus != nil) {onViewError!(["description": notification.description, "name": notification.name ] )}
    if (streamUrl != nil){rtmpConnection.connect(streamUrl!)}
  }
  
  @objc(rtmpStatusHandler:)
  func rtmpStatusHandler(notification: Notification){
    let e = Event.from(notification)
    guard let data: ASObject = e.data as? ASObject, let code: String = data["code"] as? String else {
        return
    }
    
    if (onViewStatus != nil) {onViewStatus!(data as [AnyHashable : Any])}
    if (streamUrl != nil){
    
    switch code {
    case RTMPConnection.Code.connectSuccess.rawValue:
        retryCount = 0
        rtmpStream!.publish(streamKey)
        // sharedObject!.connect(rtmpConnection)
    case RTMPConnection.Code.connectFailed.rawValue, RTMPConnection.Code.connectClosed.rawValue:
      guard retryCount <= HaishinkitView.maxRetryCount else {
            return
        }
        Thread.sleep(forTimeInterval: pow(2.0, Double(retryCount)))
        rtmpConnection.connect(streamUrl!)
        retryCount += 1
    default:
        break
    }
    }
  }


  @objc
  private func didEnterBackground(_ notification: Notification) {
      rtmpStream.receiveVideo = false
  }

  @objc
  private func didBecomeActive(_ notification: Notification) {
      rtmpStream.receiveVideo = true
  }

}
