import sqlite3

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()
c.execute("SELECT globalNumber, arabicText FROM ayah")
for row in c.fetchall():
    text = row[1]
    if '\ue003' in text or '\ue004' in text or '\ue01a' in text:
        print(f"{row[0]}: {repr(text)}")
        break
