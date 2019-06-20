plugins {
	scala
	idea
}


repositories {
    mavenCentral()
}


dependencies {
    implementation("org.scala-lang:scala-library:2.11.8")

    val dl4j_version: String by extra("1.0.0-beta4")

    implementation("org.deeplearning4j:deeplearning4j-core:$dl4j_version")
    implementation("org.nd4j:nd4j-native-platform:$dl4j_version")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("org.slf4j:slf4j-api:1.7.25")

    //compile("com.airlenet:play-json:2.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.9")

    testImplementation("org.scalatest:scalatest_2.11:3.0.0")
    //testImplementation("junit:junit:4.12")
}