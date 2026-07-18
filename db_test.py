import sqlite3
import os

print(os.path.exists('app/src/main/assets/databases/quran.db'))
conn = sqlite3.connect('app/src/main/assets/databases/quran.db')
cursor = conn.cursor()
cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
print(cursor.fetchall())
