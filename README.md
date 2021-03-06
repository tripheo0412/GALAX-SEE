# GALAX-SEE

This is an AR project which replicates exactly a solar systems (with self orbiting and ellipse trajectory), also you can share the experience in the same environment with other devices

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Android studio 3.0 or up

Android phone 7.0 or up (api 24 or up)

AR capable phone

### Installing
```
git clone https://github.com/tripheo0412/GALAX-SEE.git
```
After cloning the project please generate your ARCore API key through [Google Cloud Console](https://console.cloud.google.com/)

Copy your generated API key and paste into Android Manifest

```
<meta-data
            android:name="com.google.android.ar.API_KEY"
            android:value="YOUR_API_KEY" />
```

Connect your Firebase - realtime database, go to [Firebase](https://console.firebase.google.com/)

Create new project, add **com.example.tripheo2410.galaxsee** to project's app

Download generated google-services.json and copy it to app folder

Run the project through Android Studio
## Running the demo
**To host a solar system**

Please move phone in circle to help the AR system to detect a plane

Tap on the detected surface to place a solar system -> system will auto host the room (it takes about 1 minute)




**To receive solar system from someone else**

Place phone in same environment and place with the host device

Tap resolve button and enter room code of the host device

You will now get the same solar system with the same coordinate with the host device


## Built With

* [Google Api](https://console.cloud.google.com/) - The ARCore api was used
* [Google FireBase](https://firebase.google.com/) - Realtime database was used to host Cloud Anchor
* [Google Cloud Anchor Api](https://developers.google.com/ar/develop/java/cloud-anchors/overview-android) - Used to host and share AR object
* [Kotlin](https://github.com/JetBrains/kotlin) - This app is written in kotlin
* [Google Sceneform](https://developers.google.com/ar/develop/java/sceneform/) - Sceneform was used for easy coding purpose


## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [GALAX-SEE](https://github.com/tripheo0412/GALAX-SEE). 

## Authors

* **Tri Hoang** - *Programmer* - [tripheo0412](https://github.com/tripheo0412/)

See also the list of [contributors](https://github.com/tripheo0412/GALAX-SEE/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
