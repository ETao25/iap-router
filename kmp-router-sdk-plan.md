# KMP Router SDK 设计与实施规划

---

## 📊 实施进度追踪

> 最后更新：2026-01-21

| Phase | 名称 | 状态 | 进度 | 备注 |
|-------|------|------|------|------|
| **Phase 1** | 基础架构搭建 | ✅ 已完成 | 100% | 114 个测试 |
| **Phase 2** | 拦截器机制 | ✅ 已完成 | 100% | 27 个测试 |
| **Phase 3** | 参数管理与类型安全 | ✅ 已完成 | 100% | 14 个测试 |
| **Phase 4** | 降级与回调机制 | ✅ 已完成 | 100% | 23 个测试 |
| **Phase 5** | iOS 平台集成 | ⏳ 待开始 | 0% | |
| **Phase 6** | Android 平台集成 | ⏳ 待开始 | 0% | |
| **Phase 7** | 迁移工具与文档 | ⏳ 待开始 | 0% | |
| **Phase 8** | 优化与稳定 | ⏳ 待开始 | 0% | |

> 当前总测试数：185 个

### Phase 1 完成情况

- [x] KMP 项目结构搭建（Kotlin 2.2.21 + Gradle 8.x）
- [x] 协议解析器（Protocol Parser）
- [x] 路由匹配器（Route Matcher）- 支持精确匹配、path 参数、通配符
- [x] 路由表管理（Route Registry）- 支持页面路由和 Action 路由注册
- [x] 基础 Navigator/PageFactory/ActionExecutor 接口定义
- [x] 参数扩展工具类（ParamsExtensions）
- [x] 日志接口（Logger）
- [x] 降级处理接口（FallbackHandler）
- [x] 异常体系（RouterException）
- [x] URL 编解码（UrlCodec - 使用 Ktor HTTP）
- [x] 单元测试覆盖

### Phase 2 完成情况

- [x] RouteInterceptor 接口定义（支持 suspend function 异步拦截）
- [x] InterceptorChain 拦截器链实现
- [x] 全局拦截器支持（对所有路由生效）
- [x] 局部拦截器支持（基于路由模式匹配，如 `payment/*`）
- [x] 拦截器优先级排序（priority 数值越小优先级越高）
- [x] 参数修改能力（通过 `chain.proceed(modifiedContext)` 实现）
- [x] 重定向能力（返回 `RouteResult.Redirect`）
- [x] 路由阻断能力（返回 `RouteResult.Blocked`）
- [x] InterceptorManager 统一管理
- [x] 单元测试覆盖（27 个测试）

### Phase 3 完成情况

- [x] 基础类型自动转换（String/Int/Long/Double/Boolean）- 已在 Phase 1 实现
- [x] Float 类型支持（requireFloat/optFloat）
- [x] requireList/requireMap 方法（带类型校验）
- [x] 参数提取工具类完善
- [x] 单元测试覆盖（14 个测试）

> **注意**：根据 v1.3 API 简化设计，以下功能已移除：
> - ObjectStore（内存缓存管理器）- 改为通过 params 直接传递对象
> - requiredParams（参数校验器）- 改为接收侧实现类型安全
> - TTL 管理 - 随 ObjectStore 移除

### Phase 4 完成情况

- [x] RouteCallback 接口（路由结果回调）
- [x] RouteError 和 RouteErrorCode（错误信息封装）
- [x] SimpleRouteCallback（简化回调实现）
- [x] RouteObserver 接口（路由事件观察者）
- [x] SimpleRouteObserver（简化观察者实现）
- [x] RouteEvent（埋点事件数据）
- [x] RouteResultType（路由结果类型枚举）
- [x] ObserverManager（观察者管理器）
- [x] 单元测试覆盖（23 个测试）

> **注意**：FallbackHandler、FallbackConfig、FallbackAction 已在 Phase 1 实现，Logger 接口也已在 Phase 1 实现。

### 当前代码结构

