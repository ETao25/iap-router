# KMP Router SDK 工程规范

## 一、工程目录结构

### 1.1 整体结构

采用**单模块结构**，所有代码（commonMain/androidMain/iosMain）在同一个模块中，简化构建和依赖管理。

```
kmp-router/
├── build.gradle.kts                    # 项目构建配置
├── settings.gradle.kts                 # 项目设置
├── gradle.properties                   # Gradle 属性配置
│
├── src/
│   ├── commonMain/                     # 跨平台共享代码
│   │   └── kotlin/
│   │       └── com/worldfirst/router/
│   │           ├── Router.kt                    # Router 主入口接口
│   │           ├── RouteRegistry.kt             # 路由注册接口
│   │           ├── core/                        # 核心实现
│   │           │   ├── RouterImpl.kt
│   │           │   ├── ProtocolParser.kt        # 协议解析
│   │           │   ├── RouteMatcher.kt          # 路由匹配
│   │           │   └── RouteTable.kt            # 路由表
│   │           ├── interceptor/                 # 拦截器
│   │           │   ├── RouteInterceptor.kt
│   │           │   ├── InterceptorChain.kt
│   │           │   └── InterceptorChainImpl.kt
│   │           ├── params/                      # 参数处理
│   │           │   ├── RouteParams.kt
│   │           │   ├── ParamsExtensions.kt
│   │           │   └── ObjectStore.kt
│   │           ├── fallback/                    # 降级处理
│   │           │   ├── FallbackHandler.kt
│   │           │   └── FallbackAction.kt
│   │           ├── observer/                    # 观察者/回调
│   │           │   ├── RouteObserver.kt
│   │           │   ├── RouteCallback.kt
│   │           │   └── RouteEvent.kt
│   │           ├── model/                       # 数据模型
│   │           │   ├── RouteContext.kt
│   │           │   ├── RouteResult.kt
│   │           │   ├── ParsedRoute.kt
│   │           │   └── PageRouteConfig.kt
│   │           ├── platform/                    # 平台桥接接口 (expect)
│   │           │   ├── Navigator.kt
│   │           │   ├── PageFactory.kt
│   │           │   └── ActionExecutor.kt
│   │           ├── exception/                   # 异常定义
│   │           │   ├── RouterException.kt
│   │           │   └── RouteNotFoundException.kt
│   │           └── util/                        # 工具类
│   │               ├── Logger.kt
│   │               └── UrlEncoder.kt
│   │
│   ├── commonTest/                     # 跨平台单元测试（核心）
│   │   └── kotlin/
│   │       └── com/worldfirst/router/
│   │           ├── core/
│   │           │   ├── ProtocolParserTest.kt
│   │           │   ├── RouteMatcherTest.kt
│   │           │   └── RouteTableTest.kt
│   │           ├── interceptor/
│   │           │   └── InterceptorChainTest.kt
│   │           ├── params/
│   │           │   ├── ParamsExtensionsTest.kt
│   │           │   └── ObjectStoreTest.kt
│   │           └── testutil/                    # 测试工具
│   │               ├── TestInterceptor.kt
│   │               └── TestRouteContext.kt
│   │
│   ├── androidMain/                    # Android 平台实现 (actual)
│   │   └── kotlin/
│   │       └── com/worldfirst/router/
│   │           └── platform/
│   │               ├── AndroidNavigator.kt
│   │               ├── AndroidPageFactory.kt
│   │               └── AndroidActionExecutor.kt
│   │
│   ├── androidUnitTest/                # Android 平台特定测试
│   │   └── kotlin/
│   │       └── com/worldfirst/router/
│   │           └── platform/
│   │               └── AndroidNavigatorTest.kt
│   │
│   ├── iosMain/                        # iOS 平台实现 (actual)
│   │   └── kotlin/
│   │       └── com/worldfirst/router/
│   │           ├── platform/
│   │           │   ├── IosNavigator.kt
│   │           │   ├── IosPageFactory.kt
│   │           │   └── IosActionExecutor.kt
│   │           └── IosExceptionHandler.kt      # iOS 异常处理
│   │
│   └── iosTest/                        # iOS 平台特定测试
│       └── kotlin/
│           └── com/worldfirst/router/
│               └── platform/
│                   └── IosNavigatorTest.kt
│
└── sample/                             # 示例工程（可选）
    ├── android-app/
    └── ios-app/
```

### 1.2 单模块设计说明

**为什么采用单模块？**

