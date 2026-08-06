import sqlite3
import re

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()
c.execute("SELECT globalNumber, arabicText FROM ayah")
pua_chars = set()
for row in c.fetchall():
    text = row[1]
    for char in text:
        if 0xE000 <= ord(char) <= 0xF8FF:
            pua_chars.add(char)
        if char == '\u200F':
            pua_chars.add(char)

print([f"U+{ord(c):04X}" for c in sorted(list(pua_chars))])
