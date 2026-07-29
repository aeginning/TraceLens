# Kwestionariusz aplikacji do odrysowywania obrazów

## Opis pomysłu

Aplikacja ma działać jak cyfrowa kalka. Telefon umieszczony nad kartką lub innym obiektem pokazuje obraz z wybranej kamery, a na podgląd nakładany jest wybrany przez użytkownika obraz z regulowaną przezroczystością.

> **Ważne:** telefon nie będzie fizycznie wyświetlał obrazu na papierze jak projektor. Obraz referencyjny będzie widoczny na ekranie telefonu, na tle podglądu z kamery.

---

## Pytania

### 1. Urządzenia i dystrybucja

Na jakich urządzeniach aplikacja ma działać?

- Czy aplikacja ma być przeznaczona wyłącznie na Androida?
- Jaka powinna być minimalna wersja systemu, np. Android 10?
- Czy aplikacja ma zostać opublikowana w Google Play, czy będzie używana prywatnie?

**Odpowiedź:**

- Aplikacja będzie przeznaczona wyłącznie na urządzenia z systemem Android.
- Minimalna obsługiwana wersja systemu to **Android 10 (API 29)**.
- W pierwszej kolejności aplikacja będzie wykorzystywana wyłącznie prywatnie i nie będzie publikowana w Google Play.
- W przyszłości możliwa jest publikacja aplikacji, dlatego projekt powinien umożliwiać późniejsze przygotowanie wersji publicznej bez konieczności przebudowy całej aplikacji.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Platforma: Android
- [X] Minimalna wersja: Android 10 / API 29
- [X] Pierwsza dystrybucja: prywatny plik APK
- [ ] Publikacja w Google Play w pierwszej wersji
- [ ] Możliwość przygotowania aplikacji do publikacji w przyszłości

**Dodatkowe uwagi:**

Na etapie tworzenia pierwszej wersji należy umożliwić wygenerowanie instalacyjnego pliku APK. Mimo prywatnego charakteru aplikacji warto od początku stosować poprawne uprawnienia systemowe, stabilny identyfikator pakietu i bezpieczne przechowywanie danych. Ułatwi to ewentualną późniejszą publikację.
### 2. Ustawienie telefonu

Jak telefon będzie ustawiony podczas pracy?

- Pionowo nad kartką, z kamerą skierowaną w dół?
- Pod kątem, np. na statywie stojącym obok użytkownika?
- Czy aplikacja powinna korygować perspektywę przy ustawieniu telefonu pod kątem?

**Odpowiedź:**

- Podstawowym sposobem pracy będzie umieszczenie telefonu **pionowo nad kartką**, z kamerą skierowaną bezpośrednio w dół.
- Aplikacja powinna również umożliwiać pracę z telefonem ustawionym **pod kątem na statywie**.
- Przy ustawieniu telefonu pod kątem aplikacja powinna korygować zniekształcenie perspektywy, jeśli pozwolą na to możliwości techniczne urządzenia i zastosowane rozwiązanie.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Tryb pracy z telefonem ustawionym pionowo nad kartką
- [X] Tryb pracy z telefonem ustawionym pod kątem
- [X] Korekcja perspektywy dla telefonu ustawionego pod kątem
- [X] Możliwość ręcznego wskazania czterech rogów kartki
- [ ] Automatyczne wykrywanie krawędzi i rogów kartki
- [ ] Automatyczne śledzenie kartki po poruszeniu telefonu

**Proponowane działanie:**

W pierwszej wersji korekcja perspektywy może działać przez ręczne wskazanie czterech rogów kartki na podglądzie z kamery. Na tej podstawie aplikacja przekształci obraz referencyjny tak, aby odpowiadał płaszczyźnie kartki widzianej pod kątem.

Automatyczne wykrywanie kartki i ciągłe utrzymywanie nakładki na jej powierzchni można dodać w kolejnej wersji. Takie rozwiązanie będzie bardziej złożone i zależne m.in. od oświetlenia, kontrastu między kartką a otoczeniem oraz wydajności telefonu.
### 3. Źródła i formaty obrazów

