# Study Mapper
[![codebeat badge](https://codebeat.co/badges/1c3a9240-10b0-4f8e-b3a4-742e1bd97224)](https://codebeat.co/projects/github-com-harrymt-productivitymapping-master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/31dda94a8e80410cb9c2caee02c95257)](https://www.codacy.com/app/harrymt/ProductivityMapping?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=harrymt/ProductivityMapping&amp;utm_campaign=Badge_Grade)

Android app to improve productivity during study.
Links with it's server component [Study Mapper (Server)](https://github.com/harrymt/ProductivityMapping-Server)

**Built for the University of Nottingham as a final year BSc Computer Science project.**

In order to build this project, you must place an `environment_variables.xml` in `app/src/main/res/values/environment_variables.xml`.
This must contain a [Google Maps Android API key](https://developers.google.com/maps/signup).

`app/src/main/res/values/environment_variables.xml`

```xml

<resources>
    <!--
        Server side API key
    -->
    <string name="api_key">
        my_secret_password
    </string>

    <!--
        URL to the API server.
    -->
    <string name="api_server_url">
        http://harrys_server.com/~username/folder/api/v1
    </string>

    <!--
        Google Maps API key for project
    -->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        AInbaiA_iansdinasidnaisbdBKSYBDkASdbSHB
    </string>
</resources>


```

Uses the following technologies.

- [Gradle](http://gradle.org/)
- [Google Maps/Location API](https://developers.google.com/maps/documentation/android-api/start)
- [Google Maps Utility Library](https://github.com/googlemaps/android-maps-utils)
- [Volley](https://developer.android.com/training/volley/index.html)


### How to Build

- Install [Android Studio](https://developer.android.com/sdk/index.html) for the best experience
- Build using [Gradle](http://gradle.org/)


`build.gradle`

```groovy
dependencies {
    testCompile 'junit:junit:4.12' // For tests
    compile 'com.android.support:appcompat-v7:22.0.0' // Android design
    compile 'com.google.android.gms:play-services-maps:8.4.0' // Google Maps API
    compile 'com.google.android.gms:play-services-location:8.4.0' // Google Location API
    compile 'com.google.maps.android:android-maps-utils:0.4' // Android Google maps Utility library
    compile 'com.android.volley:volley:1.0.0' // A HTTP request library so we can interact with our API
}
```