```
kmp-router/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml          # 版本目录
└── src/
    ├── commonMain/kotlin/com/iap/router/
    │   ├── RouteRegistry.kt        # 路由注册接口
    │   ├── core/
    │   │   ├── ProtocolParser.kt   # URL 解析
    │   │   ├── RouteMatcher.kt     # 路由匹配
    │   │   └── RouteTable.kt       # 路由表
    │   ├── model/
    │   │   ├── ParsedRoute.kt      # 解析后的路由
    │   │   ├── RouteContext.kt     # 路由上下文
    │   │   ├── RouteResult.kt      # 路由结果
    │   │   ├── RouteSource.kt      # 路由来源
    │   │   ├── PageRouteConfig.kt  # 页面路由配置
    │   │   └── NavigationOptions.kt # 导航选项
    │   ├── interceptor/            # Phase 2: 拦截器机制
    │   │   ├── RouteInterceptor.kt     # 拦截器接口
    │   │   ├── InterceptorChainImpl.kt # 拦截器链实现
    │   │   └── InterceptorManager.kt   # 拦截器管理器
    │   ├── observer/               # Phase 4: 回调与观察者
    │   │   ├── RouteCallback.kt        # 路由回调接口
    │   │   ├── RouteObserver.kt        # 路由观察者接口
    │   │   ├── RouteEvent.kt           # 埋点事件数据
    │   │   └── ObserverManager.kt      # 观察者管理器（主线程操作）
    │   ├── platform/
    │   │   ├── Navigator.kt        # 导航器接口
    │   │   ├── PlatformPage.kt     # PlatformPageCreator 接口 + PageTarget
    │   │   └── ActionExecutor.kt   # Action 执行器接口
    │   ├── params/
    │   │   └── ParamsExtensions.kt # 参数扩展
    │   ├── util/
    │   │   ├── Logger.kt           # 日志接口
    │   │   └── UrlCodec.kt         # URL 编解码 (Ktor)
    │   ├── fallback/
    │   │   └── FallbackHandler.kt  # 降级处理
    │   └── exception/
    │       └── RouterException.kt  # 异常定义
    ├── commonTest/kotlin/com/iap/router/
    │   ├── core/
    │   │   ├── ProtocolParserTest.kt
    │   │   └── RouteMatcherTest.kt
    │   ├── interceptor/
    │   │   ├── InterceptorChainTest.kt
    │   │   └── InterceptorManagerTest.kt
    │   ├── observer/
    │   │   ├── ObserverManagerTest.kt
    │   │   ├── RouteCallbackTest.kt
    │   │   └── RouteEventTest.kt
    │   ├── params/
    │   │   └── ParamsExtensionsTest.kt
    │   └── testutil/
    │       └── TestRouteContext.kt
    ├── iosMain/kotlin/com/iap/router/
    │   └── platform/
    │       └── PlatformPage.kt     # IOSPageCreator + 扩展函数
    ├── iosTest/kotlin/com/iap/router/
    │   └── platform/
    │       └── IOSPageRegistrationTest.kt  # iOS 注册 API 测试
    ├── androidMain/kotlin/com/iap/router/
    │   └── platform/
    │       └── PlatformPage.kt     # AndroidPageCreator + 扩展函数
    └── androidUnitTest/kotlin/com/iap/router/
        └── platform/
            └── AndroidPageRegistrationTest.kt  # Android 注册 API 测试
```

---

## 一、项目概述

### 1.1 背景

当前双端路由存在以下核心问题：
- **业务实现一致性差**：双端通过 pageId 注册路由，但路由与页面关系不明确，参数完全依赖各自实现
- **路由拦截能力不统一**：Android SDK 拦截能力弱，iOS 完全依赖业务层实现
- **协议混乱**：SDK 定义协议与实际使用协议不一致，扩展性差
- **Action 支持不规范**：业务层利用 pageId 注册返回空的方式取巧注册
- **iOS 导航栈管理混乱**：存在 useDefaultNavi 等黑魔法参数，Present/Push 层级错乱

### 1.2 目标

基于 Kotlin Multiplatform (KMP) 构建跨平台路由 SDK，实现：
- 统一的路由协议规范
- 双端一致的核心路由能力
- 灵活可扩展的拦截器机制
- 规范的 Action 路由支持
- 平滑的迁移路径

### 1.3 设计原则

| 原则 | 说明 |
|------|------|
| **纯逻辑层** | KMP 只实现协议解析、路由表管理、拦截器链、参数校验等纯逻辑，实际页面跳转由各平台原生执行 |
| **最小化改动** | 双端基于最小化改动原则，分别将已有路由能力在新 SDK 中适配 |
| **API 稳定性** | API 一旦发布，后续版本保持向后兼容 |
| **渐进式迁移** | SDK 只支持新协议，业务层实现旧协议适配，逐步废弃 |

---

## 二、架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        业务层 (Platform-specific)                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ 旧协议适配器     │  │ 页面路由注册    │  │ Action 注册     │  │
│  │ (Legacy Adapter) │  │ (Page Registry) │  │ (Action Handler)│  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │           │
│  ┌────────▼────────────────────▼────────────────────▼────────┐  │
│  │              Platform Bridge (expect/actual)               │  │
│  │    Navigator / PageFactory / ActionExecutor 平台实现        │  │
│  └────────────────────────────┬───────────────────────────────┘  │
└───────────────────────────────┼─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                    KMP Router SDK (Shared)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                     Router Core                              ││
│  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌─────────────┐  ││
│  │  │ Protocol  │ │  Route    │ │Interceptor│ │  Fallback   │  ││
│  │  │  Parser   │ │  Matcher  │ │   Chain   │ │  Handler    │  ││
│  │  └───────────┘ └───────────┘ └───────────┘ └─────────────┘  ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Route Registry                            ││
│  │  ┌───────────────┐ ┌───────────────┐ ┌───────────────────┐  ││
│  │  │ Page Routes   │ │ Action Routes │ │ Route Metadata    │  ││
│  │  └───────────────┘ └───────────────┘ └───────────────────┘  ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Params Manager                            ││
│  │  ┌───────────────┐ ┌───────────────┐ ┌───────────────────┐  ││
│  │  │ Type Converter│ │ Memory Cache  │ │ Param Validator   │  ││
│  │  └───────────────┘ └───────────────┘ └───────────────────┘  ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Observer / Callback                       ││
│  │  ┌───────────────┐ ┌───────────────┐                        ││
│  │  │ Route Events  │ │ Lifecycle     │                        ││
│  │  └───────────────┘ └───────────────┘                        ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责