Skąd użytkownik będzie dodawał obrazy i jakie formaty powinny być obsługiwane?

- Galeria telefonu?
- Wykonanie nowego zdjęcia?
- Pliki PNG, JPG, JPEG lub WebP?
- Dokumenty PDF, np. kolorowanki lub szkice?
- Obrazy PNG z przezroczystym tłem?

**Odpowiedź:**

- Obrazy będą wybierane z galerii lub pamięci telefonu.
- Będą to przede wszystkim pliki pobrane wcześniej z internetu.
- Podstawowe obsługiwane formaty to **PNG** oraz **JPG/JPEG**.
- Aplikacja musi poprawnie obsługiwać pliki PNG z przezroczystym tłem i zachowywać ich kanał przezroczystości podczas wyświetlania nakładki.
- Użytkownik nie będzie wykonywał w aplikacji nowych zdjęć w celu użycia ich jako obrazów referencyjnych.
- Import dokumentów PDF nie jest potrzebny.
- Obrazami referencyjnymi będą zwykle niewielkie, kolorowe grafiki, ilustracje, cliparty lub grafiki wyglądające jak wektorowe.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Wybór obrazu z galerii lub pamięci telefonu
- [X] Obsługa plików PNG
- [X] Zachowanie przezroczystego tła w plikach PNG
- [X] Obsługa plików JPG/JPEG
- [ ] Obsługa plików WebP
- [ ] Obsługa rzeczywistych plików wektorowych SVG
- [ ] Wykonywanie nowego zdjęcia w aplikacji
- [ ] Import dokumentów PDF

**Kwestia do późniejszego doprecyzowania:**

Określenie „obrazki wektorowe” może oznaczać zarówno grafiki zapisane jako zwykłe pliki PNG/JPG, jak i rzeczywiste pliki wektorowe **SVG**. Obsługa SVG wymaga osobnego mechanizmu importu i renderowania, dlatego przed ustaleniem ostatecznego zakresu należy potwierdzić, czy takie pliki będą rzeczywiście używane.

**Dodatkowe uwagi:**

Do wyboru plików warto użyć systemowego selektora Androida. Pozwoli on wybierać obrazy nie tylko z lokalnej galerii, lecz także z innych źródeł udostępnionych przez system, bez przyznawania aplikacji nadmiernego dostępu do wszystkich zdjęć użytkownika.
### 4. Sterowanie nakładką

Jak użytkownik powinien móc zmieniać położenie obrazu referencyjnego?

- Przesuwanie jednym palcem?
- Skalowanie gestem dwoma palcami?
- Obracanie?
- Odbicie poziome lub pionowe?
- Przycinanie?
- Blokowanie położenia, aby zapobiec przypadkowemu przesunięciu?

**Odpowiedź:**

- Po wybraniu obrazu aplikacja powinna wyznaczyć jego środek i automatycznie umieścić go na środku obszaru podglądu.
- Użytkownik powinien móc przesuwać nakładkę jednym palcem.
- Nakładkę powinno dać się skalować gestem rozsuwania i zsuwania dwóch palców.
- Nakładkę powinno dać się obracać gestem dwoma palcami.
- Potrzebne są osobne polecenia odbicia obrazu w poziomie i w pionie.
- Przycinanie obrazu nie jest wymagane. Wystarczające będzie powiększenie obrazu poza granice ekranu lub obszaru roboczego.
- Po zakończeniu ustawiania użytkownik powinien móc zablokować nakładkę. W stanie zablokowanym gesty nie mogą przypadkowo zmieniać jej pozycji, skali ani obrotu.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Automatyczne wyznaczenie środka obrazu
- [X] Początkowe umieszczenie środka obrazu na środku podglądu
- [X] Przesuwanie jednym palcem
- [X] Skalowanie gestem dwoma palcami
- [X] Obracanie gestem dwoma palcami
- [X] Odbicie w poziomie
- [X] Odbicie w pionie
- [X] Blokowanie i odblokowywanie położenia nakładki
- [X] Zachowanie wybranego położenia, skali, obrotu i odbicia po zablokowaniu
- [ ] Przycinanie obrazu — funkcja obecnie niewymagana

