package cz.j_jzk.json

import cz.j_jzk.klang.input.InputFactory
import cz.j_jzk.klang.lesana.lesana
import cz.j_jzk.klang.parse.NodeID
import cz.j_jzk.klang.prales.constants.boolean
import cz.j_jzk.klang.prales.constants.decimal
import cz.j_jzk.klang.prales.constants.integer
import cz.j_jzk.klang.prales.constants.string
import cz.j_jzk.klang.prales.useful.separatedList

fun main() {
    val jsonLesana = lesana<Any> {
        val jsonValue = NodeID<Any>("value")
        val obj = NodeID<Map<String, Any>>("object")
        val arr = NodeID<List<Any>>("array")
        val str = include(string())

        // plain values
        jsonValue to def(obj) { it.v1 }
        jsonValue to def(arr) { it.v1 }
        jsonValue to def(include(integer(nonDecimal=false, underscoreSeparation=false))) { it.v1 }
        jsonValue to def(include(decimal(allowEmptyIntegerPart=false))) { it.v1 }
        jsonValue to def(include(boolean())) { it.v1 }
        jsonValue to def(str) { it.v1 }

        val comma = NodeID<Any?>()
        comma to def(re(",")) { null }

        // arrays
        arr to def(
            re("\\["),
            include(separatedList(jsonValue, comma, false, true)),
            re("\\]"),
        ) { (_, arr, _) -> arr }

        // objects
        val kvPair = NodeID<Pair<String, Any>>("key-value pair")
        kvPair to def(str, re(":"), jsonValue) { (key, _, value) -> Pair(key, value) }
        obj to def(
            re("{"),
            include(separatedList(kvPair, comma, false, true)),
            re("}"),
        ) { (_, pairs, _) -> pairs.toMap() }

        setTopNode(jsonValue)
        ignoreRegexes("[ \t\n]")

        onUnexpectedToken { err ->
            println(err)
        }
    }.getLesana()

    println("Enter JSON:")
    println(jsonLesana.parse(InputFactory.fromString(
        readLine()!!,
        "STDIN"
    )))
}
