#import <Flutter/Flutter.h>
#import <AliyunOSSiOS/OSSService.h>

@interface AliyunossPlugin : NSObject<FlutterPlugin>
@property (strong,atomic) OSSClient * client;
@end