**Proponowany sposób obsługi:**

Po wczytaniu obrazu aplikacja uruchomi tryb edycji nakładki. W tym trybie obraz będzie można przesuwać, skalować i obracać. Widoczny przycisk z ikoną kłódki zatwierdzi ustawienie. Po zablokowaniu nakładki gesty wykonywane na ekranie nie będą jej modyfikować. Ponowne dotknięcie kłódki przywróci tryb edycji.
### 5. Regulacja przezroczystości

Czy przezroczystość nakładki powinna być regulowana płynnie suwakiem od 0% do 100%, czy wystarczą gotowe poziomy?

**Odpowiedź:**

- Przezroczystość nakładki powinna być regulowana płynnie za pomocą suwaka w zakresie od 0% do 100%.
- Suwak jest rozwiązaniem podstawowym i bardziej praktycznym niż lista gotowych wartości.
- Przy suwaku powinna być widoczna aktualna wartość procentowa.
- Regulacja przezroczystości powinna pozostać dostępna również po zablokowaniu położenia nakładki.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Płynny suwak przezroczystości 0–100%
- [X] Wyświetlanie aktualnej wartości w procentach
- [X] Zmiana przezroczystości bez odblokowywania położenia obrazu
- [ ] Gotowe poziomy przezroczystości, np. 5%, 10%, 15%, 20% i 30%

**Sugestia:**

Oprócz suwaka można później dodać przyciski szybkiego wyboru, np. 10%, 25%, 50% i 75%. Nie są one jednak wymagane w pierwszej wersji.
### 6. Tryby przetwarzania obrazu

Jakie dodatkowe tryby obrazu powinny być dostępne?

- Skala szarości?
- Zamiana zdjęcia w kontury lub szkic?
- Zwiększenie kontrastu?
- Odwrócenie kolorów?
- Usuwanie lub ukrywanie białego tła?

**Odpowiedź:**

- Aplikacja powinna umożliwiać zmianę obrazu na skalę szarości.
- Potrzebny jest tryb automatycznej zamiany obrazu na kontur lub szkic. Wynik może zależeć od jakości i rodzaju obrazu, dlatego użytkownik powinien móc regulować parametry efektu.
- Użytkownik powinien mieć możliwość zwiększania i zmniejszania kontrastu.
- Potrzebna jest funkcja odwracania kolorów.
- Aplikacja powinna umożliwiać usunięcie dowolnie wskazanego koloru tła, a nie wyłącznie koloru białego. Użytkownik powinien móc pobrać kolor bezpośrednio z obrazu i zamienić podobne piksele na przezroczyste.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Skala szarości
- [X] Zamiana obrazu na kontur lub szkic
- [X] Regulacja kontrastu
- [X] Odwrócenie kolorów
- [X] Wybór koloru do usunięcia z obrazu
- [X] Pobieranie koloru przez dotknięcie wybranego miejsca na obrazie
- [X] Zamiana wybranego koloru na przezroczystość
- [X] Regulacja tolerancji usuwania podobnych odcieni
- [X] Podgląd efektów w czasie rzeczywistym
- [X] Możliwość wyłączenia filtra i powrotu do oryginalnego obrazu

**Proponowany sposób usuwania tła:**

Użytkownik wybierze narzędzie „Usuń kolor”, a następnie dotknie koloru na obrazie. Aplikacja uczyni ten kolor przezroczystym. Dodatkowy suwak tolerancji określi, jak podobne odcienie również mają zostać usunięte. Niska tolerancja usunie niemal dokładnie wskazany kolor, a wysoka obejmie szerszą grupę odcieni i pomoże przy niejednolitym tle.

Filtry powinny być stosowane niedestrukcyjnie: oryginalny plik pozostaje bez zmian, a aplikacja zapisuje jedynie ustawienia efektów dla danego projektu.
### 7. Zarządzanie biblioteką obrazów

Co powinno obejmować zarządzanie obrazami?

- Kolekcje i foldery?
- Ulubione?
- Lista ostatnio używanych?
- Nazwy, tagi i wyszukiwarka?
- Usuwanie tylko z biblioteki aplikacji czy również z pamięci telefonu?

