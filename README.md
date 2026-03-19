# Bahmni Module Immunization

An OpenMRS module that provides FHIR R4 Immunization resource support for Bahmni. It uses a dedicated database model (not OpenMRS Obs-based) to track immunization history including administered vaccinations, waivers, and related clinical data.

## Features

- FHIR R4 compliant Immunization resource (`GET`, `POST`, `PUT`, `DELETE`, `Search`)
- Supports `completed`, `not-done`, and `entered-in-error` statuses
- Tracks vaccine type, drug product, dose sequence, site, route, manufacturer, batch/lot number, expiration date
- Subpotent dose tracking with reason
- Multiple performers per immunization (administering provider, ordering provider)
- Annotation-based notes with author references
- Links to existing orders via `basedOn` references
- Bahmni FHIR extensions for `administeredProduct` and `basedOn`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/ws/fhir2/R4/Immunization/{id}` | Read by ID |
| POST | `/ws/fhir2/R4/Immunization` | Create |
| PUT | `/ws/fhir2/R4/Immunization/{id}` | Update |
| DELETE | `/ws/fhir2/R4/Immunization/{id}` | Soft delete |
| GET | `/ws/fhir2/R4/Immunization?patient={id}` | Search by patient |

## Database Tables

- `immunization` - Main immunization records
- `immunization_performer` - Performer references (AP/OP)
- `immunization_notes` - Clinical notes/annotations
- `immunization_based_on` - Links to orders

## Requirements

- OpenMRS Platform 2.5.12+
- openmrs-module-fhir2 2.5.0+

## Build

```bash
mvn clean install
```

The `.omod` file will be generated at `omod/target/immunization-1.0.0-SNAPSHOT.omod`.

## Deploy

Copy the `.omod` file to the OpenMRS modules directory and restart OpenMRS.
