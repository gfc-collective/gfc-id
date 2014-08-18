# gfc-id

A library that contains typed Id and Guid classes. Part of the gilt foundation classes.

Typed identifiers allow compile-time checking that an identifier that is passed into a
function or object is of the correct type, which is not the case with "untyped" String, 
Long or UUID types.

## Example Usage

com.gilt.gfc.id.Guid is a space/cpu efficient and type safe replacement for java.util.UUID

    val userGuid: Guid[User] = Guid.randomGuid
    val user: User = new User(userGuid, firstName, lastName)

com.gilt.gfc.id.Id can be used for typed identifiers (e.g. rdbms auto-increment ids).

    val userId: Id[User, Long] = Random.nextLong
    val user: User = new User(userId, firstName, lastName)

Both Guid and Id can be used in conjunction with type aliases:

    type UserGuid = Guid[User]
    type UserId = Id[User, Long]
    case class User(guid: UserGuid, id: UserId, firstName: String, lastName: String)

## License
Copyright 2014 Gilt Groupe, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0


