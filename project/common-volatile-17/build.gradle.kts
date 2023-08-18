dependencies {
    compileOnly("ink.ptms.core:v12000:12000:mapped")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}