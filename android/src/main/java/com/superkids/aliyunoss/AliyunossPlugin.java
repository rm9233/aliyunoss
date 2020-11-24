package com.superkids.aliyunoss;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.util.HashMap;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AliyunossPlugin */
public class AliyunossPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

  //https://blog.csdn.net/tdltdltdl886/article/details/80621142

    private MethodChannel channel;
    private Context context;
    private OSSClient oss;
    private Activity activity;



    @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "aliyunoss");
    channel.setMethodCallHandler(this);
    context = flutterPluginBinding.getApplicationContext();
  }


  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "aliyunoss");
    channel.setMethodCallHandler(new AliyunossPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

     if(call.method.equals("init")){
         HashMap<String,String> args = (HashMap<String, String>) call.arguments;

         //String ,String ,String ,String ,String region
         OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(args.get("accessKeyId"), args.get("accessKeySecret"), args.get("stsToken"));
         ClientConfiguration conf = new ClientConfiguration();
         conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒。
         conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒。
         conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个。
         conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次。
         oss = new OSSClient(context, args.get("region"), credentialProvider, conf);
         result.success("");

    } else if(call.method.equals("upload")){
         final HashMap<String,String> args = (HashMap<String, String>) call.arguments;


        //String ,String bucket,String region,String objectKey
         // Construct an upload request
         PutObjectRequest put = new PutObjectRequest(args.get("bucket"), args.get("objectKey"),  args.get("path"));

         // You can set progress callback during asynchronous upload
         put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
             @Override
             public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                 //Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);

                 final Map<String, String> arguments = new HashMap<String, String>();
                 arguments.put("currentSize", String.valueOf(currentSize));
                 arguments.put("totalSize", String.valueOf(totalSize));
                 invokeMethod("onProgress", arguments);
             }
         });

         OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
             @Override
             public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                 Log.d("PutObject", "UploadSuccess");
                 final Map<String, String> arguments = new HashMap<String, String>();
                 arguments.put("status", "true");
                 arguments.put("path", "https://"+args.get("bucket")+"."+args.get("region")+"/"+args.get("objectKey"));
                 invokeMethod("onUpload", arguments);
             }

             @Override
             public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                 // Request exception
                 if (clientExcepion != null) {
                     // Local exception, such as a network exception
                     final Map<String, String> arguments = new HashMap<String, String>();
                     arguments.put("status", "false");
                     arguments.put("error", clientExcepion.getMessage());
                     invokeMethod("onUpload", arguments);
                     clientExcepion.printStackTrace();
                 }
                 if (serviceException != null) {
                     // Service exception
                     Log.e("ErrorCode", serviceException.getErrorCode());
                     Log.e("RequestId", serviceException.getRequestId());
                     Log.e("HostId", serviceException.getHostId());
                     Log.e("RawMessage", serviceException.getRawMessage());
                     final Map<String, String> arguments = new HashMap<String, String>();
                     arguments.put("status", "false");
                     arguments.put("error", "ErrorCode:"+serviceException.getErrorCode()+" RawMessage:"+serviceException.getRawMessage());
                     invokeMethod("onUpload", arguments);
                 }
             }
         });
         result.success("");
     }else {
      result.notImplemented();
    }
  }

    private void invokeMethod(final String method, final Object arguments) {
      if (this.activity != null){
        activity.runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    channel.invokeMethod(method, arguments);
                  }
                });
      }
    }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}
