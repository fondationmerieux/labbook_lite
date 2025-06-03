# LabBook Lite

LabBook Lite is an **offline Android application** that extends the usage of the LabBook system to community health workers in the field. It allows entering medical analysis requests and results, validating them, and generating PDF reports, without requiring any internet connection during fieldwork.

This repository contains the source code to build and maintain the LabBook Lite APK.

## Requirements

- Android device running **Android 10 or later**
- Android Studio (latest stable version recommended)
- At least one running **LabBook v3.5.11** instance to configure and exchange data

## Installation and Usage

LabBook Lite is designed to operate fully **offline** during field missions. However, it requires a LabBook server at the laboratory for:

- **Initial configuration import** (users, subset of analyses, document headers)
- **Data export after mission** (requests, results, agent activity)

To prepare a device:

1. **Clone this repository**

    ```bash
    git clone https://github.com/fondationmerieux/labbook_lite.git
    ```

2. **Open the project in Android Studio**

3. **Build and install the APK on an Android device**

4. From LabBook (requires administrator rights):
    - Register at least one **agent user**
    - Select analyses to be available in LabBook Lite
    - Define optional PDF header content and other configurations

5. Launch the app on the device and **import configuration** from LabBook over the local Wi-Fi network

6. After entering data in the field, reconnect to LabBook over Wi-Fi and **upload collected records**

*Each LabBook Lite instance is bound to a specific LabBook server. Data must be uploaded to the same LabBook server used for configuration import.*

## Features

- Entry and modification of requests and patient information
- Results input and validation
- Offline PDF report generation and local storage
- Encrypted SQLite database
- Password-protected PDF reports
- HTTPS communication with LabBook

## Contributing

We welcome contributions! The project is newly opened, so some contribution mechanisms are still evolving.

Feel free to open issues if you find bugs, confusing behaviors, or missing features.

## License

This project is licensed under the [GNU General Public License v2.0](LICENSE.md).