package com.iap.router.testutil

import com.iap.router.platform.PlatformPage

/**
 * 测试用页面实现（JVM）
 */
class TestPage(val name: String = "TestPage") : PlatformPage()

/**
 * 订单详情测试页面
 */
class OrderDetailTestPage(val orderId: String? = null) : PlatformPage()

/**
 * 账户设置测试页面
 */
class AccountSettingsTestPage : PlatformPage()

/**
 * FX 图表测试页面
 */
class FxChartTestPage(val pairId: String? = null) : PlatformPage()

/**
 * 支付测试页面
 */
class PaymentTestPage : PlatformPage()
