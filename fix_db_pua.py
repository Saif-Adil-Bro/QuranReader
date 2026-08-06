import sqlite3
import re

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()

def remove_pua(text):
    if not text: return text
    # Remove all PUA characters
    # PUA is from E000 to F8FF
    return re.sub(r'[\uE000-\uF8FF]', '', text)

c.execute("SELECT globalNumber, arabicText FROM ayah")
rows = c.fetchall()

updated = 0
for row in rows:
    gn = row[0]
    text = row[1]
    new_text = remove_pua(text)
    if new_text != text:
        c.execute("UPDATE ayah SET arabicText = ? WHERE globalNumber = ?", (new_text, gn))
        updated += 1

conn.commit()
print(f"Updated {updated} ayahs to remove PUA characters")
conn.close()
