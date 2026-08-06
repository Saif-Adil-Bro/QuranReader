import urllib.request
import json
import sqlite3
import os

print("Fetching Bengali translation...")
req_bn = urllib.request.Request("https://api.alquran.cloud/v1/quran/bn.bengali", headers={'User-Agent': 'Mozilla/5.0'})
res_bn = urllib.request.urlopen(req_bn)
data_bn = json.loads(res_bn.read())['data']['surahs']

print("Fetching English translation...")
req_en = urllib.request.Request("https://api.alquran.cloud/v1/quran/en.sahih", headers={'User-Agent': 'Mozilla/5.0'})
res_en = urllib.request.urlopen(req_en)
data_en = json.loads(res_en.read())['data']['surahs']

print("Fetching Indo-Pak Arabic text...")
req_ar = urllib.request.Request("https://api.quran.com/api/v4/quran/verses/indopak", headers={'User-Agent': 'Mozilla/5.0'})
res_ar = urllib.request.urlopen(req_ar)
data_ar = json.loads(res_ar.read())['verses']

db_path = "quran.db"
if os.path.exists(db_path):
    os.remove(db_path)

conn = sqlite3.connect(db_path)
c = conn.cursor()

c.execute('''CREATE TABLE surah (
    number INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    englishName TEXT NOT NULL,
    englishNameTranslation TEXT NOT NULL,
    numberOfAyahs INTEGER NOT NULL,
    revelationType TEXT NOT NULL
)''')

c.execute('''CREATE TABLE ayah (
    globalNumber INTEGER NOT NULL PRIMARY KEY,
    surahNumber INTEGER NOT NULL,
    numberInSurah INTEGER NOT NULL,
    juz INTEGER NOT NULL,
    page INTEGER NOT NULL,
    arabicText TEXT NOT NULL,
    englishText TEXT NOT NULL,
    bengaliText TEXT NOT NULL
)''')

global_ayah_index = 0
for surah_idx in range(114):
    surah_bn = data_bn[surah_idx]
    surah_en = data_en[surah_idx]
    
    # Insert Surah data
    c.execute('''INSERT INTO surah (number, name, englishName, englishNameTranslation, numberOfAyahs, revelationType)
                 VALUES (?, ?, ?, ?, ?, ?)''', 
              (surah_bn['number'], surah_bn['name'], surah_bn['englishName'], surah_bn['englishNameTranslation'], len(surah_bn['ayahs']), surah_bn['revelationType']))
    
    # Insert Ayah data
    for ayah_idx in range(len(surah_bn['ayahs'])):
        ayah_bn = surah_bn['ayahs'][ayah_idx]
        ayah_en = surah_en['ayahs'][ayah_idx]
        ayah_ar = data_ar[global_ayah_index]
        
        c.execute('''INSERT INTO ayah (globalNumber, surahNumber, numberInSurah, juz, page, arabicText, englishText, bengaliText)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
                  (ayah_bn['number'], surah_bn['number'], ayah_bn['numberInSurah'], ayah_bn['juz'], ayah_bn['page'], ayah_ar['text_indopak'], ayah_en['text'], ayah_bn['text']))
        
        global_ayah_index += 1

conn.commit()
conn.close()
print(f"Database created successfully at {db_path} with {global_ayah_index} ayahs.")
