# 贡献指南

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