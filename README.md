# ChessTasks.com

ChessTasks.com API server written in Ktor/Kotlin

See ChessTasks in action: [https://chesstasks.com.pl](https://chesstasks.com.pl)

[![build](https://github.com/kacperfaber/chesstasks-server/actions/workflows/build.yml/badge.svg)](https://github.com/kacperfaber/chesstasks-server/actions/workflows/build.yml)
[![test](https://github.com/kacperfaber/chesstasks-server/actions/workflows/test.yml/badge.svg)](https://github.com/kacperfaber/chesstasks-server/actions/workflows/test.yml)
[![publish](https://github.com/kacperfaber/chesstasks-server/actions/workflows/publish.yml/badge.svg)](https://github.com/kacperfaber/chesstasks-server/actions/workflows/publish.yml)
![running-server-version](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fchesstasks.com.pl%2Fapi%2Fserver%2Fversion&query=%24.version&style=plastic&label=running)

![Baner](assets/banner.png)

## Installation

```shell
git clone https://www.github.com/kacperfaber/chesstasks-server && cd chesstasks-server
```


#### Run server locally
```shell
# Will run with testing database with some records.
# To change default database
#   Edit: src/main/kotlin/com/chesstasks/data/TrySetupTestDb.kt
gradle runDev

# To run server without testing database
gradle runDevNoInitDb
```

## Configuration

```json5
{
  "server": {
    "host": "",
    "port": 8080
  },

  "security": {
    "password-hasher": {
      // Password used to encoding/decoding user passwords.
      // In 'dev' profile, passwords are not encoded.
      "secret": "string"
    },

    "tokens": {
      // Password used to encoding/decoding access tokens.
      // In 'dev' profile, tokens are not encoded.
      "secret": "string"
    },

    "cors": {
      "allowed-origins": "white-space separated origins",
      "allowed-hosts": "white-space separated hosts"
    },

    // Api key is used to registration and confirmation endpoints.
    "api-key": "dev-api-key"
  },

  // Database
  "database": {
    "jdbc": "string",
    "username": "string",
    "password": "string"
  },

  "email": {
    // Email settings to send verification email
    "verification": {
      "hostname": "string",
      "port": 587,
      "username": "string",
      "password": "string",
      "from": "string",
      "subject": "string",
      "ssl-trust": "string"
    }
  }
}

```

## Import Lichess database.
To import lichess database, insert file '*./lichess.data.csv*' with puzzles.
To run application, and import data from this file use parameter 
<br>
**-Dcom.chesstasks.puzzles.import=true**

## Author
Kacper Faber
