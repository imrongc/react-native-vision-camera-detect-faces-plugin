#import <Foundation/Foundation.h>
#import <VisionCamera/FrameProcessorPlugin.h>
#import <VisionCamera/FrameProcessorPluginRegistry.h>
#import <VisionCamera/Frame.h>

#if __has_include("VisionCameraDetectFacesPlugin/VisionCameraDetectFacesPlugin-Swift.h")
#import "VisionCameraDetectFacesPlugin/VisionCameraDetectFacesPlugin-Swift.h"
#else
#import "VisionCameraDetectFacesPlugin-Swift.h"
#endif

@interface VisionCameraDetectFacesPlugin (FrameProcessorPluginLoader)
@end

@implementation VisionCameraDetectFacesPlugin (FrameProcessorPluginLoader)
+ (void) load {
  [FrameProcessorPluginRegistry addFrameProcessorPlugin:@"detectFaces"
    withInitializer:^FrameProcessorPlugin*(VisionCameraProxyHolder* proxy, NSDictionary* options) {
    return [[VisionCameraDetectFacesPlugin alloc] initWithProxy:proxy withOptions:options];
  }];
}
@end
