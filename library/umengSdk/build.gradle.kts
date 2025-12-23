plugins {
    alias(libs.plugins.library)
}

val umengAppKey = rootProject.extra["UMENG_APP_KEY"].toString()
var qqAppId = rootProject.extra["QQ_APP_ID"].toString()
val qqAppSecret = rootProject.extra["QQ_APP_SECRET"].toString()
val wxAppId = rootProject.extra["WX_APP_ID"].toString()
val wxAppSecret = rootProject.extra["WX_APP_SECRET"].toString()

android {
    namespace = "com.hjq.umeng.sdk"

    buildFeatures {
        // 是否生成 BuildConfig 类
        buildConfig = true
    }

    defaultConfig {
        // 模块混淆配置
        consumerProguardFiles("proguard-umeng.pro")

        // 构建配置字段
        buildConfigField("String", "UM_KEY", "\"$umengAppKey\"")
        buildConfigField("String", "QQ_ID", "\"$qqAppId\"")
        buildConfigField("String", "QQ_SECRET", "\"$qqAppSecret\"")
        buildConfigField("String", "WX_ID", "\"$wxAppId\"")
        buildConfigField("String", "WX_SECRET", "\"$wxAppSecret\"")

        // 清单占位符
        manifestPlaceholders += mapOf(
                "UM_KEY" to umengAppKey,
                "QQ_ID" to qqAppId,
                "QQ_SECRET" to qqAppSecret,
                "WX_ID" to wxAppId,
                "WX_SECRET" to wxAppSecret
        )
    }
}

// 友盟统计集成文档：https://developer.umeng.com/docs/119267/detail/118584
// 友盟社会化集成文档：https://developer.umeng.com/docs/128606/detail/193879
// 友盟远程仓库地址：https://repo1.maven.org/maven2
dependencies {
    api(libs.umengCommon)
    api(libs.umengAsms)

    api(libs.umengShareCore)
    api(libs.umengShareWX)
    api(libs.umengShareQQ)

    api(libs.qqSdk)
    api(libs.wxSdk)
}