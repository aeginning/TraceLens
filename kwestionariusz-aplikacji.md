# Kierunki rozwoju TraceLens

Dokument zawiera wyłącznie pomysły i funkcje, które nie zostały jeszcze zrealizowane po wydaniu **TraceLens 1.5**.

Gotowe elementy aplikacji są opisane w [README](README.md), a historia wdrożeń znajduje się w katalogu [changelog](changelog/README.md).

## Jak korzystać z dokumentu

Wstaw `X` pomiędzy nawiasy przy funkcjach wybranych do kolejnego wydania:

```text
- [X] Funkcja wybrana do realizacji
- [ ] Funkcja oczekująca na decyzję
```

---

## Najważniejsze kandydatury do kolejnego wydania

- [ ] **Wyszukiwarka biblioteki** — wyszukiwanie przygotowanych obrazów po nazwie.
- [X] **Zmiana nazwy projektu**.
- [ ] **Cofnij/Ponów w edytorze** — historia operacji bez utraty oryginału.
- [ ] **Pędzel przywracający** — odzyskiwanie fragmentów usuniętych gumką lub filtrem tła.
- [X] **Eksport i import projektu** — jedno archiwum zawierające obraz, ustawienia i zdjęcia postępu.
- [X] **Samouczek pierwszego uruchomienia**.
- [ ] **Testy na większej liczbie telefonów** — różni producenci, aparaty i proporcje ekranów.

**Uwagi i wybór priorytetu:**



---

## Kamera, kartka i pozycjonowanie

- [ ] **Automatyczne wykrywanie kartki** — rozpoznawanie krawędzi oraz proponowanie czterech rogów.
- [ ] **Automatyczna korekcja perspektywy** — dopasowanie nakładki do wykrytej płaszczyzny.
- [ ] **Stabilizacja nakładki** — utrzymywanie grafiki na kartce po niewielkim poruszeniu telefonu.
- [ ] **Markery stabilizacyjne** — drukowane znaczniki w rogach kartki dla dokładniejszego śledzenia.
- [ ] **Kalibracja rzeczywistego rozmiaru** — ustawienie grafiki w centymetrach.
- [X] **Ręczny punkt ostrości** — dotknięcie miejsca, na którym kamera ma ustawić ostrość.
- [ ] **Ręczna kompensacja ekspozycji**.
- [ ] **Zoom cyfrowy kamery** niezależny od skali nakładki.
- [ ] **Zamrożenie klatki z kamery** podczas precyzyjnego ustawiania grafiki.
- [ ] **Migawka bez nakładki i z nakładką** — zapis dwóch wersji zdjęcia jednym dotknięciem.

**Oczekiwany sposób działania:**



---

## Narzędzia wspomagające odrysowywanie

- [X] **Zmiana koloru i przezroczystości siatki**.
- [X] **Zmiana koloru konturu nakładki**.
- [ ] **Szybkie wartości przezroczystości** — np. 10%, 25%, 50% i 75%.
- [X] **Podział pracy na fragmenty** — przyciemnianie obszarów poza aktualnie rysowaną częścią.
- [ ] **Lupa** — powiększenie fragmentu wskazanego palcem.
- [ ] **Ochrona przed przypadkowym dotknięciem** — czasowa blokada wszystkich kontrolek.
- [ ] **Profile pracy** — np. ołówek, farby, ściana, mała kartka i duży format.

**Uwagi:**



---

## Edytor obrazów

- [ ] **Pędzel przywracający**.
- [ ] **Historia zmian Cofnij/Ponów**.
- [ ] **Presety filtrów** — zapis i ponowne użycie zestawu ustawień.
- [ ] **Obsługa SVG** z zachowaniem jakości skalowania.
- [ ] **Obsługa WebP**.
- [X] **Automatyczne prostowanie i obracanie obrazu**.
- [X] **Usuwanie drobnych artefaktów tła** — czyszczenie pojedynczych pikseli i małych wysp.
- [ ] **Podgląd na szachownicy** — czytelniejsze przedstawienie przezroczystych obszarów.

