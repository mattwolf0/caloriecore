# CalorieCore

CalorieCore is a small Android app for food logs, body data, and gym workouts.

The app keeps its data in a local SQLite database on the phone. There is no account or cloud sync, so the saved logbook belongs only to the installed app.

## Main Features

- Food entries with calories and macros
- Barcode scan with Open Food Facts lookup and a public Calorie API fallback
- Body weight, sleep, steps, and activity inputs
- Offline activity calorie log
- Training plans and workout logs
- Daily summary and simple progress charts
- English, Hungarian, and German UI text

Activity estimates use MET values from the
[2024 Adult Compendium of Physical Activities](https://pacompendium.com/adult-compendium/).
The catalog is included in the app and works offline.

## Build

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```sh
chmod +x ./gradlew
./gradlew assembleDebug
```
