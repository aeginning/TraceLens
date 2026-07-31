# Kierunki rozwoju TraceLens

Dokument zawiera wyłącznie pomysły i funkcje, które nie zostały jeszcze zrealizowane po przygotowaniu **TraceLens 1.10**.

Gotowe elementy aplikacji są opisane w [README](README.md), a historia wdrożeń znajduje się w katalogu [changelog](changelog/README.md).

## Jak korzystać z dokumentu

Wstaw `X` pomiędzy nawiasy przy funkcjach wybranych do kolejnego wydania:

```text
- [X] Funkcja wybrana do realizacji
- [ ] Funkcja oczekująca na decyzję
```

---

## Najważniejsze kandydatury do kolejnego wydania

- [ ] **Cofnij/Ponów w edytorze** — historia operacji bez utraty oryginału.
- [ ] **Pędzel przywracający** — odzyskiwanie fragmentów usuniętych gumką lub filtrem tła.
- [ ] **Testy na większej liczbie telefonów** — różni producenci, aparaty i proporcje ekranów.

**Uwagi i wybór priorytetu:**



---

## Kamera, kartka i pozycjonowanie

- [X] **Automatyczne wykrywanie kartki** — rozpoznawanie krawędzi oraz proponowanie czterech rogów.
- [X] **Automatyczna korekcja perspektywy** — dopasowanie nakładki do wykrytej płaszczyzny.
- [X] **Stabilizacja nakładki** — utrzymywanie grafiki na kartce po niewielkim poruszeniu telefonu.
- [X] **Markery stabilizacyjne** — drukowane znaczniki w rogach kartki dla dokładniejszego śledzenia.
- [X] **Kalibracja rzeczywistego rozmiaru** — ustawienie grafiki w centymetrach.
- [ ] **Ręczna kompensacja ekspozycji**.
- [ ] **Zoom cyfrowy kamery** niezależny od skali nakładki.
- [ ] **Zamrożenie klatki z kamery** podczas precyzyjnego ustawiania grafiki.
- [ ] **Migawka bez nakładki i z nakładką** — zapis dwóch wersji zdjęcia jednym dotknięciem.

**Oczekiwany sposób działania:**



---

## Narzędzia wspomagające odrysowywanie

- [ ] **Szybkie wartości przezroczystości** — np. 10%, 25%, 50% i 75%.
- [ ] **Lupa** — powiększenie fragmentu wskazanego palcem.
- [ ] **Profile pracy** — np. ołówek, farby, ściana, mała kartka i duży format.

**Uwagi:**



---

## Edytor obrazów

- [ ] **Pędzel przywracający**.
- [ ] **Historia zmian Cofnij/Ponów**.
- [ ] **Presety filtrów** — zapis i ponowne użycie zestawu ustawień.
- [ ] **Obsługa SVG** z zachowaniem jakości skalowania.
- [ ] **Obsługa WebP**.
- [ ] **Podgląd na szachownicy** — czytelniejsze przedstawienie przezroczystych obszarów.

**Uwagi:**



---

## Biblioteka obrazów

- [ ] **Tagi**.
- [ ] **Lista ostatnio używanych obrazów**.
- [ ] **Wykrywanie duplikatów podczas importu**.
- [ ] **Zaznaczanie wielu obrazów** i operacje zbiorcze.

**Uwagi:**



---

## Projekty i kopie zapasowe

- [ ] **Duplikowanie projektu** z zachowaniem konfiguracji.
- [ ] **Wyszukiwanie i sortowanie projektów**.
- [ ] **Kopia całej biblioteki i wszystkich projektów**.
- [ ] **Przenoszenie kompletnej kopii danych na drugi telefon** bez konta i chmury.

**Uwagi:**



---

## Zdjęcia postępu i prezentacja pracy

- [X] **Film z procesu rysowania** generowany ze zdjęć timelapse.
- [X] **Porównanie referencji z ukończoną pracą**.
- [X] **Zestawienie „przed i po”** jako jeden obraz.

**Uwagi:**



---

## Wygoda i dostępność

- [ ] **Regulacja jasności ekranu w aplikacji**.
- [ ] **Tryb oszczędzania energii**.
- [ ] **Sygnał haptyczny** po zablokowaniu nakładki lub wykonaniu zdjęcia.
- [ ] **Alternatywny rozmiar kontrolek i tekstu**.

