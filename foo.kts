fun main() {
    for (i in 0 until 20) {
        println("Hello, Kotlin!")
        Thread.sleep(200)
    }

    println("I like int type!") // I really, really like int type man!
}

main()
println(10 / 0) // This will cause intentional error