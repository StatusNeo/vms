# Architecture Decision Record

## Title
Use Apache POI for Excel file handling

## Status
Accepted

## Context
The application needs to generate and process Excel files for visitor and report data. Apache POI is a mature, widely-used library for working with Excel files in Java.

## Decision
We will use Apache POI for all Excel file generation and processing needs.

## Consequences
- Reliable Excel file support
- Well-documented and maintained library
- Adds a dependency to the project

## Alternatives Considered
- JExcel: Less maintained
- Manual CSV export: Less user-friendly, limited formatting 