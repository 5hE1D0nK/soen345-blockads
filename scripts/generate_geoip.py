#!/usr/bin/env python3
"""
GeoIP IPv4 Database Generator for BlockAds.

Source data:
  Provider: sapics/ip-location-db (iptoasn-country-ipv4-num.csv)
  Repository: https://github.com/sapics/ip-location-db
  License: CC0 1.0 Universal (Public Domain)

Binary Format (10 bytes per record, Big-Endian):
  - 4 bytes: start_ip (uint32)
  - 4 bytes: end_ip (uint32)
  - 2 bytes: country_code (ISO 3166-1 alpha-2 ASCII, e.g. "US", "VN")
"""

import os
import socket
import struct
import sys
import urllib.request

# Pinned immutable commit hash of sapics/ip-location-db to ensure 100% reproducible builds.
# Never use mutable branches ('main') or dynamic release tags ('latest').
DATA_COMMIT = "a6167da0594fa6e3da6b3d9b858122d98fc50638"
DATA_URL = f"https://raw.githubusercontent.com/sapics/ip-location-db/{DATA_COMMIT}/user-country/user-country-ipv4.csv"

OUTPUT_PATH = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "assets", "preset", "geoip_ipv4.bin"
)


def parse_ip(val: str) -> int:
    val = val.strip()
    if "." in val:
        return struct.unpack("!I", socket.inet_aton(val))[0]
    return int(val)


def main():
    # Allow passing local CSV path as first argument or via GEOIP_CSV_PATH env var (useful for offline F-Droid srclibs)
    local_csv = sys.argv[1] if len(sys.argv) > 1 else os.environ.get("GEOIP_CSV_PATH")

    if local_csv and os.path.isfile(local_csv):
        print(f"Reading IPv4 GeoIP data from local file: {local_csv}")
        with open(local_csv, "r", encoding="utf-8") as f:
            data = f.read()
    else:
        print(f"Downloading IPv4 GeoIP data from pinned commit: {DATA_URL}...")
        req = urllib.request.Request(DATA_URL, headers={"User-Agent": "BlockAds-GeoIP-Builder/1.0"})
        with urllib.request.urlopen(req) as resp:
            data = resp.read().decode("utf-8")

    lines = data.strip().splitlines()
    ranges = []
    for line in lines:
        parts = line.strip().split(",")
        if len(parts) >= 3:
            try:
                start_ip = parse_ip(parts[0])
                end_ip = parse_ip(parts[1])
                cc = parts[2].strip().upper()
                if len(cc) == 2 and start_ip <= end_ip:
                    ranges.append((start_ip, end_ip, cc))
            except Exception:
                pass

    # Sort deterministically by start_ip
    ranges.sort(key=lambda x: (x[0], x[1]))

    # Merge adjacent or overlapping ranges with identical country
    merged = []
    for r in ranges:
        if merged and merged[-1][2] == r[2] and merged[-1][1] + 1 >= r[0]:
            merged[-1] = (merged[-1][0], max(merged[-1][1], r[1]), r[2])
        else:
            merged.append(r)

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    with open(OUTPUT_PATH, "wb") as f:
        for s, e, cc in merged:
            f.write(struct.pack(">II2s", s, e, cc.encode("ascii")))

    file_size = os.path.getsize(OUTPUT_PATH)
    print(f"Successfully generated {OUTPUT_PATH} with {len(merged)} records ({file_size} bytes).")


if __name__ == "__main__":
    main()