**Uwagi:**



---

## Dystrybucja i jakość

- [ ] **Testy na różnych rozdzielczościach i urządzeniach**.
- [ ] **Automatyczne testy migracji danych** pomiędzy kolejnymi wersjami.
- [ ] **Automatyczne testy najważniejszych ekranów**.

**Uwagi:**

---

## Wyszukiwanie obrazów w internecie

### Wariant bez kosztów i bez klucza API

- [ ] **Otwórz wyszukiwanie w przeglądarce** — aplikacja uruchamia przygotowane zapytanie, np. „prosty ptak kontur SVG”.
- [ ] **Instrukcja pobierania legalnych obrazów** — krótka podpowiedź o licencji, autorze i prawie do wykorzystania.
- [ ] **Skróty do zaufanych katalogów otwartych zasobów** zamiast integrowania płatnej wyszukiwarki.

Ten wariant nie wymaga klucza API i nie generuje kosztu zapytań. TraceLens nie powinien pobierać ani analizować wyników wyszukiwarki bezpośrednio — takie rozwiązanie byłoby kruche, mogłoby naruszać regulamin dostawcy i utrudniałoby ocenę licencji obrazu.

Android nie pozwala aplikacji bezpiecznie „przechwycić” dowolnego obrazu klikniętego w zewnętrznej przeglądarce. Najbardziej naturalnym mechanizmem jest pobranie pliku albo użycie systemowego polecenia „Udostępnij do TraceLens”.

### Wariant z zewnętrznym API

- [ ] **Integracja z dostawcą otwartych grafik** — wyszukiwanie wyłącznie zasobów z jednoznaczną licencją.
- [ ] **Filtry przydatności do odrysowania** — kontur, wektor, przezroczyste tło, mała liczba kolorów.
- [ ] **Pokazywanie autora, źródła i licencji przed importem**.
- [ ] **Cache wyników i miniatur** w celu ograniczenia liczby zapytań.

Przed wyborem dostawcy trzeba osobno sprawdzić aktualny regulamin, limity, zasady atrybucji i możliwość komercyjnego użycia API. Klucz dostawcy zapisany w APK można odzyskać, dlatego płatne API powinno być wywoływane przez kontrolowany backend, a nie bezpośrednio z aplikacji.

**Uwagi i wybór wariantu:**



---

## Społecznościowy katalog obrazów TraceLens

### Bezpieczny etap początkowy — katalog tylko do odczytu

- [ ] **Publiczny katalog wzorów na stronie TraceLens**.
- [ ] **Manifest JSON katalogu** — identyfikator, tytuł, miniatura, plik, tagi, autor i licencja.
- [ ] **Pobieranie katalogu przez aplikację bez konta**.
- [ ] **Lokalny cache miniaturek i pobranych wzorów**.
- [ ] **Odświeżanie warunkowe przez `ETag` lub numer wersji katalogu**.
- [ ] **Kategorie i wyszukiwarka działające lokalnie po pobraniu indeksu**.
- [ ] **Przypięcie pobranego wzoru do prywatnej biblioteki użytkownika**.

Na pierwszym etapie nowe obrazy byłyby dodawane administracyjnie na serwerze. Odczyt może pozostać całkowicie publiczny, tani i pozbawiony kont użytkowników. PHP 8.2 wystarczy do wystawienia indeksu, a statyczne miniatury i pliki nie wymagają skomplikowanego API.

### Późniejszy etap — anonimowe zgłaszanie wzorów

- [ ] **Formularz zgłoszenia na stronie**, a nie bezpośredni publiczny upload z aplikacji.
- [ ] **Jednorazowy token wysyłania wydawany przez backend**.
- [ ] **CAPTCHA lub Turnstile**, limit żądań na IP i ograniczenie rozmiaru pliku.
- [ ] **Walidacja MIME, wymiarów i rzeczywistej zawartości obrazu**.
- [ ] **Usuwanie metadanych EXIF** i generowanie pliku wynikowego po stronie serwera.
- [ ] **Wykrywanie duplikatów przez hash perceptualny**.
- [ ] **Kolejka moderacyjna** — obraz nie staje się publiczny automatycznie.
- [ ] **Deklaracja praw i licencji przy zgłoszeniu**.
- [ ] **Przycisk zgłoszenia naruszenia oraz procedura usuwania treści**.
- [ ] **Losowy identyfikator zgłoszenia zapisany lokalnie** — pozwala sprawdzić status bez konta.

