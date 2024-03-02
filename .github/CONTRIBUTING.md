# 贡献指南

## 指定 config 文件

使用 `application.conf` 作为发布时使用的配置文件。 另外，在执行 `gradlew build` 等操作时，该文件会被拷贝到结果中，所以请不要将构建的内容上传到公共平台。

为了方便开发，创建多个 profile，在启动应用时候指定特定的文件。例如

- application.dev.conf
- application.test.conf

在 gradle 运行时指定配置文件的方法如下：
```bash
.\gradlew run --args="-config=src/main/resources/application.dev.conf"
```

## Docker 部署

正确创建配置文件 `application.conf`，安装 Docker 之后，运行以下命令创建 Docker 镜像

```bash
docker build -t <tag> .
```

然偶将其推送到镜像托管仓库。

使用以下命令启动镜像

```bash
docker run -p <port:port> -d <image:tag> # -p 设置端口暴露 -d detach
```

## 附录

### application.conf 模板

文件路径：src/main/resources/application.conf

```text
ktor {
    development = true

    deployment {
        port = 8080
    }
    application {
        modules = [
            club.pisquad.uiharu.github.WebhookKt.githubWebhook,
            club.pisquad.uiharu.qqbot.QQBotKt.qqbot,
            club.pisquad.uiharu.qqbot.websocket.QQBotWebsocketKt.QQBotWebsocket
        ]
    }
}
qqBot {
    appId = ""
    clientSecret = ""
    channel {
        githubNotice = ""
    }
    markdownTemplateId{
        githubWebhook = ""
    }
}
github {
    appId = ""
    privateKey = "path to .pem file"
}
```