| 考虑因素 | 说明 |
|----------|------|
| iOS Framework 限制 | iOS 只能合并输出一个 XCFramework，多模块反而增加复杂度 |
| 依赖简化 | 单模块避免模块间依赖配置，减少构建问题 |
| 代码组织 | 通过包结构（package）而非模块来组织代码，同样清晰 |
| 产物输出 | 一个模块直接输出 XCFramework (iOS) 和 AAR (Android) |

**输出产物**：

| 平台 | 产物 | 说明 |
|------|------|------|
| iOS | XCFramework | 包含 arm64 + simulator (arm64/x86_64) |
| Android | AAR | 标准 Android Library |

### 1.3 包命名规范

```
com.worldfirst.router                   # 根包
com.worldfirst.router.core              # 核心实现
com.worldfirst.router.interceptor       # 拦截器
com.worldfirst.router.params            # 参数处理
com.worldfirst.router.fallback          # 降级处理
com.worldfirst.router.observer          # 观察者/事件
com.worldfirst.router.model             # 数据模型
com.worldfirst.router.platform          # 平台桥接
com.worldfirst.router.exception         # 异常
com.worldfirst.router.util              # 工具
```

---

## 二、异常处理规范（核心）

### 2.1 设计原则

**核心目标：iOS/Android 接入时绝不 Crash**

基于 KMP 在 iOS 上的异常行为特点，制定以下策略：

| 场景 | 行为 | 处理策略 |
|------|------|----------|
| Swift 调用 Kotlin 方法抛异常 | 默认会 Crash | **必须**使用 `@Throws` 标注 |
| Kotlin 内部协程抛异常 | 会触发 UnhandledException | 设置 `setUnhandledExceptionHook` + 不 terminate |
| Android 异常 | 标准 try-catch | 正常捕获处理 |

### 2.2 异常层次结构

```kotlin
// com/worldfirst/router/exception/RouterException.kt

/**
 * 路由 SDK 异常基类
 * 所有 SDK 异常都应继承此类
 */
open class RouterException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 路由未找到异常
 */
class RouteNotFoundException(
    val url: String,
    message: String = "Route not found: $url"
) : RouterException(message)

/**
 * 路由被拦截异常
 */
class RouteBlockedException(
    val url: String,
    val reason: String,
    message: String = "Route blocked: $url, reason: $reason"
) : RouterException(message)

/**
 * 参数校验异常
 */
class ParamValidationException(
    val paramName: String,
    message: String = "Invalid or missing param: $paramName"
) : RouterException(message)

/**
 * 导航执行异常
 */
class NavigationException(
    message: String,
    cause: Throwable? = null
) : RouterException(message, cause)
```

### 2.3 公开 API 异常处理规范

#### 2.3.1 对外暴露的 API 必须使用 `@Throws`

```kotlin
// ==================== 正确示例 ====================

interface Router {
    /**
     * 执行路由
     * @throws RouterException 路由执行失败时抛出
     */
    @Throws(RouterException::class)
    fun open(
        url: String,
        params: RouteParams? = null,
        extras: Map<String, Any?> = emptyMap(),
        callback: RouteCallback? = null
    )

    /**
     * 检查路由是否可达
     * 此方法不抛异常，返回 false 表示不可达
     */
    fun canOpen(url: String): Boolean  // 无 @Throws，保证不抛异常
}

// ==================== 错误示例 ====================

// ❌ 错误：公开 API 未标注 @Throws，iOS 调用时可能 Crash
interface Router {
    fun open(url: String)  // 内部可能抛异常，但未标注
}
```

#### 2.3.2 iOS 调用方式

```swift
// Swift 调用 Kotlin API
do {
    try router.open(url: "worldfirst://order/detail/123")
} catch let error as RouterException {
    // 处理路由异常
    print("Route error: \(error.message)")
} catch {
    // 处理其他异常
    print("Unknown error: \(error)")
}
```

### 2.4 内部实现异常处理规范

#### 2.4.1 内部方法分类

| 类型 | 是否可能抛异常 | 处理方式 |
|------|---------------|----------|
| 纯计算方法 | 否 | 返回可空类型或 Result |
| IO/网络操作 | 是 | 内部 try-catch，转换为 Result 或回调 |
| 协程方法 | 是 | 使用 CoroutineExceptionHandler |

#### 2.4.2 使用 Result 包装可能失败的操作