### Ważne założenia bezpieczeństwa

- Sekret, klucz API lub certyfikat umieszczony w APK **nie jest tajny** i nie może chronić endpointu wysyłania.
- Publiczny endpoint nie powinien przyjmować nieograniczonych plików na podstawie stałego klucza aplikacji.
- Bez kont można ograniczać spam, ale nie da się wiarygodnie ustalić tożsamości autora.
- Każde zgłoszenie powinno trafiać do kwarantanny i wymagać moderacji przed publikacją.
- Backend powinien przechowywać minimalne logi bezpieczeństwa przez określony czas i opisywać je w polityce prywatności.
- Obrazy powinny mieć jasno określoną licencję; samo znalezienie pliku w internecie nie oznacza prawa do jego redystrybucji.

### Rekomendowana kolejność

1. Katalog tylko do odczytu, uzupełniany administracyjnie.
2. Integracja katalogu z aplikacją i lokalnym cache.
3. Formularz zgłoszeniowy na stronie z moderacją.
4. Dopiero po sprawdzeniu kosztów i nadużyć — zgłaszanie bezpośrednio z aplikacji.

**Uwagi dotyczące moderacji, licencji i hostingu:**



---

## Dodatkowe pomysły po wersji 1.10

- [ ] **Ocena łatwości odrysowania** — prosta, średnia lub szczegółowa.
- [ ] **Automatyczna ocena złożoności obrazu** na podstawie liczby krawędzi i kolorów.
- [ ] **Podobne wzory** wyliczane lokalnie z tagów, bez profilowania użytkownika.
- [ ] **Paczki tematyczne** — zwierzęta, kwiaty, pojazdy, dekoracje.
- [ ] **Tryb prezentacyjny projektu** — uporządkowana galeria gotowa do pokazania.
- [ ] **Lista licencji i autorów pobranych wzorów** dostępna także offline.
- [ ] **Automatyczne tworzenie kopii oryginału przed każdą destrukcyjną operacją**.

---

## Proponowany podział kolejnych etapów

Wersja 1.11 została wybrana do realizacji. Dalsze wydania pozostają propozycją uporządkowania backlogu.

### Wersja 1.11 — prezentacja i udostępnianie ukończonej pracy

- Film generowany ze zdjęć timelapse.
- Porównanie referencji z ukończoną pracą.
- Zestawienie „przed i po” gotowe do udostępnienia.

**Cel:** wzmocnienie społecznościowej strony TraceLens — użytkownik może atrakcyjnie zaprezentować proces, rezultat i postęp oraz łatwo podzielić się nimi poza aplikacją.

#### Generator filmu timelapse

- Przycisk **Generuj timelapse** znajduje się w galerii zdjęć konkretnego projektu.
- Źródłem filmu są zdjęcia postępu uporządkowane chronologicznie według daty wykonania.
- Format wynikowy: MP4, kodek H.264/AVC, pionowe proporcje 9:16, rozdzielczość 1080 × 1920 i 30 FPS.
- Zdjęcia są dopasowywane do kadru bez deformowania proporcji; niewypełniony obszar otrzymuje neutralne tło.
- Domyślny czas pojedynczego ujęcia wynosi 0,2 sekundy.
- Użytkownik może ustawić czas ujęcia w zakresie od 0,1 do 3 sekund.
- Ekran konfiguracji pokazuje przewidywany czas trwania filmu przed rozpoczęciem generowania.
- Film powinien mieć co najmniej 1 sekundę. Przy bardzo małej liczbie zdjęć ostatnia klatka może zostać odpowiednio wydłużona.
- Dla możliwie szerokiej zgodności społecznościowej aplikacja ostrzega po przekroczeniu 90 sekund i pozwala skrócić czas ujęcia.
- Generowanie odbywa się lokalnie, bez przesyłania zdjęć lub filmu na serwer.
- Podczas kodowania aplikacja pokazuje postęp, umożliwia anulowanie i nie pozwala uruchomić drugiego eksportu równocześnie.

#### Ramka i podpis filmu

