package com.clockwise.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer

@SpringBootApplication
class UserserviceApplication

fun main(args: Array<String>) {
	runApplication<UserserviceApplication>(*args)
}


//@Configuration
//class NettyConfig {
//
//	@Bean
//	fun nettyContext(context: ApplicationContext): DisposableServer {
//		val handler = WebHttpHandlerBuilder.applicationContext(context).build()
//		val adapter = ReactorHttpHandlerAdapter(handler)
//		val httpServer = HttpServer.create().host("localhost").port(8080)
//		return httpServer.handle(adapter).bindNow()
//	}
//}