```kotlin
// com/worldfirst/router/core/ProtocolParser.kt

object ProtocolParser {
    /**
     * 解析路由 URL
     * 使用 Result 包装，调用方通过 getOrNull/getOrElse 处理
     */
    fun parse(url: String): Result<ParsedRoute> {
        return runCatching {
            // 解析逻辑
            val uri = parseUri(url)
            ParsedRoute(
                scheme = uri.scheme ?: throw IllegalArgumentException("Missing scheme"),
                path = uri.path ?: "",
                queryParams = parseQueryParams(uri.query),
                pathParams = mutableMapOf()
            )
        }
    }

    /**
     * 安全解析，失败返回 null
     */
    fun parseOrNull(url: String): ParsedRoute? = parse(url).getOrNull()
}

// 调用方
val result = ProtocolParser.parse(url)
result.onSuccess { parsedRoute ->
    // 处理成功
}.onFailure { error ->
    // 处理失败，记录日志
    logger.error("Failed to parse url: $url", error)
}
```

#### 2.4.3 禁止在内部吞掉异常

```kotlin
// ==================== 错误示例 ====================

// ❌ 错误：吞掉异常，问题难以排查
fun doSomething() {
    try {
        riskyOperation()
    } catch (e: Exception) {
        // 什么都不做
    }
}

// ==================== 正确示例 ====================

// ✅ 正确：记录日志 + 返回安全默认值
fun doSomething(): Result<Unit> {
    return runCatching {
        riskyOperation()
    }.onFailure { e ->
        logger.error("doSomething failed", e)
    }
}

// ✅ 正确：向上传递异常（内部方法）
internal fun doSomethingInternal() {
    riskyOperation()  // 异常向上传递
}
```

### 2.5 协程异常处理规范

#### 2.5.1 iOS UnhandledExceptionHook 设置

```kotlin
// iosMain/kotlin/com/worldfirst/router/exception/IosExceptionHandler.kt

import kotlin.native.concurrent.freeze

object IosExceptionHandler {
    private var isInitialized = false

    /**
     * 初始化 iOS 异常处理
     * 必须在 SDK 初始化时调用
     */
    fun initialize(onException: ((Throwable) -> Unit)? = null) {
        if (isInitialized) return
        isInitialized = true

        setUnhandledExceptionHook { throwable ->
            // 1. 记录异常日志
            RouterLogger.error("Unhandled exception in Kotlin", throwable)

            // 2. 通知业务层（可选）
            onException?.invoke(throwable)

            // 3. 关键：不调用 terminate，避免 Crash
            // 对于 Kotlin 内部发起的协程异常，这样可以避免崩溃
        }
    }
}
```

#### 2.5.2 协程 CoroutineExceptionHandler

```kotlin
// commonMain/kotlin/com/worldfirst/router/util/CoroutineUtils.kt

import kotlinx.coroutines.*

/**
 * SDK 内部协程使用的 CoroutineScope
 * 统一异常处理
 */
internal val routerScope: CoroutineScope by lazy {
    CoroutineScope(
        SupervisorJob() +
        Dispatchers.Default +
        CoroutineExceptionHandler { _, throwable ->
            RouterLogger.error("Coroutine exception", throwable)
            // 不 rethrow，避免 Crash
        }
    )
}

/**
 * 安全启动协程
 */
internal fun safelaunch(block: suspend CoroutineScope.() -> Unit): Job {
    return routerScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e  // 协程取消要重新抛出
        } catch (e: Exception) {
            RouterLogger.error("Safe launch exception", e)
        }
    }
}
```

#### 2.5.3 异步拦截器异常处理

```kotlin
// commonMain/kotlin/com/worldfirst/router/interceptor/InterceptorChainImpl.kt

internal class InterceptorChainImpl(
    private val interceptors: List<RouteInterceptor>,
    private val index: Int = 0
) : InterceptorChain {

    override suspend fun proceed(context: RouteContext): RouteResult {
        if (index >= interceptors.size) {
            return RouteResult.Success(context)
        }

        val interceptor = interceptors[index]
        val nextChain = InterceptorChainImpl(interceptors, index + 1)

        return try {
            // 设置超时，避免拦截器阻塞
            withTimeout(INTERCEPTOR_TIMEOUT_MS) {
                interceptor.intercept(context, nextChain)
            }
        } catch (e: TimeoutCancellationException) {
            RouterLogger.error("Interceptor timeout: ${interceptor::class.simpleName}", e)
            // 超时后继续执行下一个拦截器
            nextChain.proceed(context)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RouterLogger.error("Interceptor exception: ${interceptor::class.simpleName}", e)
            RouteResult.Error(e)
        }
    }

    companion object {
        private const val INTERCEPTOR_TIMEOUT_MS = 5000L
    }
}
```

### 2.6 异常处理检查清单

在 Code Review 时，检查以下项目：

