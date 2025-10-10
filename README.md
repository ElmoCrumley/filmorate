![diagram](src/main/resources/diagram.svg)

## Основные операции FilmController ##
GET "/films"

POST "/films", *JSON body example:*
```json
{
  "name": "nisi eiusmod",
  "description": "adipisicing",
  "releaseDate": "1967-03-25",
  "duration": 100
}
```
PUT "/films", *JSON body example:*
```json
{
  "name": "nisi eiusmod",
  "description": "adipisicing",
  "releaseDate": "1967-03-25",
  "duration": 100
}
```
DELETE "/films", *JSON body example:*
```json
{
  "id": 123
}
```
PUT "/films/{id}/like/{userId}"

DELETE "/films/{id}/like/{userId}"

GET "/films/popular"
## Основные операции UserController ##
GET "/users"

POST "/users", *JSON body example:*
```json
{
  "login": "dolore",
  "name": "Nick Name",
  "email": "mail@mail.ru",
  "birthday": "1946-08-20"
}
```
PUT "/users", *JSON body example:*
```json
{
  "login": "dolore",
  "name": "Nick Name",
  "email": "mail@mail.ru",
  "birthday": "1946-08-20"
}
```
DELETE "/users", *JSON body example:*
```json
{
  "id": 123
}
```
PUT "/users/{id}/friends/{friendId}"

DELETE "/users/{id}/friends/{friendId}"

GET "/users/{id}/friends"

GET "/users/{id}/friends/common/{otherId}"
