# Rejestr zmian TraceLens

Każda wydana wersja aplikacji ma w tym katalogu własny plik Markdown. Nazwa pliku odpowiada numerowi wersji, np. `0.1.md`, `0.2.md` lub `1.0.md`.

## Zasady

- Każda zmiana funkcjonalna lub poprawka otrzymuje osobny, opisowy commit.
- Każdy commit zmieniający aplikację musi zostać opisany w pliku wersji przygotowywanej do wydania.
- Po wydaniu plik danej wersji nie jest przepisywany. Kolejne modyfikacje trafiają do nowego pliku wersji.
- Wydania oznaczamy tagiem Git `vX.Y`, np. `v0.1`.
- Przy nowym instalowalnym wydaniu zwiększamy Android `versionCode`.
- Opis wersji zawiera datę, tag, numer `versionCode`, zakres zmian i istotne informacje dotyczące testów.

## Wersje

- [0.5](0.5.md) — kadrowanie, usuwanie i tryb rysowania
- [0.4](0.4.md) — jednakowa wysokość kafelków biblioteki
- [0.3](0.3.md) — czytelniejszy układ nazw i akcji na kafelkach
- [0.2](0.2.md) — własne nazwy obrazów w bibliotece
- [0.1](0.1.md) — pierwsza działająca wersja aplikacji
