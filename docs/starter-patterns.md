# TraceLens — katalog wzorów offline

Wzory dołączane do aplikacji znajdują się w:

`app/src/main/assets/starter_patterns/images`

Plik `manifest.json` zawiera dane odczytywane przez aplikację Android:

- stabilny identyfikator wzoru,
- ścieżkę do PNG,
- polską i angielską nazwę,
- kategorię i tagi wyszukiwania,
- autora,
- licencję,
- źródło lub generator,
- model,
- prompt albo opis pochodzenia,
- dodatkowe uwagi licencyjne.

Lista często wybieranych autorów znajduje się w `authors.json`.

## Licencje

Narzędzie kuratorskie obsługuje:

- `AI_GENERATED` — grafika wygenerowana przez model AI,
- `OWN_WORK` — własna praca,
- `CC0-1.0`,
- `CC-BY-4.0`,
- `CUSTOM` — inna licencja opisana w uwagach.

Dla `AI_GENERATED` wymagane jest zapisanie nazwy generatora. Zalecane jest
również zachowanie modelu lub wersji, promptu i daty przygotowania materiału.
Sam fakt wygenerowania grafiki przez AI nie rozstrzyga wszystkich praw do
dystrybucji. Przed publikacją należy sprawdzić aktualny regulamin wybranego
generatora oraz upewnić się, że prompt i wynik nie naruszają praw osób trzecich.

## Początkowy pakiet

Wzory gwiazdy, serca, kwiatu i motyla są oryginalnymi, geometrycznymi
grafikami projektu TraceLens. Zostały zapisane jako PNG w katalogu wzorów,
a ich pochodzenie jest udokumentowane w manifeście jako `OWN_WORK`.

Pierwsza seria wzorów AI obejmuje psa, kota, ptaka i konia. Grafiki mają
spójny styl kolorowanki, gruby kontur i przezroczyste tło. Ich generator,
opis promptu oraz sposób przygotowania są zapisane w manifeście.

Rozszerzony pakiet zawiera co najmniej cztery wzory w każdej z 16 kategorii.
Łącznie aplikacja udostępnia 72 wzory offline, z czego 64 powstały jako
spójna seria prostych czarno-białych ilustracji do odrysowywania.

## Dodawanie kolejnych wzorów

Do przygotowania nowej grafiki służy aplikacja:

`tools/pattern-curator/dist/TraceLens-Pattern-Curator.exe`

Narzędzie zapisuje PNG oraz aktualizuje manifest w schemacie 2. Przy zapisie
należy wybrać kategorię i podać angielskie tagi rozdzielone przecinkami. Nie
należy ręcznie dodawać grafiki bez odpowiadającego jej wpisu licencyjnego.
