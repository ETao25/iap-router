package com.iap.router.platform

import com.iap.router.core.RouteTable
import com.iap.router.model.NavigationOptions

/**
 * Android 导航器实现
 *
 * 负责执行实际的页面导航操作
 *
 * 使用时需要：
 * 1. 提供 RouteTable 用于查找页面配置
 * 2. 实现 performPush/performPresent/performPop 等实际导航方法，桥接到底层 SDK
 */
class AndroidNavigator(
    /**
     * 路由表，用于查找页面配置
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
            // 尝试通过 pageId 直接跳转（由底层 SDK 处理）
            performPushWithPageId(pageId, params, options)
            return
        }

        // 2. 执行导航
        performPush(pageId, params, options)

        // 3. 更新页面栈
        pageStack.add(pageId)
    }

    override fun present(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
        // 1. 获取页面配置
        val config = routeTable.getPageConfig(pageId)
        if (config == null) {
            performPresentWithPageId(pageId, params, options)
            return
        }

        // 2. 执行 present
        performPresent(pageId, params, options)

        // 3. 更新页面栈
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
     * TODO: 桥接到底层 Android SDK
     * 示例实现：
     * ```
     * val intent = Intent(context, targetActivity::class.java)
     * intent.putExtras(params.toBundle())
     * context.startActivity(intent)
     * ```
     */
    private fun performPush(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 Android SDK 执行实际 push 操作
    }

    /**
     * 通过 pageId 执行 Push（由底层 SDK 处理页面查找）
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPushWithPageId(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 Android SDK
    }

    /**
     * 执行 Present 导航（Android 中通常也是 startActivity）
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPresent(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 Android SDK 执行实际 present 操作
    }

    /**
     * 通过 pageId 执行 Present
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPresentWithPageId(
        pageId: String,
        params: Map<String, Any?>,
        options: NavigationOptions
    ) {
        // TODO: 桥接到底层 Android SDK
    }

    /**
     * 执行 Pop（finish 当前 Activity）
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPop(result: Any?) {
        // TODO: 桥接到底层 Android SDK 执行实际 pop 操作
        // activity.setResult(resultCode, resultIntent)
        // activity.finish()
    }

    /**
     * 执行 PopTo
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPopTo(pageId: String, result: Any?) {
        // TODO: 桥接到底层 Android SDK
        // 可能需要使用 FLAG_ACTIVITY_CLEAR_TOP 等 Intent flags
    }

    /**
     * 执行 PopToRoot
     *
     * TODO: 桥接到底层 Android SDK
     */
    private fun performPopToRoot() {
        // TODO: 桥接到底层 Android SDK
        // 可能需要使用 FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP
    }
}
