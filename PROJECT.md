# NoteApp Project Document

## Overview

GeoNote is a multi-module Android app to capture geospatial project notes.
Each project contains coordinate entries, and each coordinate can have attached media files.

## Module Structure

- `app`
  - Jetpack Compose UI and navigation
  - ViewModels and domain models
  - Background workers/services for upload and sync
- `database`
  - Room database (`AppDatabase`)
  - Entities (`ProjectEntity`, `CoordinateEntity`, `MediaEntity`)
  - DAO and repository abstractions
- `network`
  - File upload repository and data sources (S3 integration)
  - Upload-related use-cases and models

## Core Data Model

### 1) Project

Represents a top-level container.

- `id` (auto-generated)
- `title`
- `description`
- `createdAt`

### 2) Coordinate

Represents a geotagged point inside a project.

- `id` (auto-generated)
- `projectId` (parent project reference)
- `latitude`
- `longitude`
- `title`
- `description`
- `createdAt`

### 3) Media

Represents a media item tied to a coordinate.

- `id` (string id)
- `coordinateId` (parent coordinate reference)
- `path` (local URI/path)
- `createdAt`
- `status` (`false` = pending upload, `true` = uploaded)

## Relationships

- One `Project` -> many `Coordinate`
- One `Coordinate` -> many `Media`

This hierarchy is exposed in Room using relation wrappers such as:

- `ProjectWithDetails`
- `CoordinateWithMedias`

## Example JSON Shape

```json
[
  {
    "projectId": 1,
    "title": "XYZ",
    "description": "xyz delectus aut autem",
    "createdAt": "12/02/2025 12:02:12",
    "coordinates": [
      {
        "id": 1,
        "latitude": 12.32332323,
        "longitude": 77.76933291,
        "title": "Point 1",
        "description": "Point 1 nice left side",
        "createdAt": "12/02/2025 12:10:11",
        "photos": [
          {
            "id": 1,
            "photoUrl": ""
          }
        ],
        "audios": [
          {
            "id": 1,
            "audioUrl": ""
          }
        ]
      }
    ]
  }
]
```

> Note: In current Room entities, media is stored in a single `media` table with `path` and `status` rather than separate `photos` and `audios` tables.

## Tech Stack

- Kotlin + Jetpack Compose
- Room (local persistence)
- WorkManager (background tasks)
- Google Maps + location services
- Firebase (Analytics / Crashlytics)
- AWS S3 client (file upload)

