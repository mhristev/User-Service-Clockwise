package com.clockwise.userservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<UserserviceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
