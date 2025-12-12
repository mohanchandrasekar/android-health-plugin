# 📘 Android Health Plugin

**Architecture • Code Quality • Security • Testing Checks for Android Projects**

The **Android Health Plugin** is a Gradle plugin that analyzes your Android project for:

* **Architectural correctness** (MVVM + Clean Architecture)
* **Security issues** (hardcoded secrets, unsafe SharedPrefs, exported components, sensitive logging)
* **Code quality issues** (complex functions, duplication, missing static analysis tools)
* **Testing health** (missing tests, low unit-test ratio)

It generates a clear **HTML report** you can use in CI/CD, PR reviews, or during refactoring.

---

# 🚀 Features

### 🧱 Architecture Checks

* God Activities/Fragments (too many lines)
* MVVM violations (ViewModel using Android classes)
* Clean architecture boundary violations (UI → Data, Domain → Android imports)

### 🔐 Security Checks

* Hardcoded secrets (`token`, `apiKey`, `password`, etc.)
* Sensitive logs (detects Log/Timber logging secrets or PII)
* Insecure SharedPreferences storage
* Exported components without permissions

### 🧹 Code Quality Checks

* Missing Detekt / Ktlint
* Long functions
* High complexity
* Duplicate code (5+ line windows)
* Dead code patterns (coming soon)

### 🧪 Testing Strategy Checks

* No unit tests
* Only instrumentation tests
* Low test-to-code ratio

---

# 📦 Install via JitPack

This plugin is published on **JitPack**, so any Android project can use it.

## 1️⃣ Add JitPack to your **project-level build.gradle.kts**

```kotlin
buildscript {
    repositories {
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.github.mohanchandrasekar:android-health-plugin:v0.1.0")
    }
}
```

## 2️⃣ Apply the plugin in `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

apply(plugin = "com.android.healthcheck")
```

This activates the plugin and adds a Gradle task:

```
./gradlew androidHealthCheck
```

---

# ⚙️ Configuration (android-health.yml)

Create a file named **`android-health.yml`** in the **root of your Android project**.

```yaml
architecture:
  enabled: true
  maxActivityLines: 400
  defaultPattern: MVVM
  cleanLayers:
    uiPackages:
      - "com.example.app.ui"
    domainPackages:
      - "com.example.app.domain"
    dataPackages:
      - "com.example.app.data"

security:
  failOnHardcodedSecrets: true
  checkSharedPrefs: true

  logging:
    allowDirectAndroidLog: false
    allowedLogWrappers:
      - "com.example.app.core.logger.AppLogger"
    sensitiveKeywords:
      - "token"
      - "password"
      - "secret"
      - "auth"
      - "email"
      - "phone"
      - "userId"

quality:
  enabled: true
  maxFunctionLines: 40
  maxComplexity: 10

rules:
  A1_GodActivity: true
  S1_HardcodedSecrets: true
  S2_InsecurePrefs: true
  S3_ExportedComponent: true
```

---

# ▶️ Run the Plugin

From your Android project root:

```bash
./gradlew androidHealthCheck
```

Reports will be generated at:

```
build/reports/android-health/report.txt
build/reports/android-health/report.html
```

Open **report.html** → you’ll see categorized findings with severity badges.

---

# 🛠 Development (Plugin Source)

If you want to build the plugin locally:

```bash
./gradlew clean build
```

This produces:

```
build/libs/android-health-plugin-0.1.0.jar
```

---

# 📤 Publishing (Optional)

To publish new versions via JitPack:

```bash
git tag v0.1.1
git push origin v0.1.1
```

Then visit:

> [https://jitpack.io/#mohanchandrasekar/android-health-plugin](https://jitpack.io/#mohanchandrasekar/android-health-plugin)

JitPack will produce:

```
com.github.mohanchandrasekar:android-health-plugin:<version>
```

---

# 🤝 Contributing

Pull requests are welcome!
Ideas for future improvements:

* Detect long ViewModels
* Detect missing error handling
* Detect misuse of Flows / LiveData
* Add scoring system (Architecture 40%, Security 40%, Quality 20%)

---

# ⭐ Support & Feedback

If you find this plugin useful, please star ⭐ the repository.
Issues & feature requests are welcome in **GitHub Issues**.

---
