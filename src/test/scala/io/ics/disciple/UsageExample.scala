package io.ics.disciple

import org.scalatest._

case class Car(mark: String, model: String, dirty: Boolean)

case class Driver(info: DriverInfo, car: Car)

case class DriverInfo(name: String)

class CarShop(car: Car) {
  def buy = car
}

class CarWash {
  def wash(car: Car) = car.copy(dirty = false)
}

class CarServices(carShop: CarShop, carWash: CarWash) {
  def getNewWashedCar = {
    val car = carShop.buy
    carWash.wash(car)
  }
}

object CarServices {
  def apply(carShop: CarShop, carWash: CarWash): CarServices = new CarServices(carShop, carWash)
}

case class User(name: String)

class UserService(val admin: User) {
  def getUser(name: String) = User(name)
}

// Notice: Factory methods are not required, but it allows to use more compact form to pass constructor as a function
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

class UsageExample extends FlatSpec with Matchers {

  "Usage example" should "show typical usecase" in {

    object Bindings {
      private val servicesModule = Module().
        bind(new CarShop(_: Car)).singleton.
        bind(new CarWash).singleton.
        bind(CarServices.apply _).singleton

      private val driverModule = Module().
        names('jackInfo, *).bind(Driver).byName('Jack).singleton.
        bind(DriverInfo("Jack")).byName('jackInfo)

      private val carModule = Module().
        bind(Car("Ford", "Focus", dirty = true))

      val binding = servicesModule combine driverModule combine carModule build()
    }

    import Bindings._

    val services = binding[CarServices]
    val car = services.getNewWashedCar
    val driver = binding[Driver]('Jack)

    car.dirty shouldBe false
    driver.info.name shouldBe "Jack"
    println(car)
  }

  "Example from README.md" should "works correctly" in {
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

  }
}