| 模块 | 职责 | 实现位置 |
|------|------|----------|
| **Protocol Parser** | 解析路由 URL，提取 scheme/host/path/params | KMP Shared |
| **Route Matcher** | 根据路径匹配注册的路由，支持 path 参数提取 | KMP Shared |
| **Interceptor Chain** | 管理拦截器链，支持同步/异步拦截、参数修改、重定向 | KMP Shared |
| **Route Registry** | 管理路由表（页面路由 + Action 路由） | KMP Shared |
| **Params Manager** | 类型转换、内存缓存对象引用、参数校验 | KMP Shared |
| **Fallback Handler** | 路由失败时的降级处理 | KMP Shared |
| **Observer/Callback** | 路由事件回调（用于埋点监控） | KMP Shared |
| **Navigator** | 实际执行页面跳转（push/present），直接调用底层路由 | Platform |
| **ActionExecutor** | 执行 Action 路由的具体逻辑 | Platform |
| **Legacy Adapter** | 旧协议到新协议的转换 | Platform/Business |

---

## 三、协议设计

### 3.1 新协议规范

采用 RESTful 风格，支持灵活深度路径：

```
iap://{path}[/:pathParam]?queryParam=value
```

#### 3.1.1 页面路由

```
# 基础格式
iap://account/settings?tab=security

# 带 path 参数
iap://order/detail/:orderId?from=list
iap://fx/trade/:pairId/chart?period=1d

# 多级路径
iap://payment/card/bindNew?source=checkout
```

#### 3.1.2 Action 路由

```
# Action 统一使用 action 前缀
iap://action/{actionName}[/:pathParam]?queryParam=value

# 示例
iap://action/showPopup?type=confirm&message=xxx
iap://action/share?content=xxx&platform=wechat
iap://action/copyText?content=xxx
```

#### 3.1.3 导航模式参数

通过 query 参数指定导航模式（各平台按需支持）：

| 参数 | 说明 | 示例值 |
|------|------|--------|
| `_navMode` | 导航模式 | push / present |
| `_presentStyle` | iOS Present 样式 | fullScreen / pageSheet / formSheet |
| `_animated` | 是否动画 | true / false |

```
iap://order/detail/:orderId?_navMode=present&_presentStyle=pageSheet
```

### 3.2 旧协议兼容（注意是业务层实现的，不在SDK中实现）

**旧协议格式**：
```
iap://app?pageId=xxx&param1=value1
```

**适配方案**：业务层实现 `LegacyRouteAdapter`，在调用 SDK 之前完成协议转换。

```kotlin
// 伪代码示例
class LegacyRouteAdapter {
    private val mapping = mapOf(
        "orderDetail" to "order/detail/:orderId",
        "accountSettings" to "account/settings",
        // ... 其他映射
    )

    fun adapt(legacyUrl: String): String? {
        // 解析旧协议，转换为新协议
    }
}
```

---

## 四、核心 API 设计

### 4.1 Router 主入口

```kotlin
// commonMain
interface Router {
    /**
     * 执行路由（页面跳转或 Action 执行）
     * @param url 路由 URL
     * @param params 附加参数（用于传递对象、覆盖 URL 参数等）
     * @param callback 路由结果回调
     */
    fun open(
        url: String,
        params: Map<String, Any?> = emptyMap(),
        callback: RouteCallback? = null
    )

    /**
     * 检查路由是否可达
     */
    fun canOpen(url: String): Boolean

    /**
     * 注册全局拦截器
     */
    fun addGlobalInterceptor(interceptor: RouteInterceptor)

    /**
     * 注册局部拦截器（针对特定路由模式）
     */
    fun addInterceptor(pattern: String, interceptor: RouteInterceptor)

    /**
     * 设置全局降级处理器
     */
    fun setFallbackHandler(handler: FallbackHandler)

    /**
     * 添加路由事件观察者
     */
    fun addObserver(observer: RouteObserver)
}
```

### 4.2 路由注册

