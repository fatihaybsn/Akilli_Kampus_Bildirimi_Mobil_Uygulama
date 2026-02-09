# Akıllı Kampüs Bildirim Sistemi (Smart Campus Notification)

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)

Üniversite ekosistemi içerisinde öğrenci ve personelin akademik duyurular, etkinlikler ve acil durum bildirimlerinden anlık olarak haberdar olmasını sağlayan, düşük gecikmeli (low-latency) bir mobil çözüm.

## 🚀 Proje Amacı
Kampüs iletişimindeki kopuklukları gidermek adına, geleneksel e-posta veya web duyurularının yerine; doğrudan kullanıcı cebine ulaşan, kategori bazlı ve gerçek zamanlı bir bildirim kanalı oluşturmak.

## 🛠 Teknik Mimari & Özellikler
Bu proje, modern mobil uygulama standartları ve bulut tabanlı backend servisleri entegre edilerek geliştirilmiştir.

- **Gerçek Zamanlı Veri Yönetimi:** Duyuru ve etkinlik verileri **Firebase Realtime Database** üzerinde asenkron olarak yönetilir.
- **Anlık Bildirim (Push Notification):** Uygulama arka planda veya kapalı olsa dahi, **Firebase Cloud Messaging (FCM)** entegrasyonu ile kullanıcılara kritik bilgiler saniyeler içinde iletilir.
- **Kategori Bazlı Filtreleme:** Kullanıcıların sadece ilgi duydukları (Akademik, Sosyal, İdari) başlıklar altında bildirim almasını sağlayan yapı.
- **Modern UI/UX:** Android Material Design prensiplerine uygun, kullanıcı yormayan sade arayüz.

## 📱 Ekran Görüntüleri

| Ana Ekran | Bildirim Detayı | Push Notification |
|-----------|-----------------|-------------------|

| ![WhatsApp Image 2026-02-09 at 23 10 36 (1)](https://github.com/user-attachments/assets/a672c5d2-fde2-4102-ac79-5dcb3574a0a9) |![WhatsApp Image 2026-02-09 at 23 10 34 (1)](https://github.com/user-attachments/assets/e2667e1e-7a54-4099-92b1-0c0c49c5e029) | ![WhatsApp Image 2026-02-09 at 23 10 34](https://github.com/user-attachments/assets/51a32014-0191-4b3e-b614-ba0978f75c0d) |

*(Not: Yukarıdaki görseller uygulamanın gerçek arayüzünden alınmıştır.)*

## 🏗 Kullanılan Teknolojiler
- **Dil:** Java (Android SDK)
- **Backend:** Firebase (FCM, Auth, Realtime Database)
- **UI:** XML, Material Design Components
- **Library:** Google Play Services, Glide (Görsel işleme için - varsa)

![WhatsApp Image 2026-02-09 at 23 10 36](https://github.com/user-attachments/assets/c2c43351-47ac-459e-a36a-dc5455f1adb3)


## 🔧 Kurulum ve Çalıştırma
1. Bu depoyu klonlayın: `git clone https://github.com/fatihaybsn/Akilli_Kampus_Bildirimi_Mobil_Uygulama.git`
2. Android Studio ile projeyi açın.
3. Firebase konsolundan projenizi oluşturun ve `google-services.json` dosyasını `app/` dizinine ekleyin.
4. Gerekli bağımlılıkların yüklenmesi için projeyi Sync edin.
5. Bir emülatör veya fiziksel cihaz üzerinde çalıştırın.

![WhatsApp Image 2026-02-09 at 23 10 33 (1)](https://github.com/user-attachments/assets/2bc79a82-f6f7-407b-b19f-dcd4aa8bdaa9)


---
**Geliştirici:** Fatih Ayıbasan  
**İletişim:** [LinkedIn](https://linkedin.com/in/fatih-ayibasan) | fathaybasn@gmail.com