- W dolnej części filmu znajduje się dyskretna belka identyfikacyjna umieszczona w bezpiecznej strefie interfejsów Reels i TikToka.
- Po lewej stronie belki znajduje się logotyp oraz skrócona nazwa **TraceLens**.
- Po prawej stronie znajduje się nazwa projektu.
- Nazwa projektu jest jednowierszowa, ma ustalony maksymalny obszar i jest inteligentnie skracana wielokropkiem.
- W podglądzie przed eksportem użytkownik widzi dokładnie tę samą ramkę, która trafi do filmu.
- Branding wykorzystuje wyłącznie własne logo TraceLens i nie dodaje znaków TikToka ani Instagrama.

#### Zapis i udostępnianie

- Gotowy film jest zapisywany przez `MediaStore` w galerii telefonu, w folderze TraceLens nazwanym według projektu.
- Po eksporcie dostępne są akcje **Udostępnij**, **Instagram** i **TikTok**.
- Udostępnianie używa bezpiecznego URI oraz typu MIME `video/mp4`.
- Przycisk ogólny otwiera systemowy arkusz udostępniania Androida.
- Przyciski Instagram i TikTok próbują otworzyć zainstalowaną aplikację bezpośrednio z przekazanym filmem; jeżeli aplikacja nie jest dostępna albo odrzuci żądanie, następuje powrót do systemowego arkusza udostępniania.
- TraceLens nie publikuje materiału automatycznie. Użytkownik zawsze zatwierdza opis, konto, odbiorców i publikację w aplikacji społecznościowej.
- Integracja przez oficjalny TikTok Share Kit pozostaje opcją późniejszą, ponieważ wymaga rejestracji aplikacji, klucza klienta i obsługi odpowiedzi SDK.

#### Kolaż postępu

- W galerii projektu znajduje się akcja **Utwórz kolaż postępu**.
- Użytkownik najpierw wybiera liczbę pól: 2, 4, 6 albo 8.
- Aplikacja pokazuje wyłącznie układy, dla których projekt zawiera wystarczającą liczbę zdjęć postępu. Przykładowo przy czterech zdjęciach dostępne są tylko układy 2 i 4.
- Po wskazaniu układu użytkownik wybiera z galerii dokładnie tyle zdjęć, ile pól zawiera kolaż.
- Wybrane zdjęcia są wyraźnie numerowane, a użytkownik może zmienić ich kolejność przed wygenerowaniem obrazu.
- Przycisk generowania pozostaje nieaktywny do chwili wybrania wymaganej liczby zdjęć.
- Domyślna kolejność odpowiada chronologii wykonania zdjęć, ale można ją zmienić ręcznie.
- Układ dwóch zdjęć prezentuje je obok siebie. Większe zestawy są rozmieszczane w czytelnej, możliwie symetrycznej siatce.
- Zdjęcia nie są deformowane. Aplikacja dopasowuje je do pól przez kadrowanie albo pozostawienie neutralnego marginesu.
- Kolaż zawiera dyskretny logotyp i nazwę TraceLens oraz skróconą nazwę projektu.
- Przed zapisaniem użytkownik widzi pełny podgląd wyniku.
- Gotowy obraz można zapisać w galerii, udostępnić przez system Android albo przekazać do Instagrama i innych zgodnych aplikacji.

#### Referencja i efekt „przed/po”

- W galerii projektu znajduje się osobna akcja **Referencja i efekt**.
- Lewa część zestawienia przedstawia przygotowany obraz referencyjny używany w projekcie.
- Prawa część przedstawia zdjęcie efektu wybrane przez użytkownika z galerii postępu.
- Domyślnie proponowane jest ostatnie chronologicznie zdjęcie, ale użytkownik może wskazać dowolną klatkę.
- Obie części są podpisane lokalizowanymi etykietami **Referencja/Efekt** albo **Przed/Po**.
- Użytkownik może wybrać wariant poziomy do publikacji w aktualnościach albo pionowy 9:16 do Stories.
- Grafika zachowuje proporcje obu obrazów, wykorzystuje neutralne tło i nie rozciąga zawartości.
- Na gotowym zestawieniu znajduje się branding TraceLens oraz skrócona wielokropkiem nazwa projektu.
- Wynik można zapisać do galerii i udostępnić tym samym mechanizmem co kolaż i film timelapse.

#### Wspólny przebieg generatorów społecznościowych