| 检查项 | 要求 |
|--------|------|
| 公开 API | 必须有 `@Throws` 或保证不抛异常 |
| 内部异常 | 使用 `Result` 包装或明确向上传递 |
| 协程 | 使用 `routerScope` 或自带 `CoroutineExceptionHandler` |
| 日志 | 异常必须记录日志，包含 throwable |
| 空检查 | 使用 `?.` 或 `?: default`，避免 NPE |
| 类型转换 | 使用 `as?` 安全转换，不用 `as` |

---

## 三、代码风格规范

### 3.1 命名规范

#### 3.1.1 类/接口命名

```kotlin
// 接口：描述能力，不加 I 前缀
interface Router { }
interface RouteInterceptor { }

// 实现类：加 Impl 后缀或具体描述
class RouterImpl : Router { }
class LoginInterceptor : RouteInterceptor { }

// 数据类：名词，描述数据内容
data class RouteContext(...)
data class ParsedRoute(...)

// 异常类：加 Exception 后缀
class RouterException(...)
class RouteNotFoundException(...)

// 枚举/密封类：名词
enum class RouteSource { INTERNAL, DEEPLINK, PUSH }
sealed class RouteResult { }
```

#### 3.1.2 函数命名

```kotlin
// 动词开头，描述行为
fun open(url: String)           // 执行动作
fun canOpen(url: String)        // 判断能力，返回 Boolean
fun parse(url: String)          // 解析/转换
fun findRoute(path: String)     // 查找

// 返回可空类型，使用 OrNull 后缀
fun parseOrNull(url: String): ParsedRoute?
fun findRouteOrNull(path: String): RouteConfig?

// 内部方法，使用 internal 修饰
internal fun doExecute(context: RouteContext)
```

#### 3.1.3 变量命名

```kotlin
// 使用有意义的名称，避免单字母
val routeContext: RouteContext      // ✅
val ctx: RouteContext               // ❌ 太短
val routeContextForNavigation: ...  // ❌ 太长

// Boolean 变量使用 is/has/can 开头
val isLoggedIn: Boolean
val hasPermission: Boolean
val canNavigate: Boolean

// 集合使用复数
val interceptors: List<RouteInterceptor>
val routes: Map<String, RouteConfig>

// 常量使用 SCREAMING_SNAKE_CASE
companion object {
    private const val DEFAULT_TIMEOUT_MS = 5000L
    private const val MAX_RETRY_COUNT = 3
}
```

### 3.2 类型安全规范

#### 3.2.1 避免使用 `Any` 和 `Any?`

```kotlin
// ==================== 尽量避免 ====================

// ❌ 类型不安全
fun process(data: Any) { }

// ==================== 推荐 ====================

// ✅ 使用泛型
fun <T> process(data: T) { }

// ✅ 使用密封类/接口
sealed class RouteTarget {
    data class Page(val pageId: String) : RouteTarget()
    data class Action(val actionName: String) : RouteTarget()
}

// 特殊情况：params 字典确实需要 Any?，但要在获取时做类型检查
val params: Map<String, Any?>  // OK，但使用时需类型安全获取
val orderId = params.requireString("orderId")  // 类型安全扩展
```

#### 3.2.2 安全类型转换

```kotlin
// ==================== 正确示例 ====================

// ✅ 安全转换
val number = value as? Int ?: 0
val list = value as? List<*> ?: emptyList()

// ✅ 使用 when 进行类型匹配
when (result) {
    is RouteResult.Success -> handleSuccess(result.context)
    is RouteResult.Error -> handleError(result.exception)
    is RouteResult.Blocked -> handleBlocked(result.reason)
    is RouteResult.Redirect -> handleRedirect(result.newUrl)
}

// ==================== 错误示例 ====================

// ❌ 不安全转换，可能 ClassCastException
val number = value as Int
```

### 3.3 空安全规范

#### 3.3.1 优先使用非空类型

```kotlin
// ==================== 推荐 ====================

// ✅ 非空参数
fun open(url: String)  // url 不会为 null

// ✅ 有默认值
fun open(url: String, callback: RouteCallback? = null)

// ==================== 避免 ====================

// ❌ 无意义的可空
fun open(url: String?)  // 如果 null 没有业务意义，不要用可空
```

#### 3.3.2 空检查最佳实践

```kotlin
// ✅ 使用 let 进行空安全调用
callback?.let { it.onSuccess(context) }

// ✅ 使用 Elvis 提供默认值
val pageId = config?.pageId ?: "unknown"

// ✅ 使用 require/check 进行前置条件检查
fun process(config: RouteConfig?) {
    requireNotNull(config) { "RouteConfig must not be null" }
    // 后续 config 自动为非空
}

// ❌ 避免 !!，除非你 100% 确定不为空且有注释说明
val value = nullable!!  // 尽量不用
```

