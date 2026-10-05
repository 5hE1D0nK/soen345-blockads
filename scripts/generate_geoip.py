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
import struct
import sys
import urllib.request

DATA_URL = "https://github.com/sapics/ip-location-db/releases/download/latest/iptoasn-country-ipv4-num.csv"
OUTPUT_PATH = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "assets", "preset", "geoip_ipv4.bin"
)

def main():
    print(f"Downloading IPv4 GeoIP data from {DATA_URL}...")
    req = urllib.request.Request(DATA_URL, headers={"User-Agent": "BlockAds-GeoIP-Builder/1.0"})
    with urllib.request.urlopen(req) as resp:
        data = resp.read().decode("utf-8")

    lines = data.strip().splitlines()
    ranges = []
    for line in lines:
        parts = line.strip().split(",")
        if len(parts) == 3:
            try:
                start_ip = int(parts[0])
                end_ip = int(parts[1])
                cc = parts[2].strip().upper()
                if len(cc) == 2:
                    ranges.append((start_ip, end_ip, cc))
            except ValueError:
                pass

    # Sort deterministically
    ranges.sort(key=lambda x: x[0])

    # Merge adjacent ranges with identical country
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

    print(f"Successfully generated {OUTPUT_PATH} with {len(merged)} records ({os.path.getsize(OUTPUT_PATH)} bytes).")

if __name__ == "__main__":
    main()