**Odpowiedź:**

- Użytkownik powinien móc importować kolejne obrazy z pamięci telefonu do prywatnej biblioteki aplikacji.
- Biblioteka powinna prezentować obrazy jako prostą galerię z miniaturami.
- Każdy obraz powinien móc zostać oznaczony gwiazdką jako ulubiony.
- Ulubione obrazy powinny być przypinane lub wyświetlane u góry galerii.
- W pierwszej wersji nie są potrzebne tagi, rozbudowana wyszukiwarka ani zaawansowany menedżer plików.
- Foldery oraz przenoszenie obrazów do folderów mogą zostać dodane jako funkcja dodatkowa.
- Usunięcie obrazu z biblioteki aplikacji nie powinno usuwać jego oryginału z pamięci telefonu.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Import obrazu do prywatnej biblioteki aplikacji
- [X] Galeria miniatur
- [X] Oznaczanie i usuwanie oznaczenia „Ulubione”
- [X] Wyświetlanie ulubionych na początku galerii
- [X] Przeglądanie całej kolekcji obrazów
- [ ] Foldery użytkownika — funkcja dodatkowa
- [ ] Przenoszenie obrazów między folderami — funkcja dodatkowa
- [ ] Tagi
- [ ] Wyszukiwarka

**Dodatkowe uwagi:**

Po imporcie aplikacja powinna przechowywać własną kopię obrazu. Dzięki temu projekt nie przestanie działać, jeśli użytkownik przeniesie lub usunie pierwotny plik z folderu pobranych plików. Biblioteka powinna rozróżniać obraz oryginalny i jego niedestrukcyjnie przetworzoną wersję.
### 8. Zapisywanie projektów

Czy aplikacja powinna zapisywać projekty i ich konfigurację, w tym wybrany obraz, pozycję, skalę, obrót, przezroczystość, kamerę i ustawienie latarki?

**Odpowiedź:**

- Zapisywanie projektów jest jedną z najważniejszych funkcji aplikacji.
- Użytkownik powinien najpierw utworzyć projekt, nadać mu nazwę i wybrać przygotowany wcześniej obraz z biblioteki aplikacji.
- Projekt powinien zapisywać pełną konfigurację pracy, aby można było go później otworzyć i kontynuować rysowanie w tych samych warunkach.
- Do projektu powinna być przypisana galeria zdjęć dokumentujących kolejne etapy pracy.

**Projekt powinien zapisywać:**

- [X] Nazwę projektu
- [X] Wybrany obraz i jego przetworzoną wersję
- [X] Pozycję, skalę i obrót nakładki
- [X] Odbicie obrazu w pionie lub poziomie
- [X] Ustawioną przezroczystość
- [X] Zastosowane filtry i ich parametry
- [X] Wybraną kamerę lub obiektyw
- [X] Ustawienie korekcji perspektywy
- [X] Wybraną orientację ekranu
- [X] Zdjęcia kolejnych etapów pracy
- [X] Datę utworzenia i ostatniej modyfikacji

**Proponowany przebieg:**

1. Użytkownik przygotowuje obraz w edytorze.
2. Tworzy nowy projekt i nadaje mu nazwę.
3. Wybiera przygotowany obraz z biblioteki.
4. Wybiera kamerę i ustawia nakładkę względem kartki.
5. Zapisuje konfigurację.
6. Podczas pracy wykonuje zdjęcia kolejnych etapów.
7. Po ponownym otwarciu projektu aplikacja przywraca jego ustawienia i galerię postępu.
### 9. Obsługa kamer

Które kamery powinny być obsługiwane?

- Tylko główna kamera tylna?
- Kamera przednia i tylna?
- Wybór konkretnego obiektywu: standardowego, szerokokątnego lub tele?

**Odpowiedź:**

