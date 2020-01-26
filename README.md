# gfc-id [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.gfccollective/gfc-id_2.12/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/org.gfccollective/gfc-id_2.12) [![Build Status](https://github.com/gfc-collective/gfc-id/workflows/Scala%20CI/badge.svg)](https://github.com/gfc-collective/gfc-id/actions) [![Coverage Status](https://coveralls.io/repos/gfc-collective/gfc-id/badge.svg?branch=master&service=github)](https://coveralls.io/github/gfc-collective/gfc-id?branch=master) 

A library that contains typed Id and Guid classes.
A fork and new home of the former Gilt Foundation Classes (`com.gilt.gfc`), now called the [GFC Collective](https://github.com/gfc-collective), maintained by some of the original authors.

## Getting gfc-id

The latest version is 1.0.0, which is cross-built against Scala 2.12.x and 2.13.x.

If you're using SBT, add the following line to your build file:

```scala
libraryDependencies += "org.gfccollective" %% "gfc-id" % "1.0.0"
```

For Maven and other build tools, you can visit [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Corg.gfccollective).
(This search will also list other available libraries from the GFC Collective.)

## Contents and Example Usage

Typed identifiers allow compile-time checking that an identifier that is passed into a
function or object is of the correct type, which is not the case with "untyped" String, 
Long or UUID types.

### org.gfccollective.id.Guid

Guid is a space/cpu efficient and type safe replacement for java.util.UUID

    val userGuid: Guid[User] = Guid.randomGuid
    val user: User = new User(userGuid, firstName, lastName)

### org.gfccollective.id.Id 

Id can be used for typed identifiers (e.g. rdbms auto-increment ids).

    val userId: Id[User, Long] = Random.nextLong
    val user: User = new User(userId, firstName, lastName)

### Type aliases

Both Guid and Id can be used in conjunction with type aliases:

    type UserGuid = Guid[User]
    type UserId = Id[User, Long]
    case class User(guid: UserGuid, id: UserId, firstName: String, lastName: String)

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0


