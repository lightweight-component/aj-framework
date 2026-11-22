[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/wechat?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/wechat)
![Java Version](https://img.shields.io/badge/Java-17%2B-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/wechat)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-framework)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)

# aj-wechat

轻量级微信服务端集成组件，基于 Spring 和 AJAXJS 工具库，封装微信支付 API v3 的下单、退款、分账及账单接口，并提供消息接口、公众号 Access Token 获取与通用 Token 定时刷新工具。

本组件不是完整的微信官方 SDK，也不提供现成的支付 Controller、业务订单表或开箱即用的全部配置。应用需要自行配置商户信息、管理密钥和 Token，并实现回调入口、业务校验与持久化。部分 API 仍在完善，接入前应核对源码和微信当前接口要求。

## 环境与安装

当前模块使用 Jakarta Servlet API，面向 Java 17+、Spring Boot 3 / Spring MVC 6 环境，不可直接用于 `javax.servlet` 体系的 Spring Boot 2 应用。

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>wechat</artifactId>
    <version>1.0</version>
</dependency>
```

模块依赖 `com.ajaxjs:base:1.6`；宿主应用需提供 Spring Web 运行环境，例如 `spring-boot-starter-web`。Maven artifactId 为 `wechat`，Java 包名为 `com.ajaxjs.wechat`。

## 功能与入口

| 入口 | 功能 |
| --- | --- |
| `payment.WxPayService` | Native 支付、JSAPI 预下单、订单查询、关闭订单、生成前端支付参数 |
| `payment.RefundService` | 申请退款、查询退款、解析退款通知 |
| `payment.PayCallback` | 支付通知验签、API v3 通知资源解密与结果转换 |
| `payment.ProfitSharingApiService` | 请求/查询分账、分账回退、完结分账、接收方管理、账单与通知处理 |
| `payment.BillService` | 交易账单、资金账单申请及下载入口 |
| `merchant.SignerMaker` | 商户私钥读取与请求签名 |
| `miniapp.Message` | 统一消息发送及部分模板信息查询 |
| `OpenAccount` | 公众号 Access Token 获取及定时获取入口 |
| `token_refresh.TokenAutoRefresher` | 由调用方提供 Token 获取逻辑，按有效期安排提前刷新 |

小程序码入口 `MiniAppService.qrCode()` 当前仅构造请求参数，实际网络请求代码已注释，不会生成或保存二维码。公众号 JS-SDK 初始化中的 ticket 也尚未接入实际获取流程，不应视为完整能力。

## Spring 接入与商户配置

`WechatConfiguration` 通过 `wechat.enabled=true` 控制组件扫描，但当前模块没有声明 Spring Boot 自动配置导入资源。请在应用中显式导入配置或自行扫描所需组件；仅添加依赖和开关不等于已经完成接入。

以下为支付相关的配置示例。除通知地址外，下面的商户属性由应用自己的 `@Bean` 方法读取，组件不会自动将其绑定为 `MerchantConfig`。

```yaml
wechat:
  enabled: true
  merchant:
    mchId: ${WECHAT_MCH_ID}
    mchSerialNo: ${WECHAT_MCH_SERIAL_NO}
    privateKeyFile: ${WECHAT_PRIVATE_KEY_RESOURCE}
    apiV3Key: ${WECHAT_API_V3_KEY}
    payNotifyUrl: https://example.com/wechat/pay/notify
    refundNotifyUrl: https://example.com/wechat/refund/notify
```

```java
import com.ajaxjs.wechat.WechatConfiguration;
import com.ajaxjs.wechat.merchant.MerchantConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Configuration
@Import(WechatConfiguration.class)
public class WechatAppConfig {
    @Bean
    public MerchantConfig merchantConfig(Environment env) {
        MerchantConfig config = new MerchantConfig();
        config.setMchId(env.getRequiredProperty("wechat.merchant.mchId"));
        config.setMchSerialNo(env.getRequiredProperty("wechat.merchant.mchSerialNo"));
        config.setPrivateKey(env.getRequiredProperty("wechat.merchant.privateKeyFile"));
        config.setApiV3Key(env.getRequiredProperty("wechat.merchant.apiV3Key"));
        return config;
    }
}
```

`MerchantConfig.privateKey` 是 PEM 私钥的资源路径，不是 PEM 内容；当前通过 Spring 上下文工具读取，需确保资源可读、相关上下文工具已初始化。部署时应使用受控的密钥资源和秘密管理机制，不要将生产私钥提交到源码仓库或公开分发的 JAR。

配置中的 `mchSerialNo` 为商户证书序列号；`apiV3Key` 为 API v3 密钥，不等同于商户私钥。开启组件扫描后，支付与退款服务会读取各自的通知地址，需同时提供对应配置。

### 发起支付

在应用服务中注入 `WxPayService`，由服务端查询业务订单并确定金额后调用：

```java
// 金额单位为分：100 表示人民币 1 元。业务订单号应由服务端生成并保证唯一。
String codeUrl = wxPayService.createNativePayment(appId, 100, outTradeNo, "商品描述");
```

返回值为微信 Native 支付的 `code_url`，应用需自行渲染二维码。

JSAPI / 小程序支付先调用 `preOrder` 获取预支付标识，再生成前端支付参数：

```java
String prepayId = wxPayService.preOrder(
    openId, appId, 100, outTradeNo, "商品描述", "业务附加信息", false);
Map<String, Object> payment = wxPayService.getRequestPayment(prepayId, appId);
```

`openId` 必须与所用 AppId 对应。不能以客户端传入金额、前端支付成功提示或二维码已生成作为订单付款成功依据。业务层需校验订单状态，并使用可信支付通知或服务端查单结果确认交易。

## 支付回调与安全边界

应用自行定义 HTTPS 回调接口，并按以下顺序处理：

1. 保存未经重序列化的原始请求体及 `Wechatpay-Signature`、`Wechatpay-Timestamp`、`Wechatpay-Nonce`、`Wechatpay-Serial` 请求头。
2. 创建 `PayCallback(apiV3Key, publicKeyPath)`，调用 `verifySignature(signature, timestamp, nonce, serial, rawBody)` 验签。
3. 验签通过后解析 JSON，再调用 `payCallback(params)` 解密并取得 `PayResult`。
4. 核对商户、AppId、业务订单、交易状态、币种和金额，进行幂等落库，并按微信协议返回应答。

`publicKeyPath` 应指向 `-----BEGIN PUBLIC KEY-----` 格式的微信支付公钥 PEM，不是商户私钥，也不是完整 X.509 证书。当前实现按传入 serial 缓存配置路径对应的公钥，**并未实现可信公钥 ID / 证书序列号匹配、自动轮换或时间戳防重放校验**；这些校验和密钥管理需要应用补齐，不能仅凭该方法认定回调已满足生产安全要求。

`payCallback(params)` 本身不会自动验签。退款和分账通知同样需要在解密及修改业务状态前验证来源。回调可能重复、乱序或延迟到达，应用必须实现幂等处理和必要的主动查单。

## 消息与 Access Token

`WeChatAppletConfig` 包含 `accessKeyId`（AppId）、`accessSecret` 和 `accessToken`，由应用构建并维护。`Message` 可通过 `new Message(config)` 创建；统一消息发送的公众号模板参数与小程序模板参数按接口要求选择其一。

当前 `Message.send()` 未自动调用业务返回码检查，不应把方法正常返回当作消息已送达；部分模板接口可能涉及微信历史能力，接入时需确认账号权限、接口可用性并处理 `errcode`。

`OpenAccount.getWxAccessToken()` 获取公众号 Token，可通过 `onTokenGet` 回调将结果同步到应用缓存。`init()` 使用内部 Timer 周期调用，当前未提供对应的停止入口。

通用 `TokenAutoRefresher` 接受 `RefreshToken`，返回实现 `TokenWithExpire` 的对象（Token 字符串及秒级有效期）。`start()` 会立即获取一次 Token，并默认提前约 20% 有效期刷新，至少提前 10 秒；应用退出时调用 `stop()`。

该工具不内置微信 Token 获取接口、分布式锁或失败重试。定时刷新异常可能中断后续刷新链，应用需增加监控、重试和多实例 Token 协调。当前部分接口会记录 Token、请求信息或解密结果，生产环境需审查并脱敏相关日志。

## 测试与开发

在仓库根目录可选择运行 Token 刷新工具测试：

```bash
mvn -pl aj-wechat -Dtest=TestTokenAutoRefresher test
```

`TestMessage` 等测试包含对微信接口的真实网络调用；完整执行 `mvn -pl aj-wechat test` 前，请检查凭据、网络环境和外部调用行为。不要使用真实生产订单进行自动化测试。本文档的补充不代表已完成微信支付联调或生产安全验证。

## 源码与许可证

- [GitHub 源码](https://github.com/lightweight-component/aj-framework/tree/master/aj-wechat)
- [JavaDoc](https://javadoc.io/doc/com.ajaxjs/wechat)
- [Apache License 2.0](../LICENSE)