### 3.4 不可变性规范

```kotlin
// ✅ 优先使用 val
val router: Router = RouterImpl()

// ✅ 使用不可变集合
val interceptors: List<RouteInterceptor> = listOf(...)

// ✅ data class 属性尽量用 val
data class RouteContext(
    val url: String,           // ✅ 不可变
    val params: Map<String, Any?>  // ✅ 不可变 Map
)

// 如需修改，返回新实例
fun RouteContext.withParams(newParams: Map<String, Any?>): RouteContext {
    return copy(params = params + newParams)
}
```

### 3.5 可见性规范

```kotlin
// 公开 API：public（默认）
interface Router { }

// 模块内可见：internal
internal class RouterImpl : Router { }

// 类内可见：private
class RouterImpl : Router {
    private val routeTable = RouteTable()
    private fun doExecute(context: RouteContext) { }
}

// 原则：尽可能使用最小可见性
// public > internal > private
```

---

## 四、协程使用规范

### 4.1 Dispatcher 选择

```kotlin
// CPU 密集型操作
withContext(Dispatchers.Default) {
    // 路由匹配、参数解析等
}

// IO 操作（KMP 中 Dispatchers.IO 可能不可用）
// 使用 Default 或平台特定实现
withContext(Dispatchers.Default) {
    // 文件读写、网络请求等
}

// UI 操作
withContext(Dispatchers.Main) {
    // 更新 UI，仅在平台代码中使用
}
```

### 4.2 结构化并发

```kotlin
// ✅ 使用 coroutineScope 确保子协程完成
suspend fun processInterceptors(context: RouteContext): RouteResult {
    return coroutineScope {
        // 子协程异常会传播到父协程
        interceptorChain.proceed(context)
    }
}

// ✅ 使用 supervisorScope 隔离子协程异常
suspend fun notifyObservers(event: RouteEvent) {
    supervisorScope {
        observers.forEach { observer ->
            launch {
                // 单个 observer 异常不影响其他
                observer.onRouteComplete(event)
            }
        }
    }
}
```

### 4.3 取消处理

```kotlin
// ✅ 检查取消状态
suspend fun longRunningTask() {
    while (isActive) {  // 检查是否被取消
        // 执行任务
        yield()  // 让出执行权，允许取消
    }
}

// ✅ 正确处理 CancellationException
suspend fun safeOperation() {
    try {
        riskyOperation()
    } catch (e: CancellationException) {
        throw e  // 必须重新抛出，否则取消不生效
    } catch (e: Exception) {
        // 处理其他异常
    }
}
```

---

## 五、日志规范

### 5.1 日志接口定义

```kotlin
// commonMain/kotlin/com/worldfirst/router/util/Logger.kt

/**
 * 日志接口，由业务层实现
 */
interface RouterLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * 日志单例，SDK 内部使用
 */
object Logger {
    private var impl: RouterLogger? = null

    fun setLogger(logger: RouterLogger) {
        impl = logger
    }

    fun debug(message: String) = impl?.debug("[Router] $message")
    fun info(message: String) = impl?.info("[Router] $message")
    fun warn(message: String, throwable: Throwable? = null) = impl?.warn("[Router] $message", throwable)
    fun error(message: String, throwable: Throwable? = null) = impl?.error("[Router] $message", throwable)
}
```

### 5.2 日志使用规范

```kotlin
// ✅ 正确的日志使用

// Debug：调试信息，生产环境可关闭
Logger.debug("Parsing url: $url")

// Info：关键流程节点
Logger.info("Route started: $url")
Logger.info("Route completed: $url, result: $result")

// Warn：异常但可恢复的情况
Logger.warn("Interceptor timeout, skipping: ${interceptor.name}")

// Error：异常，必须包含 throwable
Logger.error("Route failed: $url", exception)

// ❌ 错误的日志使用

// 不要打印敏感信息
Logger.debug("User token: $token")  // ❌

// 不要只记录异常消息，要记录完整 throwable
Logger.error("Error: ${e.message}")  // ❌ 丢失堆栈
Logger.error("Error occurred", e)     // ✅ 包含堆栈
```

---

## 六、测试规范

### 6.1 测试要求（强制）

**核心原则：每次代码改动必须保证测试通过，新代码必须补充对应测试**