```kotlin
// ==================== commonMain ====================

/**
 * 平台页面创建器接口（标记接口）
 * 各平台实现自己的 Creator 类
 */
interface PlatformPageCreator

/**
 * 页面目标：封装平台特定的创建器
 */
data class PageTarget(
    val creator: PlatformPageCreator
)

/**
 * 路由注册接口（核心）
 */
interface RouteRegistry {
    fun registerPage(pattern: String, config: PageRouteConfig)
    fun registerAction(actionName: String, handler: ActionHandler)
    fun registerAction(actionName: String, config: ActionRouteConfig, handler: ActionHandler)
    fun registerAll(routes: List<RouteDefinition>)
    fun isRegistered(pattern: String): Boolean
    fun unregisterPage(pattern: String): Boolean
    fun unregisterAction(actionName: String): Boolean
    val routeTable: RouteTable
}

data class PageRouteConfig(
    val target: PageTarget,
    val pageId: String? = null
)

// ==================== iosMain ====================

/**
 * iOS 页面创建器
 */
class IOSPageCreator(
    val factory: (Map<String, Any?>) -> UIViewController
) : PlatformPageCreator {
    fun createViewController(params: Map<String, Any?>): UIViewController
}

// iOS 扩展函数
fun RouteRegistry.registerPage(
    pattern: String,
    factory: (Map<String, Any?>) -> UIViewController
)

// ==================== androidMain ====================

/**
 * Android 页面创建器
 */
class AndroidPageCreator : PlatformPageCreator {
    val intentFactory: ((Context, Map<String, Any?>) -> Intent)?
    val activityClass: KClass<out Activity>?

    constructor(intentFactory: (Context, Map<String, Any?>) -> Intent)
    constructor(activityClass: KClass<out Activity>)

    fun createIntent(context: Context, params: Map<String, Any?>): Intent
}

// Android 扩展函数
fun RouteRegistry.registerPage(pattern: String, activityClass: KClass<out Activity>)
fun RouteRegistry.registerPage(pattern: String, intentFactory: (Context, Map<String, Any?>) -> Intent)
inline fun <reified T : Activity> RouteRegistry.registerPage(pattern: String)

// ==================== 声明式路由注册 ====================

// Android: PageRouteInfo 接口（用于 companion object 实现）
interface PageRouteInfo {
    val pattern: String
    fun createIntent(context: Context, params: Map<String, Any?>): Intent
}

fun RouteRegistry.registerPage(info: PageRouteInfo)
fun RouteRegistry.registerPages(vararg infos: PageRouteInfo)

// iOS: PageRouteDefinition 类（供 Swift 使用）
class PageRouteDefinition(
    val pattern: String,
    val factory: (Map<String, Any?>) -> UIViewController
)

fun RouteRegistry.registerPage(definition: PageRouteDefinition)
fun RouteRegistry.registerPages(definitions: List<PageRouteDefinition>)
```

**使用示例（iOS Swift）：**
```swift
// ==================== 方式1：通过工厂函数注册 ====================
registry.registerPage(pattern: "order/detail/:orderId") { params in
    OrderDetailViewController(params: params)
}

// ==================== 方式2：声明式路由注册（推荐）====================

// 1. 定义 Swift 协议
protocol PageRoutable {
    static var pattern: String { get }
    static func createPage(params: [String: Any?]) -> UIViewController
}

extension PageRoutable {
    static var routeDefinition: PageRouteDefinition {
        PageRouteDefinition(
            pattern: pattern,
            factory: { params in createPage(params: params) }
        )
    }
}

// 2. ViewController 实现协议
class OrderDetailViewController: UIViewController, PageRoutable {
    static var pattern: String { "order/detail/:orderId" }

    static func createPage(params: [String: Any?]) -> UIViewController {
        OrderDetailViewController(orderId: params["orderId"] as? String)
    }
}

class PaymentViewController: UIViewController, PageRoutable {
    static var pattern: String { "payment/checkout" }

    static func createPage(params: [String: Any?]) -> UIViewController {
        PaymentViewController()
    }
}

// 3. 注册（一行）
registry.registerPage(definition: OrderDetailViewController.routeDefinition)
registry.registerPage(definition: PaymentViewController.routeDefinition)

// 或者批量注册
registry.registerPages(definitions: [
    OrderDetailViewController.routeDefinition,
    PaymentViewController.routeDefinition,
    AccountSettingsViewController.routeDefinition
])

// 4. 可选：扩展 RouteRegistry 进一步简化
extension RouteRegistry {
    func registerPage<T: PageRoutable>(_ type: T.Type) {
        registerPage(definition: type.routeDefinition)
    }
}
// 使用：
registry.registerPage(OrderDetailViewController.self)
```