- Aplikacja powinna wykrywać wszystkie obiektywy udostępniane przez system Android danego telefonu.
- Użytkownik powinien móc wybrać konkretną kamerę lub obiektyw, np. standardowy, szerokokątny albo tele.
- Wybrany obiektyw powinien zostać zapisany osobno dla każdego projektu.
- Po ponownym otwarciu projektu aplikacja powinna spróbować automatycznie uruchomić wcześniej używaną kamerę.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Wykrywanie kamer i obiektywów udostępnianych przez urządzenie
- [X] Ręczny wybór kamery lub obiektywu
- [X] Czytelna lista dostępnych kamer
- [X] Zapis wybranej kamery w projekcie
- [X] Przywrócenie wybranej kamery po ponownym otwarciu projektu
- [X] Bezpieczne użycie kamery domyślnej, jeśli zapisana kamera nie jest już dostępna

**Ograniczenie techniczne:**

Niektóre telefony nie udostępniają aplikacjom wszystkich fizycznych obiektywów albo prezentują kilka z nich jako jedną kamerę logiczną. Aplikacja pokaże wszystkie kamery, do których system Android faktycznie zapewni dostęp.
### 10. Latarka

Jak powinno wyglądać sterowanie latarką?

- Przełącznik włącz/wyłącz?
- Regulacja jasności, jeśli urządzenie ją obsługuje?
- Ostrzeżenie o nagrzewaniu telefonu?

**Odpowiedź:**

- Potrzebny jest prosty przełącznik latarki: włącz/wyłącz.
- Regulacja jasności latarki nie jest wymagana.
- Aplikacja powinna ostrzegać użytkownika o ryzyku nagrzewania telefonu podczas długotrwałego używania latarki i kamery.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Przełącznik latarki włącz/wyłącz
- [X] Widoczny stan latarki
- [X] Ostrzeżenie o możliwym nagrzewaniu urządzenia
- [X] Automatyczne wyłączenie latarki po opuszczeniu ekranu roboczego lub zamknięciu aplikacji
- [ ] Regulacja jasności latarki — funkcja niewymagana

**Sugestia bezpieczeństwa:**

Aplikacja może monitorować stan termiczny udostępniany przez Androida. Przy podwyższonej temperaturze powinna wyświetlić ostrzeżenie, a przy stanie krytycznym wyłączyć latarkę i poinformować użytkownika. Nie każdy telefon udostępnia dokładną temperaturę, dlatego mechanizm powinien korzystać z systemowych poziomów stanu termicznego, jeśli są dostępne.
### 11. Ekran i orientacja

Czy podczas pracy ekran powinien pozostawać stale włączony? Czy użytkownik powinien móc zablokować orientację pionową lub poziomą?

**Odpowiedź:**

- Podczas aktywnej pracy ekran powinien pozostawać stale włączony.
- Użytkownik powinien móc wybrać i zablokować orientację ekranu, aby nie zmieniała się po poruszeniu telefonem.
- Dostępne powinny być co najmniej: orientacja pionowa, orientacja pozioma oraz tryb zgodny z czujnikiem/systemem.
- Blokada orientacji powinna zostać zapisana w ustawieniach projektu.
- Po opuszczeniu ekranu roboczego aplikacja powinna przywrócić standardowe zachowanie wygaszania ekranu.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Utrzymywanie włączonego ekranu podczas pracy
- [X] Blokada orientacji pionowej
- [X] Blokada orientacji poziomej
- [X] Opcjonalny tryb automatycznej orientacji
- [X] Zapis orientacji w projekcie
### 12. Dokumentowanie postępu

Czy aplikacja powinna umożliwiać wykonywanie zdjęć kolejnych etapów pracy albo nagrywanie procesu jako filmu lub timelapse?

**Odpowiedź:**

- Użytkownik powinien móc okresowo wykonywać zdjęcia kolejnych etapów rysowania.
- Zdjęcia powinny być zapisywane w galerii przypisanej do konkretnego projektu.
- Galeria postępu powinna prezentować zdjęcia chronologicznie wraz z datą i godziną wykonania.
- Nagrywanie filmu i automatyczny timelapse nie są obecnie wymagane.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Wykonywanie zdjęć postępu z poziomu ekranu roboczego
- [X] Zapisywanie zdjęć wewnątrz konkretnego projektu
- [X] Chronologiczna galeria postępu
- [X] Podgląd zdjęcia na pełnym ekranie
- [ ] Usuwanie wybranego zdjęcia po potwierdzeniu
- [ ] Film
- [ ] Automatyczny timelapse

