# http4s-laminar-stack ![build](https://github.com/keynmol/http4s-laminar-stack/workflows/build/badge.svg)


This is an example of a full-stack application with the following features:

- Scala 3 on both frontend and backend 
- [Laminar](https://github.com/raquo/Laminar) as a frontend library
- [Http4s](https://http4s.org/) as a backend HTTP server library 
- Shared code with **protocol** definitions
- Gzip compression on the server side
- Docker packaging of the full application
- Tests for the client with simulated DOM using [jsdom](https://github.com/scala-js/scala-js-env-jsdom-nodejs)

_**Note**_: this is a very basic setup, for a more complicated template (with Postgres,
API spec using Smithy, etc.) please see [Smithy4s Fullstack template](https://github.com/indoorvivants/smithy4s-fullstack-template)

Additionally, you can check out my blog series about fullstack Scala 3:

- [Twotm8](https://blog.indoorvivants.com/tags/series:twotm8) - building and deploying a full-stack 
  Scala application using Scala Native and Scala.js 

- [Smithy4s](https://blog.indoorvivants.com/tags/series:smithy4s) - building and 
  deploying a full-stack Scala app with Smithy4s and Scala.js

_**Note**_: this version of the template uses the latest and greatest from Cats Effect, http4s, Scala, etc. If you would like, please see the [last commit](https://github.com/keynmol/http4s-laminar-stack/commit/9d2078e0da73192be6d16d20ecaec1ee783db842) that referenced old versions of the libraries. Apart from Scala 3 (which is still wonky around IntelliJ support), I **highly** recommend sticking with the latest versions of the libraries. 

## Development mode

Run in SBT (uses fast JS compilation, not optimized):

```
sbt> ~runDev
```

And open http://localhost:9000/frontend

This will restart the server on any changes: shared code, client/server, assets.

## Tests
It is a prerequisite to have jsdom installed, in order for the frontend tests to run. Proposal:
```
yarn add jsdom
```
Then move into an sbt console and run tests as normal

## Production mode

Run in SBT (uses full JS optimization):

```
sbt> ~runProd
```

## Docker packaging 

```
sbt> backend/docker:publishLocal
```

Will publish the docker image with fully optimised JS code, and you can run the container:

```bash
✗ docker run --rm -p 8080:8080 laminar-http4s-example:0.1.0-SNAPSHOT

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Running server on http://0.0.0.0:8080 (mode: prod)
```


The interface is fairly simple:

![](https://imgur.com/S0f0i8i.png)
