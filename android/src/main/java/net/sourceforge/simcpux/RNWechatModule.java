package net.sourceforge.simcpux;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class RNWechatModule extends ReactContextBaseJavaModule {
  static public Promise sendReqPromise = null;
  private final ReactApplicationContext reactContext;
  private String appId;
  private IWXAPI api = null;
  private final static String NOT_REGISTERED = "registerApp required.";
  private final static String APP_ID_NULL = "appId can't be null";
  private final static String PARTNER_ID_NULL = "partnerid can't be null";
  private final static String PREPAY_ID_NULL = "prepatid can't be null";
  private final static String NONCESTR_NULL = "noncestr can't be null";
  private final static String TIMESTAMP_NULL = "timestamp can't be null";
  private final static String PACKAGEPROP_NULL = "packageProp can't be null";
  private final static String SIGN_NULL = "sign can't be null";

  public RNWechatModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNWechat";
  }

  @ReactMethod
  public void registerApp(String appid, Promise promise) {
    try {
      this.appId = appid;
      api = WXAPIFactory.createWXAPI(reactContext.getApplicationContext(), null, false);
      promise.resolve(api.registerApp(appid));
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  public void getApiVersion(Promise promise) {
    try {
      if (api == null) {
        throw new Exception(NOT_REGISTERED);
      }
      promise.resolve(api.getWXAppSupportAPI());
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  public void openWXApp(Promise promise) {
    try {
      if (api == null) {
        throw new Exception(NOT_REGISTERED);
      }
      promise.resolve(api.openWXApp());
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  public void isWXAppInstalled(Promise promise) {
    try {
      if (api == null) {
        throw new Exception(NOT_REGISTERED);
      }
      promise.resolve(api.isWXAppInstalled());
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  public void isWXAppSupportApi(Promise promise) {
    try {
      if (api == null) {
        throw new Exception(NOT_REGISTERED);
      }
      int wxSdkVersion = api.getWXAppSupportAPI();
      promise.resolve(wxSdkVersion);
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  public void sendPayRequest(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;

    String props[] = { "appid", "partnerid", "prepayid", "prepayid", "noncestr", "timestamp", "package", "sign",
        "extdata" };

    try {
      if (api == null) {
        throw new Exception(NOT_REGISTERED);
      }

      for (int i = 0; i < props.length; i++) {
        if (!requestParams.hasKey(props[i]) && !props[i].equals("extdata")) {
          throw new Exception(props[i] + " can't be null");
        }
      }
      String appId = requestParams.getString("appid");
      String partnerId = requestParams.getString("partnerid");
      String prepayId = requestParams.getString("prepayid");
      String nonceStr = requestParams.getString("noncestr");
      String timestamp = requestParams.getString("timestamp");
      String packageValue = requestParams.getString("package");
      String sign = requestParams.getString("sign");
      String extData = requestParams.getString("extdata");

      if (appId == null) {
        throw new Exception(APP_ID_NULL);
      }
      if (partnerId == null) {
        throw new Exception(PARTNER_ID_NULL);
      }
      if (prepayId == null) {
        throw new Exception(PREPAY_ID_NULL);
      }
      if (nonceStr == null) {
        throw new Exception(NONCESTR_NULL);
      }
      if (timestamp == null) {
        throw new Exception(TIMESTAMP_NULL);
      }
      if (packageValue == null) {
        throw new Exception(PACKAGEPROP_NULL);
      }
      if (sign == null) {
        throw new Exception(SIGN_NULL);
      }

      PayReq req = new PayReq();

      req.appId = appId;
      req.partnerId = partnerId;
      req.prepayId = prepayId;
      req.nonceStr = nonceStr;
      req.timeStamp = timestamp;
      req.packageValue = packageValue;
      req.sign = sign;
      req.extData = extData == null ? "" : extData;
      // promise.resolve("参数接收成功");
      api.sendReq(req);
      // if (!success) {
      // promise.reject("-1", "调起失败");
      //
      // } else {
      // WritableMap writableMap = new WritableNativeMap();
      // for (int i = 0; i < props.length; i++) {
      // if (requestParams.hasKey(props[i])) {
      // writableMap.putString(props[i], requestParams.getString(props[i]));
      // }
      // }
      //
      // }
    } catch (Exception e) {
      promise.reject("-1", e.getMessage());
    }
  }

  @ReactMethod
  // 请求微信登录认证
  public void sendLoginRequest(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;
    String props[] = { "state" };
    String state = requestParams.getString("state");
    // send oauth request
    final SendAuth.Req req = new SendAuth.Req();
    req.scope = "snsapi_userinfo";
    req.state = state;

    // 调用api接口，发送数据到微信
    api.sendReq(req);
  }

  @ReactMethod
  // 跳转小程序
  public void openMiniProgram(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;
    String props[] = { "userName", "path", "miniProgramType" };
    String userName = requestParams.getString("userName");
    String path = requestParams.getString("path");
    Integer miniProgramType = requestParams.getInt("miniProgramType");

    WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
    req.userName = userName; // 填小程序原始id
    req.path = path; //// 拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
    req.miniprogramType = miniProgramType;// 可选打开 开发版，体验版和正式版
    api.sendReq(req);
  }

  // @ReactMethod
  // 跳转微信客服
  // public void openCustomerSevice(ReadableMap requestParams, Promise promise) {
  // RNWechatModule.sendReqPromise = promise;
  // String props[] = {"corpId", "url"};
  // String corpId = requestParams.getString("corpId");
  // String url = requestParams.getString("url");
  // WXOpenCustomerServiceChat.Req req = new WXOpenCustomerServiceChat.Req();
  // req.corpId = corpId; // 企业ID
  // req.url = url; // 客服URL
  // api.sendReq(req);
  // }

  @ReactMethod
  // 分享文字到微信
  public void _shareTextToWx(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;
    String props[] = {"title", "content"};
    String title = requestParams.getString("title");
    String content = requestParams.getString("content");
    // 初始化一个 WXTextObject 对象，填写分享的文本内容
    WXTextObject textObj = new WXTextObject();
    textObj.text = title;

    // 用 WXTextObject 对象初始化一个 WXMediaMessage 对象
    WXMediaMessage msg = new WXMediaMessage();
    msg.mediaObject = textObj;
    msg.description = content;

    SendMessageToWX.Req req = new SendMessageToWX.Req();
    req.transaction = String.valueOf(System.currentTimeMillis());
    req.message = msg;
    req.scene = SendMessageToWX.Req.WXSceneSession;
    // 调用api接口，发送数据到微信
    api.sendReq(req);
  }

  @ReactMethod
  // 分享网页到微信
  public void _shareUrlToWx(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;
    String props[] = {"title", "description", "webUrl","thumbImage"};
    String title = requestParams.getString("title");
    String description = requestParams.getString("description");
    String webUrl = requestParams.getString("webUrl");
    // 初始化一个WXWebpageObject，填写url
    WXWebpageObject webpage = new WXWebpageObject();
    webpage.webpageUrl = webUrl;

    // 用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
    final WXMediaMessage msg = new WXMediaMessage(webpage);
    msg.title = title;
    msg.description = description;
    String imageUrl = requestParams.getString("thumbImage");
    Uri turi = Uri.parse(imageUrl);
    if (turi.getScheme() == null) {
      turi = getResourceDrawableUri(getReactApplicationContext(), imageUrl);
    }
    this._getImage(turi, new ResizeOptions(100, 100), new ImageCallback() {
      @Override
      public void invoke(@Nullable Bitmap bitmap) {
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap,150,150,true);
        bitmap.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        // 调用api接口，发送数据到微信
        api.sendReq(req);
      }
    });

  }

  @ReactMethod
  // 分享网页到微信朋友圈
  public void _shareUrlToWxTimeline(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;
    String props[] = {"title", "description", "webUrl","thumbImage"};
    String title = requestParams.getString("title");
    String description = requestParams.getString("description");
    String webUrl = requestParams.getString("webUrl");
    // 初始化一个WXWebpageObject，填写url
    WXWebpageObject webpage = new WXWebpageObject();
    webpage.webpageUrl = webUrl;

    // 用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
    final WXMediaMessage msg = new WXMediaMessage(webpage);
    msg.title = title;
    msg.description = description;
    String imageUrl = requestParams.getString("thumbImage");
    Uri turi = Uri.parse(imageUrl);
    if (turi.getScheme() == null) {
      turi = getResourceDrawableUri(getReactApplicationContext(), imageUrl);
    }
    this._getImage(turi, new ResizeOptions(100, 100), new ImageCallback() {
      @Override
      public void invoke(@Nullable Bitmap bitmap) {
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap,150,150,true);
        bitmap.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;

        // 调用api接口，发送数据到微信
        api.sendReq(req);
      }
    });

  }

  @ReactMethod
  // 分享图片到微信
  public void _shareImageToWx(ReadableMap requestParams, Promise promise) {
    RNWechatModule.sendReqPromise = promise;

    String props[] = {"sImage"};
    String imageUrl = requestParams.getString("sImage");
    Uri turi = Uri.parse(imageUrl);
    if (turi.getScheme() == null) {
      turi = getResourceDrawableUri(getReactApplicationContext(), imageUrl);
    }

    this._getImage(turi, null , new ImageCallback() {
      @Override
      public void invoke(@Nullable Bitmap bitmap) {
        //初始化 WXImageObject 和 WXMediaMessage 对象
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        // 调用api接口，发送数据到微信
        api.sendReq(req);
      }
    });
  }

  // 获取图片
  private static Uri getResourceDrawableUri(Context context, String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    name = name.toLowerCase().replace("-", "_");
    int resId = context.getResources().getIdentifier(
            name,
            "drawable",
            context.getPackageName());

    if (resId == 0) {
      return null;
    } else {
      return new Uri.Builder()
              .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
              .path(String.valueOf(resId))
              .build();
    }
  }

  private void _getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
    BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
      @Override
      protected void onNewResultImpl(Bitmap bitmap) {
        if (bitmap != null) {
          if (bitmap.getConfig() != null) {
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            imageCallback.invoke(bitmap);
          } else {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageCallback.invoke(bitmap);
          }
        } else {
          imageCallback.invoke(null);
        }
      }

      @Override
      protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
        imageCallback.invoke(null);
      }
    };

    ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
    if (resizeOptions != null) {
      builder = builder.setResizeOptions(resizeOptions);
    }
    ImageRequest imageRequest = builder.build();

    ImagePipeline imagePipeline = Fresco.getImagePipeline();
    DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
    dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
  }
  private interface ImageCallback {
    void invoke(@Nullable Bitmap bitmap);
  }
}
