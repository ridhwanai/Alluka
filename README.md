<p align="center">
  <img src="Alluka Module Banner.jpeg" alt="Alluka Module Banner" width="100%">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-v1.0-ff6584?style=for-the-badge&logo=fontawesome&logoColor=white" alt="Version">
  <img src="https://img.shields.io/badge/Root-KernelSU%20%7C%20APatch%20%7C%20Magisk-38bdf8?style=for-the-badge&logo=linux&logoColor=white" alt="Root">
  <img src="https://img.shields.io/badge/Android-9%20--%2015-10b981?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Telegram-@Alluka__id-229ED9?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram">
  <img src="https://img.shields.io/badge/License-Apache%202.0-f59e0b?style=for-the-badge&logo=apache&logoColor=white" alt="License">
</p>

---

## ⚡ Overview

**Alluka** is an All-In-One (AIO) system optimization module and companion Android manager app built for ultra-smooth responsiveness, cluster-aware scheduling, and adaptive battery conservation. 

By integrating the proven **Alluka Kernel Engine** (renowned for its comfortable, stutter-free daily and gaming responsiveness) with the modern **Material 3 Expressive & Floating Navbar UI** inspired by AZenith, Alluka delivers an uncompromising balance of power, battery endurance, and visual elegance.

---

## 🎨 Dynamic Character Mode Showcase

Alluka features a signature dynamic artwork system on the Manager Dashboard. The hero card seamlessly transitions character artwork according to the active profile:

| Profile | Artwork File | Engine Behavior | Ideal Scenario |
| :--- | :--- | :--- | :--- |
| **Sleep** | `Alluka x Nanika - sleep.jpeg` | Relaxed rate limits, SchedTune 0, zRAM 120% | Overnight resting, extreme battery saving |
| **Daily** | `Alluka - daily.jpeg` | Buttery-smooth Alluka tuning, zero jitter, responsive clock ramp | Social media, browsing, everyday multitasking |
| **Peforma** | `Nanika - peforma.jpeg` | Nanika awakened! Up-rate limit 0us, Top-App +18, MediaTek GED GPU/CPU boost | Intensive gaming (MLBB, Genshin, PUBG, etc.) |

---

## 🌐 Core Features

* **AZenith Floating Pill Navigation:** Modern floating capsule island navigation bar (Home, App List, Tweaks, Settings) with fluid horizontal expansion on active tabs.
* **Alluka Cluster-Aware Schedutil:** Direct cpufreq governor rate-limit tuning for multi-cluster CPUs (6 efficiency + 2 performance cores, Helio G85, and modern SoCs).
* **SchedTune Foreground Prioritization:** Guarantees top-app fluidity by boosting touch and foreground threads without micro-stutter.
* **MediaTek GED Baseline Safety:** Snapshots the device's original GED baseline and restores it cleanly on profile exit.
* **Zero-Bloat Architecture:**
  * No heavy C/Rust background polling daemons consuming RAM and draining battery.
  * No dangerous thermal throttling bypasses—keeps device temperatures safe.
  * Automatic anti-bootloop recovery protection.
* **Interactive Profile Dialog:** Quick profile switching directly from the manager dashboard or root action trigger.

---

## 📦 Installation

