# TraceLens 1.9.4 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.9.4-alpha.aab` — podpisany pakiet przesyłany do Play Console,
- `proguard-mapping.txt` — kopię mapowania R8 do diagnostyki awarii.

Bundle:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.9.4`,
- `versionCode`: `28`,
- rozmiar: 32 390 428 bajtów,
- podpis: zweryfikowany poleceniem `jarsigner`.

Mapowanie R8 jest również osadzone bezpośrednio w AAB w
`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty**.
2. Utwórz lub wybierz ścieżkę `Alpha`.
3. Utwórz nową wersję i prześlij plik AAB z katalogu `artifacts`.
4. Wklej polskie albo angielskie informacje o wersji z tego katalogu.
5. Wybierz listę testerów oraz kraje dostępne w teście.
6. Zapisz wersję, sprawdź podsumowanie i rozpocznij wdrażanie.
7. Prześlij testerom link akceptacji testu wyświetlony przez Play Console.

## Symbole natywne

Aplikacja zawiera niewielkie biblioteki natywne dostarczane przez AndroidX
i CameraX. Włączono `debugSymbolLevel = FULL`, jednak część bibliotek
dostawców jest publikowana bez pełnych symboli. Ostrzeżenie Play Console
dotyczące brakujących symboli może więc pozostać; nie blokuje ono testu Alpha.
