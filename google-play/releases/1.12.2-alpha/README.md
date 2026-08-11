# TraceLens 1.12.2 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.12.2-alpha.aab` — podpisany pakiet do przesłania do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii.

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.12.2`,
- `versionCode`: `36`,
- ścieżka: test zamknięty `Alpha`,
- rozmiar AAB: 35 248 600 bajtów,
- podpis AAB: zweryfikowany poleceniem `jarsigner`.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Utwórz nową wersję i prześlij `TraceLens-1.12.2-alpha.aab`.
3. Prześlij `proguard-mapping.txt` jako plik mapowania ReTrace.
4. Wklej informacje o wersji z odpowiedniego pliku PL lub EN.
5. Wybierz **Sprawdź wersję**, a następnie rozpocznij wdrażanie.

## Weryfikacja techniczna

- Usunięto wycofane parametry `statusBarColor` i `navigationBarColor`.
- Interfejs obsługuje edge-to-edge, paski systemowe i wycięcia przez `safeDrawing`.
- `lintDebug`, `lintVitalRelease`, R8 i podpisywanie AAB zakończyły się powodzeniem.

