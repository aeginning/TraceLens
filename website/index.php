<?php
$requestedLanguage = $_GET['lang'] ?? null;
if (in_array($requestedLanguage, ['pl', 'en'], true)) {
  setcookie('tracelens_lang', $requestedLanguage, [
    'expires' => time() + 31536000,
    'path' => '/',
    'secure' => true,
    'httponly' => false,
    'samesite' => 'Lax',
  ]);
}
$language = in_array($requestedLanguage, ['pl', 'en'], true)
  ? $requestedLanguage
  : (($_COOKIE['tracelens_lang'] ?? 'pl') === 'en' ? 'en' : 'pl');
$englishTranslations = require __DIR__ . '/translations-en.php';
if ($language === 'en') {
  ob_start(static fn(string $html): string => strtr($html, $englishTranslations));
}
$versions = json_decode(file_get_contents(__DIR__ . '/versions.json'), true);
$current = $versions[0];
function e(string $value): string { return htmlspecialchars($value, ENT_QUOTES, 'UTF-8'); }
?>
<!doctype html>
<html lang="<?= e($language) ?>">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#211530">
  <meta name="description" content="TraceLens zamienia telefon z Androidem w wygodne narzędzie do odrysowywania obrazów z podglądem aparatu.">
  <meta property="og:title" content="TraceLens — odrysowuj z podglądem aparatu">
  <meta property="og:description" content="Nałóż dowolny obraz na podgląd aparatu, dopasuj go i śledź postęp rysowania.">
  <meta property="og:type" content="website">
  <meta property="og:url" content="https://tracelens.kasztelan.me/">
  <meta property="og:image" content="https://tracelens.kasztelan.me/og.png">
  <link rel="alternate" hreflang="pl" href="https://tracelens.kasztelan.me/?lang=pl">
  <link rel="alternate" hreflang="en" href="https://tracelens.kasztelan.me/?lang=en">
  <link rel="alternate" hreflang="x-default" href="https://tracelens.kasztelan.me/">
  <title>TraceLens <?= e($current['version']) ?> — cyfrowa kalka w Twoim telefonie</title>
  <link rel="stylesheet" href="styles.css?v=1.12.3">
