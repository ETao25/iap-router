package com.iap.router.platform

import com.iap.router.core.ActionCallback

/**
 * Action 执行器接口
 * 由各平台实现具体的 Action 执行逻辑
 */
interface ActionExecutor {
    /**
     * 执行 Action
     * @param actionName Action 名称
     * @param params Action 参数
     * @param callback 执行结果回调
     */
    fun execute(actionName: String, params: Map<String, Any?>, callback: ActionCallback?)

    /**
     * 检查是否支持执行指定 Action
     * @param actionName Action 名称
     * @return 是否支持
     */
    fun canExecute(actionName: String): Boolean
}
