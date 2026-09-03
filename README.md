<p align="center"><img src="fastlane/metadata/android/en-US/images/icon.png" width="150"></p>
<h1 align="center"><b>Isai</b></h1>
<h4 align="center">A simple, rational music player for Android.</h4>
<p align="center">
    <a href="https://github.com/muthupandi/Isai/releases/tag/v4.1.5">
        <img alt="Latest Version" src="https://img.shields.io/static/v1?label=tag&message=v4.1.5&color=64B5F6&style=flat">
    </a>
    <a href="https://github.com/muthupandi/Isai/releases/">
        <img alt="Releases" src="https://img.shields.io/github/downloads/Muthupandi/Isai/total.svg?color=4B95DE&style=flat">
    </a>
    <a href="https://www.gnu.org/licenses/gpl-3.0">
        <img src="https://img.shields.io/badge/license-GPL%20v3-2B6DBE.svg?style=flat">
    </a>
    <img alt="Minimum SDK Version" src="https://img.shields.io/badge/API-24%2B-1450A8?style=flat">
</p>
<h4 align="center"><a href="/CHANGELOG.md">Changelog</a> | <a href="https://github.com/Muthupandi/Isai/wiki">Wiki</a> | <a href="https://github.com/Muthupandi/Isai#Donate">Donate</a></h4>
<p align="center">
    <a href="https://f-droid.org/app/com.muthupandi.isai"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="250"></a>
    <a href="https://accrescent.app/app/com.muthupandi.isai">
        <img alt="Get it on Accrescent" src="https://accrescent.app/badges/get-it-on.png" width="250">
    </a>
</p>
<p align="center">
    <a href="https://hosted.weblate.org/engage/isai/"><img height=64 src="https://hosted.weblate.org/widgets/isai/-/strings/287x66-grey.png" alt="Translation status" /></a>
</p>

## About

Isai is a local music player with a fast, reliable UI/UX without the many useless features present in other music players. Built off of modern media playback libraries, Isai has superior library support and listening quality compared to other apps that use outdated Android functionality. In short, **It plays music.**

*Isai is a customized fork of the open-source [Auxio](https://github.com/OxygenCobalt/Auxio) music player, originally created by OxygenCobalt. We sincerely thank the original author and contributors for their incredible work.*

**The default branch is the development version of the repository. For a stable version, see the master branch.**

## Screenshots

<p align="center">
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot0.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot1.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot2.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot3.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot4.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot5.png" width=250>
</p>


## Features

- Playback based on [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/exoplayer)
- Snappy UI derived from the latest Material Design guidelines
- Opinionated UX that prioritizes ease of use over edge cases
- Customizable behavior
- Support for disc numbers, multiple artists, release types,
precise/original dates, sort tags, and more
- Advanced artist system that unifies artists and album artists
- SD Card-aware folder management
- Reliable playlisting functionality
- Playback state persistence
- Android Auto support
- Automatic gapless playback
- Full ReplayGain support (On MP3, FLAC, OGG, OPUS, and MP4 files)
- External equalizer support (ex. Wavelet)
- Edge-to-edge
- Embedded covers support
- Search functionality
- Headset autoplay
- Stylish widgets that automatically adapt to their size
- Completely private and offline
- No rounded album covers (if you want them)

## Permissions

- Storage (`READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE`) to read and play your music files
- Services (`FOREGROUND_SERVICE`, `WAKE_LOCK`) to keep the music playing in the background
- Notifications (`POST_NOTIFICATION`) to indicate ongoing playback and music loading

## Donate

You can support Isai's development through [my Github Sponsors page](https://github.com/sponsors/Muthupandi). Get the ability to prioritize features and have your profile added to the README, Release Changelogs, and even the app itself!

<h3 align="center">Legendary supporters:</h3>
<p align="center">
    <a href="https://github.com/bkkellyh"><img src="https://avatars.githubusercontent.com/u/248118457?v=4" width=250 /></a> 
    <a href="ttps://github.com/gruntstumper"><img src="https://avatars.githubusercontent.com/u/308816080?v=4" width=125 /></a> 
</p>
<h3 align="center">
    <a href="https://github.com/bkkellyh">@bkkellyh</a> - <i>$500!</i>, <a href="https://github.com/gruntstumper">@gruntstumper</a> - $100
</h3>

<hr />

<p align="center"><b>$8/month supporters:</b></p>

<p align="center">
    <a href="https://github.com/uku3lig"><img src="https://avatars.githubusercontent.com/u/61147779?v=4" width=50 /></a>
</p>
<p align="center">
    And **1** Private Sponsor!
</p>

## Building

Isai relies on a patched version of Media3 that enables some extra playback features, alongside taglib for metadata
parsing. This adds some caveats to the build process:
1. `cmake` and `ninja-build` must be installed before building the project.
2. The project uses submodules, so when cloning initially, use `git clone --recurse-submodules` to properly
download the external code.
3. You are **unable** to build this project on windows, as the custom Media3 build runs shell scripts that
will only work on unix-based systems.

### Set up Android Studio

#### Install Android Studio.

```bash
pkg -S android-studio
```

#### Configuring Android Studio:

- Be sure to have NDK tools, version 28.2.13676358. You can search it on Languages & Frameworks > Android SDK.
- Install Java-21 with your system package manager

    ```bash
    sudo pkg -S jdk21-openjdk
    ```
    Additionally: Set java version to jdk21-openjdk

- Run ./gradlew assembleDebug

#### Connecting to your Android Device

You can connect your Mobile Phone through USB to run the app. 

1. **Enable Developer Options on your phone**
   - Go to **Settings > About phone**  
   - Tap **Build number** 7 times until you see *"You are now a developer!"*

2. **Enable USB debugging**
   - Go to **Settings > Developer options**  
   - Turn on **USB debugging**

3. **Connect your phone to the computer**
   - Use a USB cable  
   - On your phone, accept the *Allow USB debugging?* prompt

4. **Verify that your device is detected**
   ```bash
   cd ~/Android/Sdk/platform-tools
   ./adb devices
   ```

Android Studio also offers virtual devices that come with this pre-configured.

#### Install the app on the Android Phone
To install the app on your physical device or emulator, run this command:

```bash
./gradlew installDebug
```

Isai should now appear in the list of Apps

#### Load music to Isai (Optional)

You can move files from your pc to your device / emulator to test the music using this command:

```bash
cd ~/Android/Sdk/platform-tools
./adb push ~Music/ /sdcard/Music
```

## Contributing

Isai accepts most contributions as long as they follow the [Contribution Guidelines](/.github/CONTRIBUTING.md).

However, feature additions and major UI changes are less likely to be accepted. See
[Why Are These Features Missing?](https://github.com/Muthupandi/Isai/wiki/Why-Are-These-Features-Missing%3F)
for more information.



## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

Isai is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

As a fork of [Auxio](https://github.com/OxygenCobalt/Auxio), Isai retains all original copyright notices within the source code to comply with the GPL v3.0 license. All new modifications are released under the same license terms.

More information can be found [here](https://github.com/Muthupandi/Isai/wiki/Licenses).
