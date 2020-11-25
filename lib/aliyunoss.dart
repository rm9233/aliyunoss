import 'dart:async';

import 'package:flutter/services.dart';


class OssEngine{
  static void Function(Map value) onProgress;
  static void Function(Map value) onUpload;
}

class Aliyunoss {

  StreamSubscription _subscription = null;
  static final _channel = MethodChannel('aliyunoss')..setMethodCallHandler(_handler);


  static Future<dynamic> _handler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'onProgress':
        OssEngine.onProgress(methodCall.arguments);
        break;
      case 'onUpload':
        OssEngine.onUpload(methodCall.arguments);
        break;
      default:
        print('Call ${methodCall.method} from platform, arguments=${methodCall.arguments}');
    }

    return Future.value(true);
  }

  static Future<String> init(String accessKeyId,String accessKeySecret,String stsToken,String bucket,String region) async{
    final String result = await _channel.invokeMethod('init',{"region":region,"accessKeyId":accessKeyId,"accessKeySecret":accessKeySecret,"stsToken":stsToken,"bucket":bucket});
  }

  static Future<String> upload(String path,String bucket,String region,String objectKey) async{
    region += ".aliyuncs.com";
    final String result = await _channel.invokeMethod('upload',{"path":path,"bucket":bucket,"objectKey":objectKey,"region":region});
  }
}
