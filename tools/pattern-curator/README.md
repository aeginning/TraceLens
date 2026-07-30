# TraceLens Pattern Curator

Okienkowe narzędzie dla Windows do przygotowywania wzorów dołączanych do
aplikacji TraceLens.

## Funkcje

- otwieranie PNG, JPG, WebP i BMP,
- korekta orientacji EXIF,
- kadrowanie zaznaczeniem na podglądzie,
- wybór koloru tła i usuwanie tylko obszaru połączonego z krawędziami,
- regulacja tolerancji koloru,
- zapisywanie PNG z przezroczystością,
- lista wielokrotnie używanych autorów,
- licencje `AI_GENERATED`, `OWN_WORK`, `CC0-1.0`, `CC-BY-4.0` i `CUSTOM`,
- zapis generatora, modelu, promptu, źródła i uwag licencyjnych,
- automatyczna aktualizacja manifestu odczytywanego przez aplikację Android.

Uruchom `run-pattern-curator.cmd` albo gotowy plik EXE z folderu `dist`.

Do zbudowania EXE służy:

```powershell
.\build-windows.ps1
```

Narzędzie zapisuje grafiki w:

`app/src/main/assets/starter_patterns/images`

i aktualizuje:

`app/src/main/assets/starter_patterns/manifest.json`
