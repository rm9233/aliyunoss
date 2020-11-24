# aliyunoss

A new Flutter plugin.

目前只能上传，其他后续在完善

使用方法:

  OssEngine.onProgress = (Map map){
    
      // 进度 {totalSize: 8924, currentSize: 4096}
      print(map);
    };

    OssEngine.onUpload = (Map map){
     
      // 是否上传成功或失败 {totalSize: 8924, currentSize: 4096}
      //{path: https://super3.oss-cn-beijing/test.png, status: true}
      //{error: "??", status: false}
      
      print(map);
    };

 await Aliyunoss.init(se.accessKeyId,se.accessKeySecret,se.stsToken,se.bucket,se.region);
await Aliyunoss.upload(path,se.bucket,se.region,"test.png");
    
    
后台返回内容

{
	"region": "<你的 region >",
	"accessKeyId": "<你的 accessKeyId >",
	"accessKeySecret": "<你的 accessKeySecret >",
	"stsToken": "<你的 stsToken>",
	"bucket": "<你的 bucket>"
}
