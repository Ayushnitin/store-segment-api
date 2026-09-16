# Store Segment API

Spring Boot REST API implementation for managing Store Segments.

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Maven

## Database

The project uses two main tables:

- `store`
- `store_segments`

## Features

The application supports:

- Create Store Segment
- List Store Segments
- Search Store Segments
- Filter by status
- Pagination
- Update Store Segment
- Duplicate Store Segment
- Store Segment Details
- MANUAL Store Segments
- AUTOMATED Store Segments
- Validation Error Handling

## API Endpoints

### Create Store Segment

POST `/api/store-segment/create`

Example:

```json
{
  "title": "Premium Store",
  "store_segment_type": "MANUAL",
  "stores": [1, 2, 3, 4],
  "sql_query": null,
  "status": "ACTIVE"
}