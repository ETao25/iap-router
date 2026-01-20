package com.iap.router.platform

/**
 * 页面工厂接口
 * 由各平台实现具体的页面创建逻辑
 */
interface PageFactory {
    /**
     * 检查是否能创建指定页面
     * @param pageId 页面标识
     * @return 是否可以创建
     */
    fun canCreate(pageId: String): Boolean

    /**
     * 创建页面实例
     * @param pageId 页面标识
     * @param params 页面参数
     * @return 平台特定的页面对象（iOS: UIViewController, Android: Intent/Fragment）
     */
    fun create(pageId: String, params: Map<String, Any?>): Any?
}