1. Download the latest `Alluka-v1.0.zip` from the [Releases](https://github.com/ridhwanai/Alluka/releases) section.
2. Open your preferred root manager (**KernelSU**, **APatch**, or **Magisk**).
3. Navigate to **Modules** ➔ **Install from storage** ➔ select `Alluka-v1.0.zip`.
4. Reboot your device after installation completes.
5. Launch **Alluka Manager** from your app drawer to monitor status or switch profiles anytime.

---

## ⚙️ System Runtime Paths

* **Module Directory:** `/data/adb/modules/alluka`
* **Active Profile Config:** `/data/adb/.config/alluka/profile`
* **Diagnostic Log:** `/data/adb/.config/alluka/alluka.log`
* **GED Baseline Cache:** `/data/adb/.config/alluka/ged.baseline`

---

## 🛠️ Build from Source

### GitHub Actions (Automated CI/CD)
1. Fork or push to the repository: `https://github.com/ridhwanai/Alluka.git`.
2. Go to **Actions** ➔ run **Build Alluka Module**.
3. Download the build artifact containing the flashable ZIP and APK.

### Local Build
Requirements: **JDK 17** and **Android SDK 34+**.

```bash
# 1. Compile Manager APK
cd manager
./gradlew assembleRelease

# 2. Package Flashable Module ZIP
cd ..
./scripts/package.sh
```

Output is generated at `dist/Alluka-v1.0.zip`.

---

## 👨‍💻 Maintainer & Community

* **Author & Maintainer:** **Alluka**
* **Telegram Channel:** [@Alluka_id](https://t.me/Alluka_id)
* **GitHub Repository:** [ridhwanai/Alluka](https://github.com/ridhwanai/Alluka.git)

---

## 🤝 Credits & Open Source Lineage

Alluka is an open-source project that embraces the collaborative spirit of the Android optimization and modding community. We gratefully acknowledge and credit the following projects and authors:

### 🌟 AZenith Attribution
* **Project:** [AZenith](https://github.com/Liliya2727/AZenith)
* **Authors & Core Developers:** [Liliya2727](https://github.com/Liliya2727) (@Zexshia, @rianixia, @kanaochar)
* **License:** [Apache License 2.0](https://github.com/Liliya2727/AZenith/blob/main/LICENSE)
* **Implementation & Adaptation in Alluka:**
  * **UI/UX Design Language:** Alluka Manager adopts and refines the signature **Floating Pill Navigation Island**, **Expressive Material 3 Card System**, and **Modal Bottom Sheet Selectors** pioneered by AZenith.
  * **App-Specific Profiling Concepts:** The per-package profile selector and game-detection UX paradigm draws inspiration from AZenith's application management design.
  * **Independent Kernel Logic:** While adopting AZenith's UI architecture, Alluka implements its own distinct **Alluka Kernel Engine**—tailored for cluster-aware schedutil rate-limit tuning (optimized for Helio G85 & modern multi-cluster SoCs), SchedTune zero-lag foreground touch boost, MediaTek GED safety snapshotting, and dynamic character artwork switching (Sleep, Daily, Peforma) with zero background daemon overhead.

### 🛠️ Upstream & Ecosystem Credits
* **[libsu](https://github.com/topjohnwu/libsu)** by [John Wu (@topjohnwu)](https://github.com/topjohnwu) (Apache 2.0) — Powers robust, asynchronous root shell communication in Alluka Manager.
* **[KernelSU](https://github.com/tiann/KernelSU)** by [Jason Donenfeld (@tiann)](https://github.com/tiann) & Contributors (GPL-2.0 / LGPL-2.1) — Kernel-based root solution inspiring modern modular architecture.
* **[APatch](https://github.com/bmax121/APatch)** & **[Magisk](https://github.com/topjohnwu/Magisk)** — Module installation standards and environment.
* **[Uperf](https://github.com/yc9559/uperf)** & **[Scene](http://vtools.omarea.com/)** — Influential performance management concepts in the Android modding scene.
* **Yoshihiro Togashi (Hunter × Hunter)** — Artistic inspiration for the Alluka and Nanika character motif and dynamic artwork.

---

## 📄 License & Compliance

Alluka is licensed under the **Apache License, Version 2.0**.

In accordance with Section 4 of the Apache License 2.0:
* A complete copy of the license is provided in the [`LICENSE`](LICENSE) file.
* Attribution notices for upstream and derivative works are documented in [`NOTICE`](NOTICE) and in this document.
* Modifications and adaptations from third-party open-source projects are explicitly noted.

```
Copyright 2026 Alluka Contributors
Copyright 2024-2026 Liliya2727 and AZenith Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<p align="center">
  <sub>Alluka • Smooth Motion • Quiet Power • Licensed under Apache License 2.0</sub>
</p>
