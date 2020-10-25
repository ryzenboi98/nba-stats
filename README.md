# NBA Stats

NBA Stats represents a RESTful API that works as a middleware of abstraction of the already existing [NBA API](https://rapidapi.com/theapiguy/api/free-nba).
This application was built under the Framework Spring Boot in Java.

The [NBA API](https://rapidapi.com/theapiguy/api/free-nba) already provides information about NBA matches, teams, players and statistics.

The main objective of the NBA Stats API is to transform the obtained data from [NBA API](https://rapidapi.com/theapiguy/api/free-nba) to a different structure with aditive information.

## Project setup

For compiling this project you may need to install the dependencies already provided on the `pom.xml` file simply by exporting the Project and running the application with an IDE supporting Maven or Gradle. 

Secondly you need to create a postgresql instance with [Docker](https://docs.docker.com/get-docker/) and create a database.
You can achieve that by running the following command on the terminal with an instance name and password by your choice after installing [Docker](https://docs.docker.com/get-docker/).

Make sure that port 5432 is not in use which may cause conflicts later on the application execution.

`docker run --name 'instance_name' -e POSTGRES_PASSWORD='password' -d -p 5432:5432 postgres:alpine`



You can now check all of the docker instances with the command `docker container ls -a` and check if your instance is running.
If not, you must start your instance by running the command `docker start 'instance_name'`. Once the instance is running you need to make a connection the the instance using the command line:

`docker exec -it 'instance_name' /bin/bash`

After connecting to the instance you need setup your postgresql database by running the command `psql -U 'username'` with the default username as `postgres` and create the database by writing the command `create database 'database_name';`.

At this moment you are now ready to setup the final configuration on the file `application.yml` where you will need to insert the database information as it is described on the file.

After completing the configuration you can now execute and test the API endpoints for the NBA Stats Application on the default URL `localhost:8080`.

## Match structure

All Matches contains the following representative structure.

```json
{
    "id": 1,
    "homeTeam": "Boston Celtics",
    "visitorTeam": "Philadelphia 76ers",
    "homeScore": 105,
    "visitorScore": 87,
    "date": "2018-10-16T00:00:00.000+00:00",
    "allComments": [
        {
            "id": 2,
            "message": "Oh what a game from boston",
            "date": "2020-10-24T15:09:01.723+00:00"
        },
        {
            "id": 3,
            "message": "Joel did so good!",
            "date": "2020-10-24T15:09:09.147+00:00"
        }, 
        {...}
    ],
    "allStats": [
        {
            "playerName": "Joel Embiid",
            "points": 23
        },
        {
            "playerName": "Jayson Tatum",
            "points": 23
        },
        {
            "playerName": "Ben Simmons",
            "points": 19
        },
        {...}
    ]
}
```
Each Match contains the home and visitor team information, the comments of the match and the points statistics for each player that scored atleast 1 point.

# Rest API

The REST API for the NBA Stats Application is described bellow.

## Get all matches specified by a given date

The request params must be `date=[yyyy-MM-dd]` for getting all matches with the given date `yyyy-MM-dd`.

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

The request URL must have an integer representing the `id` of a match.

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
The request needs the match `id` following by the path for the comments.
## Request

```http
POST /api/nba/match/1/comment
```

## Request body

The request body must come in `ArrayJSON` object type.

```json
[
 {
    "message": "What a play from Jayson Tatum! Well done!"
 },
 {
    "message": "I was expecting more from Aron Bayne.."
 }
]
```
## Update a comment from a match

The request must have the `id` of the match followed by the comment path and the `id` of the comment.

## Request

```http
PUT /api/nba/match/1/comment/2
```

## Request Body

The request body must come in `JSON` object type.

```json
{
    "message": "I was expecting more from Aron Bayne even though the rest of the team did pretty well."
}
```

## Delete a comment from a match

The request must have the `id` of the match followed by the comment path and the `id` of the comment.

## Request

```http
DELETE /api/nba/match/1/comment/1
```