| 要求 | 说明 | 强制级别 |
|------|------|----------|
| 测试必须通过 | 任何 PR 合并前，所有单元测试必须通过 | **强制** |
| 新代码必须有测试 | 新增的类/方法必须有对应的单元测试 | **强制** |
| Bug 修复必须有测试 | 修复 Bug 时必须补充能复现该 Bug 的测试用例 | **强制** |
| 核心路径覆盖 | 公开 API 的正常路径和异常路径都需要测试 | **强制** |
| 边界条件测试 | 空值、边界值、异常输入等场景需要覆盖 | 推荐 |

### 6.2 测试分层

```
┌─────────────────────────────────────────────────────────────┐
│                    集成测试 (Integration)                    │
│              验证模块间协作，端到端流程                        │
│                    sample/android-app                        │
│                    sample/ios-app                            │
├─────────────────────────────────────────────────────────────┤
│                  平台单元测试 (Platform Unit)                 │
│              测试平台特定实现 (actual)                        │
│                    androidUnitTest/                          │
│                    iosTest/                                  │
├─────────────────────────────────────────────────────────────┤
│                  跨平台单元测试 (Common Unit)                 │
│              测试共享逻辑，核心测试所在                        │
│                    commonTest/                               │
└─────────────────────────────────────────────────────────────┘
```

### 6.3 测试文件组织

测试文件结构必须与源文件结构一一对应：

```
src/
├── commonMain/kotlin/com/worldfirst/router/
│   ├── core/
│   │   ├── ProtocolParser.kt         →  对应测试 ↓
│   │   ├── RouteMatcher.kt
│   │   └── RouteTable.kt
│   └── ...
│
└── commonTest/kotlin/com/worldfirst/router/
    ├── core/
    │   ├── ProtocolParserTest.kt     ←  必须存在
    │   ├── RouteMatcherTest.kt       ←  必须存在
    │   └── RouteTableTest.kt         ←  必须存在
    └── testutil/                      ←  测试辅助工具
        ├── TestInterceptor.kt
        ├── TestRouteContext.kt
        └── Assertions.kt
```

### 6.4 测试命名规范

```kotlin
class ProtocolParserTest {

    // ==================== 推荐：反引号描述式命名 ====================
    // 格式：`方法名 should 期望行为 when 条件`

    @Test
    fun `parse should return ParsedRoute when url is valid`() { }

    @Test
    fun `parse should return failure when scheme is missing`() { }

    @Test
    fun `parse should extract path params when pattern matches`() { }

    @Test
    fun `parse should handle empty query string`() { }

    // ==================== 替代：下划线命名 ====================
    // 格式：方法名_条件_期望结果

    @Test
    fun parse_validUrl_returnsParseRoute() { }

    @Test
    fun parse_missingScheme_returnsFailure() { }
}
```

### 6.5 测试结构（Given-When-Then）

```kotlin
@Test
fun `interceptor chain should execute interceptors in priority order`() {
    // ==================== Given：准备测试数据 ====================
    val executionOrder = mutableListOf<String>()
    val interceptorA = TestInterceptor(name = "A", priority = 10) {
        executionOrder.add("A")
    }
    val interceptorB = TestInterceptor(name = "B", priority = 20) {
        executionOrder.add("B")
    }
    val interceptorC = TestInterceptor(name = "C", priority = 5) {
        executionOrder.add("C")
    }
    val chain = InterceptorChainImpl(
        interceptors = listOf(interceptorA, interceptorB, interceptorC)
    )
    val context = createTestRouteContext(url = "worldfirst://test/page")

    // ==================== When：执行被测方法 ====================
    val result = runBlocking {
        chain.proceed(context)
    }

    // ==================== Then：验证结果 ====================
    // 验证执行顺序（按 priority 升序）
    assertEquals(listOf("C", "A", "B"), executionOrder)
    // 验证返回结果
    assertTrue(result is RouteResult.Success)
}
```

### 6.6 必须测试的场景

#### 6.6.1 公开 API 测试清单

每个公开 API 必须覆盖以下场景：

```kotlin
class RouterTest {

    // ==================== 正常路径 ====================

    @Test
    fun `open should navigate to page when route exists`() { }

    @Test
    fun `open should execute action when action route matches`() { }

    @Test
    fun `open should pass merged params to target page`() { }

    // ==================== 异常路径 ====================

    @Test
    fun `open should call fallback when route not found`() { }

    @Test
    fun `open should return error when interceptor blocks`() { }

    @Test
    fun `open should handle interceptor timeout gracefully`() { }

    // ==================== 边界条件 ====================

    @Test
    fun `open should handle empty url`() { }

    @Test
    fun `open should handle url with special characters`() { }

    @Test
    fun `open should handle null params gracefully`() { }

    // ==================== 并发场景 ====================

    @Test
    fun `open should handle concurrent calls safely`() { }
}
```

