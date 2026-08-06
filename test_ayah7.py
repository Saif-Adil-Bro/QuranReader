import urllib.request
import json
import sqlite3

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()
c.execute("SELECT arabicText FROM ayah WHERE globalNumber=7")
text = c.fetchone()[0]
print(repr(text))
for char in text:
    print(f"{char} - U+{ord(char):04X}")
