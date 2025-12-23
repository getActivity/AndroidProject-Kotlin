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

// 导入配置文件
apply(from = "${rootDir}/configs.gradle.kts")

// 读取 local.properties 文件配置
val properties = java.util.Properties()
if (project.file("local.properties").exists()) {
    properties.load(project.file("local.properties").inputStream())
    properties.forEach { (k, v) -> project.extra.set(k as String, v) }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

subprojects {
    // 通用配置
    pluginManager.withPlugin("com.android.base") {
        // 为所有模块应用 Kotlin 插件
        apply(plugin = "org.jetbrains.kotlin.android")

        // 配置 Android 扩展
        extensions.configure<BaseExtension> {

            // 编译源码版本
            compileSdkVersion(36)
            defaultConfig {
                // 最低安装版本
                minSdkVersion(23)
                // Android 版本适配指南：https://github.com/getActivity/AndroidVersionAdapter
                targetSdkVersion(36)
                versionName("1.0")
                versionCode(10)
            }

            // 支持 Java JDK 21
            compileOptions {
                targetCompatibility = JavaVersion.VERSION_21
                sourceCompatibility = JavaVersion.VERSION_21
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
                buildDir = File(buildDirPath, rootProject.name + "/build/${project.path.replace(':', '/')}")
            } else {
                // 将构建文件统一输出到项目根目录下的 build 文件夹
                buildDir = File(rootDir, "build/${project.path.replace(':', '/')}")
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
            // implementation(fileTree(mapOf("include" to listOf("*.jar", "*.aar"), "dir" to "libs")))
            add("implementation", fileTree(mapOf("include" to listOf("*.jar", "*.aar"), "dir" to "libs")))

            add("implementation", libs.appCompat)
            add("implementation", libs.material)

            add("implementation", libs.kotlinxCoroutinesCore)
            add("implementation", libs.kotlinxCoroutinesAndroid)

            add("implementation", libs.lifecycleRuntimeKTX)
            add("implementation", libs.lifecycleViewModelKTX)
            add("implementation", libs.lifecycleLiveDatalKTX)
        }
    }
}