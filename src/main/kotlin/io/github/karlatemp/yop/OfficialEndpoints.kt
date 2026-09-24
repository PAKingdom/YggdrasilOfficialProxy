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
    comment("CDN settings")
    node("enable").set(false)
    node("origin").comment("Legacy shared origin; /api and /sessionserver are appended. Optional.").set("")
    node("api-origin").comment("Base URL for api.mojang.com; no /api prefix is added. Overrides origin.").set("")
    node("sessionserver-origin").comment("Base URL for sessionserver.mojang.com; no /sessionserver prefix is added. Overrides origin.").set("")
}
