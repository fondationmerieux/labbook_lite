# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---
## \[1.0.5] - 2025-12-02

### Changed
* extracted cards into dedicated files for clarity
* replaced multi-step DB operations with withTransaction
* removed obsolete LoginActivity and TODOs

## \[1.0.4] - 2025-11-24

### Added
* Lite upload now sends user IDs for all created data items

### Changed

* updated `network-security-config.xml` to accept only HTTPS connections
* added explicit trust for user-installed CAs to simplify HTTPS setup in local laboratory networks
* refactored administrative record screen into dedicated composable cards

## \[1.0.3] - 2025-06-25

### Changed

* labels of partial report and full report become partial and full validation

## \[1.0.2] - 2025-06-03

### Added

* README and LICENSE md files

### Changed

* sending data to the LabBook server only empties new data and reports. No need to retrieve a new configuration
* asterisk in front of mandatory fields on the patient form
* date of birth change from manual entry to a datepicker

### Fixed

* enter patient age without entering date of birth

## \[1.0.1] - 2025-06-02

### Changed

* back button for About and General screens

### Fixed

* configuration recovery
* display of PARTIAL or COMPLETE
* date filter on record list
