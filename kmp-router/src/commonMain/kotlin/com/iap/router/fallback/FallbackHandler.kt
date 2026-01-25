package com.iap.router.fallback

import com.iap.router.core.RouteMatcher
import com.iap.router.model.RouteContext

/**
 * 降级动作
 */
sealed class FallbackAction {
    /**
     * 导航到指定 URL
     */
    data class NavigateTo(val url: String) : FallbackAction()

    /**
     * 显示错误信息
     */
    data class ShowError(val message: String) : FallbackAction()

    /**
     * 忽略，不做处理
     */
    data object Ignore : FallbackAction()

    /**
     * 自定义处理
     */
    data class Custom(val handler: () -> Unit) : FallbackAction()
}

/**
 * 全局降级处理器接口
 */
interface FallbackHandler {
    /**
     * 路由未找到时的处理
     */
    fun onRouteNotFound(context: RouteContext): FallbackAction

    /**
     * 路由执行错误时的处理
     */
    fun onRouteError(context: RouteContext, error: Throwable): FallbackAction
}

/**
 * 默认降级处理器
 */
class DefaultFallbackHandler : FallbackHandler {
    override fun onRouteNotFound(context: RouteContext): FallbackAction {
        return FallbackAction.Ignore
    }

    override fun onRouteError(context: RouteContext, error: Throwable): FallbackAction {
        return FallbackAction.Ignore
    }
}

/**
 * 降级管理器
 * 支持全局降级和基于 pattern 的降级规则
 *
 * 使用示例:
 * ```
 * val fallbackManager = FallbackManager()
 *
 * // 设置全局降级（路由未找到时）
 * fallbackManager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))
 *
 * // 设置特定 pattern 的降级（使用通配符）
 * fallbackManager.addPatternFallback("user/wildcard", FallbackAction.NavigateTo("iap://login"))
 * fallbackManager.addPatternFallback("payment/wildcard", FallbackAction.NavigateTo("iap://h5/payment"))
 *
 * // 带条件的 pattern 降级
 * fallbackManager.addPatternFallback(
 *     pattern = "vip/wildcard",
 *     condition = { !isVipUser() },
 *     action = FallbackAction.NavigateTo("iap://vip/upgrade")
 * )
 * ```
 * 注意：实际使用时 wildcard 应替换为星号
 */
class FallbackManager : FallbackHandler {

    /**
     * 全局降级动作（路由未找到时）
     */
    private var globalNotFoundAction: FallbackAction = FallbackAction.Ignore

    /**
     * 全局错误降级动作
     */
    private var globalErrorAction: FallbackAction = FallbackAction.Ignore

    /**
     * Pattern 降级规则列表
     * 按添加顺序匹配，先匹配的先生效
     */
    private val patternRules = mutableListOf<PatternFallbackRule>()

    /**
     * 设置全局降级动作（路由未找到时）
     */
    fun setGlobalFallback(action: FallbackAction) {
        globalNotFoundAction = action
    }

    /**
     * 设置全局错误降级动作
     */
    fun setGlobalErrorFallback(action: FallbackAction) {
        globalErrorAction = action
    }

    /**
     * 添加 pattern 降级规则
     *
     * @param pattern 路由模式，如 "user/星号", "payment/:id"
     * @param action 降级动作
     */
    fun addPatternFallback(pattern: String, action: FallbackAction) {
        patternRules.add(PatternFallbackRule(pattern, condition = { true }, action))
    }

    /**
     * 添加带条件的 pattern 降级规则
     *
     * @param pattern 路由模式，如 "user/星号", "payment/:id"
     * @param condition 条件判断，返回 true 时触发降级
     * @param action 降级动作
     */
    fun addPatternFallback(pattern: String, condition: () -> Boolean, action: FallbackAction) {
        patternRules.add(PatternFallbackRule(pattern, condition, action))
    }

    /**
     * 移除指定 pattern 的降级规则
     */
    fun removePatternFallback(pattern: String) {
        patternRules.removeAll { it.pattern == pattern }
    }

    /**
     * 清除所有 pattern 降级规则
     */
    fun clearPatternFallbacks() {
        patternRules.clear()
    }

    override fun onRouteNotFound(context: RouteContext): FallbackAction {
        // 先检查 pattern 规则
        val path = context.parsedRoute.path
        for (rule in patternRules) {
            if (RouteMatcher.match(path, rule.pattern) != null && rule.condition()) {
                return rule.action
            }
        }
        // 返回全局降级
        return globalNotFoundAction
    }

    override fun onRouteError(context: RouteContext, error: Throwable): FallbackAction {
        // 先检查 pattern 规则
        val path = context.parsedRoute.path
        for (rule in patternRules) {
            if (RouteMatcher.match(path, rule.pattern) != null && rule.condition()) {
                return rule.action
            }
        }
        // 返回全局错误降级
        return globalErrorAction
    }
}

/**
 * Pattern 降级规则
 */
private data class PatternFallbackRule(
    val pattern: String,
    val condition: () -> Boolean,
    val action: FallbackAction
)
