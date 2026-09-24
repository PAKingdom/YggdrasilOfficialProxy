# YggdrasilOfficialProxy v2.3.1

- Power by kotlin.
- Configuration powered by [@SpongePowered/Configure](https://github.com/SpongePowered/Configurate)
- AuthLib Injector by [@yushijinhun/authlib-injector](https://github.com/yushijinhun/authlib-injector/)
- HttpClient powered by [Ktor](http://ktor.io/) 
  & [OkHttp](https://github.com/square/okhttp)


> [!NOTE]
>
> 上游 YggdrasilOfficialProxy 已宣布弃用，后继项目为 [YggdrasilGateway](https://github.com/YggdrasilGateway/YggdrasilGateway)。
> 本仓库是基于原版 2.3.0 的修改分支，2.3.1 的改动如下。

# 相比原版 2.3.0 的改动

| 项目 | 原版 2.3.0 | 本分支 2.3.1 |
| --- | --- | --- |
| CDN 域名 | 只能填写一个 `CDN.origin` | 新增 `CDN.api-origin` 和 `CDN.sessionserver-origin`，可分别填写两个反代域名 |
| 独立地址的请求路径 | 共用地址后自动添加 `/api` 或 `/sessionserver` | 独立地址直接作为对应服务的基础 URL，不额外添加这两层目录 |
| 旧配置兼容 | 共用 `origin` 按路径区分两个服务 | 保留原有行为，独立地址非空时优先；无需删除旧配置 |
| 地址处理 | 未填写协议时使用 HTTP | 保留默认 HTTP，去除地址首尾空白及末尾斜杠，支持显式路径前缀 |
| 配置模板与注释 | 普通启动仅生成单个 CDN 地址，向导不生成 CDN 配置 | 两种方式均生成完整的 CDN 字段，并附中文注释解释开关、路径和回退规则 |
| 版本与验证 | 2.3.0 | 版本、JAR 文件名和首次配置提示统一为 2.3.1；增加 8 项 CDN 配置回归测试 |

启用 CDN 后，各服务按“独立地址 → 共用 `origin` 加服务路径 → Mojang 官方地址”的顺序选择地址。
关闭 CDN 时，两个服务均直连官方地址。

# Downloading

前往 [v2.3.1 Release](https://github.com/PAKingdom/YggdrasilOfficialProxy/releases/tag/v2.3.1) 下载。
三个 JAR 均支持双 CDN 域名，请按启动方式选择：

| 下载 | 用途 |
| --- | --- |
| [YggdrasilOfficialProxy-2.3.1-paperclip.jar](https://github.com/PAKingdom/YggdrasilOfficialProxy/releases/download/v2.3.1/YggdrasilOfficialProxy-2.3.1-paperclip.jar) | 随 Minecraft 服务器以 `-javaagent:...` 加载，携带较完整的依赖，通常推荐此版本 |
| [YggdrasilOfficialProxy-2.3.1-proxy.jar](https://github.com/PAKingdom/YggdrasilOfficialProxy/releases/download/v2.3.1/YggdrasilOfficialProxy-2.3.1-proxy.jar) | 以 `java -jar ...` 单独运行认证代理 |
| [YggdrasilOfficialProxy-2.3.1-minecraft.jar](https://github.com/PAKingdom/YggdrasilOfficialProxy/releases/download/v2.3.1/YggdrasilOfficialProxy-2.3.1-minecraft.jar) | 精简依赖的 `-javaagent:...` 版本，需要 Minecraft 服务端提供部分依赖 |

从原版升级时，替换所用版本的 JAR，并同步更新启动脚本中的文件名。
保留 `YggdrasilOfficialProxy.conf`；需要双域名时参考下方 [CDN 配置](#cdn) 添加两个字段，保持 `edited=true`，然后重启。

# 向导模式

下载适合的YOP版本(见下文),然后放置于服务器根目录中

执行 `java -jar YggdrasilOfficialProxy-XXX.jar setup` 进入向导模式

![setup.png](./setup.png)


# Booting
我们提供了两种启动方式：作为 agent 加载，或作为独立服务器运行。

## Proxy Booting
Proxy Edition, 是以一个独立服务器的方式运行的, 他的好处在于:
- 独立于MC服务器, 方便另外管理
- 重新配置只需要配置一个服务器 (见下文)
- 可以快速重新启动代理服务器而不需要重新启动整个群组

## Agent Booting

Agent Edition, 是以修改 javaagent参数的形式启动的,
代理服务器会随着MC的启动而启动, 随着MC的关闭而关闭
好处在于
- 不需要另外准备服务器或者运行环境
- 同样只需要配置一个服务器
- **特别说明**:
  - YggdrasilOfficialProxy-minecraft.jar 无法工作时请换用 `-paperclip` 版本
  - Spigot服务器无法使用 `YggdrasilOfficialProxy-minecraft.jar` 时请使用 `-paperclip`
    版本并上报相关错误
  - `paperclip` 版本携带了全部用到的依赖库, 大是必然的

# Configuration
在启动 YggdrasilOfficialProxy 的时候,
我们首先会为您生成一个配置文件. 然后服务器会随之关闭
你需要编辑 `YggdrasilOfficialProxy.conf`, 并且最重要的一点
最**重要**的一点, `edited=true`!

## Standard Proxy Server setup
Proxy Edition:

在启动了 Yggdrasil Proxy Server (Proxy Edition) 之后,
你可以看到以下日志

```
[YggdrasilOfficialProxy] Server running on XXXXX:XXXX
```
假设(只是假设!)运行在 `192.168.78.34:30500` 上

假设这是你的启动脚本
```shell script
#
# 如果你是 BungeeCord 群组服务器,
# 那么这是 BungeeCord 的启动脚本
#
# 如果不是, 那么就是独立服务器的启动脚本
#

java -javaagent:authlib-injector.jar=.... ....
```

我们需要将他修改成这样

```shell script
java -javaagent:authlib-injector.jar=http://192.168.78.34:30500 ....
```
然后 `:wq`, 重新启动, 此时启动Authlib-Injector可能会警告
`[authlib-injector.config] [WARNING] You are using HTTP protocol, which is INSECURE! Please switch to HTTPS if possible.`

我们直接选择将其无视掉, 因为目标服务器是我们的内网服务器, 直接安全的无视掉就可以了

## Agent Edition Startup
首先我们需要打开我们的服务器启动脚本

假设(假设!只是假设!)
- 我们的 YggdrasilOfficialProxy AgentEdition 叫做 `YggdrasilOfficialProxy-2.3.1-paperclip.jar`
- 我们的 AuthLib Injector 叫做 `authlib-injector-1.1.26-41a7a47.jar`
- 我们使用的 Yggdrasil API Root 为 `https://skin.prinzeugen.net/api/yggdrasil`

这是我们的启动脚本:
```shell script
#
# 如果你是 BungeeCord 群组服务器,
# 那么这是 BungeeCord 的启动脚本
#
# 如果不是, 那么就是独立服务器的启动脚本
#

java -javaagent:authlib-injector-1.1.26-41a7a47.jar=https://skin.prinzeugen.net/api/yggdrasil ....
```
然后, 我们需要直接把原来的 `-javaagent:authlib-injector-1.1.26-41a7a47.jar=https://skin.prinzeugen.net/api/yggdrasil` 删掉.
对,删掉, 一点都不要留, 然后换上我们的配置
```shell script
java -javaagent:YggdrasilOfficialProxy-2.3.1-paperclip.jar ....
```
然后直接启动服务器, 对, 直接启动服务器, 我们需要生成 YggdrasilOfficialProxy 的服务器,
如果你写了自动重启, 记得, 在第一次启动后, 关掉. 虽然不关也没问题.

然后打开我们的配置. 修改
```hocon
# API Root, 将这里改成原来的 API Root！
api="https://skin.prinzeugen.net/api/yggdrasil"
# Authlib Injector的位置, 无效的话服务器将无法启动
authlib-injector="authlib-injector-1.1.26-41a7a47.jar"
# 重要! 重要!, 必须为 true!
edited=true
# 在合并玩家查找的时候, 是否优先使用正版服务器的内容
official-first=false
# 代理服务器的设置
server {
    # 换成 127.0.0.1
    host="127.0.0.1"
    # 随便写, 随便写一个没有被占用的端口
    port=32217
}
# Proxy settings
proxy {
    just-example-for-http {
        type=http
        url="http://localhost/proxy"
    }
    # The proxy of official connecting
    official {
        host=localhost
        port=1080
        # type=socks
        type=direct
    }
    # The proxy of yggdrasil connecting
    yggdrasil {
        type=direct
    }
}
```
在全部工作完成之后, 输入 `:wq`, 完成我们的配置吧!

# 特别说明

根据 Minecraft Server, 只要UUID不一样, name一样服务器都会允许玩家加入游戏,
原本 Minecraft 的 Name 是不可能重复的, 但是, 但是! YggdrasilOfficialProxy!
打破了这个限制! 由于 YggdrasilOfficialProxy 的工作方式是替换 Mojang 的 hasJoined.
所以！ 你可能会遇到这种奇葩情况！

![](./export.png)

两个分别来自正版/第三方验证服务器的同名玩家,
同时加到了一个服务器里! 而且服务器还觉得没什么奇怪的!

所以！所以！你需要自己写一个插件! 保证，不会出现两个一样名字的玩家!
这点非常重要！

# CDN

由于中国大陆部分地区访问 `mojang` 官方验证服务器较慢或无法连接  
原版 `2.3.0` 加入了单地址 CDN 加速，本分支 `2.3.1` 支持 API 和 Session Server 使用不同的 CDN 域名。
在现有配置文件的 `CDN` 中添加以下字段即可，无需删除配置文件：

```hocon
CDN {
    # 启用 CDN；false 时忽略所有反代地址，直连官方服务
    enable=true
    # api.mojang.com 的反代基础 URL，不会额外添加 /api
    api-origin="https://api.mojang.com.example.com"
    # sessionserver.mojang.com 的反代基础 URL，不会额外添加 /sessionserver
    sessionserver-origin="https://sessionserver.mojang.example.com"
    # 可选：旧版共用地址；使用两个独立地址时可留空
    origin=""
}
```

这两个字段直接填写各自反代的基础 URL，程序不会额外添加 `/api` 或 `/sessionserver`。
上述配置实际请求的地址分别为：

- `https://api.mojang.com.example.com/profiles/minecraft`
- `https://sessionserver.mojang.example.com/session/minecraft/hasJoined`

请保留 `https://` 或 `http://` 协议前缀；省略时兼容旧行为，使用 `http://`。
允许 URL 末尾带 `/`，也可以显式包含反代所需的路径前缀。
启用 CDN 时，各服务优先使用自己的非空字段；未填写的服务会回退到旧 `origin` 加对应路径，
如果 `origin` 也为空则直连该服务的 Mojang 官方地址。`enable=false` 时两个服务均直连官方地址。

首次启动和 `setup` 向导生成的配置都包含上述四个字段及中文说明，默认 `enable=false`，三个地址均为空。
已有配置的注释不会自动改写，可以直接参考上述示例手动添加新字段。

原来的单域名、按路径反代方式仍然兼容：

```hocon
# CDN settings
CDN {
    enable=true
    origin="https://cdn.example.com"
}
```
好了, 现在配置文件有了如何配置一个加速源呢  
此处以 Nginx 反代为例 当然你可以用你熟悉的 web 服务器
```
location /sessionserver/ {
    proxy_pass https://sessionserver.mojang.com/;
}

location /api/ {
    proxy_pass https://api.mojang.com/;
}
```
配置完成后将你的服务器基础 URL 填入 `origin` 字段并将 `enable` 改成 `true` 即可。
这种方式仍会请求 `origin/api/...` 和 `origin/sessionserver/...`。

# 从源码构建

使用 JDK 11 和仓库自带的 Gradle Wrapper：

```shell
# Linux / macOS
./gradlew test standardShadowJar shadowJar shadowPaperclip

# Windows
gradlew.bat test standardShadowJar shadowJar shadowPaperclip
```

三个发行版 JAR 输出到 `build/libs/`，文件名均包含 `2.3.1`。

# 交流群
QQ Group: `832210691`
