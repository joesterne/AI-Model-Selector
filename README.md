# StudioPod

StudioPod is a retro-inspired Android application that faithfully recreates the classic iPod click-wheel experience. Built entirely with modern Android technologies (Kotlin and Jetpack Compose), it blends nostalgia with functional application design.

## Features

* **Classic Click Wheel Interface:** A fully functional virtual click wheel that responds to circular touch gestures, complete with haptic feedback and authentic mechanical "click" sounds for a tactile experience.
* **Retro LCD Aesthetic:** A meticulously tuned 4-color monochrome palette (pale green-gray background with dark green-black pixels) that authentically mimics early 2000s portable media player displays. Includes inverted pixel highlights for active selections.
* **Dynamic Hardware Integration:** 
  * Features a custom-drawn retro battery icon that reads and reflects your device's actual battery level.
  * System Back button interception with a nostalgic "Power Off?" confirmation modal that is fully navigable via the click wheel.
* **Fluid Navigation:** 
  * Smooth animations transition between the main program list and the detailed "Now Playing" metadata screen.
  * Custom fading vertical scrollbar that perfectly mimics the classic iPod position indicator when navigating long lists or descriptions.
* **Sleep Mode & Screensaver:** Automatically dims the "backlight" (screen contrast shifts dynamically) and displays a digital monochrome clock after 10 seconds of inactivity. Instantly wakes and restores state upon any touch or click wheel interaction.
* **Local Persistence:** Program data and user states are stored securely using Android's Room Database.

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Animations, Canvas Drawing, Gestures)
* **Data Storage:** Room Database
* **Architecture:** MVVM (Model-View-ViewModel)
