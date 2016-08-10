package io.ics

import io.ics.souce.module.Module
import org.scalatest._
class UsageExample extends FlatSpec with Matchers {

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

  "Usage example" should "show typical usecase" in {

    object Bindings {
      private val servicesModule = Module().
        bind(new CarShop(_: Car)).singleton.
        bind(new CarWash).singleton.
        bind(CarServices.apply _).singleton

      private val driverModule = Module().
        bindNamed(Some('jackInfo), None)(Driver).byName('Jack).singleton.
        bind(DriverInfo("Jack")).byName('jackInfo)

      private val carModule = Module().
        bind(Car("Ford", "Focus", dirty = true))

      val dep = servicesModule combine driverModule combine carModule build()
    }

    import Bindings._

    val services = dep[CarServices]
    val car = services.getNewWashedCar
    val driver = dep[Driver]('Jack)

    car.dirty shouldBe false
    driver.info.name shouldBe "Jack"
    println(car)
  }

}
