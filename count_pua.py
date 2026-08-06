import sqlite3
from collections import Counter

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()
c.execute("SELECT arabicText FROM ayah")
counts = Counter()
for row in c.fetchall():
    text = row[0]
    for char in text:
        if 0xE000 <= ord(char) <= 0xF8FF:
            counts[char] += 1
        if char == '\u200F' or char == '\u200B' or char == '\u200C' or char == '\u200D':
            counts[char] += 1

for char, count in counts.most_common():
    print(f"U+{ord(char):04X}: {count}")
