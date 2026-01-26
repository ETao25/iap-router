package com.iap.router.platform

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
