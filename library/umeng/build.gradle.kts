plugins {
    alias(libs.plugins.library)
}

val UMENG_APP_KEY = rootProject.extra["UMENG_APP_KEY"].toString()
val QQ_APP_ID = rootProject.extra["QQ_APP_ID"].toString()
val QQ_APP_SECRET = rootProject.extra["QQ_APP_SECRET"].toString()
val WX_APP_ID = rootProject.extra["WX_APP_ID"].toString()
val WX_APP_SECRET = rootProject.extra["WX_APP_SECRET"].toString()

android {
    namespace = "com.hjq.umeng"

    buildFeatures {
        // 是否生成 BuildConfig 类
        buildConfig = true
    }

    defaultConfig {
        // 模块混淆配置
        consumerProguardFiles("proguard-umeng.pro")

        // 构建配置字段
        buildConfigField("String", "UM_KEY", "\"$UMENG_APP_KEY\"")
        buildConfigField("String", "QQ_ID", "\"$QQ_APP_ID\"")
        buildConfigField("String", "QQ_SECRET", "\"$QQ_APP_SECRET\"")
        buildConfigField("String", "WX_ID", "\"$WX_APP_ID\"")
        buildConfigField("String", "WX_SECRET", "\"$WX_APP_SECRET\"")

        // 清单占位符
        manifestPlaceholders += mapOf(
                "UM_KEY" to UMENG_APP_KEY,
                "QQ_ID" to QQ_APP_ID,
                "QQ_SECRET" to QQ_APP_SECRET,
                "WX_ID" to WX_APP_ID,
                "WX_SECRET" to WX_APP_SECRET
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
    api(libs.wechatSdk)
}