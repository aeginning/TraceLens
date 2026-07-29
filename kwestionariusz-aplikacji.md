# Kierunki rozwoju TraceLens

Dokument zawiera wyłącznie pomysły i funkcje, które nie zostały jeszcze zrealizowane po wydaniu **TraceLens 1.1**.

Gotowe elementy aplikacji są opisane w [README](README.md), a szczegółowa historia ich wdrażania znajduje się w katalogu [changelog](changelog/README.md).

## Jak korzystać z dokumentu

Wstaw `X` pomiędzy nawiasy przy funkcjach, które mają trafić do kolejnych wersji:

```text
- [X] Funkcja wybrana do realizacji
- [ ] Funkcja oczekująca na decyzję
```

Przy wybranych funkcjach warto dopisać oczekiwany sposób działania w przeznaczonym na to miejscu.

---

## Najważniejsze kandydatury do kolejnej wersji

- [ ] **Automatyczne wykrywanie kartki** — odnajdywanie krawędzi i rogów kartki w podglądzie kamery.
- [ ] **Stabilizacja nakładki** — utrzymywanie grafiki w tym samym miejscu po niewielkim poruszeniu telefonu lub statywu.
- [ ] **Blokada ostrości i ekspozycji** — zapobieganie ciągłemu rozjaśnianiu obrazu i zmianom ostrości podczas przesuwania dłoni nad kartką.
- [ ] **Siatka i prowadnice** — nakładana na podgląd siatka, linie środka oraz regulowany rozmiar pól.
- [ ] **Eksport i import projektu** — zapis projektu razem z obrazem, konfiguracją i zdjęciami postępu do jednego archiwum.
- [ ] **Wyszukiwarka biblioteki** — wyszukiwanie przygotowanych obrazów po nadanej nazwie.
- [ ] **Samouczek pierwszego uruchomienia** — krótkie objaśnienie importu, edycji obrazu, tworzenia projektu i trybu rysowania.

**Uwagi i wybór priorytetu:**



---

## Kamera, kartka i pozycjonowanie

- [ ] **Automatyczne wykrywanie kartki** — rozpoznawanie prostokątnej powierzchni i proponowanie jej czterech rogów.
- [ ] **Automatyczna korekcja perspektywy** — dopasowanie nakładki do wykrytej płaszczyzny kartki.
- [ ] **Stabilizacja nakładki** — śledzenie kartki po drobnym przesunięciu telefonu.
- [ ] **Markery stabilizacyjne** — możliwość wydrukowania znaczników umieszczanych w rogach kartki w celu dokładniejszego śledzenia.
- [ ] **Kalibracja rzeczywistego rozmiaru** — ustawienie rozmiaru grafiki w centymetrach, np. 10 × 15 cm.
- [ ] **Zapamiętywanie ustawienia statywu** — pomoc w odtworzeniu pozycji telefonu po przerwie.
- [ ] **Blokada ostrości** — ręczne ustawienie punktu ostrości i zablokowanie automatycznego autofocusa.
- [ ] **Blokada ekspozycji** — utrzymanie stałej jasności podglądu.
- [ ] **Ręczna kompensacja ekspozycji** — prosty suwak rozjaśniania i przyciemniania obrazu z kamery.
- [ ] **Powiększenie cyfrowe kamery** — zoom podglądu niezależny od skali nakładki.

**Oczekiwany sposób działania:**



---

## Narzędzia wspomagające odrysowywanie

- [ ] **Siatka pomocnicza** — nakładana na cały obszar roboczy.
- [ ] **Regulowany rozmiar pól siatki**.
- [ ] **Linie środka i prowadnice**.
- [ ] **Zmiana koloru i przezroczystości siatki**.
- [ ] **Podział pracy na fragmenty** — zaciemnianie obszarów poza aktualnie odrysowywaną częścią.
- [ ] **Lupa** — powiększony podgląd fragmentu wskazanego palcem.
- [ ] **Zamrożenie klatki z kamery** — ułatwienie dokładnego ustawienia nakładki bez ruchu obrazu.
- [ ] **Przycisk porównania** — przytrzymanie przycisku chwilowo ukrywa nakładkę.
- [ ] **Tryb A/B** — szybkie przełączanie pomiędzy samą kamerą a kamerą z nakładką.
- [ ] **Automatyczne miganie nakładki** — regulowane naprzemienne pokazywanie i ukrywanie grafiki.
- [ ] **Zmiana koloru konturów** — wybór koloru najlepiej widocznego na danym materiale.
- [ ] **Szybkie wartości przezroczystości** — np. 10%, 25%, 50% i 75% obok obecnego suwaka.
- [ ] **Ochrona przed przypadkowym dotknięciem** — czasowe zablokowanie wszystkich kontrolek podczas rysowania.

