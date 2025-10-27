[versions]
# ... suas outras versões
hilt = "2.51.1"
hiltNavigationCompose = "1.2.0"

[libraries]
# ... suas outras bibliotecas
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

[plugins]
# ... seus outros plugins
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" } # Verifique se já não existe
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }