# NBA Stats

NBA Stats represents a RESTful API that works as a middleware of abstraction of the already existing [NBA API](https://rapidapi.com/theapiguy/api/free-nba) provided on Rapid API.
The application was built using the Framework Spring Boot in Java.

The [NBA API](https://rapidapi.com/theapiguy/api/free-nba) already provides information about NBA matches, Teams, Players and Statistics.

The main objective of the NBA Stats API is to transform the data obtained from [NBA API](https://rapidapi.com/theapiguy/api/free-nba) to a different structure with aditive information.

# Rest API

The REST API for the NBA Stats App is described bellow.

## Get all matches specified by a given date

The given date has to be in the specific format yyyy-MM-dd

## Request

```http
GET /api/nba/match/?date=2019-02-09
```
## Response

```json
[
 {
    "id": 48751,
    "homeTeam": "Boston Celtics",
    "visitorTeam": "LA Clippers",
    "homeScore": 112,
    "visitorScore": 123,
    "date": "2019-02-09T00:00:00.000+00:00",
    "allComments": [...],
    "allStats": [...]
 },
 {
    "id": 48751,
    "homeTeam": "Boston Celtics",
    "visitorTeam": "LA Clippers",
    "homeScore": 112,
    "visitorScore": 123,
    "date": "2019-02-09T00:00:00.000+00:00",
    "allComments": [...],
    "allStats": [...]
 }
]
```

## Get a match specified by the ID

## Request

```http
GET /api/nba/match/1
```

## Response 

```json
[
 {
    "id": 1,
    "homeTeam": "Boston Celtics",
    "visitorTeam": "Philadelphia 76ers",
    "homeScore": 105,
    "visitorScore": 87,
    "date": "2018-10-16T00:00:00.000+00:00",
    "allComments": [...],
    "allStats": [...]
 }
]
```

## Create multiple comments for a match

## Request

```http
GET /api/nba/match/1/comment
```

## Request body

```json
[
 {
    "message": "What a play from Jayson Tatum! Well done!"
 },
 {
    "message": "I was expecting more from Aron Bayne.."
 }
]






