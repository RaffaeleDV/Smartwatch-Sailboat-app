package com.example.sailboatapp.presentation.data

fun windSpeedKnots(value: Float, unit: String): Double {
    if (unit == "N") // knots
        return (value * 1.0)
    if (unit == "M") // ms/s
        return (value * 1.944)
    if (unit == "K") // km/h
        return (value * 0.54)
    return -1.0
}

fun readNMEA(data: String): HashMap<String, String> {

    var hm = HashMap<String, String>()
    var sentenze = data.split('\n')

    sentenze.forEachIndexed { index, s ->
        run {
            //println("Index: "+ index + " Singola: " + s)

            if (!s.isNullOrEmpty()) {
                //println(s.toString())
                if (s.isNotEmpty() && !s.equals("") && !s.equals(" ") && !s.isBlank()) {
                    var row = s.split(',')

                    //println("Virgola: "+row[0])

                    //println(row.toString())
                    if (row[0].isNotEmpty()) {
                        when (row[0].substring(3)) {
                            "GLL" -> {
                                println("GLL")
                                val lat = row[1]
                                val latDegree = lat.substring(0, 2)
                                val latMinutes = lat.substring(2, 8)

                                var latDecimal = String.format(
                                    "%.7f", (latDegree.toFloat() + (latMinutes.toFloat() / 60))
                                )

                                if (row[2].equals("S")) {
                                    latDecimal = "-" + latDecimal
                                }

                                val long = row[3]
                                val longDegree = long.substring(0, 3)
                                val longMinutes = long.substring(3, 8)

                                var longDecimal = String.format(
                                    "%.7f", (longDegree.toFloat() + (longMinutes.toFloat() / 60))
                                )

                                if (row[4].equals("W")) {
                                    longDecimal = "-" + longDecimal
                                }

                                hm.put("latitude", latDecimal)
                                hm.put("longitude", longDecimal)
                                //println(latDecimal + " " + longDecimal)

                            }

                            "MWV" -> {
                                var windAngle = row[1]
                                var windSpeed =
                                    String.format("%.2f", windSpeedKnots(row[3].toFloat(), row[4]))

                                hm.put("windAngle", windAngle)
                                hm.put("windSpeed", windSpeed)

                            }

                            "MAXMWV" -> {
                                var rafficaAngle = row[1]
                                var maxWindSpeed = row[2]

                                hm.put("maxWindSpeed", maxWindSpeed)
                            }

                            "VTG" -> {
                                var shipSpeed = row[5]
                                var shipSpeedKm = row[7]

                                hm.put("shipSpeed", shipSpeed)
                            }

                            "HDT" -> {
                                var shipDirection = row[1]

                                hm.put("shipDirection", shipDirection)
                            }


                        }

                    }
                }

            }

        }
    }


    // data.toa


    /*sentence = sentence[2].split("\n")*/

    //println("Sentenza: "+ sentence[0])

    return hm

}