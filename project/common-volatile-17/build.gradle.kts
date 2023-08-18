dependencies {
    compileOnly("ink.ptms.core:v12000:12000:mapped")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}