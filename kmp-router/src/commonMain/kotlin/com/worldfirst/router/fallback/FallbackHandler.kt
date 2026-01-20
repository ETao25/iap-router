package com.worldfirst.router.fallback

import com.worldfirst.router.model.RouteContext

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
 * 单路由降级配置
 */
data class FallbackConfig(
    /**
     * 降级条件判断
     * 返回 true 时触发降级
     */
    val condition: () -> Boolean,

    /**
     * 降级动作
     */
    val action: FallbackAction
)

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