**Kwestia do późniejszego doprecyzowania:**

Należy ustalić, czy zdjęcie postępu ma zawierać sam obraz z kamery, czy również widoczną nakładkę referencyjną. Możliwe jest także udostępnienie obu wariantów.
### 13. Języki

Czy interfejs ma działać wyłącznie po polsku, czy również w innych językach, np. po angielsku?

**Odpowiedź:**

- Pierwsza wersja interfejsu będzie dostępna w języku polskim.
- Architektura aplikacji powinna od początku umożliwiać dodanie kolejnych tłumaczeń bez przebudowy interfejsu.
- Wszystkie teksty widoczne dla użytkownika powinny znajdować się w zasobach językowych, a nie być wpisane bezpośrednio w kodzie.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Język polski w pierwszej wersji
- [X] Przygotowanie aplikacji do lokalizacji
- [ ] Język angielski w pierwszej wersji
- [ ] Inne języki w pierwszej wersji
### 14. Zakres interfejsu

Czy aplikacja ma być prostym narzędziem skupionym na jednym ekranie roboczym, czy rozbudowanym menedżerem wielu projektów?

**Odpowiedź:**

- Interfejs powinien pozostać prosty i koncentrować się na głównym zadaniu aplikacji.
- Nie jest potrzebny rozbudowany menedżer plików.
- Aplikacja powinna rozdzielać przygotowanie obrazów od właściwej pracy z kamerą.

**Główne obszary aplikacji:**

1. **Edytor i biblioteka obrazów** — importowanie grafik, oznaczanie ulubionych oraz przygotowanie obrazu za pomocą filtrów.
2. **Projekty i ekran roboczy** — tworzenie i otwieranie projektów, wybór przygotowanego obrazu, konfiguracja kamery i nakładki, rysowanie oraz dokumentowanie postępu.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Prosta nawigacja między dwoma głównymi obszarami
- [X] Edytor obrazów oddzielony od ekranu rysowania
- [X] Lista projektów jako część obszaru projektów
- [X] Jeden główny ekran roboczy z kamerą i nakładką
- [ ] Rozbudowany menedżer wielu plików
### 15. Wygląd aplikacji

Jak powinien wyglądać interfejs?

- Jasny, ciemny czy zgodny z ustawieniem systemowym?
- Minimalistyczny czy z widocznymi wszystkimi narzędziami?
- Czy najważniejsze przyciski powinny być duże, aby łatwo korzystać z nich podczas rysowania?

**Odpowiedź:**

- Motyw aplikacji powinien automatycznie odpowiadać jasnemu lub ciemnemu motywowi systemu Android.
- Interfejs powinien być minimalistyczny.
- Najważniejsze elementy sterujące powinny być duże i łatwe do naciśnięcia podczas pracy.
- Mniej używane ustawienia mogą być ukryte w rozwijanym panelu, aby nie zasłaniały obrazu z kamery.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Motyw zgodny z ustawieniami systemowymi
- [X] Tryb jasny
- [X] Tryb ciemny
- [X] Minimalistyczny interfejs
- [X] Duże przyciski najważniejszych funkcji
- [X] Ograniczenie elementów zasłaniających podgląd z kamery
### 16. Obsługa bez dotykania ekranu

Czy potrzebne jest sterowanie przyciskami głośności, głosem, gestami lub urządzeniem Bluetooth?

**Odpowiedź:**

Sterowanie bez dotykania ekranu nie jest potrzebne.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [ ] Sterowanie przyciskami głośności
- [ ] Sterowanie głosowe
- [ ] Gesty bezdotykowe
- [ ] Pilot lub przycisk Bluetooth
### 17. Dane i prywatność

Czy wszystkie obrazy i projekty mają pozostawać wyłącznie na urządzeniu, czy potrzebna będzie synchronizacja lub kopia zapasowa w chmurze?

**Odpowiedź:**