#### 6.6.2 核心组件测试清单

| 组件 | 必须测试的场景 |
|------|---------------|
| **ProtocolParser** | 有效 URL、无效 URL、特殊字符、URL 编码、空路径、多级路径 |
| **RouteMatcher** | 精确匹配、path 参数提取、通配符匹配、无匹配、优先级 |
| **InterceptorChain** | 执行顺序、拦截阻断、参数修改、重定向、超时、异常处理 |
| **ObjectStore** | 存取对象、TTL 过期、并发访问、清理机制 |
| **ParamsExtensions** | 类型转换、默认值、必需参数缺失、类型不匹配 |

### 6.7 测试工具类

在 `commonTest/testutil/` 中提供测试辅助工具：

```kotlin
// testutil/TestInterceptor.kt
class TestInterceptor(
    val name: String,
    override val priority: Int = 100,
    private val onIntercept: (suspend () -> Unit)? = null,
    private val result: RouteResult? = null
) : RouteInterceptor {

    var intercepted = false
        private set

    override suspend fun intercept(
        context: RouteContext,
        chain: InterceptorChain
    ): RouteResult {
        intercepted = true
        onIntercept?.invoke()
        return result ?: chain.proceed(context)
    }
}

// testutil/TestRouteContext.kt
fun createTestRouteContext(
    url: String = "worldfirst://test/page",
    params: Map<String, Any?> = emptyMap(),
    source: RouteSource = RouteSource.INTERNAL
): RouteContext {
    return RouteContext(
        url = url,
        parsedRoute = ProtocolParser.parseOrNull(url) ?: error("Invalid test url"),
        params = params,
        source = source,
        timestamp = System.currentTimeMillis(),
        objectStore = ObjectStore()
    )
}

// testutil/Assertions.kt
fun assertRouteSuccess(result: RouteResult) {
    assertTrue(result is RouteResult.Success, "Expected Success but got $result")
}

fun assertRouteBlocked(result: RouteResult, expectedReason: String? = null) {
    assertTrue(result is RouteResult.Blocked, "Expected Blocked but got $result")
    if (expectedReason != null) {
        assertEquals(expectedReason, (result as RouteResult.Blocked).reason)
    }
}
```

### 6.8 测试执行命令

```bash
# 运行所有测试
./gradlew allTests

# 运行 commonTest（跨平台测试）
./gradlew commonTest

# 运行 Android 测试
./gradlew androidUnitTest

# 运行 iOS 测试（需要 macOS）
./gradlew iosSimulatorArm64Test

# 运行单个测试类
./gradlew :commonTest --tests "com.worldfirst.router.core.ProtocolParserTest"

# 生成测试覆盖率报告（如配置了 Kover）
./gradlew koverHtmlReport
```

### 6.9 CI/CD 集成要求

测试必须集成到 CI 流程中：

```yaml
# .github/workflows/test.yml 示例
name: Test

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: macos-latest  # iOS 测试需要 macOS

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Common Tests
        run: ./gradlew commonTest

      - name: Run Android Tests
        run: ./gradlew androidUnitTest

      - name: Run iOS Tests
        run: ./gradlew iosSimulatorArm64Test

      - name: Upload Test Results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: build/reports/tests/
```

### 6.10 测试覆盖率要求

| 代码区域 | 最低覆盖率 | 说明 |
|----------|-----------|------|
| commonMain 核心逻辑 | **80%** | 协议解析、路由匹配、拦截器链等 |
| 公开 API | **90%** | Router、RouteRegistry 等接口 |
| 平台实现 (actual) | **60%** | androidMain、iosMain |
| 工具类 | **70%** | util 包下的辅助类 |

### 6.11 测试代码审查检查清单

PR 审查时检查以下测试相关项目：

- [ ] 新增代码是否有对应的单元测试？
- [ ] 测试是否覆盖正常路径和异常路径？
- [ ] 测试是否覆盖边界条件（空值、边界值）？
- [ ] 测试命名是否清晰描述测试意图？
- [ ] 测试是否遵循 Given-When-Then 结构？
- [ ] 测试是否可重复运行（无副作用）？
- [ ] 是否避免了测试间依赖？
- [ ] CI 测试是否全部通过？

### 6.12 测试编写最佳实践

