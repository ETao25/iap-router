package com.iap.router.platform

import kotlin.reflect.KClass

/**
 * 平台页面基类
 * iOS: UIViewController
 * Android: Activity
 * JVM: Any (用于测试)
 */
expect abstract class PlatformPage

/**
 * 页面构建器接口
 * 类型安全：返回类型限制为 PlatformPage
 */
fun interface PageBuilder {
    /**
     * 构建页面实例
     * @param params 路由参数
     * @return 平台特定的页面实例
     */
    fun build(params: Map<String, Any?>): PlatformPage
}

/**
 * 页面目标：封装 builder 或 class 两种注册方式
 */
sealed class PageTarget {
    /**
     * 通过构建器创建页面
     */
    data class Builder(val builder: PageBuilder) : PageTarget()

    /**
     * 通过类创建页面
     */
    data class ClassRef(val pageClass: KClass<out PlatformPage>) : PageTarget()
}