**Uwagi:**



---

## Edytor obrazów

- [ ] **Uproszczenie obrazu** — ograniczenie liczby kolorów lub poziomu szczegółowości.
- [ ] **Gumka** — ręczne usuwanie niepotrzebnych fragmentów obrazu po automatycznym usunięciu tła.
- [ ] **Pędzel przywracający** — odzyskiwanie fragmentów usuniętych zbyt dużą tolerancją koloru.
- [ ] **Automatyczne usuwanie tła** — wykrywanie głównego obiektu bez ręcznego wskazywania koloru.
- [ ] **Historia zmian Cofnij/Ponów** — cofanie kolejnych operacji edycyjnych.
- [ ] **Porównanie przed/po** — chwilowy podgląd oryginału obok przetworzonego obrazu.
- [ ] **Zapisywanie ustawień jako preset** — ponowne użycie tych samych filtrów dla podobnych grafik.
- [ ] **Obsługa SVG** — import rzeczywistych plików wektorowych z zachowaniem jakości przy skalowaniu.
- [ ] **Obsługa WebP**.
- [ ] **Eksport przygotowanego obrazu** — zapis przetworzonej grafiki do galerii telefonu.

**Uwagi:**



---

## Biblioteka obrazów

- [ ] **Wyszukiwarka po nazwie**.
- [ ] **Sortowanie** — według nazwy, daty dodania, ostatniego użycia lub ulubionych.
- [ ] **Foldery i kolekcje**.
- [ ] **Przenoszenie obrazów pomiędzy folderami**.
- [ ] **Tagi**.
- [ ] **Lista ostatnio używanych obrazów**.
- [ ] **Wykrywanie duplikatów podczas importu**.
- [ ] **Zaznaczanie wielu obrazów** — zbiorcze usuwanie, przypinanie albo przenoszenie.
- [ ] **Udostępnianie obrazu do TraceLens z innej aplikacji** — obsługa systemowego polecenia „Udostępnij”.

**Uwagi:**



---

## Projekty i kopie zapasowe

- [ ] **Zmiana nazwy istniejącego projektu**.
- [ ] **Duplikowanie projektu** — utworzenie kopii z zachowaniem konfiguracji.
- [ ] **Archiwizowanie zakończonych projektów**.
- [ ] **Wyszukiwanie i sortowanie projektów**.
- [ ] **Eksport projektu do jednego archiwum**.
- [ ] **Import wcześniej wyeksportowanego projektu**.
- [ ] **Kopia całej biblioteki i wszystkich projektów**.
- [ ] **Przenoszenie danych na drugi telefon bez konta i chmury**.
- [ ] **Informacja o zajętym miejscu** — rozmiar obrazów i zdjęć postępu wraz z możliwością sprzątania danych.
- [ ] **Automatyczny zapis awaryjny** — dodatkowe zabezpieczenie ostatniego stanu po nieoczekiwanym zamknięciu aplikacji.

**Uwagi:**



---

## Zdjęcia postępu i prezentacja pracy

- [ ] **Automatyczny timelapse** — wykonywanie zdjęcia co określony czas.
- [ ] **Ręczne ustawienie interwału timelapse**.
- [ ] **Film z procesu rysowania**.
- [ ] **Porównanie referencji z ukończoną pracą**.
- [ ] **Zestawienie „przed i po”** — wygenerowanie jednego obrazu do zapisania lub udostępnienia.
- [ ] **Opis zdjęcia postępu** — możliwość dodania krótkiej notatki do etapu.
- [ ] **Wybór zdjęcia okładkowego projektu**.
- [ ] **Udostępnianie zdjęcia lub całej galerii projektu**.

**Uwagi:**



---

## Wygoda i dostępność

