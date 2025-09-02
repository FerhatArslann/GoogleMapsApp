# 📍 Google Maps App

[![Made with Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://developer.android.com/)
[![Database](https://img.shields.io/badge/Database-SQLite-lightgrey?logo=sqlite)](https://www.sqlite.org/)

An Android application built with **Kotlin** and the **Google Maps API** that allows you to **save, view, search, and manage custom places** on a map.

---

## ✨ Features

- 🔎 **Search & Save Locations** – Add places with a name/description and coordinates.
- 📍 **Map Integration** – View saved places as pins directly on Google Maps.
- 🗂️ **Local Database** – Store and manage locations using SQLite.
- 🗑️ **Edit & Delete** – Update or remove saved places easily.
- 📱 **Mobile-Optimized** – Designed for smooth performance on Android devices.

---

## 🎥 Demo Video
 
https://www.youtube.com/watch?v=fy8J5tYL9I8

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **Framework:** Android SDK  
- **Database:** SQLite  
- **Maps:** Google Maps API  
- **IDE:** Android Studio

---

## 🚀 Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/FerhatArslann/GoogleMapsApp.git
   cd GoogleMapsApp
   ```

2. **Open in Android Studio**
   - Use **Android Studio (2022.3.1) or newer**.  
   - Gradle sync will install dependencies automatically.

3. **Get a Google Maps API Key**
   - Go to **Google Cloud Console** and enable **Maps SDK for Android**.
   - Create an **API Key** and *restrict it* to Android apps (package name + SHA-1).
   - Add your key into:
     ```xml
     app/src/main/res/values/google_maps_api.xml
     ```

4. **Build and run**
   - Select a device/emulator (see below).
   - Press **Run ▶** in Android Studio.

---

## 📱 Device / Emulator Recommendation

- ✅ **Physical Device**
  - Android 10 (API 29) or newer
  - Must have **Google Play Services** installed

- ✅ **Emulator (AVD)**
  - Device: **Pixel 4 or newer**
  - Image: **Android 10 (API 29) or higher, x86_64, with Google Play**
  - Enable **Maps & Location Services**


---

## 📖 How to Use

1. **Open the app** → Grant location permission if prompted.  
2. **Save a place** → Enter a name/description and coordinates, then tap **Save**.  
3. **View places** → All saved places appear as pins on the map.  
4. **Search** → Use the search bar to quickly find saved entries.  
5. **Edit/Delete** → Update details or remove places from the database.

---

## 🗄️ Accessing the Local Database  

You can view the app’s SQLite database directly from Android Studio:  

1. Run the app on an **emulator** (recommended) or a physical device.  
2. Open **Device Explorer** in Android Studio:  
   - `View > Tool Windows > Device Explorer`  
3. Navigate to the database path:  
   - `bin/data/data/<package_name>/databases/<your_db_name>.db`
