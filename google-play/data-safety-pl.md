# Google Play — deklaracja Data Safety

Stan dla TraceLens 1.5.

## Odpowiedzi główne

- Czy aplikacja zbiera lub udostępnia wymagane typy danych użytkownika? **Nie.**
- Czy dane są szyfrowane podczas przesyłania? **Nie dotyczy — aplikacja nie przesyła danych.**
- Czy użytkownik może poprosić o usunięcie danych? **Dane są przechowywane wyłącznie lokalnie i użytkownik usuwa je samodzielnie w aplikacji, galerii telefonu, ustawieniach systemowych lub przez odinstalowanie aplikacji.**
- Czy aplikacja przeszła niezależny przegląd bezpieczeństwa? **Nie.**

## Uzasadnienie

- Brak kont użytkowników, reklam, analityki, telemetrii i zewnętrznych SDK przesyłających dane.
- Obrazy, projekty, ustawienia i zdjęcia są przetwarzane lokalnie.
- Funkcja udostępniania uruchamia systemowy panel Android wyłącznie na żądanie użytkownika.
- Strona pobierania i polityka prywatności nie są otwierane automatycznie i nie otrzymują danych aplikacji.

## Uprawnienia

| Uprawnienie | Cel |
|---|---|
| `android.permission.CAMERA` | Podgląd aparatu, zdjęcia postępu i obsługa latarki |

Aplikacja nie żąda lokalizacji, mikrofonu, kontaktów, telefonu, kalendarza, blokady procesora ani szerokiego dostępu do pamięci. Utrzymywanie włączonego ekranu korzysta z flagi okna Android i nie wymaga dodatkowego uprawnienia.

## Kontrola przed wysłaniem

Deklarację należy ponownie przejrzeć po dodaniu dowolnego SDK analitycznego, reklamowego, chmurowego, raportowania awarii lub logowania użytkownika.
