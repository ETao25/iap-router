// iOS Swift Demo - KMP Router 路由注册示例
// 文件位置：iOS 项目中的路由配置文件

import UIKit
import KMPRouter  // KMP 框架导入

// MARK: - 示例页面定义

class OrderDetailViewController: UIViewController {
    var orderId: String?
    var source: String?

    convenience init(params: [String: Any?]) {
        self.init()
        self.orderId = params["orderId"] as? String
        self.source = params["source"] as? String
    }
}

class AccountSettingsViewController: UIViewController {
    // ...
}

class PaymentViewController: UIViewController {
    var amount: Double?

    convenience init(params: [String: Any?]) {
        self.init()
        self.amount = params["amount"] as? Double
    }
}

class FxChartViewController: UIViewController {
    var pairId: String?
    var period: String?

    convenience init(params: [String: Any?]) {
        self.init()
        self.pairId = params["pairId"] as? String
        self.period = params["period"] as? String
    }
}

// MARK: - 路由注册配置

class RouteConfiguration {

    static func registerAllRoutes(registry: RouteRegistry) {

        // ==================== 方式1：通过 builder 注册（推荐）====================
        // 类型安全：返回类型必须是 UIViewController

        // 订单详情页 - 带路径参数
        registry.registerPage(
            pattern: "order/detail/:orderId",
            builder: PageBuilder { params in
                OrderDetailViewController(params: params)
            }
        )

        // FX 图表页 - 多级路径参数
        registry.registerPage(
            pattern: "fx/:pairId/chart",
            builder: PageBuilder { params in
                FxChartViewController(params: params)
            }
        )

        // 支付页
        registry.registerPage(
            pattern: "payment/newFeature",
            builder: PageBuilder { params in
                PaymentViewController(params: params)
            }
        )

        // ==================== 方式2：通过 class 注册 ====================
        // 适用于无需参数处理的简单页面

        registry.registerPage(
            pattern: "account/settings",
            pageClass: AccountSettingsViewController.self
        )

        // ==================== 方式3：使用通配符 ====================

        registry.registerPage(
            pattern: "webview/*",
            builder: PageBuilder { params in
                // 通配符匹配的路径在 params["*"] 中
                let path = params["*"] as? String ?? ""
                return WebViewController(path: path)
            }
        )

        // ==================== Action 路由注册 ====================

        registry.registerAction(actionName: "showPopup") { params, callback in
            let message = params["message"] as? String ?? ""
            // 显示弹窗逻辑
            showAlert(message: message)
            callback?.onSuccess(nil)
        }

        registry.registerAction(actionName: "copyText") { params, callback in
            if let text = params["text"] as? String {
                UIPasteboard.general.string = text
                callback?.onSuccess(true)
            } else {
                callback?.onError(NSError(domain: "Router", code: -1, userInfo: nil))
            }
        }
    }
}

// MARK: - 辅助类

class WebViewController: UIViewController {
    var path: String

    init(path: String) {
        self.path = path
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

func showAlert(message: String) {
    // 实现弹窗显示逻辑
}

// MARK: - AppDelegate 中初始化

/*
 在 AppDelegate 中调用：

 func application(_ application: UIApplication, didFinishLaunchingWithOptions...) {
     // 获取 RouteRegistry 实例
     let registry = KMPRouter.shared.registry

     // 注册所有路由
     RouteConfiguration.registerAllRoutes(registry: registry)

     // ==================== 配置降级策略（使用 FallbackManager）====================
     // 注意：降级配置是基于 pattern 的，不是单页面维度的

     let fallbackManager = FallbackManager()

     // 设置全局降级（路由未找到时）
     fallbackManager.setGlobalFallback(FallbackAction.navigateTo("iap://error/404"))

     // 设置基于 pattern 的降级规则
     fallbackManager.addPatternFallback(pattern: "payment/*", action: FallbackAction.navigateTo("iap://h5/payment"))
     fallbackManager.addPatternFallback(pattern: "user/*", action: FallbackAction.navigateTo("iap://login"))

     // 注册到 Router
     KMPRouter.shared.setFallbackHandler(fallbackManager)
 }
*/

// MARK: - 路由跳转示例

/*
 跳转示例：

 // 简单跳转
 KMPRouter.shared.open("iap://order/detail/12345")

 // 带参数跳转
 KMPRouter.shared.open("iap://order/detail/12345?source=homepage")

 // 带额外参数（传递对象）
 KMPRouter.shared.open(
     url: "iap://order/detail/12345",
     params: ["viewModel": orderViewModel]
 )

 // FX 图表页
 KMPRouter.shared.open("iap://fx/USDCNY/chart?period=1d")

 // Action 调用
 KMPRouter.shared.open("iap://action/showPopup?message=Hello")
 KMPRouter.shared.open("iap://action/copyText?text=复制内容")
*/