**Uwagi:**



---

## Biblioteka obrazów

- [ ] **Tagi**.
- [ ] **Lista ostatnio używanych obrazów**.
- [ ] **Wykrywanie duplikatów podczas importu**.
- [ ] **Zaznaczanie wielu obrazów** i operacje zbiorcze.
- [X] **Udostępnianie obrazu do TraceLens z innej aplikacji**.
- [X] **Wbudowany pakiet startowy** — kilka prostych, legalnych wzorów dostępnych bez internetu.

**Uwagi:**



---

## Projekty i kopie zapasowe

- [ ] **Duplikowanie projektu** z zachowaniem konfiguracji.
- [ ] **Wyszukiwanie i sortowanie projektów**.
- [ ] **Kopia całej biblioteki i wszystkich projektów**.
- [ ] **Przenoszenie danych na drugi telefon bez konta i chmury**.
- [ ] **Eksport paczki diagnostycznej** bez prywatnych zdjęć.

**Uwagi:**



---

## Zdjęcia postępu i prezentacja pracy

- [ ] **Film z procesu rysowania** generowany ze zdjęć timelapse.
- [ ] **Porównanie referencji z ukończoną pracą**.
- [ ] **Zestawienie „przed i po”** jako jeden obraz.
- [ ] **Opis zdjęcia postępu** — krótka notatka do etapu.
- [ ] **Eksport osi czasu projektu** — zdjęcia, daty i notatki w jednym raporcie.

**Uwagi:**



---

## Wygoda i dostępność

- [X] **Samouczek pierwszego uruchomienia**.
- [X] **Opcjonalne podpowiedzi tekstowe przy ikonach**.
- [ ] **Regulacja jasności ekranu w aplikacji**.
- [ ] **Tryb oszczędzania energii**.
- [X] **Rozbudowany monitor temperatury i baterii**.
- [ ] **Sygnał haptyczny** po zablokowaniu nakładki lub wykonaniu zdjęcia.
- [X] **Własny wybór motywu** — jasny, ciemny lub systemowy.
- [ ] **Alternatywny rozmiar kontrolek i tekstu**.
- [X] **Język angielski**.

**Uwagi:**



---

## Dystrybucja i jakość

- [X] **Sprawdzanie dostępności aktualizacji** z Google Play lub strony TraceLens.
- [ ] **Testy na różnych rozdzielczościach i urządzeniach**.
- [ ] **Test długiej pracy** — kamera, ekran, timelapse i latarka przez kilkadziesiąt minut.
- [ ] **Automatyczne testy migracji danych** pomiędzy kolejnymi wersjami.
- [ ] **Automatyczne testy najważniejszych ekranów**.
- [X] **Raport awarii przechowywany lokalnie** i eksportowany wyłącznie na żądanie.

**Uwagi:**

- Konto deweloperskie Google Play jest w trakcie zakładania.

---

## Wyszukiwanie obrazów w internecie

### Wariant bez kosztów i bez klucza API

- [ ] **Otwórz wyszukiwanie w przeglądarce** — aplikacja uruchamia przygotowane zapytanie, np. „prosty ptak kontur SVG”.
- [ ] **Udostępnij znaleziony obraz do TraceLens** — użytkownik korzysta z systemowej funkcji „Udostępnij”, a TraceLens importuje otrzymany plik.
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

## Dodatkowe pomysły po wersji 1.5

- [ ] **Ocena łatwości odrysowania** — prosta, średnia lub szczegółowa.
- [ ] **Automatyczna ocena złożoności obrazu** na podstawie liczby krawędzi i kolorów.
- [ ] **Podobne wzory** wyliczane lokalnie z tagów, bez profilowania użytkownika.
- [ ] **Paczki tematyczne** — zwierzęta, kwiaty, pojazdy, dekoracje.
- [ ] **Tryb prezentacyjny projektu** — uporządkowana galeria gotowa do pokazania.
- [ ] **Lista licencji i autorów pobranych wzorów** dostępna także offline.
- [ ] **Automatyczne tworzenie kopii oryginału przed każdą destrukcyjną operacją**.