**使用示例（Android Kotlin）：**
```kotlin
// ==================== 方式1-3：基础注册方式 ====================

// 方式1：通过 Activity KClass 注册
registry.registerPage("order/detail/:orderId", OrderDetailActivity::class)

// 方式2：通过 reified 类型参数
registry.registerPage<OrderDetailActivity>("order/detail/:orderId")

// 方式3：通过 Intent 工厂函数
registry.registerPage("order/detail/:orderId") { context, params ->
    Intent(context, OrderDetailActivity::class.java).apply {
        putExtra("orderId", params["orderId"] as? String)
    }
}

// ==================== 方式4：声明式路由注册（推荐）====================

// 1. Activity 的 companion object 实现 PageRouteInfo 接口
class OrderDetailActivity : Activity() {
    companion object : PageRouteInfo {
        override val pattern = "order/detail/:orderId"

        override fun createIntent(context: Context, params: Map<String, Any?>): Intent {
            return Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("orderId", params["orderId"] as? String)
            }
        }
    }
}

class PaymentActivity : Activity() {
    companion object : PageRouteInfo {
        override val pattern = "payment/checkout"

        override fun createIntent(context: Context, params: Map<String, Any?>): Intent {
            return Intent(context, PaymentActivity::class.java)
        }
    }
}

// 2. 注册（一行）
registry.registerPage(OrderDetailActivity)
registry.registerPage(PaymentActivity)

// 或者批量注册
registry.registerPages(
    OrderDetailActivity,
    PaymentActivity,
    AccountSettingsActivity
)
```

### 4.3 拦截器

```kotlin
// commonMain
interface RouteInterceptor {
    /**
     * 拦截器优先级，数值越小优先级越高
     */
    val priority: Int get() = 100

    /**
     * 执行拦截
     * @param context 路由上下文
     * @param chain 拦截器链
     */
    suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult
}

interface InterceptorChain {
    /**
     * 继续执行下一个拦截器
     */
    suspend fun proceed(context: RouteContext): RouteResult
}

data class RouteContext(
    val url: String,
    val parsedRoute: ParsedRoute,
    val params: Map<String, Any?>,         // 合并后的参数（URL path/query + params）
    val source: RouteSource,               // 路由来源
    val timestamp: Long
)

sealed class RouteResult {
    data class Success(val context: RouteContext) : RouteResult()
    data class Redirect(val newUrl: String, val newParams: Map<String, Any?> = emptyMap()) : RouteResult()
    data class Blocked(val reason: String) : RouteResult()
    data class Error(val exception: Throwable) : RouteResult()
}
```

### 4.4 平台桥接接口

```kotlin
// commonMain - expect 声明
expect interface Navigator {
    /**
     * Push 跳转（默认导航方式）
     * @param pageId 页面标识符（来自 PageRouteConfig，可能是 pattern 或显式指定的值）
     * @param params 合并后的参数
     * @param options 导航选项
     */
    fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions)

    /**
     * Present 跳转（iOS modal / Android 可按需实现或忽略）
     */
    fun present(pageId: String, params: Map<String, Any?>, options: NavigationOptions)

    /**
     * 返回上一页
     */
    fun pop(result: Any? = null)
}

data class NavigationOptions(
    val animated: Boolean = true,
    val presentStyle: String? = null,  // iOS: fullScreen / pageSheet / formSheet 等
    val navMode: NavMode = NavMode.PUSH
)

// iOS 实现示例 - 直接调用底层 IAPRouter
// class IOSNavigator : Navigator {
//     override fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
//         IAPRouter.shared.open(pageId: pageId, params: params)
//     }
// }

// Android 实现示例
// class AndroidNavigator : Navigator {
//     override fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
//         AndroidRouter.open(pageId, params)
//     }
// }

expect interface ActionExecutor {
    fun execute(actionName: String, params: Map<String, Any?>, callback: ActionCallback?)
}
```

### 4.5 参数传递

采用**调用侧简单 + 接收侧类型安全**的设计模式。

#### 4.5.1 调用方式

```kotlin
// ==================== 调用方式（简单灵活）====================

// 方式1：纯 URL（简单场景）
router.open("iap://order/detail/123?from=list")

// 方式2：URL + params（传递对象或覆盖参数）
router.open(
    url = "iap://order/detail/123?from=list",
    params = mapOf("viewModel" to myViewModel)
)

// 方式3：平台特有参数（通过约定前缀）
router.open(
    url = "iap://order/detail/123",
    params = mapOf(
        "_ios_presentStyle" to "pageSheet",
        "_android_flags" to "FLAG_ACTIVITY_NEW_TASK"
    )
)
```

#### 4.5.2 参数合并优先级

当同一参数在多处定义时，优先级（高 → 低）：
1. `params` 字典（最高优先级）
2. URL query 参数
3. URL path 参数

#### 4.5.3 目标页面接收参数（类型安全）

