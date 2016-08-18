<snippet>
  <content>
# DIsciple

DIsciple is a small dependency injection library for Scala inspired by Guice and Scaldi on pure no-dep Scala without
macro and reflection (actually using ClassTag). The main feature is early determining cycle references
and graph incompleteness.

## Installation

```scala
resolvers += Resolver.bintrayRepo("korpse", "maven")

libraryDependencies += "io.ics" %% "disciple" % "1.0"
```

## Usage

### Getting started

1. Pass constructor functions to ```binding``` function
2. Combine multiple modules with Module().combine
3. Call build
4. If there's a cyclic dependency, or lack of some component, an ```IllegalStateException``` would be thrown
5. Use ```binding[T]``` or ```binding[T]('Id)``` to get instance

```scala
import io.ics.disciple.Module

case class User(name: String)

class UserService(val admin: User) {
  def getUser(name: String) = User(name)
}

// Notice: DIsciple requires binding as a constructor function, so Factory methods are not required, but it allows to use]
// more compact form
object UserService {
  def getInstance(admin: User) = new UserService(admin)
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

object Bindings {
  val binding = Module().
    bind(UserController.getInstance _).singleton.
    bindNamed(Some('admin))(UserService.getInstance _).singleton.
    bind(User("Admin")).byName('admin).
    bind(User("Jack")).byName('customer).
    build()
}

import Bindings._

println(binding[User]('customer)) // user with id 'customer' is Jack
println(binding[UserService].admin) // service's admin is User(Admin)
println(binding[UserController].renderUser("George")) // controller has it's dependency

```

### Features

#### Lazy dependency binding
Default all bindings are lazy

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
  bindNamed(Some('a))(B).
  bindNamed(Some('anotherA), None)(C). // Use this if you want some args binded by name and others by type
  build()

val a = binding[A]('a) // calls binding called 'a of type A
```

### Singleton component

```scala
val binding = Module().
  bind(System.currentTimeMillis()).
  bind(A).singleton.
  build()

val a = binding[A] // - every time exactly the same instance would be returned
```

### [For more examples please look at tests](https://github.com/KORPSE/disciple/tree/master/src/test/scala/io/ics)

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## License

**DIsciple** is licensed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