---

## Proponowany podział kolejnych etapów

Poniższa roadmapa obejmuje wyłącznie funkcje oznaczone wyżej przez użytkownika jako wybrane. Powtórzone wymagania zostały scalone w pojedyncze zadania.

### Wersja 1.8 — pierwsze uruchomienie, dostępność i języki

- Samouczek pierwszego uruchomienia.
- Opcjonalne podpowiedzi tekstowe przy ikonach.
- Wybór motywu: jasny, ciemny lub systemowy.
- Język angielski.

**Dlaczego razem:** dodanie języka angielskiego wymaga przeniesienia tekstów interfejsu do zasobów tłumaczeń. To dobry moment, aby ujednolicić komunikaty, podpowiedzi i treść samouczka.

**Najważniejsza decyzja przed implementacją:** czy język ma być zawsze zgodny z systemem, czy użytkownik może wybrać go ręcznie w ustawieniach.

### Wersja 1.9 — dokładniejszy edytor i pakiet startowy

- Automatyczne prostowanie i obracanie importowanego obrazu.
- Usuwanie drobnych artefaktów tła, pojedynczych pikseli i małych wysp.
- Wbudowany pakiet prostych, legalnych wzorów dostępnych offline.

**Dlaczego razem:** wydanie skupia się na doprowadzeniu obrazu do stanu gotowego do odrysowania przed utworzeniem projektu.

**Wymaganie prawne:** każdy wzór w pakiecie startowym musi mieć udokumentowane źródło i licencję pozwalającą na dystrybucję z aplikacją.

### Wersja 1.10 — precyzja podczas rysowania

- Ręczny punkt ostrości wybierany dotknięciem podglądu.
- Zmiana koloru i przezroczystości siatki.
- Zmiana koloru konturu nakładki.
- Podział pracy na fragmenty i przyciemnianie nieaktywnego obszaru.
- Rozbudowany monitor temperatury i poziomu baterii.

**Dlaczego razem:** wszystkie funkcje działają bezpośrednio na ekranie aparatu i powinny zostać przetestowane wspólnie podczas dłuższej sesji rysowania.

**Ryzyko:** zmiany gestów na podglądzie nie mogą kolidować ze skalowaniem, obracaniem nakładki ani blokadą jej położenia.

### Wersja 1.11 — aktualizacje i diagnostyka

- Sprawdzanie dostępności aktualizacji z Google Play lub strony TraceLens.
- Lokalny raport awarii przechowywany na urządzeniu.
- Eksport raportu wyłącznie po świadomej decyzji użytkownika.

**Dlaczego razem:** wydanie skupia się na utrzymaniu aplikacji po szerszej publikacji i diagnozowaniu problemów bez automatycznej telemetrii.

**Założenie prywatności:** aplikacja nie wysyła raportów samodzielnie. Użytkownik przegląda zakres danych i uruchamia systemowe udostępnianie.

### Dalszy etap — katalog wzorów

Społecznościowy katalog pozostaje w backlogu, ale nie został jeszcze oznaczony do realizacji. Najbezpieczniej rozpocząć go dopiero po ukończeniu lokalnej organizacji biblioteki i importu przez „Udostępnij”.

- Publiczny katalog tylko do odczytu.
- Kategorie, wyszukiwanie i cache lokalny.
- Import wzoru do biblioteki.
- Informacje o autorze oraz licencji.
- Moderowane zgłaszanie nowych grafik na stronie.

### Dalszy etap — inteligentne pozycjonowanie 2.0

Funkcje rozpoznawania kartki nie zostały zaznaczone do najbliższej realizacji i pozostają kierunkiem długoterminowym:

- wykrywanie kartki,
- korekcja perspektywy,
- stabilizacja nakładki,
- opcjonalne markery stabilizacyjne.

**Planowany numer wersji:** `1.8`

**Kolejność realizacji:** `1.8` → `1.9` → `1.10` → `1.11`
