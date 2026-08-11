# TraceLens 1.12.4 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.12.4-alpha.aab` — podpisany Android App Bundle do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii i błędów ANR.

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.12.4`,
- `versionCode`: `38`,
- ścieżka: test zamknięty `Alpha`,
- rozmiar AAB: 35 247 122 bajty,
- podpis AAB: zweryfikowany poleceniem `jarsigner`,
- `bundleRelease`, `lintDebug`, `lintVitalRelease` i optymalizacja R8: zakończone powodzeniem.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Wybierz **Utwórz nową wersję**.
3. Prześlij `artifacts/TraceLens-1.12.4-alpha.aab`.
4. Jeżeli Play Console poprosi o mapowanie, prześlij `artifacts/proguard-mapping.txt` jako plik ReTrace.
5. Wklej tekst z `release-notes-pl.txt` dla `pl-PL` oraz z `release-notes-en.txt` dla `en-US`.
6. Wybierz **Sprawdź wersję**, usuń ewentualne blokujące komunikaty i rozpocznij wdrażanie do testerów Alpha.

## Zakres aktualizacji

- projekt otwiera się bezpośrednio w trybie rysowania,
- panel ustawień jest domyślnie zamknięty i dostępny pod ikoną sterowania,
- przycisk trybu rysowania znajduje się ponad panelem ustawień,
- przycisk fragmentów pracy wykorzystuje całą szerokość,
- ikony i etykiety w siatce ustawień mają jednolite wyrównanie,
- aplikacja obsługuje edge-to-edge oraz wycięcia ekranu w Androidzie 15 i nowszym,
- usunięto wycofane ustawienia kolorów pasków systemowych.
