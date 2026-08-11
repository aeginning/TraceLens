# Optymalizacje do zrobienia

Dokument zawiera pomysły pozostawione do realizacji w kolejnych etapach rozwoju. Zadania celowo nie są przypisane do konkretnych wersji.

## Asynchroniczny i odporny zapis danych

- Przenieść zapis metadanych obrazów i projektów na `Dispatchers.IO`.
- Łączyć serię szybkich zmian projektu w jeden zapis wykonywany po krótkim opóźnieniu.
- Zapisywać projekt natychmiast po zakończeniu gestu, wyjściu z projektu lub przejściu aplikacji do tła.
- Wprowadzić zapis atomowy przez plik tymczasowy i podmianę, aby przerwanie procesu nie uszkodziło pliku JSON.
- Ocenić migrację metadanych do Room dopiero po testach z setkami i tysiącami rekordów.

## Zarządzanie pamięcią pełnych bitmap

- Dodać mały cache pełnych obrazów aktualnie używanych w edytorze i projekcie.
- Dopasowywać maksymalny rozmiar dekodowanej bitmapy do rozdzielczości ekranu oraz planowanego eksportu.
- Usuwać nieużywane wpisy cache po wyjściu z ciężkich ekranów.
- Przeprowadzić testy pamięci z dużymi plikami JPG i PNG oraz wielokrotnym przechodzeniem między edytorem, biblioteką i projektem.
- Monitorować liczbę alokacji, działanie garbage collectora i ryzyko `OutOfMemoryError` w Android Studio Profiler.

## Energooszczędny timelapse

- Dobrać rozdzielczość zdjęć timelapse do finalnego filmu zamiast zawsze korzystać z maksymalnej jakości aparatu.
- Zapewnić pojedynczą kolejkę zapisu, aby następna klatka nie rozpoczynała się przed zakończeniem poprzedniej.
- Pomijać klatkę, gdy aparat lub zapis poprzedniego zdjęcia nadal pracuje.
- Ograniczać lub zatrzymywać timelapse przy wysokiej temperaturze urządzenia i krytycznie niskim poziomie baterii.
- Rozważyć opcjonalny tryb oszczędzania energii dla wielogodzinnych sesji.

## Pomiary i profile wydajności

- Dodać moduł Macrobenchmark uruchamiany na fizycznym telefonie.
- Mierzyć zimny start, czas do pełnej gotowości, przewijanie biblioteki, otwieranie edytora i uruchamianie projektu.
- Przygotować Baseline Profile obejmujący uruchomienie aplikacji oraz najczęstsze ścieżki użytkownika.
- Przygotować Startup Profile obejmujący wyłącznie kod potrzebny do pierwszego interaktywnego ekranu.
- Zapisywać wyniki przed i po zmianach, aby optymalizacje wynikały z pomiarów, a nie z odczuć.

## Rozmiar aplikacji i zasobów

- Przeanalizować udział wbudowanych wzorów, bibliotek natywnych i pakietu ikon w rozmiarze AAB.
- Zweryfikować możliwość bezstratnej lub wizualnie bezstratnej konwersji części PNG do WebP.
- Usunąć niewykorzystywane zasoby i zależności po analizie raportu R8 oraz App Size Analyzer.
- Sprawdzić, czy używane ikony można ograniczyć do mniejszego zestawu bez pogorszenia spójności interfejsu.
- Porównywać rozmiar AAB oraz rozmiar pliku APK dostarczanego konkretnemu urządzeniu przez Google Play.

## Kryteria weryfikacji

- Brak widocznych przycięć podczas szybkiego przewijania dużej biblioteki.
- Stabilne zużycie pamięci po wielokrotnym otwieraniu obrazów i galerii.
- Brak utraty ustawień projektu po zamknięciu procesu lub przejściu aplikacji do tła.
- Brak nakładających się operacji zapisu zdjęć podczas timelapse.
- Udokumentowana poprawa czasów i liczby wolnych klatek w testach Macrobenchmark.
