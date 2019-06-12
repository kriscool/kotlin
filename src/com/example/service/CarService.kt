package com.example.com.example.service

import com.example.com.example.dao.CarUsers
import com.example.com.example.dao.Cars
import com.example.com.example.model.Car
import com.example.com.example.model.CarUser
import com.example.dao.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CarService {
    fun getAllCars(): ArrayList<Car> {
        var list = arrayListOf<Car>()
        transaction {
            for (car in Cars.selectAll().iterator()) {
                list.add(Car(car.data[0] as Int, car.data[1] as String))
            }
        }
        return list
    }

    fun createCar(car: Car) {
        val carId = transaction {
            Cars.insert {
                it[Cars.name] = car.name
            }.generatedKey
        }
    }

    fun getCar(id: Int): Car? {
        return transaction {
            Cars.select { Cars.id eq id }
                .map { Car(it[Cars.id], it[Cars.name]) }
        }.getOrNull(0)
    }

    fun getMyCars(idUser: Int) : ArrayList<Car?> {
        var list = arrayListOf<Car?>()
        transaction {
            (Users innerJoin CarUsers).slice(Users.id,CarUsers.car).select { (Users.id.eq(idUser)) }.forEach {
                list.add(getCar(it[CarUsers.car]))
            }
        }
        return list
    }

    fun deleteCarFromUser(idUser:Int,IdCar: Int){
        transaction {
            CarUsers.deleteWhere {(CarUsers.car eq IdCar) and (CarUsers.user eq idUser)}
        }
    }

    fun addCar(idCar: Int,idUser: Int){
        transaction {
            CarUsers.insert {
                it[CarUsers.car] =idCar
                it[CarUsers.user] = idUser
            }
        }
    }


    fun updateCar(car: Car?) {
        transaction {
            Cars.update({ Cars.id eq car!!.id }) {
                it[name] = car!!.name
            }
        }
    }
}