1. Użytkownik wybiera rodzaj materiału: film timelapse, kolaż postępu albo referencja i efekt.
2. Wybiera zdjęcia oraz właściwy układ.
3. Otrzymuje podgląd z brandingiem i nazwą projektu.
4. Generuje materiał lokalnie na urządzeniu.
5. Zapisuje go w galerii albo uruchamia systemowe udostępnianie.

Jeżeli projekt nie ma wystarczającej liczby zdjęć, niedostępne generatory i układy wyświetlają krótkie wyjaśnienie zamiast pozwalać rozpocząć niepełny eksport.

#### Ograniczenia i przypadki brzegowe

- Generator wymaga co najmniej jednego zdjęcia postępu.
- Uszkodzone lub niedostępne zdjęcie jest pomijane, a użytkownik otrzymuje podsumowanie pominiętych klatek.
- Przy braku miejsca w pamięci eksport jest zatrzymywany z czytelnym komunikatem.
- Zmiana orientacji zdjęć jest respektowana na podstawie metadanych i istniejącej normalizacji galerii projektu.
- Usunięcie projektu nie usuwa filmu wcześniej wyeksportowanego do publicznej galerii telefonu.

### Wersja 1.12 — centrum instrukcji

- [X] Osobna zakładka „Instrukcja” pomiędzy Projektami a Ustawieniami.
- [X] Rozdziały: biblioteka, edytor, wzory, projekty, rysowanie, galeria postępu i udostępnianie.
- [X] Wyjaśnienie każdej ikony i mniej oczywistych pojęć, w tym mieszania, blokady kamery, ręcznej ostrości i blokady gestów.
- [X] Wyszukiwanie treści instrukcji i szybkie przechodzenie do rozdziałów.
- [X] Pełna treść po polsku i angielsku.
- [X] Podmiana 12 zielonych makiet na docelowe zrzuty według `docs/instrukcja-aplikacji/README.md`.

**Cel:** użytkownik rozumie cały przepływ pracy i każdą funkcję bez opuszczania aplikacji.

### Wersja 1.13 — bezpieczniejsza edycja obrazów

- Cofnij/Ponów w edytorze.
- Pędzel przywracający usunięte fragmenty.
- Podgląd przezroczystości na szachownicy.
- Automatyczna kopia oryginału przed operacją destrukcyjną.
- Opcjonalnie: presety filtrów oraz obsługa WebP.

**Cel:** użytkownik może eksperymentować z obróbką bez ryzyka utraty dobrze przygotowanego obrazu.

### Wersja 1.14 — organizacja projektów i pełna kopia danych

- Duplikowanie projektu.
- Wyszukiwanie i sortowanie projektów.
- Kopia całej biblioteki, projektów i ustawień.
- Odtwarzanie kopii oraz przenoszenie kompletu danych na drugi telefon.
- Automatyczne testy migracji danych.

**Cel:** zabezpieczenie pracy użytkownika i wygodna obsługa większej liczby projektów.

### Wersja 1.15 — dodatkowe narzędzia kamery

- Ręczna kompensacja ekspozycji.
- Zoom cyfrowy kamery niezależny od nakładki.
- Zamrożenie klatki podczas ustawiania.
- Kalibracja rzeczywistego rozmiaru grafiki.
- Migawka bez nakładki i z nakładką.

**Cel:** większa kontrola nad podglądem bez wchodzenia jeszcze w kosztowną stabilizację obrazu.

### Dalszy etap — katalog wzorów

Społecznościowy katalog pozostaje w backlogu, ale nie został jeszcze oznaczony do realizacji. Najbezpieczniej rozpocząć go dopiero po ukończeniu lokalnej organizacji biblioteki i importu przez „Udostępnij”.

- Publiczny katalog tylko do odczytu.
- Kategorie, wyszukiwanie i cache lokalny.
- Import wzoru do biblioteki.
- Informacje o autorze oraz licencji.
- Moderowane zgłaszanie nowych grafik na stronie.

### Dalszy etap — inteligentne pozycjonowanie 2.0

Funkcje rozpoznawania kartki są zaznaczone do realizacji, ale pozostają osobnym, technicznie wymagającym etapem po wydaniu społecznościowym 1.11:

- wykrywanie kartki,
- korekcja perspektywy,
- stabilizacja nakładki,
- opcjonalne markery stabilizacyjne.

**Najbliższy wybrany numer wersji:** `1.12`

**Proponowana kolejność:** `1.12` → `1.13` → `1.14` → `1.15`
