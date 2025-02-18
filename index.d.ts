
/**
 * 向微信注册应用
 * 必须先注册应用，在 Android 后面的调用才会起作用
 * @param appid 通过微信开放平台，[获取appid](https://open.weixin.qq.com/)
 * @param universalLink 参数在 iOS 中有效，Universal Link(通用链接)是苹果在 iOS9 推出的，一种能够方便的通过传统 HTTPS 链接来启动 APP 的功能，可以使用相同的网址打开网址和 APP。
 */
export function registerApp(appid: string, universalLink: string): Promise<any>;
/**
 * 检查微信是否已被用户安装  
 * 微信已安装返回 `true`，未安装返回 `false`。
 */
export function isWXAppInstalled(): Promise<boolean>;
/**
 * 判断当前微信的版本是否支持 OpenApi  
 * 支持返回 `true`，不支持返回 `false`
 */
export function isWXAppSupportApi(): Promise<boolean>;
/**
 * 打开微信，成功返回 `true`，不支持返回 失败返回 `false`
 */
export function openWXApp(): Promise<boolean>;
/**
 * 获取当前微信SDK的版本号
 */
export function getApiVersion(): Promise<string>; 


export type RequestOption = {
  appId: string;
  partnerId: string;
  prepayId: string;
  nonceStr: string;
  timestamp: string;
  packageValue: string;
  sign: string;
  extData?: string;
}
/**
 * 发送请求支付请求
 */
export function sendPayRequest(requestOption: RequestOption) : Promise<any>;

/**
 * 发送登录认证请求
 * @param state 唯一标识码
 */
export function sendLoginRequest(requestOption: {state: string}) : Promise<any>;

/**
 * 跳转小程序
 * @param userName 小程序id
 * @param path 小程序页面路径，不填默认首页
 * @param miniProgramType 小程序版本 0: 正式版， 1：开发版， 2：体验版
*/
export function openMiniProgram(requestOption: {userName: string, path: string, miniProgramType: number}) : Promise<any>;

/**
 * 跳转微信客服
 * @param corpid 企业id
 * @param url 客服url
*/
// export function openCustomerSevice(requestOption: {corpid: string, url: string}) : Promise<any>;

/**
 * 分享文字到微信
 * @param title 标题
 * @param content 内容
*/
export function shareTextToWx(requestOption: {title: string, content: string}) : Promise<any>;


/**
 * 分享URL到微信
 * @param title 标题
 * @param description 介绍
 * @param webUrl 网页链接
 * @param thumbImage
*/
export function shareUrlToWx(requestOption: {title: string, description: string,webUrl: string,thumbImage:string}) : Promise<any>;


/**
 * 分享URL到微信朋友圈
 * @param title 标题
 * @param description 介绍
 * @param webUrl 网页链接
 * @param thumbImage
*/
export function shareUrlToWxTimeline(requestOption: {title: string, description: string,webUrl: string,thumbImage:string}) : Promise<any>;

/**
 * 分享图片到微信
 * @param sImage 图片
*/
export function shareImageToWx(requestOption:{sImage: string}) : Promise<any>;