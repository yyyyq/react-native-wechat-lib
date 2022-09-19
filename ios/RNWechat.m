#import "RNWechat.h"
#import <React/RCTBridge.h>
#import <React/RCTImageLoader.h>
// Define error messages
#define INVOKE_FAILED (@"WeChat API invoke returns false.")

// 支付
static RCTPromiseResolveBlock sendPayResolverStatic = nil;

static RCTPromiseRejectBlock sendPayRejecterStatic = nil;

// 登录
static RCTPromiseResolveBlock sendLoginResolverStatic = nil;

static RCTPromiseRejectBlock sendLoginRejecterStatic = nil;

// 小程序
static RCTPromiseResolveBlock sendMiniProResolverStatic = nil;

static RCTPromiseRejectBlock sendMiniProRejecterStatic = nil;

@implementation RNWechat {
    BOOL *_api;
}

@synthesize bridge = _bridge;


+ (RCTPromiseResolveBlock)getSendPayResolverStatic {
    return sendPayResolverStatic;
}

+ (RCTPromiseRejectBlock) getSendPayRejecterStatic {
    return sendPayRejecterStatic;
}

+ (RCTPromiseResolveBlock)getSendLoginResolverStatic {
    return sendLoginResolverStatic;
}

+ (RCTPromiseRejectBlock) getSendLoginRejecterStatic {
    return sendLoginRejecterStatic;
}

+ (RCTPromiseResolveBlock)getSendMiniProResolverStatic {
    return sendMiniProResolverStatic;
}

+ (RCTPromiseRejectBlock) getSendMiniProRejecterStatic {
    return sendMiniProRejecterStatic;
}

RCT_EXPORT_MODULE()
- (instancetype)init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification" object:nil];
        // 在register之前打开log, 后续可以根据log排查问题
        [WXApi startLogByLevel:WXLogLevelDetail logBlock:^(NSString *log) {
            NSLog(@"WeChatSDK: %@", log);
        }];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification
{
    NSString * aURLString =  [aNotification userInfo][@"url"];
    NSURL * aURL = [NSURL URLWithString:aURLString];

    if ([WXApi handleOpenURL:aURL delegate:self])
    {
        return YES;
    } else {
        return NO;
    }
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

// 注册 appid
RCT_REMAP_METHOD(registerApp, appid:(NSString *)appid universalLink:(NSString*)universalLink resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    @try {
        self.appId = appid;
        resolve(@([WXApi registerApp: appid universalLink: universalLink]));
    } @catch (NSException *exception) {
        reject(@"-10404", [NSString stringWithFormat:@"%@ %@", exception.name, exception.userInfo], nil);
    }
}

// 检查微信是否已被用户安装, 微信已安装返回YES，未安装返回NO。
RCT_EXPORT_METHOD(isWXAppInstalled: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    if ([WXApi isWXAppInstalled]) {
        resolve(@YES);
    } else {
        resolve(@NO);
    }
}

/*! @brief 打开微信
 * @return 成功返回YES，失败返回NO。
 */
RCT_EXPORT_METHOD(openWXApp: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    if ([WXApi openWXApp]) {
        resolve(@YES);
    } else {
        resolve(@NO);
    }
}

// 判断当前微信的版本是否支持OpenApi，支持返回YES，不支持返回NO。
RCT_EXPORT_METHOD(isWXAppSupportApi: (RCTPromiseResolveBlock)resolve :(RCTPromiseRejectBlock)reject) {
    if ([WXApi isWXAppSupportApi]) {
        resolve(@YES);
    } else {
        resolve(@NO);
    }
}

// 获取当前微信SDK的版本号
RCT_EXPORT_METHOD(getApiVersion: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi getApiVersion]);
}

// 发送支付请求
RCT_REMAP_METHOD(sendPayRequest, params:(NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    
    sendPayResolverStatic = resolve;
    sendPayRejecterStatic = reject;
    
    NSLog(@"WeChatSDK: %@", params);
    PayReq *request = [[PayReq alloc] init];
    
    [params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        if ([key isEqualToString:@"partnerid"]) {
            request.partnerId = obj;
        } else if ([key isEqualToString:@"prepayid"]) {
            request.prepayId = obj;
        } else if ([key isEqualToString:@"package"]) {
            request.package = obj;
        } else if ([key isEqualToString:@"noncestr"]) {
            request.nonceStr = obj;
        } else if ([key isEqualToString:@"timestamp"]) {
             request.timeStamp = [obj intValue];
        } else if ([key isEqualToString:@"sign"]) {
            request.sign = obj;
        }
          
    }];

    [WXApi sendReq:request completion:^(BOOL success) {
        NSLog(@"WeChatSDK SELF: %d", success);
    }];
}

// 发送登录验证请求
RCT_EXPORT_METHOD(sendLoginRequest: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  
  sendLoginResolverStatic = resolve;
  sendLoginRejecterStatic = reject;
  
  //构造SendAuthReq结构体
	SendAuthReq* req =[[SendAuthReq alloc]init];

	req.scope = @"snsapi_userinfo";
	req.state = params[@"state"];
	//第三方向微信终端发送一个SendAuthReq消息结构
	[WXApi sendReq:req completion:^(BOOL success) {
      NSLog(@"WeChatSDK SELF: %d", success);
  }];
}

