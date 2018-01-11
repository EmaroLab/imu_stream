# IMU Stream
Set of Android applications (mobile and wear) to stream IMU data from smartwatch to an MQTT broker.
## Installation
**mobile app** 

1. From the git repository download [release/mobile-release.apk](https://github.com/EmaroLab/imu_stream/blob/master/release/mobile-release.apk) into your smartphone;
1. Tap on the downloaded file and follow on screen instruction to complete the installation.

**wearable app**

There exist different approaches to install the app into your smartwatch, we describe one of them that requires few external software.

1. Download on a Windows PC [Android Wear 2.0 APK Installer.exe](https://drive.google.com/file/d/0B12CIETdWT5vNzlwQl93NHcwVVk/view);
1. Download on the same PC [release/mobile-release.apk](https://github.com/EmaroLab/imu_stream/blob/master/release/mobile-release.apk);
1. Plug your smartwatch to the PC;
1. Run _Android Wear 2.0 APK Installer.exe_;
1. Press the **Start ADB server** button and whait for the green notification;
1. Check that your smartwatch name appears in the ADB Devices tab;
1. One the smartwatch is detected, in the control tab press the **Batch mode** button;
1. Press **Browse** and select the folder were the apk was downloaded, the installation process will start automatically.

**NOTE** 
1. On both devices, smartphone and smartwatch, should be enabled the usb debugging option from the developer options menu;
1. If you experience problem with _Android Wear 2.0 APK Installer.exe_ check your antivirus.

## Author 

[Alessandro Carfì](https://github.com/ACarfi) e-mail: alessandro.carfi@dibris.unige.it
