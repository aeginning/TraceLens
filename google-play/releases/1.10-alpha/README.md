# TraceLens 1.10 — test zamknięty Alpha

## Artefakty lokalne

Katalog `artifacts` jest celowo ignorowany przez Git i zawiera:

- `TraceLens-1.10-alpha.aab` — podpisany pakiet do przesłania do Play Console,
- `proguard-mapping.txt` — mapowanie R8 do analizy awarii,

Parametry wydania:

- pakiet: `pl.tracelens.app`,
- `versionName`: `1.10`,
- `versionCode`: `30`,
- ścieżka: zamknięty test `Alpha`.
- rozmiar AAB: 32 416 630 bajtów,
- podpis AAB: zweryfikowany poleceniem `jarsigner`.

## Publikacja

1. W Play Console otwórz **Testowanie → Test zamknięty → Alpha**.
2. Utwórz nową wersję i prześlij `TraceLens-1.10-alpha.aab`.
3. Jeżeli konsola poprosi o mapowanie lub symbole, prześlij odpowiedni plik z `artifacts`.
4. Wklej polskie i angielskie informacje o wersji z tego katalogu.
5. Sprawdź listę testerów, kraje i link dołączenia do testu.
6. Zapisz wersję i wybierz **Sprawdź wersję**, a następnie rozpocznij wdrażanie.

## Uwagi

- Aplikacja nie korzysta z identyfikatora reklamowego ani reklam.
- Gradle nie wygenerował osobnego archiwum symboli natywnych. Ostrzeżenie dotyczące bibliotek AndroidX/CameraX dostarczonych bez symboli może pozostać i nie blokuje testu.
- Artefakt należy wgrywać tylko wtedy, gdy kod wersji `30` nie został wcześniej użyty w Play Console.
