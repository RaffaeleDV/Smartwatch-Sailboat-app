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

    val hm = HashMap<String, String>()
    val sentenze = data.split('\n')

    sentenze.forEachIndexed { index, s ->
        run {
            //println("Index: "+ index + " Singola: " + s)

            if (!s.isNullOrEmpty()) {
                //println(s.toString())
                if (s.isNotEmpty() && !s.equals("") && !s.equals(" ") && !s.isBlank()) {
                    val row = s.split(',')

                    //println("Virgola: "+row[0])

                    //println(row.toString())
                    if (row[0].isNotEmpty()) {
                        when (row[0].substring(3)) {
                            "GLL" -> {
                                //println("GLL")
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

                                hm["latitude"] = latDecimal
                                hm["longitude"] = longDecimal
                                //println(latDecimal + " " + longDecimal)

                            }

                            "MWV" -> {
                                val windAngle = row[1]
                                val windSpeed =
                                    String.format("%.2f", windSpeedKnots(row[3].toFloat(), row[4]))

                                hm["windAngle"] = windAngle
                                hm["windSpeed"] = windSpeed

                            }

                            "MAXMWV" -> {
                                val rafficaAngle = row[1]
                                val maxWindSpeed = row[2]

                                hm["maxWindSpeed"] = maxWindSpeed
                            }

                            "MWD" -> {
                                val windDirection = row[1]

                                hm["windDirection"] = windDirection

                            }

                            "VTG" -> {
                                val courseOverGround = row[1]
                                val shipSpeed = row[5]
                                val shipSpeedKm = row[7]

                                hm["shipSpeed"] = shipSpeed
                                hm["courseOverGround"] = courseOverGround
                            }

                            "RMC" -> {
                                val speedOverGround = row[7]

                                hm["speedOverGround"] = speedOverGround

                            }

                            "HDT" -> {
                                val shipDirection = row[1]

                                hm["shipDirection"] = shipDirection
                            }


                        }

                    }
                }

            }

        }
    }
    return hm
}