```kotlin
// ==================== 目标页面定义参数模型 ====================

/**
 * 订单详情页参数（接收侧类型安全）
 */
data class OrderDetailParams(
    val orderId: String,
    val from: String?,
    val showHeader: Boolean
) {
    companion object {
        fun from(params: Map<String, Any?>) = OrderDetailParams(
            orderId = params.requireString("orderId"),
            from = params.optString("from"),
            showHeader = params.optBoolean("showHeader", default = true)
        )
    }
}

// ==================== 目标页面使用 ====================

class OrderDetailPage {
    fun onCreate(context: RouteContext) {
        // 方式1：使用类型安全的参数模型
        val params = OrderDetailParams.from(context.params)
        println(params.orderId)  // 类型安全
        println(params.from)     // 类型安全

        // 方式2：直接从 params 获取
        val viewModel = context.params["viewModel"] as? MyViewModel
    }
}
```

#### 4.5.4 参数提取工具

```kotlin
// 扩展函数，方便类型安全地获取参数
fun Map<String, Any?>.requireString(key: String): String =
    this[key]?.toString() ?: throw IllegalArgumentException("Missing required param: $key")

fun Map<String, Any?>.optString(key: String, default: String? = null): String? =
    this[key]?.toString() ?: default

fun Map<String, Any?>.optInt(key: String, default: Int = 0): Int =
    (this[key] as? Number)?.toInt() ?: this[key]?.toString()?.toIntOrNull() ?: default

fun Map<String, Any?>.optBoolean(key: String, default: Boolean = false): Boolean =
    (this[key] as? Boolean) ?: this[key]?.toString()?.toBooleanStrictOrNull() ?: default
```

### 4.6 回调与观察者

```kotlin
// commonMain
interface RouteCallback {
    fun onSuccess(context: RouteContext)
    fun onError(error: RouteError)
}

interface RouteObserver {
    fun onRouteStart(context: RouteContext)
    fun onRouteComplete(context: RouteContext, result: RouteResult)
    fun onInterceptorExecuted(interceptor: RouteInterceptor, context: RouteContext, durationMs: Long)
}

// 用于埋点的事件数据
data class RouteEvent(
    val url: String,
    val source: RouteSource,
    val result: RouteResultType,
    val durationMs: Long,
    val interceptorChain: List<String>,
    val timestamp: Long
)
```

---

## 五、降级策略

### 5.1 FallbackManager（推荐）

使用 `FallbackManager` 统一管理全局降级和基于 pattern 的降级规则：

```kotlin
val fallbackManager = FallbackManager()

// 设置全局降级（路由未找到时）
fallbackManager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))

// 设置全局错误降级
fallbackManager.setGlobalErrorFallback(FallbackAction.ShowError("Something went wrong"))

// 设置特定 pattern 的降级（支持通配符）
fallbackManager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))
fallbackManager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))

// 带条件的 pattern 降级
fallbackManager.addPatternFallback(
    pattern = "vip/*",
    condition = { !isVipUser() },
    action = FallbackAction.NavigateTo("iap://vip/upgrade")
)

// 注册到 Router
router.setFallbackHandler(fallbackManager)
```

### 5.2 FallbackHandler 接口

如果需要更灵活的降级逻辑，可以直接实现 `FallbackHandler` 接口：

```kotlin
router.setFallbackHandler(object : FallbackHandler {
    override fun onRouteNotFound(context: RouteContext): FallbackAction {
        // 可选：跳转到 404 页面、首页、或执行其他逻辑
        return FallbackAction.NavigateTo("iap://error/404?originalUrl=${context.url}")
    }

    override fun onRouteError(context: RouteContext, error: Throwable): FallbackAction {
        return FallbackAction.ShowError(error.message ?: "Unknown error")
    }
})
```

### 5.3 FallbackAction 类型

```kotlin
sealed class FallbackAction {
    data class NavigateTo(val url: String) : FallbackAction()
    data class ShowError(val message: String) : FallbackAction()
    data object Ignore : FallbackAction()
    data class Custom(val handler: () -> Unit) : FallbackAction()
}
```

---

## 六、分阶段实施计划

### Phase 1: 基础架构搭建（核心路由能力） ✅ 已完成

**目标**：搭建 KMP 基础架构，实现核心路由解析和匹配能力

**交付物**：
- [x] KMP 项目结构搭建（Kotlin 2.2.21 + Gradle 配套版本）
- [x] 协议解析器（Protocol Parser）
- [x] 路由匹配器（Route Matcher）- 支持灵活深度路径和 path 参数
- [x] 路由表管理（Route Registry）- 支持页面路由和 Action 路由注册
- [x] 基础 Navigator 接口定义（expect/actual）
- [x] 单元测试覆盖核心解析和匹配逻辑（127 个测试）

**关键设计决策**：
- 协议格式：`iap://{path}?params`（scheme 可由业务方自定义）
- 路由匹配支持通配符和 path 参数（如 `:orderId`）
- 路由表使用 Map 结构实现高效匹配

