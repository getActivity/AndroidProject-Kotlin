plugins {
    alias(libs.plugins.library)
}

android {
    namespace = "com.hjq.base"

    defaultConfig {
        // 模块混淆配置
        consumerProguardFiles("proguard-base.pro")
    }
}