// 拉起小程序
RCT_EXPORT_METHOD(openMiniProgram: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  sendMiniProResolverStatic = resolve;
  sendMiniProRejecterStatic = reject;

  WXLaunchMiniProgramReq *launchMiniProgramReq = [WXLaunchMiniProgramReq object];
  launchMiniProgramReq.userName = params[@"userName"];  //拉起的小程序的账号id
  launchMiniProgramReq.path = params[@"path"];    ////拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
  NSString *miniProgramType = [params objectForKey:@"miniProgramType"];
  launchMiniProgramReq.miniProgramType = [miniProgramType intValue]; //拉起小程序的类型
  return  [WXApi sendReq:launchMiniProgramReq completion:^(BOOL success) {
      NSLog(@"WeChatSDK openMiniProgram: %d", success);
  }];
}

// 拉起微信客服
// RCT_EXPORT_METHOD(openCustomerSevice: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
//   sendMiniProResolverStatic = resolve;
//   sendMiniProRejecterStatic = reject;

//   WXOpenCustomerServiceReq *req = [[WXOpenCustomerServiceReq alloc] init];
//   req.corpid = params[@"corpid"];	//企业ID
//   req.url = params[@"url"];			//客服URL
//   return [WXApi sendReq:req completion:nil];
// }

// 分享文字到微信
RCT_EXPORT_METHOD(_shareTextToWx: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  sendMiniProResolverStatic = resolve;
  sendMiniProRejecterStatic = reject;
    
    NSLog(@"WeChatSDK text: %@", params[@"title"]);
  WXMediaMessage *message = [WXMediaMessage message];
  message.title = params[@"title"];    // 标题
  message.description = params[@"content"]; // 介绍
  SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
  req.bText = YES;
  req.text = params[@"title"];
  req.message = message;
  req.scene = WXSceneSession;
  return  [WXApi sendReq:req completion:^(BOOL success) {
    NSLog(@"WeChatSDK shareUrlToWx: %d", success);
  }];
}

// 分享webUrl到微信
RCT_EXPORT_METHOD(_shareUrlToWx: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  sendMiniProResolverStatic = resolve;
  sendMiniProRejecterStatic = reject;
    
    NSLog(@"WeChatSDK ssdsds: %@", params[@"webUrl"]);

  WXWebpageObject *webpageObject = [WXWebpageObject object];
  webpageObject.webpageUrl = params[@"webUrl"];
  WXMediaMessage *message = [WXMediaMessage message];
  message.title = params[@"title"];	// 标题
  message.description = params[@"description"]; // 介绍
  NSString *imageUrl  = params[@"thumbImage"];
  NSURL *url = [NSURL URLWithString:imageUrl];
      NSURLRequest *imageRequest = [NSURLRequest requestWithURL:url];
      [[self.bridge moduleForName:@"ImageLoader"]  loadImageWithURLRequest:imageRequest size:CGSizeMake(100, 100) scale:1 clipped:FALSE resizeMode:RCTResizeModeStretch progressBlock:nil partialLoadBlock:nil
            completionBlock:^(NSError *error, UIImage *image) {
          if(image){
              [message setThumbImage: image];
          }else{
              [message setThumbImage: [UIImage imageNamed:@"rnwechat_send_img.png"]];
          }
          
          message.mediaObject = webpageObject;
          SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
          req.bText = NO;
          req.message = message;
          req.scene = WXSceneSession;
          return  [WXApi sendReq:req completion:^(BOOL success) {
              NSLog(@"WeChatSDK shareUrlToWx: %d", success);
          }];
      }];
}

// 分享webUrl到微信朋友圈
RCT_EXPORT_METHOD(_shareUrlToWxTimeline: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  sendMiniProResolverStatic = resolve;
  sendMiniProRejecterStatic = reject;
    
    NSLog(@"WeChatSDK ssdsds: %@", params[@"webUrl"]);

  WXWebpageObject *webpageObject = [WXWebpageObject object];
  webpageObject.webpageUrl = params[@"webUrl"];
  WXMediaMessage *message = [WXMediaMessage message];
  message.title = params[@"title"];    // 标题
  message.description = params[@"description"]; // 介绍
  NSString *imageUrl  = params[@"thumbImage"];
  NSURL *url = [NSURL URLWithString:imageUrl];
      NSURLRequest *imageRequest = [NSURLRequest requestWithURL:url];
      [[self.bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:imageRequest size:CGSizeMake(100, 100) scale:1 clipped:FALSE resizeMode:RCTResizeModeStretch progressBlock:nil partialLoadBlock:nil
            completionBlock:^(NSError *error, UIImage *image) {
          if(image){
              [message setThumbImage: image];
          }else{
              [message setThumbImage: [UIImage imageNamed:@"rnwechat_send_img.png"]];
          }
          
          message.mediaObject = webpageObject;
          SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
          req.bText = NO;
          req.message = message;
          req.scene = WXSceneTimeline;
          return  [WXApi sendReq:req completion:^(BOOL success) {
              NSLog(@"WeChatSDK shareUrlToWx: %d", success);
          }];
      }];
}

// 分享图片到微信
RCT_EXPORT_METHOD(_shareImageToWx: (NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    sendMiniProResolverStatic = resolve;
    sendMiniProRejecterStatic = reject;

    WXMediaMessage *message = [WXMediaMessage message];
  NSString *imageUrl  = params[@"sImage"];
    NSURL *url = [NSURL URLWithString:imageUrl];
  NSURLRequest *imageRequest = [NSURLRequest requestWithURL:url];
  [[self.bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:imageRequest callback:^(NSError *error, UIImage *image) {
    if (image == nil){
        NSLog(@"fail to load image resource");
        return;
    } else {
       WXImageObject *imageObject = [WXImageObject object];
       imageObject.imageData = UIImagePNGRepresentation(image);
        message.mediaObject = imageObject;
        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = WXSceneSession;
        return  [WXApi sendReq:req completion:^(BOOL success) {
            NSLog(@"WeChatSDK shareUrlToWx: %d", success);
        }];
    }
  }];
}

@end
