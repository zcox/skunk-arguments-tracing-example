Simple example that shows Skunk leaking database query/command arguments to tracing. These arguments can contain sensitive data, PII, etc, which should not be exposed to tracing backends.

This example is based on the [Skunk command example](https://tpolecat.github.io/skunk/tutorial/Command.html#full-example) and uses the [Natchez log tracing backend](https://tpolecat.github.io/natchez/backends/log.html).

```
docker compose up -d
sbt run | grep arguments
```

Note the following output:

```
  "arguments" : "Bob,12",
  "arguments" : "John,2,George,3,Paul,6,Ringo,3",
```