- Wszystkie obrazy, projekty, ustawienia i zdjęcia postępu będą przechowywane lokalnie na urządzeniu.
- Synchronizacja z chmurą i konto użytkownika nie są potrzebne.
- Aplikacja nie powinna wysyłać prywatnych obrazów ani zdjęć na zewnętrzne serwery.
- Przetwarzanie obrazu powinno odbywać się lokalnie.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Lokalne przechowywanie danych
- [X] Lokalne przetwarzanie obrazów
- [X] Działanie bez konta użytkownika
- [X] Możliwość pracy bez internetu
- [ ] Synchronizacja w chmurze
- [ ] Automatyczna zdalna kopia zapasowa

**Dodatkowa sugestia:**

W przyszłości można dodać ręczny eksport i import kopii projektu do pojedynczego archiwum. Nie wymagałoby to konta ani chmury, a pozwoliłoby przenieść projekt na inne urządzenie lub utworzyć kopię zapasową.
### 18. Model aplikacji

Czy aplikacja ma być bezpłatna, płatna jednorazowo, zawierać reklamy lub oferować dodatkowe funkcje premium?

**Odpowiedź:**

- Aplikacja powinna być bezpłatna.
- Pierwsza prywatna wersja nie będzie zawierać reklam, zakupów w aplikacji ani płatnych funkcji.
- Ewentualny model dystrybucji po przyszłej publikacji nie jest obecnie ustalony.

**Ustalenia projektowe wynikające z odpowiedzi:**

- [X] Bezpłatna aplikacja
- [X] Brak reklam
- [X] Brak zakupów w aplikacji
- [X] Brak funkcji premium w pierwszej wersji
---

## Proponowany zakres pierwszej wersji (MVP)

Wstaw `X` pomiędzy nawiasy przy wybranych funkcjach, np. `[X]`.

- [ ] Podgląd obrazu na żywo z tylnej kamery
- [X] Import obrazów PNG, JPG i JPEG z galerii
- [ ] Obsługa obrazów WebP z galerii
- [ ] Wykonanie zdjęcia i użycie go jako nakładki — funkcja obecnie niewymagana
- [X] Prywatna biblioteka zaimportowanych obrazów
- [X] Oznaczanie obrazów jako ulubione
- [X] Tworzenie, zapisywanie i ponowne otwieranie projektów
- [X] Zapisywanie konfiguracji obrazu, kamery i orientacji w projekcie
- [X] Galeria zdjęć postępu przypisana do projektu
- [X] Nakładanie obrazu na podgląd z kamery
- [X] Automatyczne wyśrodkowanie nowo wybranej nakładki
- [X] Płynna regulacja przezroczystości od 0% do 100%
- [X] Przesuwanie obrazu jednym palcem
- [X] Skalowanie obrazu gestem dwoma palcami
- [X] Obracanie obrazu
- [X] Odbicie obrazu w poziomie i pionie
- [X] Blokowanie położenia i rozmiaru nakładki
- [X] Wybór dostępnej kamery lub obiektywu
- [X] Włączanie i wyłączanie latarki
- [X] Utrzymywanie aktywnego ekranu podczas pracy
- [X] Blokowanie orientacji ekranu
- [X] Tryb pełnoekranowy
- [X] Automatyczne zapisywanie i przywracanie ostatniej sesji
- [X] Duże, łatwo dostępne kontrolki
- [X] Ukrywanie kontrolek podczas odrysowywania

**Uwagi dotyczące pierwszej wersji:**




---

## Przydatne rozszerzenia

Wstaw `X` pomiędzy nawiasy przy rozszerzeniach, które chcesz uwzględnić.

### Dopasowanie i stabilizacja

- [X] **Ręczna korekcja perspektywy** — wskazanie czterech rogów kartki i dopasowanie do nich obrazu
- [ ] **Automatyczne wykrywanie kartki** — rozpoznawanie jej krawędzi w obrazie z kamery
- [ ] **Stabilizacja nakładki** — utrzymywanie obrazu w tym samym miejscu po niewielkim poruszeniu telefonu
- [ ] **Znaczniki w rogach kartki** — śledzenie wydrukowanych markerów i precyzyjne pozycjonowanie obrazu
- [ ] **Kalibracja rozmiaru** — ustawienie rzeczywistego rozmiaru obrazu, np. 10 × 15 cm
- [ ] **Zapamiętywanie ustawienia statywu** — pomoc w ponownym ustawieniu telefonu po przerwie

