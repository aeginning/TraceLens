<?php
$versions = json_decode(file_get_contents(__DIR__ . '/versions.json'), true);
$current = $versions[0];
function e(string $value): string { return htmlspecialchars($value, ENT_QUOTES, 'UTF-8'); }
?>
<!doctype html>
<html lang="pl">
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
  <title>TraceLens <?= e($current['version']) ?> — cyfrowa kalka w Twoim telefonie</title>
  <link rel="stylesheet" href="styles.css">
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
            <img src="screenshots/biblioteka-obrazow.jpeg" alt="Biblioteka obrazów TraceLens z zapisanym obrazem misia" loading="lazy">
          </button>
          <figcaption><b>Biblioteka obrazów</b><span>Dodawaj, opisuj i przygotowuj wzory.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/edytor-obrazu.jpeg" data-title="Edytor obrazu">
            <img src="screenshots/edytor-obrazu.jpeg" alt="Edytor obrazu TraceLens z narzędziem kadrowania i filtrami" loading="lazy">
          </button>
          <figcaption><b>Edytor obrazu</b><span>Kadruj, twórz kontury i usuwaj tło.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/projekty.jpeg" data-title="Lista projektów">
            <img src="screenshots/projekty.jpeg" alt="Lista projektów TraceLens z projektem Misio" loading="lazy">
          </button>
          <figcaption><b>Lista projektów</b><span>Wracaj do pracy z zapisanymi ustawieniami.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/projekt-ustawienia.jpeg" data-title="Ustawienia projektu">
            <img src="screenshots/projekt-ustawienia.jpeg" alt="Projekt TraceLens z widocznymi ustawieniami przezroczystości i aparatu" loading="lazy">
          </button>
          <figcaption><b>Ustawienia projektu</b><span>Dopasuj przezroczystość, odbicie i obiektyw.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/tryb-rysowania.jpeg" data-title="Tryb rysowania">
            <img src="screenshots/tryb-rysowania.jpeg" alt="Pełnoekranowy tryb rysowania TraceLens z nakładką misia" loading="lazy">
          </button>
          <figcaption><b>Tryb rysowania</b><span>Maksimum miejsca na aparat i nakładkę.</span></figcaption>
        </figure>
        <figure>
          <button class="screenshot-button" data-image="screenshots/galeria-postepu.jpeg" data-title="Galeria postępu">
            <img src="screenshots/galeria-postepu.jpeg" alt="Galeria zdjęć postępu projektu Misio" loading="lazy">
          </button>
          <figcaption><b>Galeria postępu</b><span>Porównuj kolejne etapy swojej pracy.</span></figcaption>
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
