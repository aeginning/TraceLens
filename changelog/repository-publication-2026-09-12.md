# Przygotowanie publicznego repozytorium — 12 września 2026

- Zweryfikowano historię Git skanerem Gitleaks 8.30.1 z ukrywaniem wykrywanych wartości w raportach; nie wykryto sekretów.
- Dodatkowo sprawdzono wszystkie obiekty Git pod kątem konkretnych wartości haseł z lokalnych konfiguracji oraz nazw plików kluczy i środowiska.
- Potwierdzono ignorowanie `.env.deploy`, `.env.signing`, `local.properties` i klucza podpisującego aplikację.
- Rozszerzono `.gitignore` o pozostałe warianty `.env` oraz magazyny i pliki kluczy prywatnych.
- Dodano skrypt `scripts/check-publication.py` do ponawiania kontroli przed kolejnymi publikacjami.
- Uzupełniono README o linki do aplikacji i opis lokalnej konfiguracji. Skrypt publikacji FTP odczytuje hasło z ignorowanego pliku i nie zawiera go w kodzie.

Raport skanera i lokalne dane uwierzytelniania pozostają poza repozytorium. Kontrola nie zastępuje sprawdzania zmian przed przyszłymi commitami.
