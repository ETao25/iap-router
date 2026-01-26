package com.iap.router.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.iap.router.RouteRegistry
import com.iap.router.model.PageRouteConfig
import kotlin.reflect.KClass

/**
 * Android 页面创建器
 * 支持两种方式：
 * 1. 通过工厂函数创建 Intent (intentFactory)
 * 2. 通过 Activity KClass 创建 (activityClass)
 */
class AndroidPageCreator : PlatformPageCreator {
    /**
     * Intent 工厂函数：接收 Context 和参数，返回 Intent
     */
    val intentFactory: ((Context, Map<String, Any?>) -> Intent)?

    /**
     * Activity 的 KClass
     */
    val activityClass: KClass<out Activity>?

    /**
     * 通过 Intent 工厂函数创建
     */
    constructor(intentFactory: (Context, Map<String, Any?>) -> Intent) {
        this.intentFactory = intentFactory
        this.activityClass = null
    }

    /**
     * 通过 Activity KClass 创建
     */
    constructor(activityClass: KClass<out Activity>) {
        this.intentFactory = null
        this.activityClass = activityClass
    }

    /**
     * 创建 Intent
     */
    fun createIntent(context: Context, params: Map<String, Any?>): Intent {
        return when {
            intentFactory != null -> intentFactory.invoke(context, params)
            activityClass != null -> {
                Intent(context, activityClass.java).apply {
                    // 将参数放入 Intent extras
                    params.forEach { (key, value) ->
                        when (value) {
                            is String -> putExtra(key, value)
                            is Int -> putExtra(key, value)
                            is Long -> putExtra(key, value)
                            is Double -> putExtra(key, value)
                            is Float -> putExtra(key, value)
                            is Boolean -> putExtra(key, value)
                            // 其他类型忽略或可扩展
                        }
                    }
                }
            }
            else -> throw IllegalStateException("No intentFactory or activityClass specified")
        }
    }
}

// ==================== 辅助函数 ====================

/**
 * 从 PageRouteConfig 获取 Android 页面创建器
 */
fun PageRouteConfig.getAndroidPageCreator(): AndroidPageCreator? {
    return target.creator as? AndroidPageCreator
}

/**
 * 创建 Intent
 */
fun PageRouteConfig.createIntent(context: Context, params: Map<String, Any?>): Intent? {
    return getAndroidPageCreator()?.createIntent(context, params)
}

// ==================== 声明式路由注册 ====================

/**
 * 页面路由接口
 * Activity 的 companion object 实现此接口，即可通过 registerPage(Activity) 一行代码注册
 *
 * 使用示例:
 * ```kotlin
 * class OrderDetailActivity : Activity() {
 *     companion object : PageRoutable {
 *         override val pattern = "order/detail/:orderId"
 *         override fun createIntent(context: Context, params: Map<String, Any?>) =
 *             Intent(context, OrderDetailActivity::class.java).apply {
 *                 putExtra("orderId", params["orderId"] as? String)
 *             }
 *     }
 * }
 *
 * // 注册（一行）
 * registry.registerPage(OrderDetailActivity)
 * ```
 */
interface PageRoutable {
    /**
     * 路由模式
     */
    val pattern: String

    /**
     * 创建 Intent
     */
    fun createIntent(context: Context, params: Map<String, Any?>): Intent
}

/**
 * 获取 PageRouteConfig（用于一行注册）
 *
 * 使用示例:
 * ```kotlin
 * registry.registerPage(OrderDetailActivity.pageBuilder)
 * ```
 */
val PageRoutable.pageBuilder: PageRouteConfig
    get() {
        val routable = this
        val creator = AndroidPageCreator { context, params ->
            routable.createIntent(context, params)
        }
        return PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = null
        )
    }

/**
 * 注册页面路由（通过 PageRoutable）
 * 用于 Activity companion object 实现 PageRoutable 的场景
 *
 * 使用示例:
 * ```kotlin
 * registry.registerPage(OrderDetailActivity)
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
 *     OrderDetailActivity,
 *     AccountSettingsActivity,
 *     PaymentActivity
 * )
 * ```
 */
fun RouteRegistry.registerPages(vararg pages: PageRoutable) {
    pages.forEach { registerPage(it) }
}
