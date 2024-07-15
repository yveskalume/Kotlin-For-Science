kotlin {
    kjna {
        generate {
            packages {
                add("kjna.gsl.specfunc") {

                    // Disabled packages will have no function implementations
                    // Use the 'isAvailable()' companion method to check at runtime
                    enabled = true

                    addHeader(
                        "gsl/gsl_specfunc.h", // Header path
                        "GSLSpecialFunctions" // Generated class name
                    )


                    libraries = listOf("gsl")
                }
            }
        }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.7.3")
            implementation("org.jetbrains.lets-plot:lets-plot-batik:4.3.3")
        }
    }
}