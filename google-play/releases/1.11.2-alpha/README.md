# TraceLens 1.11.2 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.11.2-alpha.aab` — podpisany pakiet do przesłania do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii.

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.11.2`,
- `versionCode`: `33`,
- ścieżka: test zamknięty `Alpha`.
- rozmiar AAB: 33 893 593 bajty,
- podpis AAB: zweryfikowany poleceniem `jarsigner`.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Utwórz nową wersję i prześlij `TraceLens-1.11.2-alpha.aab`.
3. Opcjonalnie prześlij `proguard-mapping.txt` jako plik mapowania ReTrace.
4. Wklej polskie i angielskie informacje o wersji z tego katalogu.
5. Sprawdź testerów, kraje i link dołączenia do testu.
6. Wybierz **Sprawdź wersję**, a następnie rozpocznij wdrażanie.

## Uwagi

- Aplikacja nie korzysta z reklam ani identyfikatora reklamowego.
- Kod wersji `33` musi być niewykorzystany wcześniej w Play Console.
- Biblioteki Media3 służą wyłącznie do lokalnego generowania filmów MP4.
