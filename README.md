# TraceLens

TraceLens to prywatna aplikacja na Androida działająca jak cyfrowa kalka. Łączy podgląd z kamery z półprzezroczystą nakładką obrazu, dzięki czemu można odrysowywać grafikę na kartce i obserwować postęp na ekranie telefonu.

## Wymagania

- Android 10 (API 29) lub nowszy
- aparat; latarka jest opcjonalna
- Android SDK 36
- JDK 17

## Obecne funkcje

- lokalna biblioteka PNG i JPG
- ulubione obrazy przypinane na początku biblioteki
- niedestrukcyjny edytor: skala szarości, kontur, kontrast, negatyw i usuwanie wskazanego koloru
- lokalne projekty zapisujące konfigurację nakładki
- podgląd CameraX i wybór kamer udostępnionych przez urządzenie
- przesuwanie, skalowanie, obracanie i odbijanie nakładki
- blokada nakładki i suwak przezroczystości
- latarka z reakcją na krytyczny stan termiczny
- blokada orientacji pionowej i poziomej
- zdjęcia postępu oraz galeria przypisana do projektu
- dane przechowywane wyłącznie na urządzeniu

## Budowanie

```powershell
.\gradlew.bat :app:assembleDebug
```

APK powstaje w `app/build/outputs/apk/debug/app-debug.apk`.

Instalacja na podłączonym urządzeniu:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Pełne ustalenia produktu znajdują się w pliku `kwestionariusz-aplikacji.md`.

Historia wydań znajduje się w katalogu [`changelog`](changelog/README.md).
