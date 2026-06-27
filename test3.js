const https = require('https');
https.get('https://api.alquran.cloud/v1/juz/1/quran-uthmani', (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => { 
    try {
      const parsed = JSON.parse(data);
      console.log("Ayah keys:", Object.keys(parsed.data.ayahs[0]));
      console.log("Surah info:", parsed.data.ayahs[0].surah.number);
    } catch(e) { console.log(e); }
  });
}).on('error', (err) => { console.log('Error: ', err.message); });
