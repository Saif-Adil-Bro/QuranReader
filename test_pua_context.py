import sqlite3

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()
c.execute("SELECT globalNumber, arabicText FROM ayah LIMIT 10")
for row in c.fetchall():
    print(f"{row[0]}: {repr(row[1])}")
