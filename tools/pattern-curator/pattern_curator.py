from __future__ import annotations

import json
import math
import re
import sys
import tkinter as tk
from collections import deque
from dataclasses import dataclass
from pathlib import Path
from tkinter import filedialog, messagebox, simpledialog, ttk

from PIL import Image, ImageDraw, ImageOps, ImageTk


APP_TITLE = "TraceLens Pattern Curator"
LICENSES = [
    "AI_GENERATED",
    "OWN_WORK",
    "CC0-1.0",
    "CC-BY-4.0",
    "CUSTOM",
]
CATEGORIES = [
    "pets", "birds", "farm-animals", "wild-animals", "sea-animals",
    "dinosaurs", "plants", "landscapes", "vehicles", "food",
    "everyday-objects", "sports", "fantasy", "space", "seasonal", "symbols",
]


def find_catalog() -> Path:
    candidates = [Path.cwd(), Path(__file__).resolve().parent]
    if getattr(sys, "frozen", False):
        candidates.insert(0, Path(sys.executable).resolve().parent)
    for base in candidates:
        for parent in [base, *base.parents]:
            target = parent / "app" / "src" / "main" / "assets" / "starter_patterns"
            if target.exists():
                return target
    return Path.cwd() / "starter_patterns"


def slugify(value: str) -> str:
    value = value.lower().strip()
    value = re.sub(r"[^a-z0-9]+", "-", value)
    return value.strip("-") or "pattern"


def color_distance(a: tuple[int, ...], b: tuple[int, ...]) -> float:
    return math.sqrt(sum((int(a[i]) - int(b[i])) ** 2 for i in range(3)))


def edge_background(image: Image.Image, target: tuple[int, int, int], tolerance: int) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    visited = bytearray(width * height)
    queue: deque[tuple[int, int]] = deque()

    def offer(x: int, y: int) -> None:
        index = y * width + x
        if visited[index]:
            return
        visited[index] = 1
        if color_distance(pixels[x, y], target) <= tolerance:
            queue.append((x, y))

    for x in range(width):
        offer(x, 0)
        offer(x, height - 1)
    for y in range(height):
        offer(0, y)
        offer(width - 1, y)
    while queue:
        x, y = queue.popleft()
        r, g, b, _ = pixels[x, y]
        pixels[x, y] = (r, g, b, 0)
        if x:
            offer(x - 1, y)
        if x + 1 < width:
            offer(x + 1, y)
        if y:
            offer(x, y - 1)
        if y + 1 < height:
            offer(x, y + 1)
    return rgba


@dataclass
class Crop:
    left: int
    top: int
    right: int
    bottom: int

    def normalized(self) -> "Crop":
        return Crop(
            min(self.left, self.right),
            min(self.top, self.bottom),
            max(self.left, self.right),
            max(self.top, self.bottom),
        )


