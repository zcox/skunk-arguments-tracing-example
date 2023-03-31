Simple example that shows Skunk leaking database query/command arguments to tracing and logging. These arguments can contain sensitive data, PII, etc, which should not be exposed to tracing/logging backends.

This example is based on the [Skunk command example](https://tpolecat.github.io/skunk/tutorial/Command.html#full-example) and uses the [Natchez log tracing backend](https://tpolecat.github.io/natchez/backends/log.html).

```
docker compose up -d
sbt run
```

Note that Skunk leaks all inserted data to the tracing backend, in the `arguments` field:

```
  "arguments" : "Bob,12",
  "arguments" : "John,2,George,3,Paul,6,Ringo,3",
```

On a Postgres error/exception (e.g. primary key violation), Skunk leaks the arguments to the tracing backend in the `error.argument.n.value` fields:

```
  "error.argument.1.value" : "Bob",
  "error.argument.2.value" : "12",
```

And the `error.detail` field:

```
 "error.detail" : "Key (name)=(Bob) already exists."
```

Skunk also leaks the arguments to the console:

```
[error] skunk.exception.PostgresErrorException:
[error] 🔥
[error] 🔥  Postgres ERROR 23505 raised in _bt_check_unique (nbtinsert.c:664)
[error] 🔥
[error] 🔥    Problem: Duplicate key value violates unique constraint "pets_pkey".
[error] 🔥     Detail: Key (name)=(Bob) already exists.
[error] 🔥
[error] 🔥  The statement under consideration was defined
[error] 🔥    at /Users/ZCox/code/zcox/skunk-arguments-tracing-example/src/main/scala/Main.scala:28
[error] 🔥
[error] 🔥    INSERT INTO pets VALUES ($1, $2)
[error] 🔥
[error] 🔥  and the arguments were provided
[error] 🔥    at /Users/ZCox/code/zcox/skunk-arguments-tracing-example/src/main/scala/Main.scala:47
[error] 🔥
[error] 🔥    $1 varchar    Bob
[error] 🔥    $2 int2       12
[error] 🔥
[error] 🔥  If this is an error you wish to trap and handle in your application, you can do
[error] 🔥  so with a SqlState extractor. For example:
[error] 🔥
[error] 🔥    doSomething.recoverWith { case SqlState.UniqueViolation(ex) =>  ...}
[error] 🔥
[error]
[error] skunk.exception.PostgresErrorException: Duplicate key value violates unique constraint "pets_pkey".
[error]   at skunk.net.protocol.Execute$$anon$1$$anonfun$$nestedInanonfun$apply$2$1.$anonfun$applyOrElse$8(Execute.scala:69)
[error]   at flatten$extension @ skunk.util.Pool$.$anonfun$ofF$2(Pool.scala:103)
[error]   at flatMap @ skunk.net.BufferedMessageSocket$$anon$1.$anonfun$receive$1(BufferedMessageSocket.scala:150)
[error]   at get @ skunk.util.Pool$.free$1(Pool.scala:156)
[error]   at flatMap @ skunk.net.BufferedMessageSocket$$anon$1.receive(BufferedMessageSocket.scala:147)
[error] stack trace is suppressed; run last Compile / run for the full output
[error] (Compile / run) skunk.exception.PostgresErrorException:
[error] 🔥
[error] 🔥  Postgres ERROR 23505 raised in _bt_check_unique (nbtinsert.c:664)
[error] 🔥
[error] 🔥    Problem: Duplicate key value violates unique constraint "pets_pkey".
[error] 🔥     Detail: Key (name)=(Bob) already exists.
[error] 🔥
[error] 🔥  The statement under consideration was defined
[error] 🔥    at /Users/ZCox/code/zcox/skunk-arguments-tracing-example/src/main/scala/Main.scala:28
[error] 🔥
[error] 🔥    INSERT INTO pets VALUES ($1, $2)
[error] 🔥
[error] 🔥  and the arguments were provided
[error] 🔥    at /Users/ZCox/code/zcox/skunk-arguments-tracing-example/src/main/scala/Main.scala:47
[error] 🔥
[error] 🔥    $1 varchar    Bob
[error] 🔥    $2 int2       12
[error] 🔥
[error] 🔥  If this is an error you wish to trap and handle in your application, you can do
[error] 🔥  so with a SqlState extractor. For example:
[error] 🔥
[error] 🔥    doSomething.recoverWith { case SqlState.UniqueViolation(ex) =>  ...}
[error] 🔥
```