```kotlin
// ==================== ✅ 好的测试 ====================

@Test
fun `parse should extract orderId from path param`() {
    // 测试单一职责，清晰
    val result = ProtocolParser.parse("worldfirst://order/detail/12345")

    assertTrue(result.isSuccess)
    assertEquals("12345", result.getOrNull()?.pathParams?.get("orderId"))
}

@Test
fun `parse should return failure for malformed url`() {
    // 明确测试异常场景
    val result = ProtocolParser.parse("not a valid url")

    assertTrue(result.isFailure)
}

// ==================== ❌ 不好的测试 ====================

@Test
fun testParse() {
    // ❌ 命名不清晰，不知道测试什么
    val result = ProtocolParser.parse("worldfirst://test")
    assertNotNull(result)
}

@Test
fun `test everything`() {
    // ❌ 测试太多内容，不符合单一职责
    val result1 = ProtocolParser.parse("url1")
    val result2 = ProtocolParser.parse("url2")
    val result3 = ProtocolParser.parse("url3")
    // ... 验证很多东西
}

@Test
fun `test with side effects`() {
    // ❌ 依赖外部状态，测试不可重复
    Router.initialize()  // 全局状态
    // ...
}
```

---

## 七、代码审查检查清单

### 7.1 异常处理

- [ ] 公开 API 是否都有 `@Throws` 或保证不抛异常？
- [ ] 内部异常是否使用 `Result` 或明确传递？
- [ ] 协程是否使用带 `CoroutineExceptionHandler` 的 Scope？
- [ ] 异常是否都记录了日志（包含 throwable）？

### 7.2 类型安全

- [ ] 是否避免了不必要的 `Any` 类型？
- [ ] 类型转换是否使用 `as?` 安全转换？
- [ ] 空检查是否完善？是否避免了 `!!`？

### 7.3 可见性

- [ ] 是否使用了最小可见性原则？
- [ ] 内部实现是否标记为 `internal`？
- [ ] 是否有不应暴露的 API 被 public 了？

### 7.4 命名

- [ ] 命名是否清晰表达意图？
- [ ] 是否遵循命名规范（驼峰、常量大写等）？

### 7.5 协程

- [ ] 是否正确处理了 `CancellationException`？
- [ ] 是否有适当的超时控制？
- [ ] 是否使用了结构化并发？

### 7.6 测试（强制）

- [ ] **新增代码是否有对应的单元测试？**
- [ ] **所有测试是否通过？（CI 必须绿色）**
- [ ] 测试是否覆盖正常路径和异常路径？
- [ ] 测试是否覆盖边界条件？
- [ ] 测试命名是否清晰描述测试意图？
- [ ] Bug 修复是否补充了能复现该 Bug 的测试？

---

## 八、iOS/Android 特定注意事项

### 8.1 iOS 特定

| 注意点 | 说明 |
|--------|------|
| `@Throws` | 所有可能抛异常的公开 API 必须标注 |
| `setUnhandledExceptionHook` | SDK 初始化时必须设置，且不调用 terminate |
| 冻结对象 | 跨线程传递的对象需要考虑 freeze（新内存模型已改善） |
| 方法名 | 避免 Kotlin 保留字，注意生成的 ObjC 方法名 |

### 8.2 Android 特定

| 注意点 | 说明 |
|--------|------|
| 主线程 | UI 操作必须在主线程，使用 `Dispatchers.Main` |
| 生命周期 | 注意 Activity/Fragment 生命周期，避免内存泄漏 |
| Context | 避免持有 Activity Context，使用 Application Context |
| ProGuard | 确保正确的混淆规则 |

---

## 九、版本与兼容性

### 9.1 KMP 版本要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| Kotlin | 2.2.21 | 按团队要求 |
| Gradle | 8.x | 配套 KMP 版本 |
| kotlinx.coroutines | 1.7+ | 异步支持 |

### 9.2 API 兼容性原则

- 新增 API：允许
- 修改 API 签名：禁止（需新增 API + 废弃旧 API）
- 删除 API：先废弃一个版本，再删除
- 行为变更：必须在 Release Notes 中说明

```kotlin
// 废弃 API 示例
@Deprecated(
    message = "Use open() instead",
    replaceWith = ReplaceWith("open(url, params, extras, callback)"),
    level = DeprecationLevel.WARNING  // WARNING -> ERROR -> HIDDEN
)
fun navigate(url: String)
```

---

*文档版本：v1.1*
*创建日期：2025-01-19*
*最后更新：2025-01-19*

---

## 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.1 | 2025-01-19 | 改为单模块结构；大幅扩展测试规范（强制要求、测试分层、覆盖率要求、CI 集成） |
| v1.0 | 2025-01-19 | 初始版本 |
