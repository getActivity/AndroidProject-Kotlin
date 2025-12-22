plugins {
    alias(libs.plugins.library)
}

android {
    namespace = "com.hjq.base"
}

dependencies {
    implementation(project(":library:core"))
}