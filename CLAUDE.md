# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

KMP Router SDK - 基于 Kotlin Multiplatform 的跨平台路由 SDK，为 iOS 和 Android 提供统一的路由能力。

核心目标：
- 统一的路由协议规范（`iap://{path}?params`）
- 双端一致的核心路由能力
- 灵活可扩展的拦截器机制
- KMP 只实现纯逻辑层，实际页面跳转由各平台原生执行

## 常用命令

```bash
cd kmp-router

# 构建
./gradlew build

# 运行所有测试
./gradlew allTests

# 运行跨平台测试（主要测试）
./gradlew jvmTest

# 运行 Android 测试
./gradlew testDebugUnitTest

# 运行 iOS 测试（需要 macOS）
./gradlew iosSimulatorArm64Test

# 运行单个测试类
./gradlew jvmTest --tests "com.iap.router.core.ProtocolParserTest"

# 清理构建
./gradlew clean
```

## 架构概览

```
kmp-router/src/
├── commonMain/          # 跨平台共享代码（核心）
│   └── kotlin/com/iap/router/
│       ├── core/        # 协议解析、路由匹配、路由表
│       ├── interceptor/ # 拦截器链机制
│       ├── observer/    # 路由回调与观察者
│       ├── fallback/    # 降级处理
│       ├── params/      # 参数类型转换扩展
│       ├── model/       # 数据模型（RouteContext, RouteResult 等）
│       ├── platform/    # 平台桥接接口（PlatformPageCreator）
│       └── exception/   # 异常定义
├── androidMain/         # Android 平台实现（AndroidPageCreator）
├── iosMain/             # iOS 平台实现（IOSPageCreator）
├── commonTest/          # 跨平台单元测试
├── androidUnitTest/     # Android 平台测试
└── iosTest/             # iOS 平台测试
```

## 关键设计决策

### 路由协议格式
```
iap://{path}[/:pathParam]?queryParam=value

# 页面路由示例
iap://order/detail/:orderId?from=list

# Action 路由示例
iap://action/showPopup?type=confirm
```

### 路由匹配规则
- 精确匹配：`order/detail` 匹配 `order/detail`
- Path 参数：`order/detail/:orderId` 匹配 `order/detail/123`，提取 orderId=123
- 通配符：`payment/*` 匹配 `payment/card/bindNew`

### 平台注册 API 分离
- `commonMain` 定义 `PlatformPageCreator` 接口和 `PageTarget`
- `iosMain` 提供 `IOSPageCreator` + 工厂函数注册 + `PageRouteDefinition` 声明式注册
- `androidMain` 提供 `AndroidPageCreator` + KClass/Intent 注册 + `PageRouteInfo` 声明式注册

### 拦截器机制
- 支持全局拦截器和局部拦截器（基于路由模式匹配）
- 使用 `suspend function` 支持异步拦截
- 优先级：数值越小优先级越高
- 拦截器可修改参数、重定向、阻断路由

### 降级策略
- 使用 `FallbackManager` 统一管理全局降级和基于 pattern 的降级规则
- 支持 `NavigateTo`、`ShowError`、`Ignore`、`Custom` 四种降级动作

## 测试规范

- 每次代码改动必须保证测试通过
- 新代码必须补充对应测试
- 测试命名格式：`` `方法名 should 期望行为 when 条件` ``
- 测试结构遵循 Given-When-Then

## 异常处理规范

- 公开 API 必须使用 `@Throws` 标注或保证不抛异常
- 内部方法使用 `Result` 包装可能失败的操作
- 协程使用 `CoroutineExceptionHandler` 处理异常
- iOS 调用 Kotlin 方法抛异常会导致 Crash，必须正确处理

## 技术栈

- Kotlin 2.2.21
- AGP 8.10.0
- kotlinx.coroutines 1.10.2
- ktor-http 3.2.3（URL 编解码）
