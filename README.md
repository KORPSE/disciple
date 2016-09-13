# DIsciple [![Build Status](https://travis-ci.org/KORPSE/disciple.svg?branch=master)](https://travis-ci.org/KORPSE/disciple)

DIsciple is a small dependency injection library for Scala inspired by Guice and Scaldi on pure Scala without
any external dependencies.

The key feature of DIsciple is early determining cycle references and graph incompleteness.

DIsciple implements dependency injection via constructor arguments injection, but allows you to use any function returning target object
as a constructor.

## Installation

Add next to build.sbt
```scala
libraryDependencies += "io.ics" %% "disciple" % "1.2.1"
```

## Usage

### Getting started

1. Pass constructor functions to ```bind``` method to wire components by its names
2. To wire constructor arguments by names put ```forNames('id1, 'id2, ...)``` before ```bind```
3. Combine multiple modules with Module().combine
4. Module.build will return you a ```DependencyGraph```, which you would be able to use to get an actual instances of your components
5. If there's a cyclic dependency, or lack of some component, an ```IllegalStateException``` would be thrown
6. Use ```dependencyGraph[T]``` or ```dependencyGraph[T]('Id)``` to get an instance

```scala
import io.ics.disciple._

case class User(name: String)

class UserService(val admin: User) {
  def getUser(name: String) = User(name)
}

// Notice: DIsciple requires binding as a constructor function, so factory-methods is the most
// comfortable way to pass constructor as function
object UserService {
  var isCreated: Boolean = false
  def getInstance(admin: User) = {
    isCreated = true
    new UserService(admin)
  }
}

class UserController(service: UserService) {
  def renderUser(name: String): String = {
    val user = service.getUser(name)
    s"User is $user"
  }
}

object UserController {
  def getInstance(service: UserService) = new UserController(service)
}

val binding = Module().
  bind(UserController.getInstance _).singleton.
  forNames('admin).bind(UserService.getInstance).singleton.nonLazy.
  bind(User("Admin")).byName('admin).
  bind(User("Jack")).byName('customer).
  build()

assert(UserService.isCreated) // nonlazy binding creates just after building the graph

println(binding[User]('customer)) // user with id 'customer' is Jack
println(binding[UserService].admin) // service's admin is User(Admin)
println(binding[UserController].renderUser("George")) // controller has it's dependency

```

### Features

#### Singleton component

```scala
val binding = Module().
  bind(System.currentTimeMillis()).
  bind(A).singleton.
  build()

val a = binding[A] // - every time exactly the same instance would be returned
```

#### Lazy dependency binding
By default all bindings are lazy

#### Non-lazy dependency binding
You can mark your component binding as non-lazy to force its creation on module build.
Notice: component should be marked as singleton
```scala
Module().
  bind(Service.getInstance).singleton.nonLazy.
  build()
```

#### Binding by name
```scala
class A()
val binding = Module().
  bind[A](new A()).byName('a).build()

val a = binding[A]('a) // calls binding called 'a of type A
```

#### Inject by name
```scala
case class A()
case class B(a: A)
case class C(a: A, b: B)

val binding = Module().
  bind(A()).byName('a).
  bind(A()).byName('anotherA).
  forNames('a).bind(B).
  forNames('anotherA, *).bind(C). // Use this if you want some args bound by name and others by type
  build()

val a = binding[A]('a) // calls binding called 'a of type A
```
* To wire component by name place ```.byName('name)``` after it.
* To get component which was wired by name, call ```dependencyGraph[T]('name)```
* To wire another component as dependent from named components use ```forNames(...)``` with arguments like ```'name```
if parameter should be wired by name or ```*``` it should be wired by type

#### Polymorphic dependencies
By default, component would be bound to a type of its constructor function result.
But often we want to bind it to it's supertype. To achieve this you should explicitly specify constructor function result type:
```scala
trait Service

class ServiceImpl extends Service

val binding =
  Module().
    bind(new ServiceImpl(): Service).
    build()
```

### [For more examples please take a look at tests](https://github.com/KORPSE/disciple/tree/master/src/test/scala/io/ics/disciple)

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## License

**DIsciple** is licensed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
