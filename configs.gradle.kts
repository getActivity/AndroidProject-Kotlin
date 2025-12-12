// 测试服
val SERVER_TYPE_TEST = "test"
// 预发布服
val SERVER_TYPE_PREVIEW = "pre"
// 正式服
val SERVER_TYPE_PRODUCT = "product"

var taskName = gradle.startParameter.taskNames.firstOrNull() ?: ""
// 打印当前执行的任务名称
println("GradleLog TaskNameOutput $taskName")

var serverType = SERVER_TYPE_PRODUCT

serverType = when {
    taskName.endsWith("Debug") -> SERVER_TYPE_TEST
    taskName.endsWith("Preview") -> SERVER_TYPE_PREVIEW
    else -> SERVER_TYPE_PRODUCT
}

// 从 Gradle 命令中读取参数配置，例如：./gradlew assembleRelease -P ServerType="test"
if (project.hasProperty("ServerType")) {
    serverType = project.property("ServerType").toString()
}

// 打印当前服务器配置
println("GradleLog ServerTypeOutput $serverType")

// 友盟 AppKey
extra["UMENG_APP_KEY"] = "5cb16d93570df399fd0014e2"
// QQ AppId
extra["QQ_APP_ID"] = "101828096"
// QQ Secret
extra["QQ_APP_SECRET"] = "9dfd3300c3aa3c4596a07796c64914b2"
// 微信 AppId
extra["WX_APP_ID"] = "wxd35706cc9f46114c"
// 微信 Secret
extra["WX_APP_SECRET"] = "0c8c7cf831dd135a32b3e395ea459b5a"

when (serverType) {
    SERVER_TYPE_TEST, SERVER_TYPE_PREVIEW -> {
        extra["LOG_ENABLE"] = true
        extra["BUGLY_ID"] = "请自行替换 Bugly 上面的 AppId"
        extra["BUGLY_KEY"] = "请自行替换 Bugly 上面的 AppKey"
        extra["HOST_URL"] = if (serverType == SERVER_TYPE_PREVIEW) {
            "https://www.pre.baidu.com/"
        } else {
            "https://www.test.baidu.com/"
        }
    }
    SERVER_TYPE_PRODUCT -> {
        extra["LOG_ENABLE"] = false
        extra["BUGLY_ID"] = "请自行替换 Bugly 上面的 AppId"
        extra["BUGLY_KEY"] = "请自行替换 Bugly 上面的 AppKey"
        extra["HOST_URL"] = "https://www.baidu.com/"
    }
}