Building Serval Mesh modified by CU Boulder
===========================================

Obtaining the source code
-------------------------

The [CU Boulder's Serval Mesh][] source code is available from [BitBucket][].
Download and build it for the first time using the following commands:

    $ git clone https://jihoonbrandon@bitbucket.org/csam233/batphone_cathleen.git
    Cloning into 'batphone'...
    $ cd batphone_cathleen


Pre-Built Libraries
------------------------------

1. The gradle build process needs to know the install location of the Android SDK & NDK. 
If you open the project in Android Studio these locations will be writen to local.properties as follows;

    ndk.dir={PATH}/Sdk/ndk-bundle
    sdk.dir={PATH}/Sdk

Or, you can manually create "local.properties" on the top directory.

2. Run app/src/main/jni/libsodium/autogen.sh.

3. ./gradlew libsodiumBuild

4. ./gradlew assembleDebug

The app-debug.apk will be created on app/build/outputs/apk/debug.
