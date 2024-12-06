Spring 道場 Blog API
==

## セットアップ

```shell
$ docker compose up -d

$ docker exec -it localstack /bin/bash
$ awslocal s3 mb s3://profile-images
$ awslocal s3 ls

$ ./gradlew flywayMigrate
$ ./gradlew bootRun
```
