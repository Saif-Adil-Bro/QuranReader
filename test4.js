const https = require('https');
https.get('https://api.quran.com/api/v4/verses/by_chapter/2?words=true&word_fields=text_uthmani,location&translations=161&language=bn', (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => { 
    try {
      const parsed = JSON.parse(data);
      const words = parsed.verses[0].words;
      console.log("Words count:", words.length);
      console.log("Word 1:", words[0].text_uthmani);
      console.log("Word 2:", words[1].text_uthmani);
    } catch(e) { console.log(e); }
  });
}).on('error', (err) => { console.log('Error: ', err.message); });
