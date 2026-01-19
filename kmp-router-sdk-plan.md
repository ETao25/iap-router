# KMP Router SDK 设计与实施规划

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
| **Navigator** | 实际执行页面跳转（push/present） | Platform |
| **PageFactory** | 创建目标页面实例 | Platform |
| **ActionExecutor** | 执行 Action 路由的具体逻辑 | Platform |
| **Legacy Adapter** | 旧协议到新协议的转换 | Platform/Business |

---

## 三、协议设计

### 3.1 新协议规范

采用 RESTful 风格，支持灵活深度路径：

```
worldfirst://{path}[/:pathParam]?queryParam=value
```

#### 3.1.1 页面路由

```
# 基础格式
worldfirst://account/settings?tab=security

# 带 path 参数
worldfirst://order/detail/:orderId?from=list
worldfirst://fx/trade/:pairId/chart?period=1d

# 多级路径
worldfirst://payment/card/bindNew?source=checkout
```

#### 3.1.2 Action 路由

```
# Action 统一使用 action 前缀
worldfirst://action/{actionName}[/:pathParam]?queryParam=value

# 示例
worldfirst://action/showPopup?type=confirm&message=xxx
worldfirst://action/share?content=xxx&platform=wechat
worldfirst://action/copyText?content=xxx
```

#### 3.1.3 导航模式参数

通过 query 参数指定导航模式（各平台按需支持）：

| 参数 | 说明 | 示例值 |
|------|------|--------|
| `_navMode` | 导航模式 | push / present |
| `_presentStyle` | iOS Present 样式 | fullScreen / pageSheet / formSheet |
| `_animated` | 是否动画 | true / false |

```
worldfirst://order/detail/:orderId?_navMode=present&_presentStyle=pageSheet
```

### 3.2 旧协议兼容

**旧协议格式**：
```
worldfirst://app?pageId=xxx&param1=value1
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
     * @param params 类型安全参数（可选，用于编译期检查）
     * @param extras 额外参数字典（用于 ViewModel、平台特有参数等）
     * @param callback 路由结果回调
     */
    fun open(
        url: String,
        params: RouteParams? = null,
        extras: Map<String, Any?> = emptyMap(),
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

/**
 * 类型安全参数接口
 * 各路由可定义对应的参数类实现此接口
 */
interface RouteParams {
    fun toMap(): Map<String, Any?>
}
```

### 4.2 路由注册

```kotlin
// commonMain
interface RouteRegistry {
    /**
     * 注册页面路由
     * @param pattern 路由模式，如 "order/detail/:orderId"
     * @param config 路由配置
     */
    fun registerPage(pattern: String, config: PageRouteConfig)

    /**
     * 注册 Action 路由
     * @param actionName Action 名称
     * @param handler Action 处理器
     */
    fun registerAction(actionName: String, handler: ActionHandler)

    /**
     * 批量注册
     */
    fun registerAll(routes: List<RouteDefinition>)
}

data class PageRouteConfig(
    val pageId: String,                          // 页面业务标识符，用于：
                                                  // 1. 与 VC/Activity 类名或注册标识对应
                                                  // 2. 埋点、日志中的页面标识
                                                  // 3. 白名单/黑名单控制
                                                  // 注：pageId 与 pattern 独立，pattern 是 URL 路径匹配模式
    val requiredParams: List<String> = emptyList(), // 必需参数
    val fallback: FallbackConfig? = null,        // 单路由降级配置
    val metadata: Map<String, Any?> = emptyMap() // 扩展元数据
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
    val params: Map<String, Any?>,         // 合并后的参数（只读）
    val source: RouteSource,               // 路由来源
    val timestamp: Long,
    internal val objectStore: ObjectStore  // 内部对象存储（用于 extras 中的对象引用）
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
    val extras: Map<String, Any?> = emptyMap()  // 平台特有参数
)

expect interface PageFactory {
    fun canCreate(pageId: String): Boolean
    fun create(pageId: String, params: Map<String, Any?>): Any // Platform-specific page type
}

expect interface ActionExecutor {
    fun execute(actionName: String, params: Map<String, Any?>, callback: ActionCallback?)
}
```

### 4.5 参数传递

采用**混合模式**：类型安全参数 + 自由字典，兼顾类型安全和灵活性。

```kotlin
// ==================== 类型安全参数定义 ====================

/**
 * 示例：订单详情页参数
 */
data class OrderDetailParams(
    val orderId: String,
    val from: String? = null,
    val showHeader: Boolean = true
) : RouteParams {
    override fun toMap() = mapOf(
        "orderId" to orderId,
        "from" to from,
        "showHeader" to showHeader
    )
}

// ==================== 调用方式 ====================

// 方式1：类型安全参数（推荐，有编译期检查）
router.open(
    url = "worldfirst://order/detail/123",
    params = OrderDetailParams(orderId = "123", from = "list")
)

// 方式2：纯 URL 参数（简单场景）
router.open(url = "worldfirst://order/detail/123?from=list")

// 方式3：传递 ViewModel 等对象（通过 extras）
router.open(
    url = "worldfirst://order/detail/123",
    params = OrderDetailParams(orderId = "123"),
    extras = mapOf("viewModel" to myViewModel)  // 对象引用
)

// 方式4：平台特有参数
router.open(
    url = "worldfirst://order/detail/123",
    extras = mapOf(
        "_ios_presentStyle" to "pageSheet",  // iOS 特有
        "_android_flags" to "FLAG_ACTIVITY_NEW_TASK"  // Android 特有
    )
)

// ==================== 目标页面获取参数 ====================

// SDK 会将 params.toMap() + URL 参数 + extras 合并后传递给目标页面
// 目标页面通过 RouteContext 获取合并后的参数
class OrderDetailPage {
    fun onCreate(context: RouteContext) {
        val orderId = context.params.requireString("orderId")
        val viewModel = context.params["viewModel"] as? MyViewModel
    }
}
```

