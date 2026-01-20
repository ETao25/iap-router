package com.worldfirst.router.platform

import com.worldfirst.router.model.NavigationOptions

/**
 * 导航器接口
 * 由各平台实现具体的页面跳转逻辑
 */
interface Navigator {
    /**
     * Push 跳转（默认导航方式）
     * @param pageId 页面标识
     * @param params 页面参数
     * @param options 导航选项
     */
    fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions)

    /**
     * Present 跳转（iOS modal / Android 可按需实现或忽略）
     * @param pageId 页面标识
     * @param params 页面参数
     * @param options 导航选项
     */
    fun present(pageId: String, params: Map<String, Any?>, options: NavigationOptions)

    /**
     * 返回上一页
     * @param result 返回结果（可选）
     */
    fun pop(result: Any? = null)

    /**
     * 返回到指定页面
     * @param pageId 目标页面标识
     * @param result 返回结果（可选）
     */
    fun popTo(pageId: String, result: Any? = null)

    /**
     * 返回到根页面
     */
    fun popToRoot()
}