</head>
<body>
  <header class="nav">
    <a class="brand" href="#start" aria-label="TraceLens — początek strony">
      <span class="brand-mark" aria-hidden="true"><span></span></span>
      <span>TraceLens</span>
    </a>
    <nav aria-label="Główna nawigacja">
      <a href="#mozliwosci">Możliwości</a>
      <a href="#jak-dziala">Jak działa</a>
      <a href="#galeria">Galeria</a>
      <a href="#wersje">Wersje</a>
    </nav>
    <div class="language-switch" aria-label="Wybór języka">
      <a href="?lang=pl#start" lang="pl"<?= $language === 'pl' ? ' class="active" aria-current="page"' : '' ?>>PL</a>
      <a href="?lang=en#start" lang="en"<?= $language === 'en' ? ' class="active" aria-current="page"' : '' ?>>EN</a>
    </div>
    <a class="nav-download" href="<?= e($current['apk']) ?>" download>Pobierz APK</a>
  </header>

  <main>
    <section class="hero" id="start">
      <div class="hero-copy">
        <p class="eyebrow"><span></span> Wersja <?= e($current['version']) ?> jest gotowa</p>
        <h1>Cyfrowa kalka,<br><em>którą masz zawsze przy sobie.</em></h1>
        <p class="lead">Nałóż wybrany obraz na podgląd aparatu, ustaw jego przezroczystość i odrysowuj bezpośrednio na papierze. TraceLens pomaga zachować proporcje i dokumentować każdy etap pracy.</p>
        <div class="hero-actions">
          <a class="button primary" href="<?= e($current['apk']) ?>" download>
            <span class="download-icon" aria-hidden="true">↓</span>
            Pobierz TraceLens <?= e($current['version']) ?>
          </a>
          <a class="button secondary" href="#jak-dziala">Zobacz, jak działa</a>
        </div>
        <ul class="facts" aria-label="Wymagania i właściwości">
          <li>Android 10+</li>
          <li>Bezpłatna</li>
          <li>Działa lokalnie</li>
        </ul>
      </div>

      <div class="hero-visual" aria-label="Schemat działania TraceLens">
        <div class="orbit orbit-one"></div>
        <div class="orbit orbit-two"></div>
        <div class="phone">
          <div class="phone-speaker"></div>
          <div class="camera-view">
            <div class="paper">
              <div class="trace-bird">
                <span class="wing"></span><span class="body"></span><span class="tail"></span>
              </div>
              <div class="pencil-line"></div>
            </div>
            <div class="overlay-label">Nakładka 18%</div>
            <div class="camera-controls"><span></span><b></b><span></span></div>
          </div>
        </div>
        <div class="floating-card card-camera"><b>Podgląd na żywo</b><span>Aparat + nakładka</span></div>
        <div class="floating-card card-progress"><b>5 etapów</b><span>Galeria postępu</span></div>
      </div>
    </section>

    <section class="trust-strip" aria-label="Najważniejsze zalety">
      <div><strong>Bez konta</strong><span>Uruchamiasz i rysujesz</span></div>
      <div><strong>Bez chmury</strong><span>Twoje dane zostają na telefonie</span></div>
      <div><strong>Bez reklam</strong><span>Nic nie odciąga uwagi od pracy</span></div>
    </section>

    <section class="section" id="mozliwosci">
      <div class="section-heading">
        <p class="eyebrow">Wszystko, czego potrzebujesz</p>
        <h2>Od obrazka do gotowego rysunku</h2>
        <p>Prosty zestaw narzędzi zaprojektowany wokół jednego celu: wygodnego odrysowywania z telefonu.</p>
      </div>
      <div class="feature-grid">
        <article class="feature feature-wide">
          <span class="feature-number">01</span>
          <div class="feature-icon camera-icon" aria-hidden="true"></div>
          <h3>Nakładka na obraz z aparatu</h3>
          <p>Widzisz kartkę i wzór jednocześnie. Skaluj, obracaj, odbijaj i blokuj pozycję obrazu przed rozpoczęciem pracy.</p>
        </article>
        <article class="feature">
          <span class="feature-number">02</span>
          <div class="feature-icon opacity-icon" aria-hidden="true"></div>
          <h3>Pełna kontrola widoczności</h3>
          <p>Płynnie dopasuj przezroczystość, aby wzór pomagał, ale nie zasłaniał powstającego rysunku.</p>
        </article>
        <article class="feature">
          <span class="feature-number">03</span>
          <div class="feature-icon crop-icon" aria-hidden="true"></div>
          <h3>Przygotowanie obrazu</h3>
          <p>Kadruj, zwiększaj kontrast, twórz kontury i usuwaj wybrany kolor tła.</p>
        </article>
        <article class="feature">
          <span class="feature-number">04</span>
          <div class="feature-icon gallery-icon" aria-hidden="true"></div>
          <h3>Galeria postępu</h3>
          <p>Rób zdjęcia kolejnych etapów, porównuj je i przeglądaj w pełnoekranowej galerii projektu.</p>
        </article>
        <figure class="feature-pattern feature-pattern-dog" aria-label="Przykładowy wzór psa dostępny w aplikacji">
          <span>Wzory dostępne offline</span>
          <img src="screenshots/wzor-pies.png" alt="Prosty wzór psa do odrysowania" loading="lazy">
        </figure>
        <article class="feature feature-wide">
          <span class="feature-number">05</span>
          <div class="feature-icon library-icon" aria-hidden="true"></div>
          <h3>Biblioteka i projekty</h3>
          <p>Zachowuj przygotowane obrazy, oznaczaj ulubione i wracaj do projektów z zapisanymi ustawieniami kamery oraz nakładki.</p>
        </article>
        <article class="feature">
          <span class="feature-number">06</span>
          <div class="feature-icon gallery-icon" aria-hidden="true"></div>
          <h3>Materiały do udostępnienia</h3>
          <p>Generuj pionowe timelapse’y, kolaże i porównania efektów, a potem publikuj je w mediach społecznościowych.</p>
        </article>
        <article class="feature feature-wide">
          <span class="feature-number">07</span>
          <div class="feature-icon library-icon" aria-hidden="true"></div>
          <h3>Instrukcja całej aplikacji</h3>
          <p>Wbudowany przewodnik pokazuje każdy etap pracy, objaśnia wszystkie ikony i pozwala wyszukać mniej oczywiste funkcje, takie jak mieszanie, ręczna ostrość czy blokada kamery.</p>
        </article>
        <figure class="feature-pattern feature-pattern-cat" aria-label="Przykładowy wzór kota dostępny w aplikacji">
          <span>Znajdź swój następny rysunek</span>
          <img src="screenshots/wzor-kot.png" alt="Prosty wzór kota do odrysowania" loading="lazy">
        </figure>
      </div>
    </section>

    <section class="workflow" id="jak-dziala">
      <div class="section-heading light">
        <p class="eyebrow">Prosty proces</p>
        <h2>Trzy kroki i możesz zaczynać</h2>
      </div>
      <ol class="steps">
        <li><span>1</span><div><h3>Dodaj obraz</h3><p>Wybierz PNG lub JPG z pamięci telefonu i przygotuj go w edytorze.</p></div></li>
        <li><span>2</span><div><h3>Utwórz projekt</h3><p>Wskaż miniaturę, ustaw aparat, rozmiar, pozycję i przezroczystość.</p></div></li>
        <li><span>3</span><div><h3>Rysuj i zapisuj postęp</h3><p>Ukryj sterowanie, skup się na pracy i fotografuj kolejne etapy.</p></div></li>
      </ol>
    </section>

    <section class="section showcase" id="galeria">
      <div class="section-heading">
        <p class="eyebrow">Aplikacja w działaniu</p>
        <h2>Od biblioteki do gotowego projektu</h2>
        <p>Zobacz prawdziwe ekrany TraceLens. Kliknij dowolny zrzut, aby otworzyć go w pełnym podglądzie.</p>
      </div>
      <div class="screenshot-grid">
        <figure>
          <button class="screenshot-button" data-image="screenshots/biblioteka-obrazow.jpeg" data-title="Biblioteka obrazów">
            <img src="screenshots/biblioteka-obrazow.jpeg" alt="Biblioteka obrazów TraceLens z wzorami zwierząt" loading="lazy">
          </button>
          <figcaption><b>Biblioteka obrazów</b><span>Wyszukuj, grupuj i przygotowuj własne wzory.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/projekty.jpeg" data-title="Lista projektów">
            <img src="screenshots/projekty.jpeg" alt="Lista aktywnych projektów TraceLens" loading="lazy">
          </button>
          <figcaption><b>Projekty</b><span>Wznawiaj pracę, archiwizuj i przenoś projekty.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/ustawienia.jpeg" data-title="Ustawienia aplikacji">
            <img src="screenshots/ustawienia.jpeg" alt="Ustawienia domyślne nowych projektów TraceLens" loading="lazy">
          </button>
          <figcaption><b>Ustawienia domyślne</b><span>Dopasuj siatkę, kamerę i zachowanie nowych projektów.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/pakiet-wzorow.jpeg" data-title="Pakiet wzorów offline">
            <img src="screenshots/pakiet-wzorow.jpeg" alt="Galeria prostych wzorów dostępnych offline w TraceLens" loading="lazy">
          </button>
          <figcaption><b>72 wzory offline</b><span>Wybieraj gotowe motywy bez szukania ich w internecie.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/podglad-wzoru.jpeg" data-title="Podgląd wzoru">
            <img src="screenshots/podglad-wzoru.jpeg" alt="Pełnoekranowy podgląd wzoru ptaka z informacją o autorze" loading="lazy">
          </button>
          <figcaption><b>Podgląd i licencja</b><span>Sprawdź wzór, autora i od razu utwórz projekt.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/transfer-qr.jpeg" data-title="Przenoszenie projektu">
            <img src="screenshots/transfer-qr.jpeg" alt="Przenoszenie projektu TraceLens na drugi telefon kodem QR" loading="lazy">
          </button>
          <figcaption><b>Transfer kodem QR</b><span>Przenoś projekty bez konta i zewnętrznej chmury.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/tryb-rysowania.jpeg" data-title="Tryb rysowania">
            <img src="screenshots/tryb-rysowania.jpeg" alt="Tryb rysowania TraceLens z siatką i nakładką chomika" loading="lazy">
          </button>
          <figcaption><b>Tryb rysowania</b><span>Ustaw wzór na obrazie z kamery i reguluj przezroczystość.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/sterowanie-rysowaniem.jpeg" data-title="Sterowanie rysowaniem">
            <img src="screenshots/sterowanie-rysowaniem.jpeg" alt="Panel sterowania aktywnym projektem TraceLens" loading="lazy">
          </button>
          <figcaption><b>Sterowanie projektem</b><span>Odbijaj wzór, blokuj kamerę i kontroluj timelapse.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/galeria-postepu.jpeg" data-title="Galeria postępu">
            <img src="screenshots/galeria-postepu.jpeg" alt="Galeria trzech zdjęć postępu projektu Chomiś" loading="lazy">
          </button>
          <figcaption><b>Galeria postępu</b><span>Dokumentuj etapy i twórz materiały do udostępnienia.</span></figcaption>
        </figure>
      </div>
    </section>

    <section class="section versions" id="wersje">
      <div class="section-heading">
        <p class="eyebrow">Historia rozwoju</p>
        <h2>Każda wersja ma swoją historię</h2>
        <p>TraceLens rozwija się etapami. Tutaj zawsze znajdziesz aktualne wydanie oraz najważniejsze zmiany.</p>
      </div>
      <div class="release-current">
        <div>
          <span class="release-badge">Aktualna</span>
          <h3>TraceLens <?= e($current['version']) ?></h3>
          <p><?= e($current['description']) ?></p>
        </div>
        <a class="button primary" href="<?= e($current['apk']) ?>" download>Pobierz APK</a>
      </div>
      <div class="timeline">
        <?php foreach (array_slice($versions, 1) as $release): ?>
          <article>
            <time><?= e($release['version']) ?></time>
            <div><h3><?= e($release['title']) ?></h3><p><?= e($release['description']) ?></p></div>
          </article>
        <?php endforeach; ?>
      </div>
    </section>

    <section class="download-section">
      <div>
        <p class="eyebrow">Gotowy do odrysowywania?</p>
        <h2>Zainstaluj TraceLens<br>na swoim Androidzie.</h2>
        <p>Po pobraniu system może poprosić o zgodę na instalację aplikacji z tego źródła.</p>
      </div>
      <a class="button primary large" href="<?= e($current['apk']) ?>" download><span aria-hidden="true">↓</span> Pobierz wersję <?= e($current['version']) ?></a>
    </section>
  </main>

  <footer>
    <a class="brand" href="#start"><span class="brand-mark" aria-hidden="true"><span></span></span><span>TraceLens</span></a>
    <p>Proste narzędzie do odrysowywania obrazów z podglądem aparatu.</p>
    <p>© <span id="year">2026</span> TraceLens</p>
  </footer>
  <dialog class="lightbox" id="lightbox">
    <button class="lightbox-close" type="button" aria-label="Zamknij podgląd">×</button>
    <img src="" alt="">
    <p></p>
  </dialog>
  <script src="app.js"></script>
</body>
</html>
