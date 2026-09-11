"""Read-only pre-publication audit. Reports identifiers, never secret values."""
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT)


def main():
    secrets = set()
    for path in ROOT.glob(".env*"):
        if not path.is_file() or path.name.endswith(".example"):
            continue
        for line in path.read_text(encoding="utf-8-sig").splitlines():
            if "=" not in line or line.lstrip().startswith("#"):
                continue
            key, value = line.split("=", 1)
            if re.search(r"password|secret|token", key, re.I) and value.strip():
                secrets.add(value.strip().encode())

    objects = git("rev-list", "--objects", "--all").splitlines()
    issues = []
    for entry in objects:
        parts = entry.split(b" ", 1)
        if len(parts) != 2:
            continue
        name = parts[1].decode("utf-8", errors="replace")
        base = name.rsplit("/", 1)[-1]
        if ((base == ".env" or base.startswith(".env.")) and not base.endswith(".example")) or base == "local.properties" or re.search(r"\.(jks|keystore|p12|pfx|key|pem)$", base, re.I):
            issues.append("Sensitive file in history: " + name)

    process = subprocess.Popen(["git", "cat-file", "--batch"], cwd=ROOT,
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    scanned = 0
    try:
        for entry in objects:
            oid = entry.split(b" ", 1)[0]
            process.stdin.write(oid + b"\n")
            process.stdin.flush()
            header = process.stdout.readline().split()
            if len(header) != 3:
                raise RuntimeError("Unable to read Git object")
            size = int(header[2])
            data = process.stdout.read(size)
            if len(data) != size or process.stdout.read(1) != b"\n":
                raise RuntimeError("Incomplete Git object")
            if any(secret in data for secret in secrets):
                issues.append("Local secret found in object " + oid.decode())
            key_header = rb"-----BEGIN (?:RSA )?PRIVATE" + rb" KEY-----\r?\n[A-Za-z0-9+/=]+"
            if re.search(key_header, data):
                issues.append("Private key found in object " + oid.decode())
            scanned += 1
    finally:
        process.stdin.close()
        process.stdout.close()
        process.wait()
    if process.returncode:
        raise RuntimeError("Git object scan failed")
    if issues:
        print("\n".join(sorted(set(issues))))
        raise SystemExit(1)
    print(f"OK: {scanned} Git objects checked; {len(secrets)} distinct local secrets checked.")


if __name__ == "__main__":
    main()
