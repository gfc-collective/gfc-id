

# gfc-id [![Build Status](https://travis-ci.org/gilt/gfc-id.svg?branch=master)](https://travis-ci.org/gilt/gfc-id) [![Coverage Status](https://coveralls.io/repos/gilt/gfc-id/badge.svg?branch=master&service=github)](https://coveralls.io/github/gilt/gfc-id?branch=master) [![Join the chat at https://gitter.im/gilt/gfc](https://badges.gitter.im/gilt/gfc.svg)](https://gitter.im/gilt/gfc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A library that contains typed Id and Guid classes. Part of the gilt foundation classes.

## Getting gfc-id

The latest version is 0.0.6, which is cross-built against Scala 2.10.x, 2.11.x and 2.12.0-RC1.

If you're using SBT, add the following line to your build file:

```scala
libraryDependencies += "com.gilt" %% "gfc-id" % "0.0.6"
```

For Maven and other build tools, you can visit [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Ccom.gilt%20gfc).
(This search will also list other available libraries from the gilt fundation classes.)

## Contents and Example Usage

Typed identifiers allow compile-time checking that an identifier that is passed into a
function or object is of the correct type, which is not the case with "untyped" String, 
Long or UUID types.

### com.gilt.gfc.id.Guid

Guid is a space/cpu efficient and type safe replacement for java.util.UUID

    val userGuid: Guid[User] = Guid.randomGuid
    val user: User = new User(userGuid, firstName, lastName)

### com.gilt.gfc.id.Id 

Id can be used for typed identifiers (e.g. rdbms auto-increment ids).

    val userId: Id[User, Long] = Random.nextLong
    val user: User = new User(userId, firstName, lastName)

### Type aliases

Both Guid and Id can be used in conjunction with type aliases:

    type UserGuid = Guid[User]
    type UserId = Id[User, Long]
    case class User(guid: UserGuid, id: UserId, firstName: String, lastName: String)

## License
Copyright 2016 Gilt Groupe, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0


