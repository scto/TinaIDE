package com.scto.mobileide.core.config.ai

/**
 * AI 连接模式
 *
 * - MOBILE_GATEWAY: 私有后端网关模式，开源版不再内置账号鉴权与额度体系
 * - CUSTOM_BYOK: 用户自定义上游（BYOK），用用户填写的 API Key 鉴权
 */
enum class AiAccessMode {
    MOBILE_GATEWAY,
    CUSTOM_BYOK;
}
