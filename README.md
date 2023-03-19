## Bring UP MySQL database
Install the latest Docker version with Docker-Compose plugin
Checkout project and cd into repo and Run in separate window:
```bash
docker compose down --volumes || echo OK
docker compose up
```

## Building and populating data
1. Install any JDK 17.
2. Add JDK's bin subfolder to your PATH
3. export JAVA_HOME to location where JDK is installed 
4. Make sure MySQL is running
5. `./mvnw clean package`
6. run `java -jar target/poller-api-0.1.jar`

100k answers will be already populated by unit test using GraphQL API by sending 10 mutations per single request. Loading times could be improved more if batch will be increased.
Stats are calculated using "eventually consistent" approach with small lag (1-2sec). Stats calc is not using DB aggregations. Such approach has benefits of not slowing down GraphQL API performance with a growth of data volume. Also, concurrent queries will not create CPU load on database. Custom algorithms could be implemented easily beyond what is possible with MySQL features. For example if questions need to be plotted then several "time series" aggregates could be calculated with different resolutions.

## Testing GraphQL API
1. Point browser to https://localhost:8443/graphiql
2. Accept self-signed certificate
## Sample Queries

```graphql
{
    wordsStats {
        word
        count
        textQuestion {
            id
            question
        }
    }
}
```

```graphql
{
    scalarAnswersStats {
        scalarQuestion {
            id
            question
        }
        count
        average
    }
}
```

```graphql
{
    scalarAnswersStatsPerAnswer {
        scalarQuestion {
            id
            question
        }
        answer
        count
    }
}
```

```graphql
{
    textAnswersStats {
        count
        textQuestion {
            id,
            question
        }
    }
}
```

## Micronaut 3.8.7 Documentation

- [User Guide](https://docs.micronaut.io/3.8.7/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.8.7/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.8.7/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---
