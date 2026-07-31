# TraceLens 1.12.1 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.12.1-alpha.aab` — podpisany pakiet do przesłania do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii.

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.12.1`,
- `versionCode`: `35`,
- ścieżka: test zamknięty `Alpha`.
- rozmiar AAB: 35 247 595 bajtów,
- podpis AAB: zweryfikowany poleceniem `jarsigner`.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Utwórz nową wersję i prześlij `TraceLens-1.12.1-alpha.aab`.
3. Opcjonalnie prześlij `proguard-mapping.txt` jako plik mapowania ReTrace.
4. Wklej polskie i angielskie informacje o wersji z tego katalogu.
5. Sprawdź testerów, kraje i link dołączenia do testu.
6. Wybierz **Sprawdź wersję**, a następnie rozpocznij wdrażanie.

## Uwagi

- Aplikacja nie korzysta z reklam ani identyfikatora reklamowego.
- Kod wersji `35` musi być niewykorzystany wcześniej w Play Console.
- Instrukcja oraz jej zrzuty są dostępne offline i nie wymagają połączenia z internetem.
