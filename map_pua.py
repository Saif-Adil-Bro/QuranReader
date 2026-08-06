import sqlite3
import urllib.request
import json

conn = sqlite3.connect("app/src/main/assets/databases/quran.db")
c = conn.cursor()

pua_chars = ['\ue003', '\ue004', '\ue01a', '\ue01b', '\ue01c', '\ue01e', '\ue01f', '\ue021', '\ue022']
for pua in pua_chars:
    c.execute("SELECT globalNumber, arabicText FROM ayah WHERE arabicText LIKE ? LIMIT 1", (f"%{pua}%",))
    row = c.fetchone()
    if row:
        gn = row[0]
        text = row[1]
        
        # fetch standard uthmani text for the same globalNumber
        try:
            req = urllib.request.Request(f"https://api.alquran.cloud/v1/ayah/{gn}/quran-uthmani")
            res = urllib.request.urlopen(req)
            data = json.loads(res.read())
            std_text = data['data']['text']
            print(f"PUA {repr(pua)} -> GN {gn}")
            print(f" IndoPak: {text}")
            print(f" Uthmani: {std_text}")
            print("-" * 40)
        except Exception as e:
            print(f"Error fetching gn {gn}: {e}")
