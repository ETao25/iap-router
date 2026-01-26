// Android Kotlin Demo - KMP Router 路由注册示例
// 文件位置：Android 项目中的路由配置文件

package com.example.app.router

import android.app.Activity
import android.os.Bundle
import com.iap.router.RouteRegistry
import com.iap.router.core.ActionCallback
import com.iap.router.core.ActionHandler
import com.iap.router.platform.PageBuilder

// ==================== 示例页面定义 ====================

class OrderDetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderId = intent.getStringExtra("orderId")
        val source = intent.getStringExtra("source")
        // 初始化页面...
    }
}

class AccountSettingsActivity : Activity() {
    // ...
}

class PaymentActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val amount = intent.getDoubleExtra("amount", 0.0)
        // 初始化页面...
    }
}

class FxChartActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pairId = intent.getStringExtra("pairId")
        val period = intent.getStringExtra("period")
        // 初始化页面...
    }
}

class WebViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra("path")
        // 加载 WebView...
    }
}

// ==================== 路由注册配置 ====================

object RouteConfiguration {

    fun registerAllRoutes(registry: RouteRegistry) {

        // ==================== 方式1：通过 builder 注册 ====================
        // 类型安全：返回类型必须是 Activity

        // 订单详情页 - 带路径参数
        registry.registerPage(
            pattern = "order/detail/:orderId",
            builder = PageBuilder { params ->
                // 注意：Android 中通常返回 Activity 实例
                // 但实际导航时会通过 Navigator 处理 Intent
                OrderDetailActivity()
            }
        )

        // FX 图表页 - 多级路径参数
        registry.registerPage(
            pattern = "fx/:pairId/chart",
            builder = PageBuilder { params ->
                FxChartActivity()
            }
        )

        // 支付页
        registry.registerPage(
            pattern = "payment/newFeature",
            builder = PageBuilder { PaymentActivity() }
        )

        // ==================== 方式2：通过 class 注册（推荐 Android）====================
        // Android 推荐使用 class 方式，Navigator 会创建 Intent

        registry.registerPage(
            pattern = "account/settings",
            pageClass = AccountSettingsActivity::class
        )

        // 使用 reified 扩展函数（更简洁）
        registry.registerPage<OrderDetailActivity>("order/detail/:orderId")
        registry.registerPage<AccountSettingsActivity>("account/settings")

        // ==================== 方式3：使用通配符 ====================

        registry.registerPage(
            pattern = "webview/*",
            builder = PageBuilder { params ->
                // 通配符匹配的路径在 params["*"] 中
                WebViewActivity()
            }
        )

        // ==================== Action 路由注册 ====================

        registry.registerAction("showPopup", object : ActionHandler {
            override fun execute(params: Map<String, Any?>, callback: ActionCallback?) {
                val message = params["message"] as? String ?: ""
                // 显示弹窗逻辑
                showToast(message)
                callback?.onSuccess(null)
            }
        })

        registry.registerAction("copyText", object : ActionHandler {
            override fun execute(params: Map<String, Any?>, callback: ActionCallback?) {
                val text = params["text"] as? String
                if (text != null) {
                    // 复制到剪贴板
                    copyToClipboard(text)
                    callback?.onSuccess(true)
                } else {
                    callback?.onError(IllegalArgumentException("Missing text parameter"))
                }
            }
        })
    }

    private fun showToast(message: String) {
        // 实现 Toast 显示逻辑
    }

    private fun copyToClipboard(text: String) {
        // 实现剪贴板复制逻辑
    }
}

// ==================== Application 中初始化 ====================

/*
 在 Application 中调用：

 class MyApplication : Application() {
     override fun onCreate() {
         super.onCreate()

         // 获取 RouteRegistry 实例
         val registry = KMPRouter.instance.registry

         // 注册所有路由
         RouteConfiguration.registerAllRoutes(registry)

         // ==================== 配置降级策略（使用 FallbackManager）====================
         // 注意：降级配置是基于 pattern 的，不是单页面维度的

         val fallbackManager = FallbackManager()

         // 设置全局降级（路由未找到时）
         fallbackManager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))

         // 设置基于 pattern 的降级规则
         fallbackManager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))
         fallbackManager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))

         // 注册到 Router
         KMPRouter.instance.setFallbackHandler(fallbackManager)
     }
 }
*/

// ==================== 路由跳转示例 ====================

/*
 跳转示例：

 // 简单跳转
 KMPRouter.instance.open("iap://order/detail/12345")

 // 带参数跳转
 KMPRouter.instance.open("iap://order/detail/12345?source=homepage")

 // 带额外参数（传递对象）
 KMPRouter.instance.open(
     url = "iap://order/detail/12345",
     params = mapOf("viewModel" to orderViewModel)
 )

 // FX 图表页
 KMPRouter.instance.open("iap://fx/USDCNY/chart?period=1d")

 // Action 调用
 KMPRouter.instance.open("iap://action/showPopup?message=Hello")
 KMPRouter.instance.open("iap://action/copyText?text=复制内容")
*/
