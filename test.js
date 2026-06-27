const https = require('https');
https.get('https://api.quran.com/api/v4/verses/by_page/2?words=true&word_fields=text_uthmani,location&translations=161&language=bn&per_page=1000', (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => { 
    try {
      const parsed = JSON.parse(data);
      console.log("Verse 1 id:", parsed.verses[0].id);
      console.log("Verse 1 verse_number:", parsed.verses[0].verse_number);
    } catch(e) { console.log(e); }
  });
}).on('error', (err) => { console.log('Error: ', err.message); });
