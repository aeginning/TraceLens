# Lista publikacyjna Google Play — TraceLens 1.9.4

## Gotowe w repozytorium

- [x] Unikalny identyfikator pakietu `pl.tracelens.app`.
- [x] `targetSdk 36`.
- [x] Minimalny Android 10 (`minSdk 29`).
- [x] Produkcyjny wariant `release` z R8 i usuwaniem nieużywanych zasobów.
- [x] Lokalny klucz wysyłania poza repozytorium Git.
- [x] Podpisany Android App Bundle z `versionCode 28`.
- [x] Audyt uprawnień.
- [x] Publiczna polityka prywatności i odnośnik w aplikacji.
- [x] Projekt deklaracji Data Safety.
- [x] Polska karta sklepu.
- [x] Ikona 512 × 512, grafika 1024 × 500 i zrzuty telefonu.

## Do wykonania w Play Console przez właściciela konta

- [x] Utworzono aplikację z identyfikatorem `pl.tracelens.app`.
- [x] Skonfigurowano Play App Signing i wcześniejszy test wewnętrzny.
- [ ] Otworzyć **Testowanie → Test zamknięty** i utworzyć ścieżkę `Alpha`.
- [ ] Przesłać `TraceLens-1.9.4-alpha.aab`.
- [ ] Wkleić informacje o wersji z `releases/1.9.4-alpha/release-notes-pl.txt`.
- [ ] Dodać listę testerów albo Grupę dyskusyjną Google do ścieżki Alpha.
- [ ] Wybrać kraje i regiony dostępne w teście.
- [ ] Zapisać wersję, sprawdzić podsumowanie i rozpocząć wdrażanie do Alpha.
- [ ] Uzupełnić Data Safety według `data-safety-pl.md`.
- [ ] Uzupełnić ankietę klasyfikacji treści.
- [ ] Ustawić grupę docelową i deklarację reklam.
- [ ] Wkleić opis oraz przesłać materiały z katalogu `assets`.
- [ ] Dodać kraje dystrybucji i zapisać ustawienie aplikacji jako bezpłatnej.
- [ ] Otworzyć link akceptacji testu na koncie testera i zainstalować wersję
  z Google Play.

## Bezpieczeństwo klucza

Pliki `keystore/tracelens-upload.jks` i `.env.signing` są lokalne oraz ignorowane przez Git. Należy wykonać zaszyfrowaną kopię zapasową. Utrata klucza wysyłania wymaga procedury resetu w Play Console.