class PatternCurator(tk.Tk):
    def __init__(self) -> None:
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("1280x820")
        self.minsize(1050, 700)
        self.catalog = find_catalog()
        self.images_dir = self.catalog / "images"
        self.manifest_path = self.catalog / "manifest.json"
        self.authors_path = self.catalog / "authors.json"
        self.images_dir.mkdir(parents=True, exist_ok=True)
        self.original: Image.Image | None = None
        self.processed: Image.Image | None = None
        self.preview_photo: ImageTk.PhotoImage | None = None
        self.preview_scale = 1.0
        self.preview_offset = (0, 0)
        self.crop: Crop | None = None
        self.drag_start: tuple[int, int] | None = None
        self.mode = tk.StringVar(value="crop")
        self.remove_color = (255, 255, 255)
        self._build_ui()
        self._load_authors()

    def _build_ui(self) -> None:
        toolbar = ttk.Frame(self, padding=8)
        toolbar.pack(fill="x")
        ttk.Button(toolbar, text="Otwórz obraz", command=self.open_image).pack(side="left")
        ttk.Button(toolbar, text="Resetuj", command=self.reset_image).pack(side="left", padx=6)
        ttk.Separator(toolbar, orient="vertical").pack(side="left", fill="y", padx=10)
        ttk.Radiobutton(toolbar, text="Kadrowanie", variable=self.mode, value="crop").pack(side="left")
        ttk.Radiobutton(toolbar, text="Wybór koloru tła", variable=self.mode, value="color").pack(side="left")
        ttk.Button(toolbar, text="Usuń tło", command=self.remove_background).pack(side="left", padx=6)
        ttk.Button(toolbar, text="Zastosuj kadr", command=self.apply_crop).pack(side="left")
        self.color_chip = tk.Label(toolbar, width=3, background="#ffffff", relief="solid")
        self.color_chip.pack(side="left", padx=(12, 4))
        ttk.Label(toolbar, text="Tolerancja").pack(side="left")
        self.tolerance = tk.IntVar(value=45)
        ttk.Scale(toolbar, from_=0, to=180, variable=self.tolerance, orient="horizontal").pack(
            side="left", fill="x", expand=True, padx=8
        )

        body = ttk.Panedwindow(self, orient="horizontal")
        body.pack(fill="both", expand=True, padx=8, pady=(0, 8))
        preview_frame = ttk.Frame(body)
        form_frame = ttk.Frame(body, padding=12)
        body.add(preview_frame, weight=3)
        body.add(form_frame, weight=2)

        self.canvas = tk.Canvas(preview_frame, background="#2a2730", highlightthickness=0)
        self.canvas.pack(fill="both", expand=True)
        self.canvas.bind("<Configure>", lambda _: self.render())
        self.canvas.bind("<ButtonPress-1>", self.canvas_press)
        self.canvas.bind("<B1-Motion>", self.canvas_drag)
        self.canvas.bind("<ButtonRelease-1>", self.canvas_release)

        self.title_pl = self._entry(form_frame, "Nazwa polska")
        self.title_en = self._entry(form_frame, "Nazwa angielska")
        ttk.Label(form_frame, text="Kategoria").pack(anchor="w", pady=(10, 2))
        self.category = ttk.Combobox(form_frame, values=CATEGORIES, state="readonly")
        self.category.set("pets")
        self.category.pack(fill="x")
        self.tags = self._entry(form_frame, "Tagi (po przecinku, po angielsku)")
        ttk.Label(form_frame, text="Autor").pack(anchor="w", pady=(10, 2))
        author_row = ttk.Frame(form_frame)
        author_row.pack(fill="x")
        self.author = ttk.Combobox(author_row)
        self.author.pack(side="left", fill="x", expand=True)
        ttk.Button(author_row, text="Dodaj autora", command=self.add_author).pack(side="left", padx=(6, 0))

        ttk.Label(form_frame, text="Licencja").pack(anchor="w", pady=(10, 2))
        self.license = ttk.Combobox(form_frame, values=LICENSES, state="readonly")
        self.license.set("AI_GENERATED")
        self.license.pack(fill="x")
        self.source = self._entry(form_frame, "Źródło / narzędzie (np. Gemini Banana Pro)")
        self.model = self._entry(form_frame, "Model / wersja")
        ttk.Label(form_frame, text="Prompt lub opis pochodzenia").pack(anchor="w", pady=(10, 2))
        self.prompt = tk.Text(form_frame, height=5, wrap="word")
        self.prompt.pack(fill="x")
        ttk.Label(form_frame, text="Uwagi licencyjne").pack(anchor="w", pady=(10, 2))
        self.license_notes = tk.Text(form_frame, height=4, wrap="word")
        self.license_notes.pack(fill="x")

        info = (
            "AI_GENERATED oznacza grafikę wygenerowaną przez wskazane narzędzie. "
            "Zapisz nazwę generatora, model i prompt/opis, aby zachować historię pochodzenia."
        )
        ttk.Label(form_frame, text=info, wraplength=410, foreground="#625b71").pack(
            fill="x", pady=12
        )
        ttk.Button(
            form_frame,
            text="Zapisz PNG i metadane w pakiecie TraceLens",
            command=self.save_pattern,
        ).pack(fill="x", ipady=10, pady=(8, 0))
        ttk.Label(form_frame, text=f"Katalog: {self.catalog}", wraplength=410).pack(
            fill="x", pady=8
        )

    @staticmethod
    def _entry(parent: ttk.Frame, label: str) -> ttk.Entry:
        ttk.Label(parent, text=label).pack(anchor="w", pady=(10, 2))
        entry = ttk.Entry(parent)
        entry.pack(fill="x")
        return entry

    def _load_authors(self) -> None:
        authors: list[str] = []
        if self.authors_path.exists():
            try:
                authors = json.loads(self.authors_path.read_text(encoding="utf-8")).get("authors", [])
            except (json.JSONDecodeError, OSError):
                pass
        self.author["values"] = authors
        if authors:
            self.author.set(authors[0])

    def add_author(self) -> None:
        value = self.author.get().strip()
        if not value:
            value = simpledialog.askstring(APP_TITLE, "Podaj nazwę autora:") or ""
        if not value:
            return
        authors = list(self.author["values"])
        if value not in authors:
            authors.append(value)
            authors.sort(key=str.casefold)
            self.authors_path.write_text(
                json.dumps({"authors": authors}, ensure_ascii=False, indent=2) + "\n",
                encoding="utf-8",
            )
            self.author["values"] = authors
        self.author.set(value)

    def open_image(self) -> None:
        path = filedialog.askopenfilename(
            title="Wybierz grafikę",
            filetypes=[("Obrazy", "*.png *.jpg *.jpeg *.webp *.bmp"), ("Wszystkie pliki", "*.*")],
        )
        if not path:
            return
        try:
            image = ImageOps.exif_transpose(Image.open(path)).convert("RGBA")
        except OSError as exc:
            messagebox.showerror(APP_TITLE, f"Nie udało się otworzyć obrazu:\n{exc}")
            return
        self.original = image
        self.processed = image.copy()
        self.crop = None
        if not self.title_pl.get():
            self.title_pl.insert(0, Path(path).stem)
        if not self.title_en.get():
            self.title_en.insert(0, Path(path).stem)
        self.render()

    def reset_image(self) -> None:
        if self.original is not None:
            self.processed = self.original.copy()
            self.crop = None
            self.render()

    def canvas_to_image(self, x: int, y: int) -> tuple[int, int] | None:
        if self.processed is None:
            return None
        ox, oy = self.preview_offset
        ix = int((x - ox) / self.preview_scale)
        iy = int((y - oy) / self.preview_scale)
        if 0 <= ix < self.processed.width and 0 <= iy < self.processed.height:
            return ix, iy
        return None

    def canvas_press(self, event: tk.Event) -> None:
        point = self.canvas_to_image(event.x, event.y)
        if point is None:
            return
        if self.mode.get() == "color":
            assert self.processed is not None
            self.remove_color = self.processed.getpixel(point)[:3]
            color = "#%02x%02x%02x" % self.remove_color
            self.color_chip.configure(background=color)
        else:
            self.drag_start = point
            self.crop = Crop(point[0], point[1], point[0], point[1])

    def canvas_drag(self, event: tk.Event) -> None:
        if self.drag_start is None or self.processed is None:
            return
        point = self.canvas_to_image(event.x, event.y)
        if point:
            self.crop = Crop(self.drag_start[0], self.drag_start[1], point[0], point[1])
            self.render()

    def canvas_release(self, event: tk.Event) -> None:
        self.canvas_drag(event)
        self.drag_start = None

    def apply_crop(self) -> None:
        if self.processed is None or self.crop is None:
            return
        crop = self.crop.normalized()
        if crop.right - crop.left < 5 or crop.bottom - crop.top < 5:
            messagebox.showwarning(APP_TITLE, "Zaznacz większy fragment obrazu.")
            return
        self.processed = self.processed.crop((crop.left, crop.top, crop.right, crop.bottom))
        self.crop = None
        self.render()

    def remove_background(self) -> None:
        if self.processed is None:
            messagebox.showwarning(APP_TITLE, "Najpierw otwórz obraz.")
            return
        self.processed = edge_background(
            self.processed,
            self.remove_color,
            int(self.tolerance.get()),
        )
        self.render()

    def render(self) -> None:
        self.canvas.delete("all")
        if self.processed is None:
            self.canvas.create_text(
                self.canvas.winfo_width() / 2,
                self.canvas.winfo_height() / 2,
                text="Otwórz obraz, aby rozpocząć",
                fill="#eee8f4",
                font=("Segoe UI", 18),
            )
            return
        width = max(100, self.canvas.winfo_width() - 40)
        height = max(100, self.canvas.winfo_height() - 40)
        self.preview_scale = min(width / self.processed.width, height / self.processed.height)
        display = self.processed.resize(
            (
                max(1, int(self.processed.width * self.preview_scale)),
                max(1, int(self.processed.height * self.preview_scale)),
            ),
            Image.Resampling.LANCZOS,
        )
        checker = Image.new("RGBA", display.size, "#f4f0f7")
        draw = ImageDraw.Draw(checker)
        step = 20
        for y in range(0, display.height, step):
            for x in range(0, display.width, step):
                if (x // step + y // step) % 2:
                    draw.rectangle((x, y, x + step, y + step), fill="#ded8e4")
        checker.alpha_composite(display)
        self.preview_photo = ImageTk.PhotoImage(checker.convert("RGB"))
        ox = (self.canvas.winfo_width() - display.width) // 2
        oy = (self.canvas.winfo_height() - display.height) // 2
        self.preview_offset = (ox, oy)
        self.canvas.create_image(ox, oy, image=self.preview_photo, anchor="nw")
        if self.crop:
            crop = self.crop.normalized()
            self.canvas.create_rectangle(
                ox + crop.left * self.preview_scale,
                oy + crop.top * self.preview_scale,
                ox + crop.right * self.preview_scale,
                oy + crop.bottom * self.preview_scale,
                outline="#7c4dff",
                width=3,
            )

    def save_pattern(self) -> None:
        if self.processed is None:
            messagebox.showwarning(APP_TITLE, "Najpierw otwórz i przygotuj obraz.")
            return
        title_pl = self.title_pl.get().strip()
        title_en = self.title_en.get().strip()
        author = self.author.get().strip()
        license_name = self.license.get().strip()
        category = self.category.get().strip()
        if not all((title_pl, title_en, author, license_name, category)):
            messagebox.showwarning(
                APP_TITLE,
                "Uzupełnij nazwy, kategorię, autora oraz licencję.",
            )
            return
        if license_name == "AI_GENERATED" and not self.source.get().strip():
            messagebox.showwarning(
                APP_TITLE,
                "Dla grafiki AI podaj generator w polu „Źródło / narzędzie”.",
            )
            return
        pattern_id = slugify(title_en)
        file_name = f"{pattern_id}.png"
        output = self.images_dir / file_name
        if output.exists() and not messagebox.askyesno(APP_TITLE, f"{file_name} już istnieje. Nadpisać?"):
            return
        image = self.processed
        max_size = 1600
        if max(image.size) > max_size:
            ratio = max_size / max(image.size)
            image = image.resize(
                (int(image.width * ratio), int(image.height * ratio)),
                Image.Resampling.LANCZOS,
            )
        image.save(output, "PNG", optimize=True)
        manifest = {"schemaVersion": 2, "patterns": []}
        if self.manifest_path.exists():
            try:
                manifest = json.loads(self.manifest_path.read_text(encoding="utf-8"))
            except (json.JSONDecodeError, OSError):
                pass
        entry = {
            "id": pattern_id,
            "file": f"images/{file_name}",
            "name": {"en": title_en, "pl": title_pl},
            "category": category,
            "tags": [tag.strip().lower() for tag in self.tags.get().split(",") if tag.strip()],
            "author": author,
            "license": license_name,
            "source": self.source.get().strip(),
            "model": self.model.get().strip(),
            "prompt": self.prompt.get("1.0", "end").strip(),
            "licenseNotes": self.license_notes.get("1.0", "end").strip(),
        }
        patterns = [item for item in manifest.get("patterns", []) if item.get("id") != pattern_id]
        patterns.append(entry)
        manifest["schemaVersion"] = 2
        manifest["patterns"] = sorted(patterns, key=lambda item: item["id"])
        self.manifest_path.write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        self.add_author()
        messagebox.showinfo(APP_TITLE, f"Zapisano:\n{output}\n\nZaktualizowano manifest licencji.")


if __name__ == "__main__":
    PatternCurator().mainloop()
