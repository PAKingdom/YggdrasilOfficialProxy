package io.github.karlatemp.yop

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode

internal class OfficialEndpoints(cdn: ConfigurationNode) {
    val hasJoined = buildString {
        val origin = cdn.originFor("sessionserver")
        if (origin != null) {
            append(origin)
        } else {
            // Keep the official host split to skip AuthLib Injector rewriting.
            append("https://sessionserver.")
            append("mojang.com")
        }
        append("/session/minecraft/hasJoined")
    }

    val profilesMinecraft = buildString {
        val origin = cdn.originFor("api")
        if (origin != null) {
            append(origin)
        } else {
            // Keep the official host split to skip AuthLib Injector rewriting.
            append("https://api.")
            append("mojang.com")
        }
        append("/profiles/minecraft")
    }

    private fun ConfigurationNode.originFor(service: String): String? {
        if (!node("enable").boolean) return null
        normalizeOrigin(node("$service-origin").string)?.let { return it }
        return normalizeOrigin(node("origin").string)?.let { "$it/$service" }
    }

    private fun normalizeOrigin(value: String?): String? {
        val origin = value?.trim()?.trimEnd('/')?.takeIf { it.isNotEmpty() } ?: return null
        return if (origin.startsWith("http://", ignoreCase = true) ||
                origin.startsWith("https://", ignoreCase = true)) origin else "http://$origin"
    }
}

internal fun CommentedConfigurationNode.setCdnDefaults() {
    comment("正版验证 CDN 设置：支持 API 和 Session Server 使用不同的反代域名。")
    node("enable").comment("是否启用 CDN。false 时忽略以下地址，直连 Mojang 官方服务。").set(false)
    node("origin").comment(
            "可选：兼容旧版的共用反代地址，会分别添加 /api 和 /sessionserver。\n" +
                    "使用独立域名时可以留空；所有地址均为空的服务会直连 Mojang。"
    ).set("")
    node("api-origin").comment(
            "api.mojang.com 的反代基础 URL，例如 https://api.mojang.com.example.com。\n" +
                    "不自动添加 /api；非空时覆盖 origin，留空时回退到 origin/api。"
    ).set("")
    node("sessionserver-origin").comment(
            "sessionserver.mojang.com 的反代基础 URL，例如 https://sessionserver.mojang.example.com。\n" +
                    "不自动添加 /sessionserver；非空时覆盖 origin，留空时回退到 origin/sessionserver。"
    ).set("")
}
