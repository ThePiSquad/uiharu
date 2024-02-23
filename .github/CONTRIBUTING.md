# 贡献指南

## Docker 部署相关

正确创建配置文件 `application.conf`，安装 Docker 之后，运行以下命令创建 Docker 镜像

```bash
docker build -t <tag> .
```

然偶将其推送到镜像托管仓库。

使用以下命令启动镜像

```bash
docker run -p <port:port> -d <image:tag>
# -p 设置端口暴露
# -d detach
```

## application.conf 模板

文件路径：src/main/resources/application.conf

Ktor 应用启动时会自动加载该文件，敏感配置信息联系管理员获取。

另外，在执行 `gradlew build` 等操作时，该文件会被拷贝到结果中，所以请不要将构建的内容上传到公共平台。

```text
ktor {
    development = true

    deployment {
        port = 8080
    }
    application {
        modules = [
            club.pisquad.uiharu.github.WebhookKt.githubWebhook,
            club.pisquad.uiharu.qqbot.QQBotKt.qqbot
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