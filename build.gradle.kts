import com.iskportal.kfs.binder.plugin.BinderPlugin
import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import java.nio.file.Paths

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("dev.toastbits.kjna").apply(false)
    id("com.iskportal.kfs.binder.plugin").apply(false)
}


allprojects {

    group = "com.iskportal"
    version = ext["project.version"] as String

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {

    apply {
        plugin("org.jetbrains.kotlin.multiplatform")
        plugin("maven-publish")
        plugin("org.jetbrains.dokka")
        plugin("signing")
        plugin("dev.toastbits.kjna")
        plugin("com.iskportal.kfs.binder.plugin")
    }


    val kfsBuildDir = project.layout.buildDirectory.dir("kfs").get().asFile
    val kfsJavaSourceDir = kfsBuildDir.resolve("src/java")


    kotlin {
        withSourcesJar(true)
        jvmToolchain(22)
        jvm()

        sourceSets {
            commonMain {
                kotlin {
                    srcDirs(kfsBuildDir.resolve("src/commonMain"))
                }
            }
            jvmMain {
                kotlin {
                    srcDirs(kfsBuildDir.resolve("src/jvmMain"))
                }
            }

            commonMain.dependencies {
                implementation("com.iskportal.kfs.binder:lib:${properties["project.version"]}")
                implementation("com.squareup:kotlinpoet:1.18.1")
                implementation("dev.toastbits.kjna:runtime:0.0.5")
            }
            commonTest.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        targets.all {
            compilations.all {
                compileTaskProvider {
                    compilerOptions {
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }

        kjna {
            generate {
                java_output_dir = kfsJavaSourceDir
                jextract {
                    binary.jextract_archive_extract_directory = Paths
                        .get(System.getProperty("user.home") as String)
                        .resolve("jextract")
                        .toFile()
                }
            }
        }
    }

    sourceSets["main"].java {
        srcDirs(kfsJavaSourceDir)
    }

    tasks.named("assemble"){
        dependsOn(tasks.withType<KJnaJextractGenerateTask>())
    }

    apply<BinderPlugin>()

    // docs
    val dokkaOutputDir = layout.buildDirectory.dir("dokka")
    tasks.dokkaHtml { outputDirectory.set(file(dokkaOutputDir)) }
    val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") { delete(dokkaOutputDir) }
    val javadocJar = tasks.create<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
        from(dokkaOutputDir)
    }


    publishing {
        publications {
            publications.withType<MavenPublication> {
                artifact(javadocJar)

                pom {
                    name.set("KotlinForScience")
                    description.set("various bindings for scientific C libraries")
                    url.set("https://github.com/Independent-Society-of-Knowledge/Kotlin-For-Science")

                    licenses {
                        license {
                            name.set("GNU General Public License v3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }

                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/Independent-Society-of-Knowledge/Kotlin-For-Science/issues")
                    }

                    developers {
                        developer {
                            id.set("nort3x")
                            name.set("Human Ardaki")
                            email.set("humanardaki@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com:Independent-Society-of-Knowledge/Kotlin-For-Science.git")
                        developerConnection.set("scm:git:ssh://github.com:Independent-Society-of-Knowledge/Kotlin-For-Science.git")
                        url.set("https://github.com/Independent-Society-of-Knowledge/Kotlin-For-Science")
                    }
                }
            }
        }
    }

}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    val node = rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>()
    node.version = "22.0.0"
    node.download = true
}

kotlin {
    jvm("root")
}

tasks.withType<Jar> {
    enabled = false
}
tasks.withType<AbstractPublishToMaven> {
    enabled = false
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--enable-preview")
}