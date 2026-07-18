import urllib.request
import json
import sqlite3
import os

db_name = 'quran.db'
if os.path.exists(db_name):
    os.remove(db_name)

conn = sqlite3.connect(db_name)
cursor = conn.cursor()

cursor.execute('''CREATE TABLE IF NOT EXISTS surah (
    number INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    englishName TEXT NOT NULL,
    englishNameTranslation TEXT NOT NULL,
    numberOfAyahs INTEGER NOT NULL,
    revelationType TEXT NOT NULL
)''')

cursor.execute('''CREATE TABLE IF NOT EXISTS ayah (
    globalNumber INTEGER NOT NULL PRIMARY KEY,
    surahNumber INTEGER NOT NULL,
    numberInSurah INTEGER NOT NULL,
    juz INTEGER NOT NULL,
    page INTEGER NOT NULL,
    arabicText TEXT NOT NULL,
    bengaliText TEXT NOT NULL
)''')

def get_json(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode())

print("Fetching surahs...")
res = get_json('https://api.alquran.cloud/v1/surah')
for s in res['data']:
    cursor.execute('''
    INSERT INTO surah (number, name, englishName, englishNameTranslation, numberOfAyahs, revelationType)
    VALUES (?, ?, ?, ?, ?, ?)
    ''', (s['number'], s['name'], s['englishName'], s['englishNameTranslation'], s['numberOfAyahs'], s['revelationType']))

print("Fetching arabic text...")
ar_res = get_json('https://api.alquran.cloud/v1/quran/quran-uthmani')

print("Fetching bengali text...")
bn_res = get_json('https://api.alquran.cloud/v1/quran/bn.bengali')

ar_surahs = ar_res['data']['surahs']
bn_surahs = bn_res['data']['surahs']

for i in range(114):
    ar_s = ar_surahs[i]
    bn_s = bn_surahs[i]
    surah_num = ar_s['number']
    
    for j in range(len(ar_s['ayahs'])):
        ar_a = ar_s['ayahs'][j]
        bn_a = bn_s['ayahs'][j]
        
        cursor.execute('''
        INSERT INTO ayah (globalNumber, surahNumber, numberInSurah, juz, page, arabicText, bengaliText)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (ar_a['number'], surah_num, ar_a['numberInSurah'], ar_a['juz'], ar_a['page'], ar_a['text'], bn_a['text']))

conn.commit()
conn.close()
print("Database 'quran.db' created successfully!")
