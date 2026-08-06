import urllib.request
import json

url = "https://api.quran.com/api/v4/resources/translations?language=bn"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    response = urllib.request.urlopen(req)
    data = json.loads(response.read())
    print(len(data['translations']))
    print(data['translations'][0])
except Exception as e:
    print(e)
