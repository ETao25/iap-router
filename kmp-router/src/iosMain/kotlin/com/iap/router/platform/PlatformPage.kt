package com.iap.router.platform

import com.iap.router.RouteRegistry
import com.iap.router.model.PageRouteConfig
import platform.UIKit.UIViewController

/**
 * iOS 页面创建器
 * 通过工厂函数创建 UIViewController
 */
class IOSPageCreator(
    /**
     * 工厂函数：接收参数，返回 UIViewController
     */
    val factory: (Map<String, Any?>) -> UIViewController
) : PlatformPageCreator {

    /**
     * 创建 ViewController 实例
     */
    fun createViewController(params: Map<String, Any?>): UIViewController {
        return factory.invoke(params)
    }
}

// ==================== iOS 专用扩展函数 ====================

/**
 * 注册页面路由（通过工厂函数）
 * Swift 调用示例:
 * ```swift
 * registry.registerPage(pattern: "order/detail/:orderId") { params in
 *     OrderDetailViewController(params: params)
 * }
 * ```
 */
fun RouteRegistry.registerPage(
    pattern: String,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        pattern = pattern,
        target = PageTarget(creator),
        pageId = null
    )
    registerPage(config)
}

/**
 * 注册页面路由（通过工厂函数 + 自定义 pageId）
 */
fun RouteRegistry.registerPage(
    pattern: String,
    pageId: String,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        pattern = pattern,
        target = PageTarget(creator),
        pageId = pageId
    )
    registerPage(config)
}

// ==================== 辅助函数 ====================

/**
 * 从 PageRouteConfig 获取 iOS 页面创建器
 */
fun PageRouteConfig.getIOSPageCreator(): IOSPageCreator? {
    return target.creator as? IOSPageCreator
}

/**
 * 创建 UIViewController
 */
fun PageRouteConfig.createViewController(params: Map<String, Any?>): UIViewController? {
    return getIOSPageCreator()?.createViewController(params)
}

// ==================== 声明式路由注册 ====================

/**
 * 页面路由接口
 * 用于声明式路由注册，Swift 侧可以使用 protocol 实现
 *
 * Swift 使用示例:
 * ```swift
 * // 1. 定义 Swift 协议（继承 KMP 的 PageRoutable）
 * // 注意：由于 KMP 导出的限制，建议在 Swift 侧定义 protocol 并桥接
 *
 * protocol SwiftPageRoutable {
 *     static var pattern: String { get }
 *     static func createPage(params: [String: Any?]) -> UIViewController
 * }
 *
 * extension SwiftPageRoutable {
 *     static var pageBuilder: PageRouteConfig {
 *         PageRoutable.Companion.shared.createConfig(
 *             pattern: pattern,
 *             factory: { params in createPage(params: params) }
 *         )
 *     }
 * }
 *
 * // 2. ViewController 实现协议
 * class OrderDetailViewController: UIViewController, SwiftPageRoutable {
 *     static var pattern: String { "order/detail/:orderId" }
 *
 *     static func createPage(params: [String: Any?]) -> UIViewController {
 *         OrderDetailViewController(orderId: params["orderId"] as? String)
 *     }
 * }
 *
 * // 3. 注册（一行）
 * registry.registerPage(config: OrderDetailViewController.pageBuilder)
 * ```
 */
interface PageRoutable {
    /**
     * 路由模式
     */
    val pattern: String

    /**
     * 创建 UIViewController
     */
    fun createPage(params: Map<String, Any?>): UIViewController

    companion object {
        /**
         * 创建 PageRouteConfig
         * Swift 可通过此方法创建配置
         */
        fun createConfig(
            pattern: String,
            factory: (Map<String, Any?>) -> UIViewController
        ): PageRouteConfig {
            val creator = IOSPageCreator(factory)
            return PageRouteConfig(
                pattern = pattern,
                target = PageTarget(creator),
                pageId = null
            )
        }
    }
}

/**
 * 获取 PageRouteConfig（用于一行注册）
 *
 * Kotlin 使用示例:
 * ```kotlin
 * registry.registerPage(MyViewController.pageBuilder)
 * ```
 */
val PageRoutable.pageBuilder: PageRouteConfig
    get() {
        val routable = this
        val creator = IOSPageCreator { params ->
            routable.createPage(params)
        }
        return PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = null
        )
    }

/**
 * 注册页面路由（通过 PageRoutable）
 *
 * 使用示例:
 * ```kotlin
 * registry.registerPage(MyViewController)
 * ```
 */
fun RouteRegistry.registerPage(page: PageRoutable) {
    registerPage(page.pageBuilder)
}

/**
 * 批量注册页面路由
 *
 * 使用示例:
 * ```kotlin
 * registry.registerPages(
 *     OrderDetailViewController,
 *     AccountSettingsViewController,
 *     PaymentViewController
 * )
 * ```
 */
fun RouteRegistry.registerPages(pages: List<PageRoutable>) {
    pages.forEach { registerPage(it) }
}
