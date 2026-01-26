import UIKit
import KMPRouter

/// Swift protocol for declarative route registration.
/// Both `pattern` and `createPage` are static members.
public protocol PageRoutable {
    static var pattern: String { get }
    static func createPage(params: [String: Any?]) -> UIViewController
}

/// RouteRegistry extension to support PageRoutable protocol
public extension RouteRegistry {
    /// Register a ViewController that implements PageRoutable protocol
    func registerPage<T: PageRoutable>(_ type: T.Type) {
        registerPage(pattern: T.pattern) { params in
            T.createPage(params: params)
        }
    }

    /// Batch register multiple PageRoutable types
    func registerPages(_ types: PageRoutable.Type...) {
        types.forEach { registerPage($0) }
    }
}
