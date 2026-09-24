package io.github.karlatemp.yop

import org.junit.Assert.assertEquals
import org.junit.Test
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.BufferedReader
import java.io.StringReader

class OfficialEndpointsTest {
    private fun endpoints(cdn: String): OfficialEndpoints {
        val configuration = HoconConfigurationLoader.builder()
                .source { BufferedReader(StringReader("CDN { $cdn }")) }
                .build().load()
        return OfficialEndpoints(configuration.node("CDN"))
    }

    @Test
    fun `separate origins replace the official hosts without adding service prefixes`() {
        val endpoints = endpoints("""
            enable=true
            api-origin="https://api.mojang.com.example.com"
            sessionserver-origin="https://sessionserver.mojang.example.com"
        """)
        assertEquals("https://api.mojang.com.example.com/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("https://sessionserver.mojang.example.com/session/minecraft/hasJoined", endpoints.hasJoined)
    }

    @Test
    fun `legacy origin keeps both service prefixes`() {
        val endpoints = endpoints("""
            enable=true
            origin="https://cdn.example.com/"
        """)
        assertEquals("https://cdn.example.com/api/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("https://cdn.example.com/sessionserver/session/minecraft/hasJoined", endpoints.hasJoined)
    }

    @Test
    fun `each separate origin overrides only its own legacy endpoint`() {
        val apiOverride = endpoints("""
            enable=true
            origin="https://legacy.example.com"
            api-origin="https://api.example.com"
        """)
        assertEquals("https://api.example.com/profiles/minecraft", apiOverride.profilesMinecraft)
        assertEquals("https://legacy.example.com/sessionserver/session/minecraft/hasJoined", apiOverride.hasJoined)

        val sessionOverride = endpoints("""
            enable=true
            origin="https://legacy.example.com"
            sessionserver-origin="https://session.example.com"
        """)
        assertEquals("https://legacy.example.com/api/profiles/minecraft", sessionOverride.profilesMinecraft)
        assertEquals("https://session.example.com/session/minecraft/hasJoined", sessionOverride.hasJoined)
    }

    @Test
    fun `disabled CDN ignores all configured origins`() {
        assertOfficial(endpoints("""
            enable=false
            origin="https://legacy.example.com"
            api-origin="https://api.example.com"
            sessionserver-origin="https://session.example.com"
        """))
    }

    @Test
    fun `missing or blank origins use official endpoints`() {
        assertOfficial(endpoints(""))
        assertOfficial(endpoints("""
            enable=true
            origin=" "
            api-origin=""
            sessionserver-origin="  "
        """))

        val endpoints = endpoints("""
            enable=true
            api-origin="https://api.example.com"
        """)
        assertEquals("https://api.example.com/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("https://sessionserver.mojang.com/session/minecraft/hasJoined", endpoints.hasJoined)
    }

    @Test
    fun `blank service overrides fall back to legacy origin`() {
        val endpoints = endpoints("""
            enable=true
            origin="cdn.example.com/root/"
            api-origin=" "
            sessionserver-origin=""
        """)
        assertEquals("http://cdn.example.com/root/api/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("http://cdn.example.com/root/sessionserver/session/minecraft/hasJoined", endpoints.hasJoined)
    }

    @Test
    fun `origins normalize whitespace and trailing slashes while retaining explicit paths`() {
        val endpoints = endpoints("""
            enable=true
            api-origin=" http://api.example.com:8080/custom/api/// "
            sessionserver-origin=" session.example.com/custom/session/ "
        """)
        assertEquals("http://api.example.com:8080/custom/api/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("http://session.example.com/custom/session/session/minecraft/hasJoined", endpoints.hasJoined)
    }

    @Test
    fun `generated defaults expose both origins and disable CDN`() {
        val cdn = CommentedConfigurationNode.root()
        cdn.setCdnDefaults()
        assertEquals(false, cdn.node("enable").boolean)
        assertEquals("", cdn.node("origin").string)
        assertEquals("", cdn.node("api-origin").string)
        assertEquals("", cdn.node("sessionserver-origin").string)
        assertOfficial(OfficialEndpoints(cdn))
    }

    private fun assertOfficial(endpoints: OfficialEndpoints) {
        assertEquals("https://api.mojang.com/profiles/minecraft", endpoints.profilesMinecraft)
        assertEquals("https://sessionserver.mojang.com/session/minecraft/hasJoined", endpoints.hasJoined)
    }
}
