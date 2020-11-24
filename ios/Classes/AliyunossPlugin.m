#import "AliyunossPlugin.h"

@implementation AliyunossPlugin

FlutterMethodChannel *CHANNEL;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"aliyunoss"
            binaryMessenger:[registrar messenger]];
  AliyunossPlugin* instance = [[AliyunossPlugin alloc] init];
  CHANNEL = channel;
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"init" isEqualToString:call.method]) {
      if(_client == nil){
          NSDictionary * dic = call.arguments;
          NSString *endpoint = dic[@"region"];
          
          // 移动端建议使用STS方式初始化OSSClient。
          id<OSSCredentialProvider> credential = [[OSSStsTokenCredentialProvider alloc] initWithAccessKeyId:dic[@"accessKeyId"] secretKeyId:dic[@"accessKeySecret"] securityToken:dic[@"stsToken"]];

          _client = [[OSSClient alloc] initWithEndpoint:endpoint credentialProvider:credential];
      }
      result(@"");
      
  } else if([@"upload" isEqualToString:call.method]){
      NSDictionary * dic = call.arguments;
      NSString *bucket = dic[@"bucket"];
      NSString *path = dic[@"path"];
      NSString *region = dic[@"region"];
      NSString *objectKey = dic[@"objectKey"];
      OSSPutObjectRequest * put = [OSSPutObjectRequest new];
      put.bucketName = bucket;
      //objectKey等同于objectName，表示上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
      put.objectKey = objectKey;
      // 直接上传NSData。
      put.uploadingData = [NSData dataWithContentsOfFile:path];
      put.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
          //NSLog(@"%lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
          NSDictionary *arguments = @{
                      @"currentSize":  [NSString stringWithFormat:@"%lld",totalByteSent],
                      @"totalSize": [NSString stringWithFormat:@"%lld",totalBytesExpectedToSend]
          };
          [CHANNEL invokeMethod:@"onProgress" arguments:arguments];
      };
      OSSTask * putTask = [_client putObject:put];
      [putTask continueWithBlock:^id(OSSTask *task) {
          if (!task.error) {
              NSDictionary *arguments = @{
                             @"status": @"true",
                             @"path": [NSString stringWithFormat:@"https://%@.%@/%@", bucket,region,objectKey]
                         };
            NSLog(@"upload object success! %@" , task);
            [CHANNEL invokeMethod:@"onUpload" arguments:arguments];
            
          } else {
              NSDictionary *arguments = @{
                             @"status": @"false",
                             @"message": [NSString stringWithFormat:@"%@",task.error]
                         };
            NSLog(@"upload object failed, error: %@" , task.error);
            [CHANNEL invokeMethod:@"onUpload" arguments:arguments];
            
          }
          return nil;
      }];
      // 等待任务完成。
      [putTask waitUntilFinished];
      result(@"");
  }else {
    result(FlutterMethodNotImplemented);
  }
}

@end