---

### Phase 2: 拦截器机制 ✅ 已完成

**目标**：实现完整的拦截器链机制

**交付物**：
- [x] 拦截器链（Interceptor Chain）实现
- [x] 全局拦截器支持
- [x] 局部拦截器支持（基于路由模式匹配，如 `payment/*`）
- [x] 异步拦截支持（suspend function）
- [x] 参数修改能力
- [x] 重定向能力
- [x] 拦截器优先级排序
- [x] 单元测试（27 个测试）

**示例拦截器**：
```kotlin
// 登录拦截器
class LoginInterceptor(private val authService: AuthService) : RouteInterceptor {
    override val priority = 10

    private val requireLoginPatterns = listOf("payment/*", "wallet/*", "settings/security")

    override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
        if (requireLoginPatterns.any { context.parsedRoute.matchesPattern(it) }) {
            if (!authService.isLoggedIn()) {
                return RouteResult.Redirect(
                    newUrl = "iap://auth/login",
                    newParams = mapOf("returnUrl" to context.url)
                )
            }
        }
        return chain.proceed(context)
    }
}
```

---

### Phase 3: 参数管理与类型安全

**目标**：实现类型安全的参数传递机制

**交付物**：
- [ ] 基础类型自动转换（String/Int/Long/Double/Boolean）
- [ ] 内存缓存管理器（用于对象引用传递）
- [ ] 参数校验器（必需参数检查）
- [ ] 参数提取工具类
- [ ] TTL 管理和自动清理
- [ ] 单元测试

**API 示例**：
```kotlin
// 注册时声明必需参数
routeRegistry.registerPage(
    pattern = "order/detail/:orderId",
    config = PageRouteConfig(
        pageId = "orderDetail",
        requiredParams = listOf("orderId")
    )
)

// 类型安全的参数获取
class OrderDetailParams(params: Map<String, Any?>) {
    val orderId: String = params.requireString("orderId")
    val from: String? = params.optString("from")
    val showHeader: Boolean = params.optBoolean("showHeader", default = true)
}
```

---

### Phase 4: 降级与回调机制

**目标**：实现完整的降级策略和事件回调

**交付物**：
- [ ] 全局 Fallback 处理器
- [ ] 单路由降级配置
- [ ] 路由结果回调（RouteCallback）
- [ ] 路由事件观察者（RouteObserver）
- [ ] 事件数据结构（用于埋点）
- [ ] Logger 接口（delegate 模式，由业务实现）
- [ ] 单元测试

---

### Phase 5: iOS 平台集成

**目标**：完成 iOS 平台的 actual 实现和业务集成

**交付物**：
- [ ] iOS Navigator actual 实现
  - push 实现（直接调用 IAPRouter，传入 pageId 和参数）
  - present 实现（支持 presentationStyle 参数）
- [ ] iOS ActionExecutor actual 实现
- [ ] 旧协议适配器（LegacyRouteAdapter）
- [ ] 现有拦截器迁移到新 SDK
- [ ] 新路由使用新 SDK，旧路由保持现状
- [ ] 集成测试

**iOS 导航整改策略**：
- 新注册的路由统一使用 SDK 默认导航逻辑
- 现有的 switch-case 和 useDefaultNavi 逻辑暂时保留
- 后续逐步将旧页面迁移到新路由方式

---

### Phase 6: Android 平台集成

**目标**：完成 Android 平台的 actual 实现和业务集成

**交付物**：
- [ ] Android Navigator actual 实现
  - push 实现（直接调用 AndroidRouter，传入 pageId 和参数）
  - present 实现（可按需实现或忽略）
- [ ] Android ActionExecutor actual 实现
- [ ] 旧协议适配器（LegacyRouteAdapter）
- [ ] 现有拦截器迁移到新 SDK
- [ ] 集成测试

---

### Phase 7: 迁移工具与文档

**目标**：提供迁移支持和完善文档

**交付物**：
- [ ] 旧协议 -> 新协议映射配置
- [ ] 迁移检查脚本（检测代码中的旧协议调用）
- [ ] Deprecation 警告机制
- [ ] SDK 接入文档
- [ ] API Reference 文档
- [ ] 最佳实践指南
- [ ] 迁移指南

---

### Phase 8: 优化与稳定

**目标**：性能优化和稳定性保障

**交付物**：
- [ ] 路由匹配性能优化
- [ ] 内存占用优化
- [ ] 边界情况处理完善
- [ ] 异常场景覆盖测试
- [ ] 灰度发布方案
- [ ] 监控告警接入

---

