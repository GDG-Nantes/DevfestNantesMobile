package com.gdgnantes.devfest

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}