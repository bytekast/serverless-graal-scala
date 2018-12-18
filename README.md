# Serverless Graal Scala Demo

This demo project uses [GraalVM](https://www.graalvm.org/) to compile a simple [Scala](https://www.scala-lang.org/) project into a single **native executable file**. As a result, the application can run without Java/JVM installed in the target machine.

See [Bootstrap.scala](src/main/scala/Bootstrap.scala)

It utilizes the new [AWS Lambda Runtime API](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html) to call the native executable as a **Lambda Function**.

Deployment is powered and managed by the [Serverless Framework](https://serverless.com/).

#### Prerequisites

1) **Linux** development machine - This project builds a *native binary* that is deployed to **AWS** so it must run in a Linux machine.
2) Install [NodeJS](https://nodejs.org/en/) and the [Serverless Framework](https://serverless.com/framework/docs/getting-started/)
3) [Amazon Web Services](https://aws.amazon.com/) account

You also need to setup your AWS credentials/profiles in the `~/.aws/credentials` file.

```
[dev]
aws_access_key_id = XXXXXXXXXXXXXX
aws_secret_access_key = XXXXXXXXXXXXXX
region = us-east-1
```

### Build

To build the project, run `./gradlew clean build`. 

This will download all of the project and build dependencies including the GraalVM SDK and will create a native executable file named `bootstrap` in the `build/graal` directory.


### Deploy

To deploy, simply run `sls deploy`. This demo project will create 2 lambda functions: `echo` and `reverse`:

To invoke the functions, run:

```
sls invoke -f echo -d 'hello'
sls invoke -f reverse -d 'hello'
```