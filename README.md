# 📖 Al-Quran & Islamic Companion App (কুরআন ও ইসলামিক অ্যাপ)

[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-green?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

একটি পূর্ণাঙ্গ, আধুনিক এবং দৃষ্টিনন্দন এন্ড-টু-এন্ড অ্যান্ড্রয়েড অ্যাপলিকেশন—যা পবিত্র কুরআন তেলাওয়াত, অধ্যয়ন, অডিও শ্রবণ এবং বিষয়ভিত্তিক নির্দেশিকা সহজতর করার লক্ষ্যে জেটপ্যাক কম্পোজ (Jetpack Compose) এবং আধুনিক অ্যান্ড্রয়েড আর্কিটেকচার দ্বারা নির্মিত।

---

## ✨ অ্যাপের প্রধান বৈশিষ্ট্যসমূহ (Key Features)

### 🕌 ১. বহুমুখী কুরআন রিডিং মোড (Multiple Reading Modes)
- **হাফেজী কুরআন মোড (Hafezi Mode):** সুন্দর হাফেজী স্ক্রিপ্ট ব্যাকগ্রাউন্ডসহ পৃষ্ঠা অনুযায়ী তেলাওয়াত।
- **তাজবীদ মোড (Tajweed Mode):** তাজবীদের নিয়ম সহজ করার জন্য কালার-কোডেড তাজবীদ ভিউ।
- **মুসহাফ ভিউয়ার (Mushaf Viewer):** সুন্দর পেজিং এবং পারফেক্ট পেজ নেভিগেশন।
- **কাস্টমাইজেশন:** ফন্ট সাইজ, লাইন স্পেসিং, ডার্ক মোড এবং বুকমার্ক সাপোর্ট।

### 🎧 ২. অডিও তেলাওয়াত ও কারী নির্বাচন (Audio Recitation)
- বিশ্বখ্যাত কারীগণের কন্ঠে সূরা ও আয়াতভিত্তিক অডিও শোনা।
- পেজ রিপিট, প্লেব্যাক স্পিড কন্ট্রোল এবং ব্যাকগ্রাউন্ড অডিও প্লেয়ার।

### 📚 ৩. বিষয়ভিত্তিক কুরআন ও মানযিল (Subject-wise & Manzil)
- বিষয়ভিত্তিক আয়াত সংগ্রহ (Subject-wise Topics)।
- **মানযিল (Manzil):** দ্রুত ও সহজ পঠনের জন্য মানযিলের সকল আয়াত সংরক্ষণ এবং ক্যাশিং সিস্টেম।

### 🔍 ৪. স্মার্ট অনুসন্ধান ও বুকমার্ক (Search & Bookmarks)
- সূরা, পারা এবং আয়াত দ্রুত খুঁজে পাওয়ার সুবিধাজনক সার্চ বার।
- যেকোনো পছন্দনীয় আয়াত সহজে বুকমার্ক করে রাখার ব্যবস্থা।

### 📰 ৫. দৈনিক ইসলামিক পোস্ট ও আর্টিকেলে আপডেট
- পোস্ট, আর্টিকেলে নোটিফিকেশন সুবিধা এবং অফলাইন সিঙ্ক ক্যাশ।

---

## 🛠️ টেকনোলজি স্ট্যাক (Tech Stack)

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
- **Architecture:** MVVM + Clean Architecture principles
- **Asynchronous & Reactive Data:** Kotlin Coroutines & StateFlow / Flow
- **Local Database & Persistence:** Room Database + DataStore + Local Cache
- **Dependency Management:** Gradle Version Catalog (`libs.versions.toml`)
- **JSON Parsing:** Gson & Local Asset Management

---

## 🚀 যেভাবে রান করবেন (How to Build & Run)

### পূর্বশর্ত (Prerequisites)
- **Android Studio** (Ladybug / Jellyfish বা তার চেয়ে নতুন সংস্করণ)
- **JDK:** 17 বা তার পরবর্তী ভার্সন
- **Android SDK:** Compile SDK 34/35

### প্রজেক্ট ক্লোন ও রান করার নিয়ম:

```bash
# ১. রিপোজিটরি ক্লোন করুন
git clone https://github.com/your-username/your-repo-name.git

# ২. প্রজেক্ট ফোল্ডারে যান
cd your-repo-name

# ৩. অ্যান্ড্রয়েড স্টুডিওতে ওপেন করুন এবং Gradle Sync সম্পন্ন হতে দিন
