import urllib.request
import json

url = "https://api.quran.com/api/v4/resources/translations?language=bn"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    response = urllib.request.urlopen(req)
    data = json.loads(response.read())
    trans85 = next((t for t in data['translations'] if t['id'] == 85), None)
    print(trans85)
except Exception as e:
    print(e)
