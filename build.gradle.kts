import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// AndroidProject-Kotlin 版本：v16.0
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.aop) apply false
    alias(libs.plugins.easyLauncher) apply false
}

fun main() {
    loadAppConfig()
}

subprojects {
    // 通用配置
    pluginManager.withPlugin("com.android.base") {
        // 为所有模块应用 Kotlin 插件
        plugins.apply("org.jetbrains.kotlin.android")
        plugins.apply("org.jetbrains.kotlin.kapt")

        // 配置 Android 扩展
        extensions.configure<BaseExtension> {

            // 编译源码版本
            compileSdkVersion(37)
            defaultConfig {
                // 最低安装版本
                minSdkVersion(23)
                // Android 版本适配指南：https://github.com/getActivity/AndroidVersionAdapter
                targetSdkVersion(37)
                versionName("1.0")
                versionCode(10)
            }

            compileOptions {
                // 允许项目代码使用 Java 21 的新语法
                sourceCompatibility = JavaVersion.VERSION_21
                // 编译后的字节码适配 Java 21 虚拟机，为了兼容安卓低版本系统 ART
                targetCompatibility = JavaVersion.VERSION_21
                // 开启 JDK 核心库脱糖：把高版本 JDK 新增的类 (如 java.time) 打包进 apk，让安卓低版本手机也能正常运行不闪退
                // https://developer.android.google.cn/studio/write/java8-support-table?hl=zh-cn
                isCoreLibraryDesugaringEnabled = true
            }

            // 设置存放 so 文件的目录
            sourceSets.getByName("main") {
                jniLibs.srcDirs("libs")
            }

            // 可在 Studio 最左侧中的 Build Variants 选项中切换默认的构建类型
            buildTypes {
                getByName("debug") {}
                create("preview") {
                    matchingFallbacks += listOf("debug")
                }
                getByName("release") {}
            }

            // 代码警告配置
            lintOptions {
                // HardcodedText：禁用文本硬编码警告
                // ContentDescription：禁用图片描述警告
                disable("HardcodedText", "ContentDescription")
            }

            // 读取 local.properties 文件配置
            val properties = java.util.Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { properties.load(it) }
            }

            val buildDirPath = properties.getProperty("build.dir")
            if (!buildDirPath.isNullOrEmpty()) {
                // 将构建文件统一输出到指定的目录下
                layout.buildDirectory = File(buildDirPath, rootProject.name + "/${project.path.replace(':', '/')}")
            }
        }

        // 配置 Kotlin 编译选项
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
                freeCompilerArgs.add("-Xcontext-parameters")
            }
        }

        // 通用依赖配置（排除 library:base，因为它使用 api 依赖）
        dependencies {
            // 依赖 libs 目录下所有的 jar 和 aar 包
            add("implementation", fileTree(mapOf("include" to listOf("*.jar", "*.aar"), "dir" to "libs")))

            add("implementation", libs.appCompat)
            add("implementation", libs.material)

            add("implementation", libs.kotlinxCoroutinesCore)
            add("implementation", libs.kotlinxCoroutinesAndroid)

            add("implementation", libs.lifecycleRuntimeKTX)
            add("implementation", libs.lifecycleViewModelKTX)
            add("implementation", libs.lifecycleLiveDatalKTX)

            add("coreLibraryDesugaring", libs.desugarJdkLibs)
        }
    }
}

/**
 * 根据 Gradle 命令行参数配置 App 的服务器类型和相关参数
 */
fun loadAppConfig() {
    // 测试服
    val serverTypeTest = "test"
    // 预发布服
    val serverTypePreview = "pre"
    // 正式服
    val serverTypeProduct = "product"

    val taskName = gradle.startParameter.taskNames.firstOrNull() ?: ""
    // 打印当前执行的任务名称
    println("GradleLog TaskNameOutput $taskName")

    var serverType: String

    serverType = when {
        taskName.endsWith("Debug") -> serverTypeTest
        taskName.endsWith("Preview") -> serverTypePreview
        else -> serverTypeProduct
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
        serverTypeTest, serverTypePreview -> {
            extra["LOG_ENABLE"] = true
            extra["BUGLY_ID"] = "请自行替换 Bugly 上面的 AppId"
            extra["BUGLY_KEY"] = "请自行替换 Bugly 上面的 AppKey"
            extra["HOST_URL"] = if (serverType == serverTypePreview) {
                "https://www.pre.baidu.com/"
            } else {
                "https://www.test.baidu.com/"
            }
        }
        serverTypeProduct -> {
            extra["LOG_ENABLE"] = false
            extra["BUGLY_ID"] = "请自行替换 Bugly 上面的 AppId"
            extra["BUGLY_KEY"] = "请自行替换 Bugly 上面的 AppKey"
            extra["HOST_URL"] = "https://www.baidu.com/"
        }
    }
}

// 由于 Gradle 这种脚本代码没有函数入口，所以必须手动调用一下 main 方法
main()