#### 4.5.1 参数合并优先级

当同一参数在多处定义时，优先级（高 → 低）：
1. `extras` 字典（最高优先级）
2. `params.toMap()` 类型安全参数
3. URL query 参数
4. URL path 参数
5. 路由默认配置（最低）

#### 4.5.2 对象生命周期管理

`extras` 中的对象引用采用**路由完成后自动清理**策略：

```kotlin
// commonMain
data class RouteContext(
    val url: String,
    val parsedRoute: ParsedRoute,
    val params: Map<String, Any?>,       // 合并后的参数（只读）
    val source: RouteSource,
    val timestamp: Long,
    internal val objectStore: ObjectStore  // 内部对象存储
) {
    /**
     * 获取对象引用（会从 objectStore 中取出）
     * 路由完成后 objectStore 会被清理
     */
    inline fun <reified T> getObject(key: String): T? = objectStore.get(key) as? T
}

// 路由执行流程：
// 1. router.open() 调用时，extras 中的对象存入 RouteContext.objectStore
// 2. 目标页面通过 context.getObject() 获取对象
// 3. 路由完成（目标页面 onCreate 执行完毕）后，objectStore 自动清理
// 4. 如目标页面需要长期持有对象，应在 onCreate 中保存引用
```

#### 4.5.3 参数提取工具

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

### 5.1 全局 Fallback

```kotlin
router.setFallbackHandler(object : FallbackHandler {
    override fun onRouteNotFound(context: RouteContext): FallbackAction {
        // 可选：跳转到 404 页面、首页、或执行其他逻辑
        return FallbackAction.NavigateTo("worldfirst://error/404?originalUrl=${context.url}")
    }

    override fun onRouteError(context: RouteContext, error: Throwable): FallbackAction {
        return FallbackAction.ShowError(error.message ?: "Unknown error")
    }
})

sealed class FallbackAction {
    data class NavigateTo(val url: String) : FallbackAction()
    data class ShowError(val message: String) : FallbackAction()
    object Ignore : FallbackAction()
    data class Custom(val handler: () -> Unit) : FallbackAction()
}
```

### 5.2 单路由降级

```kotlin
routeRegistry.registerPage(
    pattern = "payment/newFeature",
    config = PageRouteConfig(
        pageId = "paymentNewFeature",
        fallback = FallbackConfig(
            condition = { appVersion < "5.0.0" },  // 版本判断
            action = FallbackAction.NavigateTo("worldfirst://h5/payment/newFeature")
        )
    )
)
```

---

## 六、分阶段实施计划

### Phase 1: 基础架构搭建（核心路由能力）

**目标**：搭建 KMP 基础架构，实现核心路由解析和匹配能力

**交付物**：
- [ ] KMP 项目结构搭建（Kotlin 2.2.21 + Gradle 配套版本）
- [ ] 协议解析器（Protocol Parser）
- [ ] 路由匹配器（Route Matcher）- 支持灵活深度路径和 path 参数
- [ ] 路由表管理（Route Registry）- 支持页面路由和 Action 路由注册
- [ ] 基础 Navigator 接口定义（expect/actual）
- [ ] 单元测试覆盖核心解析和匹配逻辑

**关键设计决策**：
- 协议格式：`worldfirst://{path}?params`
- 路由匹配支持通配符和 path 参数（如 `:orderId`）
- 路由表使用 Trie 树或 Map 结构实现高效匹配

---

### Phase 2: 拦截器机制

**目标**：实现完整的拦截器链机制

**交付物**：
- [ ] 拦截器链（Interceptor Chain）实现
- [ ] 全局拦截器支持
- [ ] 局部拦截器支持（基于路由模式匹配，如 `payment/*`）
- [ ] 异步拦截支持（suspend function / Deferred）
- [ ] 参数修改能力
- [ ] 重定向能力
- [ ] 拦截器优先级排序
- [ ] 单元测试

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
                    newUrl = "worldfirst://auth/login",
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
  - push 实现（使用 SDK 默认 TopVC 查找逻辑）
  - present 实现（支持 presentationStyle 参数）
- [ ] iOS PageFactory actual 实现
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
  - push 实现（Activity 跳转）
  - present 实现（可按需实现或忽略）
- [ ] Android PageFactory actual 实现
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
| 协程 | kotlinx.coroutines | 异步拦截器支持 |
| 序列化 | kotlinx.serialization | 参数序列化（可选） |
| 测试 | kotlin.test | 跨平台单元测试 |
| 构建 | Gradle 8.x | 配套 KMP 2.2.21 |

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

*文档版本：v1.1*
*创建日期：2025-01-19*
*最后更新：2025-01-19*

---

## 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.1 | 2025-01-19 | API 命名改为 `open`；移除 `replace` 能力；参数传递改为混合模式（RouteParams + extras）；对象生命周期改为路由完成后清理 |
| v1.0 | 2025-01-19 | 初始版本 |
