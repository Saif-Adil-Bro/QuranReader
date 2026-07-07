#!/bin/bash
cd app/src/main/res/font/
rm *.ttf
wget -qO almarai_regular.ttf "https://github.com/google/fonts/raw/main/ofl/almarai/Almarai-Regular.ttf"
wget -qO amiri_quran.ttf "https://github.com/google/fonts/raw/main/ofl/amiriquran/AmiriQuran-Regular.ttf"
wget -qO amiri_regular.ttf "https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Regular.ttf"
wget -qO lateef_regular.ttf "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Regular.ttf"
wget -qO scheherazade_new.ttf "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Regular.ttf"
wget -qO tajawal_regular.ttf "https://github.com/google/fonts/raw/main/ofl/tajawal/Tajawal-Regular.ttf"
ls -lh
