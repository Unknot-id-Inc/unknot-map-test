# Unknot Map Test

Sample app using the Unknot SDK featuring map integrations with both Mapbox and Google Maps for DFW
and Mohegan Sun locations.

## secrets.properties

This app, just like [Unknot Example App](https://github.com/Unknot-id-Inc/unknot-example-app),
requires a few settings whose values should probably not be commited into source control. Instead
these values are stored in the `secrets.properties` file. This file is required to compile the app.
The settings without values are:

```
GOOGLE_MAPS_API_KEY=
MAPBOX_API_KEY=

API_KEY=
AUTH_TARGET=
INGESTER_TARGET=
STREAM_TARGET=
```

Put the appropriate values in and save the file to the root directory of the project.

Most of these values will be put in `BuildConfig` (e.g. `BuildConfig.API_KEY`), while the map API
keys are placed in string resources for Mapbox, and manifest placeholders for Google Maps. For
Google Maps, the key must then be referenced in the `AndroidManifest.xml` file like so:
```xml
<application>
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${MAPS_API_KEY}" />
```

> `GOOGLE_MAPS_API_KEY` and `MAPBOX_API_KEY` are only required when using the google or mapbox
> flavors respectively.

## Flavors

The app comes in a few flavor combinations, one for Mapbox and Google Maps, and the other for DFW
or Mohegan. For instance, the `moheganMapbox` flavor combination will setup the app to use Mapbox
to display the Mohegan Sun location. Likewise, `moheganGoogle` will do the same but with Google
Maps. 

> Currently the DFW flavor does not have a configuration to use Google Maps, as Google Map's support
> for 3D GeoJSON rendering is rather more limited than Mapbox.

## Mock testing

To facilitate the testing of the Unknot SDK without having to setup a full location on your account,
use a mock data file and send it to `UnknotServiceController.startMockTrackSession()`. This function
takes a `File` reference to the mock data, and a few other optional parameters to control playback
speed, start and end times within the mock data, and whether to repeat the mock session once
reaching the end:

```kotlin
UnknotServiceController.startMockTrackSession(
    ctx = ctx,
    file = tfile,
    speed = 10,
    repeat = true,
    start = 5.minutes.inWholeMilliseconds,
    end = 7.minutes.inWholeMilliseconds,
    notification = notification.getNotification("Mock session running")
)
```

In this project the mock data files are JSON files stored in the `assets` directory for the
appropriate location flavors.

> Since `startMockSession()` creates an `Intent` to send to the Unknot Service, sending the full
> file contents in the Intent bundle is not practical. Thus a `File` reference is used. In the case
> of this app, as the files are stored in assets which cannot be referenced by a File object, the
> assets are first copied to a real file (in the app's cache directory) and then referenced. This
> would also be required if the mock data was stored as resources.

```kotlin
// Write asset data into real file so it can be accessed by the service
val tfile = File(ctx.cacheDir, "mock-traj.json")
ctx.resources.assets.open(mockDataAsset).use { input ->
    FileOutputStream(tfile).use { output ->
        input.copyTo(output)
    }
}
```

In this app, to start the mock session, select `Mock Session` from the settings menu at the top
right.