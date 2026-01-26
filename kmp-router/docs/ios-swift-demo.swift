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

// MARK: - 声明式路由协议（可选，Swift 侧定义）

/// Swift 协议：用于声明式路由注册
/// pattern 和 createPage 都是静态成员
protocol PageRoutable {
    static var pattern: String { get }
    static func createPage(params: [String: Any?]) -> UIViewController
}

/// 扩展 RouteRegistry 支持 PageRoutable 协议
extension RouteRegistry {
    func registerPage<T: PageRoutable>(_ type: T.Type) {
        registerPage(pattern: T.pattern) { params in
            T.createPage(params: params)
        }
    }
}

// MARK: - ViewController 实现 PageRoutable 协议

extension OrderDetailViewController: PageRoutable {
    static var pattern: String { "order/detail/:orderId" }

    static func createPage(params: [String: Any?]) -> UIViewController {
        OrderDetailViewController(params: params)
    }
}

extension FxChartViewController: PageRoutable {
    static var pattern: String { "fx/:pairId/chart" }

    static func createPage(params: [String: Any?]) -> UIViewController {
        FxChartViewController(params: params)
    }
}

extension PaymentViewController: PageRoutable {
    static var pattern: String { "payment/checkout" }

    static func createPage(params: [String: Any?]) -> UIViewController {
        PaymentViewController(params: params)
    }
}

// MARK: - 路由注册配置

class RouteConfiguration {

    static func registerAllRoutes(registry: RouteRegistry) {

        // ==================== 方式1：工厂函数注册（简单场景）====================

        registry.registerPage(pattern: "account/settings") { params in
            AccountSettingsViewController()
        }

        registry.registerPage(pattern: "webview/*") { params in
            // 通配符匹配的路径在 params["*"] 中
            let path = params["*"] as? String ?? ""
            return WebViewController(path: path)
        }

        // ==================== 方式2：PageRoutable 协议注册（推荐）====================
        // 使用 Swift 协议 + 静态成员，一行注册

        registry.registerPage(OrderDetailViewController.self)
        registry.registerPage(FxChartViewController.self)
        registry.registerPage(PaymentViewController.self)

        // ==================== Action 路由注册 ====================

        registry.registerAction(actionName: "showPopup") { params, callback in
            let message = params["message"] as? String ?? ""
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
