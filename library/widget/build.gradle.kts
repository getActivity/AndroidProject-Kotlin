plugins {
    alias(libs.plugins.library)
}

android {
    namespace = "com.hjq.widget"
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:smallestWidth"))
}