- [ ] **Samouczek pierwszego uruchomienia**.
- [ ] **Podpowiedzi tekstowe przy ikonach** — opcjonalny tryb dla nowych użytkowników.
- [ ] **Profile ustawień** — np. ołówek, farby, ściana, mała kartka i duży format.
- [ ] **Regulacja jasności ekranu w aplikacji** — niezależnie od latarki.
- [ ] **Tryb oszczędzania energii** — niższa jakość lub liczba klatek podglądu podczas długiej pracy.
- [ ] **Monitor temperatury i baterii** — czytelny wskaźnik podczas używania kamery i latarki.
- [ ] **Sygnał haptyczny** — delikatna wibracja po zablokowaniu nakładki lub wykonaniu zdjęcia.
- [ ] **Własny wybór motywu** — jasny, ciemny albo zgodny z systemem.
- [ ] **Większy interfejs** — alternatywny rozmiar kontrolek i tekstu.
- [ ] **Język angielski**.

**Uwagi:**



---

## Dystrybucja i jakość aplikacji

- [ ] **Sprawdzanie dostępności nowej wersji** — informacja o nowszym APK opublikowanym na stronie TraceLens.
- [ ] **Bezpieczna aktualizacja z poziomu strony** — przejście bezpośrednio do aktualnego wydania APK.
- [ ] **Podpis produkcyjny APK** — stały klucz wymagany przed szerszym udostępnianiem aplikacji.
- [ ] **Wariant release** — zoptymalizowana kompilacja przeznaczona do dystrybucji.
- [ ] **Testy na różnych rozdzielczościach i producentach telefonów**.
- [ ] **Test długiej pracy** — kamera, ekran i latarka uruchomione przez kilkadziesiąt minut.
- [ ] **Lokalny raport diagnostyczny** — możliwość wyeksportowania informacji technicznych bez wysyłania prywatnych zdjęć.
- [ ] **Przygotowanie strony i aplikacji do przyszłej publikacji w Google Play**.

**Uwagi:**



---

## Propozycje po wydaniu 1.1

Poniższe pomysły wynikają z testowania obecnego fundamentu aplikacji i mogą szczególnie poprawić codzienną pracę.

- [ ] **Tryb „Ustaw statyw ponownie”** — zapisanie zdjęcia kontrolnego i półprzezroczyste pokazanie go przy kolejnym uruchomieniu projektu. Ułatwi ustawienie telefonu dokładnie w poprzednim miejscu bez skomplikowanego śledzenia AR.
- [ ] **Blokada ostrości, ekspozycji i balansu bieli jednym przyciskiem** — obraz nie będzie „pompował” ani zmieniał barwy, gdy ręka znajdzie się pod aparatem.
- [ ] **Kontrola czytelności nakładki** — szybki wybór koloru konturu oraz trybu mieszania odpowiedniego do jasnej lub ciemnej powierzchni.
- [ ] **Migawka bez nakładki i z nakładką** — opcja zapisania dwóch wersji zdjęcia postępu za jednym dotknięciem.
- [ ] **Wskaźnik ustawienia telefonu** — prosta poziomica pokazująca, czy telefon znajduje się równolegle do kartki.
- [ ] **Ostrzeżenie o poruszeniu statywu** — komunikat, gdy po zablokowaniu projektu telefon znacząco zmieni położenie.
- [ ] **Szybkie wznowienie ostatniego projektu** — skrót na ekranie startowym aplikacji.
- [ ] **Automatyczna miniatura postępu na kafelku projektu** — ostatnie zdjęcie pracy zamiast samej grafiki referencyjnej.
- [ ] **Kontrola wolnego miejsca** — ostrzeżenie przed wykonaniem zdjęcia, jeśli pamięć telefonu jest bliska zapełnienia.
- [ ] **Eksport paczki diagnostycznej** — wersja aplikacji, model telefonu, lista kamer i ustawienia projektu bez dołączania prywatnych obrazów.

**Pomysły użytkownika:**



---

## Wybrane do następnego wydania

Po zaznaczeniu funkcji powyżej można tutaj przepisać ostateczny zakres następnej wersji.

- [ ] Pierwsza funkcja
- [ ] Druga funkcja
- [ ] Trzecia funkcja

**Planowany numer wersji:**



**Dodatkowe ustalenia:**