## 七、风险与缓解措施

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| KMP 兼容性问题 | 双端表现不一致 | 充分的单元测试 + 集成测试覆盖 |
| 旧协议迁移周期长 | 两套协议长期并存 | 明确迁移时间表，提供自动化迁移工具 |
| 拦截器异步复杂度 | 死锁、超时问题 | 设置拦截器超时机制，提供调试工具 |
| iOS 导航栈兼容 | 新旧逻辑冲突 | 渐进式迁移，新页面使用新逻辑 |
| API 稳定性 | 频繁变更影响业务 | API 评审机制，发布前充分验证 |

---

## 八、技术选型

| 技术点 | 选型 | 说明 |
|--------|------|------|
| KMP 版本 | 2.2.21 | 按团队要求 |
| 协程 | kotlinx.coroutines 1.10.2 | 异步拦截器支持 |
| URL 编解码 | ktor-http 3.2.3 | 跨平台 URL 编解码 |
| 序列化 | kotlinx.serialization | 参数序列化（可选） |
| 测试 | kotlin.test | 跨平台单元测试 |
| 构建 | Gradle 8.x | 配套 KMP 2.2.21 |
| Android | AGP 8.10.0 | Android Gradle Plugin |

---

## 九、成功指标

| 指标 | 目标 |
|------|------|
| 协议一致性 | 双端使用统一的新协议格式 |
| 拦截器覆盖 | 登录、权限等核心拦截逻辑双端统一 |
| 降级覆盖率 | 核心页面 100% 配置降级策略 |
| 测试覆盖率 | KMP Shared 核心模块 > 80% |
| 旧协议迁移 | Phase 7 完成后，新代码全部使用新协议 |

---

## 十、附录

### A. 路由模式匹配规则

| 模式 | 示例 URL | 是否匹配 |
|------|----------|----------|
| `order/detail/:id` | `order/detail/123` | ✅ 匹配，id=123 |
| `order/detail/:id` | `order/detail/123/extra` | ❌ 不匹配 |
| `payment/*` | `payment/card/bindNew` | ✅ 匹配 |
| `fx/:pair/chart` | `fx/USDCNY/chart` | ✅ 匹配，pair=USDCNY |

### B. 拦截器执行顺序

```
Request → GlobalInterceptor(priority=10)
        → GlobalInterceptor(priority=50)
        → LocalInterceptor(pattern match, priority=100)
        → RouteExecution
        → Response
```

### C. 约定的平台特有参数前缀

| 前缀 | 说明 | 示例 |
|------|------|------|
| `_ios_` | iOS 平台特有参数 | `_ios_presentStyle` |
| `_android_` | Android 平台特有参数 | `_android_flags` |
| `_` | 通用导航参数 | `_navMode`, `_animated` |

---

*文档版本：v1.8*
*创建日期：2025-01-19*
*最后更新：2026-01-25*

---

## 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.8 | 2026-01-25 | 简化降级机制：移除 per-page fallback（PageRouteConfig、PageRouteInfo、PageRouteDefinition 不再包含 fallback）；新增 FallbackManager 支持全局降级和基于 pattern 的降级规则（如 user/* 可统一配置降级）；删除 FallbackConfig 类 |
| v1.7 | 2026-01-25 | 平台注册 API 完全分离：commonMain 只定义 PlatformPageCreator 接口和 PageTarget；iosMain 提供 IOSPageCreator + 工厂函数注册 + PageRouteDefinition 声明式注册；androidMain 提供 AndroidPageCreator + KClass/Intent 注册 + PageRouteInfo 声明式注册；删除 jvmMain；移除 Synchronization（所有操作约定主线程执行）；新增 iosTest 和 androidUnitTest 测试平台注册 API |
| v1.6 | 2026-01-23 | 统一路由注册 API：新增类型安全的 PlatformPage (expect/actual)、PageBuilder、PageTarget；支持 builder 和 class 两种注册方式；iOS 返回 UIViewController，Android 返回 Activity |
| v1.5 | 2026-01-22 | 简化路由注册架构：pageId 改为可选（默认使用 pattern）；移除 PageFactory 接口（平台已有页面创建逻辑）；Navigator 直接调用底层路由 |
| v1.4 | 2026-01-21 | Router.open 参数从 `extras` 改为 `params`；移除 NavigationOptions.extras |
| v1.3 | 2025-01-20 | 简化 API 设计：移除 RouteParams 接口，Router.open 只保留 url + params；移除 PageRouteConfig 的 requiredParams 和 metadata；移除 RouteContext 的 objectStore；类型安全改为接收侧实现 |
| v1.2 | 2025-01-20 | 添加进度追踪部分；协议 scheme 从 `worldfirst` 改为 `iap`；添加当前代码结构；更新技术选型（添加 ktor-http）；Phase 1 标记为已完成 |
| v1.1 | 2025-01-19 | API 命名改为 `open`；移除 `replace` 能力；参数传递改为混合模式（RouteParams + extras）；对象生命周期改为路由完成后清理 |
| v1.0 | 2025-01-19 | 初始版本 |
