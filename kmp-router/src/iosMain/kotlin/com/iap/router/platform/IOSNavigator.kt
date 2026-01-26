package com.iap.router.platform

import com.iap.router.core.RouteTable
import com.iap.router.model.NavigationOptions
import platform.UIKit.UIViewController

/**
 * iOS 导航器实现
 *
 * 负责执行实际的页面导航操作
 *
 * 使用时需要：
 * 1. 提供 RouteTable 用于查找页面配置
 * 2. 实现 pageIdToViewController 桥接方法，将 pageId 映射到 UIViewController
 * 3. 实现 performPush/performPresent/performPop 等实际导航方法
 */
class IOSNavigator(
    /**
     * 路由表，用于查找页面配置并创建 UIViewController
     */
    private val routeTable: RouteTable
) : Navigator {

    /**
     * 当前显示的页面栈（用于 popTo 等操作）
     * 实际实现时应由底层 SDK 管理
     */
    private val pageStack = mutableListOf<String>()

    // ==================== Navigator 接口实现 ====================

    override fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
        // 1. 获取页面配置
        val config = routeTable.getPageConfig(pageId)
        if (config == null) {
            // 尝试通过 pattern 匹配查找
            // 如果直接找不到，pageId 可能是动态 pattern，此时需要通过其他方式查找
            performPushWithPageId(pageId, params, options)
            return
        }

        // 2. 创建 UIViewController
        val viewController = config.createViewController(params)
        if (viewController == null) {
            // TODO: 处理创建失败的情况
            return
        }

        // 3. 执行导航
        performPush(viewController, pageId, params, options)

        // 4. 更新页面栈
        pageStack.add(pageId)
    }

    override fun present(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
        // 1. 获取页面配置
        val config = routeTable.getPageConfig(pageId)
        if (config == null) {
            performPresentWithPageId(pageId, params, options)
            return
        }

        // 2. 创建 UIViewController
        val viewController = config.createViewController(params)
        if (viewController == null) {
            // TODO: 处理创建失败的情况
            return
        }

        // 3. 执行 present
        performPresent(viewController, pageId, params, options)

        // 4. 更新页面栈
        pageStack.add(pageId)
    }

    override fun pop(result: Any?) {
        performPop(result)

        // 更新页面栈
        if (pageStack.isNotEmpty()) {
            pageStack.removeLast()
        }
    }

    override fun popTo(pageId: String, result: Any?) {
        // 查找目标页面在栈中的位置
        val targetIndex = pageStack.lastIndexOf(pageId)
        if (targetIndex < 0) {
            // 目标页面不在栈中，尝试直接调用底层 SDK
            performPopTo(pageId, result)
            return
        }

        // 执行 popTo
        performPopTo(pageId, result)

        // 更新页面栈：移除目标页面之后的所有页面
        while (pageStack.size > targetIndex + 1) {
            pageStack.removeLast()
        }
    }

    override fun popToRoot() {
        performPopToRoot()

        // 清空页面栈（保留根页面）
        if (pageStack.isNotEmpty()) {
            val root = pageStack.first()
            pageStack.clear()
            pageStack.add(root)
        }
    }

    // ==================== 底层 SDK 桥接方法（需要实际实现）====================

    /**
     * 执行 Push 导航
     *
     * TODO: 桥接到底层 iOS SDK
     * 示例实现：
     * ```
     * IAPRouter.shared.push(viewController, animated: options.animated)
     * ```
     */
    private fun performPush(
        viewController: UIViewController,
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 iOS SDK 执行实际 push 操作
        // IAPRouter.shared.push(viewController, animated: options.animated)
    }

    /**
     * 通过 pageId 执行 Push（不创建 VC，由底层 SDK 处理）
     *
     * TODO: 桥接到底层 iOS SDK
     * 用于 pageId 已在底层 SDK 注册的场景
     */
    private fun performPushWithPageId(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 iOS SDK
        // IAPRouter.shared.push(pageId: pageId, params: params, animated: options.animated)
    }

    /**
     * 执行 Present 导航
     *
     * TODO: 桥接到底层 iOS SDK
     */
    private fun performPresent(
        viewController: UIViewController,
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 iOS SDK 执行实际 present 操作
        // IAPRouter.shared.present(viewController, animated: options.animated, style: options.presentStyle)
    }

    /**
     * 通过 pageId 执行 Present
     *
     * TODO: 桥接到底层 iOS SDK
     */
    private fun performPresentWithPageId(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 iOS SDK
        // IAPRouter.shared.present(pageId: pageId, params: params, animated: options.animated)
    }

    /**
     * 执行 Pop
     *
     * TODO: 桥接到底层 iOS SDK
     */
    private fun performPop(result: Any?) {
        // TODO: 桥接到底层 iOS SDK 执行实际 pop 操作
        // IAPRouter.shared.pop(animated: true, result: result)
    }

    /**
     * 执行 PopTo
     *
     * TODO: 桥接到底层 iOS SDK
     */
    private fun performPopTo(pageId: String, result: Any?) {
        // TODO: 桥接到底层 iOS SDK
        // IAPRouter.shared.popTo(pageId: pageId, animated: true, result: result)
    }

    /**
     * 执行 PopToRoot
     *
     * TODO: 桥接到底层 iOS SDK
     */
    private fun performPopToRoot() {
        // TODO: 桥接到底层 iOS SDK
        // IAPRouter.shared.popToRoot(animated: true)
    }
}
