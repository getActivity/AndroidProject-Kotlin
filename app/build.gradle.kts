import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.aop)
    alias(libs.plugins.easylauncher)
}

apply(plugin = "org.jetbrains.kotlin.kapt")

val LOG_ENABLE = rootProject.extra["LOG_ENABLE"].toString().toBoolean()
val HOST_URL = rootProject.extra["HOST_URL"].toString()
val BUGLY_ID = rootProject.extra["BUGLY_ID"].toString()
val BUGLY_KEY = rootProject.extra["BUGLY_KEY"].toString()

// Android 代码规范文档：https://github.com/getActivity/AndroidCodeStandard
android {
    namespace = "com.hjq.demo"

    buildFeatures {
        // 是否生成 BuildConfig 类
        buildConfig = true
    }

    // 资源目录存放指引：https://developer.android.google.cn/guide/topics/resources/providing-resources
    defaultConfig {

        // 无痛修改包名：https://www.jianshu.com/p/17327e191d2e
        applicationId = "com.hjq.demo"

        // 仅保留中文语种的资源
        resConfigs("zh")

        // 仅保留 xxhdpi 图片资源（目前主流分辨率 1920 * 1080）
        resConfigs("xxhdpi")

        // 混淆配置
        proguardFiles("proguard-sdk.pro", "proguard-app.pro")

        // 日志开关
        buildConfigField("boolean", "LOG_ENABLE", LOG_ENABLE.toString())
        // 主机地址
        buildConfigField("String", "HOST_URL", "\"$HOST_URL\"")
        // BuglyId
        buildConfigField("String", "BUGLY_ID", "\"$BUGLY_ID\"")
        // BuglyKey
        buildConfigField("String", "BUGLY_KEY", "\"$BUGLY_KEY\"")

        // 仅保留 arm64-v8a 架构（需要注意的是 mmkv 库在 2.0 及之后的版本已经不支持在 32 位的机器上面运行）
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    sourceSets {
        getByName("main") {
            // res 资源目录配置
            res.srcDirs(
                    "src/main/res",
                    "src/main/res-common"
            )
        }
    }

    // Apk 签名的那些事：https://www.jianshu.com/p/a1f8e5896aa2
    signingConfigs {
        create("config") {
            // 从 gradle.properties 读取签名配置
            val keystorePropertiesFile = file("gradle.properties")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                // 加载 gradle.properties 文件内容
                keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
            }

            this.storeFile = file(keystoreProperties.getProperty("STORE_FILE"))
            this.storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
            this.keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
            this.keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
        }
    }

    // 构建配置：https://developer.android.google.cn/studio/build/build-variants
    buildTypes {
        getByName("debug") {
            // 给包名添加后缀
            applicationIdSuffix = ".debug"
            // 调试模式开关
            isDebuggable = true
            isJniDebuggable = true
            // 移除无用的资源
            isShrinkResources = false
            // 代码混淆开关
            isMinifyEnabled = false
            // 签名信息配置
            signingConfig = signingConfigs.getByName("config")
            // 添加清单占位符
            manifestPlaceholders += mapOf(
                    "app_name" to "@string/app_name_debug"
            )
        }

        getByName("preview") {
            initWith(getByName("debug"))
            applicationIdSuffix = ""
            // 添加清单占位符
            manifestPlaceholders += mapOf(
                    "app_name" to "@string/app_name_preview"
            )
        }

        getByName("release") {
            // 调试模式开关
            isDebuggable = false
            isJniDebuggable = false
            // 移除无用的资源
            isShrinkResources = true
            // 代码混淆开关
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("config")
            // 添加清单占位符
            manifestPlaceholders += mapOf(
                    "app_name" to "@string/app_name"
            )
        }
    }

    // AOP 配置（exclude 和 include 二选一）
    androidAopConfig {
        // 排除一些第三方库的包名（Gson、 LeakCanary 和 AOP 有冲突）
        // exclude 'androidx', 'com.google', 'com.squareup', 'org.apache', 'com.alipay', 'com.taobao', 'versions.9'
        // 只对以下包名做 AOP 处理
        defaultConfig.applicationId?.let {
            include(it)
        }
    }

    applicationVariants.all {
        // apk 输出文件名配置
        outputs.all {
            val output = this as BaseVariantOutputImpl
            val buildTypeName = buildType.name
            var outputFileName = "${rootProject.name}_v${versionName}_${buildTypeName}"
            if (buildTypeName == "release") {
                val dateStr = SimpleDateFormat("MMdd", Locale.getDefault()).format(Date())
                outputFileName += "_${dateStr}"
            }
            outputFileName += ".apk"
            output.outputFileName = outputFileName
        }
    }
}

// 添加构建依赖项：https://developer.android.google.cn/studio/build/dependencies
// api 与 implementation 的区别：https://www.jianshu.com/p/8962d6ba936e
dependencies {
    // 基类封装
    implementation(project(":library:base"))
    // 控件封装
    implementation(project(":library:widget"))
    // 友盟封装
    implementation(project(":library:umeng"))

    implementation(libs.deviceCompat)
    implementation(libs.xxPermissions)

    implementation(libs.titleBar)

    implementation(libs.toaster)

    implementation(libs.easyHttp)
    implementation(libs.okHttp)

    implementation(libs.gsonFactory) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    implementation(libs.gson) {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    implementation(libs.kotlinReflect)

    implementation(libs.shapeView)
    implementation(libs.shapeDrawable)

    implementation(libs.nestedScrollLayout)

    implementation(libs.glide)
    add("kapt", libs.glideCompiler)

    implementation(libs.immersionBar)

    implementation(libs.photoView)

    implementation(libs.buglyPro)

    implementation(libs.lottie)

    implementation(libs.refreshLayoutKernel)
    implementation(libs.refreshHeaderMaterial)

    implementation(libs.timber)

    implementation(libs.circleIndicator)

    implementation(libs.mmkvStatic) {
        exclude(group = "androidx.annotation", module = "annotation")
    }

    implementation(libs.softInputEvent)
    implementation(libs.androidAopCore)
    add("kapt", libs.androidAopApt)

    debugImplementation(libs.leakCanary)
    previewImplementation(libs.leakCanary)

    // 多语种：https://github.com/getActivity/MultiLanguages
    // 悬浮窗：https://github.com/getActivity/EasyWindow
    // 日志输出：https://github.com/getActivity/Logcat
    // 工具类：https://github.com/Blankj/AndroidUtilCode
    // 轮播图：https://github.com/bingoogolapple/BGABanner-Android
    // 二维码：https://github.com/bingoogolapple/BGAQRCode-Android
    // 跑马灯：https://github.com/sunfusheng/MarqueeView
    // 对象注解：https://www.jianshu.com/p/f1f888e4a35f
    // 对象存储：https://github.com/leavesC/DoKV
    // 多渠道打包：https://github.com/Meituan-Dianping/walle
    // 设备唯一标识：http://msa-alliance.cn/col.jsp?id=120
    // 嵌套滚动容器：https://github.com/donkingliang/ConsecutiveScroller
    // 隐私调用监控：https://github.com/allenymt/PrivacySentry
}