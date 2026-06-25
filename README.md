# CalorieCore

CalorieCore is a small Android app for food logs, body data, and gym workouts.

The app keeps its data in a local SQLite database on the phone. There is no account or cloud sync, so the saved logbook belongs only to the installed app.

## Main Features

- Food entries with calories and macros
- Barcode scan and OpenFoodFacts lookup
- Body weight, sleep, steps, and activity inputs
- Training plans and workout logs
- Daily summary and simple progress charts
- English, Hungarian, and German UI text

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
