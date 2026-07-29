# Lista publikacyjna Google Play — TraceLens 1.5

## Gotowe w repozytorium

- [x] Unikalny identyfikator pakietu `pl.tracelens.app`.
- [x] `targetSdk 36`.
- [x] Minimalny Android 10 (`minSdk 29`).
- [x] Produkcyjny wariant `release` z R8 i usuwaniem nieużywanych zasobów.
- [x] Lokalny klucz wysyłania poza repozytorium Git.
- [x] Podpisany Android App Bundle.
- [x] Audyt uprawnień.
- [x] Publiczna polityka prywatności i odnośnik w aplikacji.
- [x] Projekt deklaracji Data Safety.
- [x] Polska karta sklepu.
- [x] Ikona 512 × 512, grafika 1024 × 500 i zrzuty telefonu.

## Do wykonania w Play Console przez właściciela konta

- [ ] Utworzyć aplikację z identyfikatorem `pl.tracelens.app`.
- [ ] Zaakceptować regulaminy i skonfigurować Play App Signing.
- [ ] Przesłać `TraceLens-1.5.aab` jako pierwsze wydanie testu wewnętrznego.
- [ ] Uzupełnić Data Safety według `data-safety-pl.md`.
- [ ] Uzupełnić ankietę klasyfikacji treści.
- [ ] Ustawić grupę docelową i deklarację reklam.
- [ ] Wkleić opis oraz przesłać materiały z katalogu `assets`.
- [ ] Dodać kraje dystrybucji i zapisać ustawienie aplikacji jako bezpłatnej.
- [ ] Dodać testerów i przeprowadzić test wewnętrzny przed produkcją.

## Bezpieczeństwo klucza

Pliki `keystore/tracelens-upload.jks` i `.env.signing` są lokalne oraz ignorowane przez Git. Należy wykonać zaszyfrowaną kopię zapasową. Utrata klucza wysyłania wymaga procedury resetu w Play Console.
