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
 *
 * Swift 调用示例:
 * ```swift
 * registry.registerPage(pattern: "order/detail/:orderId") { params in
 *     OrderDetailViewController(params: params)
 * }
 * ```
 *
 * 声明式注册示例（Swift 静态协议）:
 * ```swift
 * // 1. 定义 Swift 协议（静态成员）
 * protocol PageRoutable {
 *     static var pattern: String { get }
 *     static func createPage(params: [String: Any?]) -> UIViewController
 * }
 *
 * // 2. ViewController 实现协议
 * extension OrderDetailViewController: PageRoutable {
 *     static var pattern: String { "order/detail/:orderId" }
 *     static func createPage(params: [String: Any?]) -> UIViewController {
 *         OrderDetailViewController(params: params)
 *     }
 * }
 *
 * // 3. 扩展 RouteRegistry（Swift 侧）
 * extension RouteRegistry {
 *     func registerPage<T: PageRoutable>(_ type: T.Type) {
 *         registerPage(pattern: T.pattern) { params in T.createPage(params: params) }
 *     }
 * }
 *
 * // 4. 注册（一行）
 * registry.registerPage(OrderDetailViewController.self)
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
