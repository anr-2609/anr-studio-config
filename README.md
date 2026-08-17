# Fire Ants DevConfig Library

Thư viện hỗ trợ Developer Checklist và Admin Menu dành riêng cho các dự án của Fire Ants.

## 📦 Cài đặt

### 1. Thêm thư viện vào App
Mở file `app/build.gradle`, tiến hành thêm thư viện và khai báo các biến môi trường:

```gradle
android {
    defaultConfig {
        // ... các config khác ...
        
        // Bắt buộc: Khai báo phiên bản các SDK đang sử dụng để hiển thị lên màn hình Checklist
        buildConfigField "String", "FIREANTS_ADS_VERSION", "\"1.0.7\"" // Có thể thay bằng biến
        buildConfigField "String", "PLAY_SERVICES_ADS_VERSION", "\"24.7.0\""
        buildConfigField "String", "GDPR_MODULE_VERSION", "\"2.0.2\""
    }
}

dependencies {
    // Thêm dòng này (Cập nhật phiên bản mới nhất thay cho 1.0.0)
    implementation 'com.github.YOUR_GITHUB_USERNAME:fireants-dev-config:1.0.6'
}
```

## 🚀 Hướng dẫn tích hợp (Sử dụng)

Sau khi sync Gradle thành công, bạn cần làm 2 bước đơn giản sau:

### Bước 1: Khởi tạo GlobalApp
Thư viện cần được khởi tạo **một lần duy nhất** ngay khi mở app để nhận diện cấu hình. Mở file `GlobalApp.kt` (class kế thừa `Application`) và thêm lệnh init:

```kotlin
import com.fireants.devconfig.FireAntsDevConfig
// ...

class GlobalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo FireAntsDevConfig và truyền version vào
        FireAntsDevConfig.init(
            context = this,
            fireantsAdsVersion = BuildConfig.FIREANTS_ADS_VERSION,
            playServicesAdsVersion = BuildConfig.PLAY_SERVICES_ADS_VERSION,
            gdprModuleVersion = BuildConfig.GDPR_MODULE_VERSION
        )
        
        // Code khác của bạn...
    }
}
```

### Bước 2: Kích hoạt màn hình ẩn ở LanguageActivity (hoặc màn hình bất kỳ)
Để mở được menu Fire Ants DevConfig, bạn cần gắn bộ kích hoạt (trigger) vào một View bất kỳ (ví dụ: title của màn hình). Thường chúng ta sẽ gắn ở `LanguageActivity`.

Mở file `LanguageActivity.kt` và thêm dòng sau vào hàm khởi tạo view:

```kotlin
import com.fireants.devconfig.utils.setOnAdminAdToggleListener
// ...

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    
    override fun initViews() {
        // Gắn listener vào View mong muốn
        mBinding.tvTitle.setOnAdminAdToggleListener()
    }
}
```
