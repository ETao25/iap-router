package com.iap.router.platform

import com.iap.router.core.ActionCallback
import com.iap.router.core.ProtocolParser
import com.iap.router.core.RouteLookupResult
import com.iap.router.core.RouteTable

/**
 * Android Action 执行器实现
 *
 * 负责执行已注册的 Action 路由
 */
class AndroidActionExecutor(
    /**
     * 路由表，用于查找 Action 配置
     */
    private val routeTable: RouteTable
) : ActionExecutor {

    override fun execute(actionName: String, params: Map<String, Any?>, callback: ActionCallback?) {
        // 构造 action URL 进行查找
        val actionPath = "action/$actionName"
        val parsedRoute = ProtocolParser.parseOrNull("iap://$actionPath")

        if (parsedRoute == null) {
            callback?.onError(IllegalArgumentException("Invalid action name: $actionName"))
            return
        }

        // 查找 Action 配置
        val lookupResult = routeTable.lookup(parsedRoute)

        when (lookupResult) {
            is RouteLookupResult.ActionRoute -> {
                // 合并参数
                val mergedParams = params.toMutableMap()
                mergedParams.putAll(lookupResult.matchResult.pathParams)

                // 执行 Action
                try {
                    lookupResult.handler.execute(mergedParams, callback)
                } catch (e: Exception) {
                    callback?.onError(e)
                }
            }
            else -> {
                callback?.onError(IllegalArgumentException("Action not found: $actionName"))
            }
        }
    }

    override fun canExecute(actionName: String): Boolean {
        val actionPath = "action/$actionName"
        val parsedRoute = ProtocolParser.parseOrNull("iap://$actionPath") ?: return false
        val lookupResult = routeTable.lookup(parsedRoute)
        return lookupResult is RouteLookupResult.ActionRoute
    }
}