### Przetwarzanie obrazu

- [X] **Tryb konturów** — automatyczna zamiana zdjęcia w czytelny szkic
- [X] **Skala szarości**
- [X] **Regulacja kontrastu**
- [X] **Odwrócenie kolorów**
- [X] **Usuwanie wybranego koloru tła** — wybór koloru z obrazu i regulacja tolerancji
- [X] **Regulacja grubości wykrytych konturów**
- [ ] **Uproszczenie obrazu** — ograniczenie liczby szczegółów lub kolorów
- [ ] **Przycinanie obrazu** — funkcja obecnie niewymagana
- [ ] **Gumka do usuwania fragmentów nakładki**

### Narzędzia wspomagające rysowanie

- [ ] **Siatka pomocnicza**
- [ ] **Regulowany rozmiar pól siatki**
- [ ] **Linie środka i prowadnice**
- [ ] **Podział pracy na fragmenty**
- [ ] **Powiększenie wybranego fragmentu**
- [ ] **Zamrożenie obrazu z kamery podczas ustawiania nakładki**
- [ ] **Tryb porównania** — chwilowe ukrywanie nakładki
- [ ] **Tryb A/B** — szybkie przełączanie między samą kamerą a kamerą z nakładką
- [ ] **Automatyczne miganie nakładki** z regulowaną częstotliwością
- [ ] **Zmiana koloru konturów nakładki**

### Projekty i biblioteka

- [X] **Zapisywanie wielu projektów**
- [X] **Miniatury projektów**
- [ ] **Foldery i kolekcje** — funkcja dodatkowa
- [X] **Ulubione obrazy**
- [ ] **Lista ostatnio używanych obrazów**
- [ ] **Tagi i wyszukiwarka**
- [ ] **Duplikowanie projektu**
- [ ] **Eksport i import projektu**
- [ ] **Kopia zapasowa w chmurze** — funkcja niewymagana

### Dokumentowanie postępu

- [X] **Zdjęcia kolejnych etapów pracy**
- [X] **Galeria postępu przypisana do projektu**
- [ ] **Nagrywanie filmu** — funkcja obecnie niewymagana
- [ ] **Automatyczny timelapse** — funkcja obecnie niewymagana
- [ ] **Porównanie obrazu referencyjnego z ukończoną pracą**
- [ ] **Eksport zestawienia „przed i po”**

### Obsługa i wygoda

- [ ] **Tryb ochrony przed przypadkowym dotknięciem**
- [ ] **Sterowanie przyciskami głośności** — funkcja niewymagana
- [ ] **Sterowanie głosowe** — funkcja niewymagana
- [ ] **Obsługa pilota lub przycisku Bluetooth** — funkcja niewymagana
- [X] **Tryb jasny i ciemny zgodny z ustawieniem systemowym**
- [X] **Automatyczne dopasowanie interfejsu do orientacji ekranu**
- [ ] **Samouczek dla nowych użytkowników**
- [ ] **Profile ustawień dla różnych rodzajów pracy**
- [ ] **Regulacja jasności latarki** — funkcja niewymagana
- [X] **Ostrzeżenie o nagrzewaniu telefonu i wysokim zużyciu baterii**
- [ ] **Tryb oszczędzania energii**

### Import i eksport

- [ ] **Import plików PDF** — funkcja obecnie niewymagana
- [ ] **Wybór strony z dokumentu PDF**
- [ ] **Import obrazu z aparatu** — funkcja obecnie niewymagana
- [ ] **Udostępnianie obrazu do aplikacji z innych aplikacji**
- [ ] **Eksport przetworzonego obrazu lub jego konturów**
- [ ] **Drukowanie obrazu lub markerów stabilizacyjnych**

**Inne pomysły i wymagania:**




---

## Priorytety

### Funkcje absolutnie niezbędne




### Funkcje przydatne, ale możliwe do dodania później




### Funkcje, których aplikacja nie potrzebuje
