package com.wagdybuild.whatsapp

import java.util.Scanner

fun main() {
    val input = Scanner(System.`in`)
    print("Enter the size of the array : ")
    val size = input.nextInt()

    val arr = Array(size) { Array(size) { 0 } }

    var sum1 = 0
    var sum2 = 0
    var count = size-1

    for (i in 0 until size) {
        for (j in 0 until size) {
            print("Enter an integer: ")
            arr[i][j] = input.nextInt()
        }
    }

    for (i in 0 until size) {
        for (j in 0 until size) {
            if (i == j) {
                sum1 += arr[i][j]
                println("Sum1 = $sum1")
            }
        }
        for (j in 4 downTo 0) {
            if (j == count) {
                sum2 += arr[i][j]
                count--
                println("Sum2 = $sum2")
            }
        }
    }
    print("Sum1 = $sum1 || Sum2 = $sum2")

}