package com.worldfirst.router.model

/**
 * 导航选项配置
 */
data class NavigationOptions(
    /**
     * 是否带动画
     */
    val animated: Boolean = true,

    /**
     * iOS Present 样式
     * 可选值: fullScreen / pageSheet / formSheet 等
     */
    val presentStyle: String? = null,

    /**
     * 导航模式
     */
    val navMode: NavMode = NavMode.PUSH,

    /**
     * 平台特有参数
     */
    val extras: Map<String, Any?> = emptyMap()
)

/**
 * 导航模式
 */
enum class NavMode {
    /** 压栈导航（默认） */
    PUSH,
    /** 模态呈现 */
    PRESENT
}
