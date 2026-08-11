# TraceLens 1.12.8 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.12.8-alpha.aab` — podpisany Android App Bundle do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii i błędów ANR.

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.12.8`,
- `versionCode`: `42`,
- ścieżka: test zamknięty `Alpha`,
- rozmiar AAB: 35 256 955 bajtów,
- podpis AAB: zweryfikowany poleceniem `jarsigner`,
- `bundleRelease`, `lintDebug`, `lintVitalRelease` i optymalizacja R8: zakończone powodzeniem.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Wybierz **Utwórz nową wersję**.
3. Prześlij `artifacts/TraceLens-1.12.8-alpha.aab`.
4. Jeżeli Play Console poprosi o mapowanie, prześlij `artifacts/proguard-mapping.txt` jako plik ReTrace.
5. Wklej tekst z `release-notes-pl.txt` dla `pl-PL` oraz z `release-notes-en.txt` dla `en-US`.
6. Wybierz **Sprawdź wersję**, usuń ewentualne blokujące komunikaty i rozpocznij wdrażanie do testerów Alpha.

## Zakres aktualizacji

- szybsze ładowanie miniatur w bibliotece, selektorach projektów i galeriach,
- szybsza reakcja edytora podczas zmiany filtrów,
- mniejsze obciążenie czujników na ekranie kamery,
- aktualne powiadomienie podczas szybkiego dodawania wielu wzorów,
- kompaktowy wskaźnik baterii, ładowania i temperatury,
- poprawne formatowanie temperatury po zmianie języka.
