package com.example.sailboatapp.presentation.data



fun readNMEA(data : String) : HashMap<String,String> {

    var hm = HashMap<String,String>()
    var sentenze = data.split('\n')

    sentenze.forEachIndexed { index, s ->
        run {
            //println("Index: "+ index + "Singola: " + s)

            var row = s.split(',')

            //println("Virgola: "+row[0])

            //println(row.toString())
            if(row[0].isNotEmpty()){
                when(row[0].substring(3)){
                    "GLL" -> {
                        println("GLL")
                        val lat = row[1]
                        val latDegree = lat.substring(0,2)
                        val latMinutes = lat.substring(2,8)

                        var latDecimal = (latDegree.toFloat() + (latMinutes.toFloat()/60)).toString()

                        if (row[2].equals("S")) {
                            latDecimal = "-" + latDecimal
                        }


                        val long = row[3]
                        val longDegree = long.substring(0,3)
                        val longMinutes = long.substring(3,8)

                        var longDecimal = (longDegree.toFloat() + (longMinutes.toFloat()/60)).toString()

                        if (row[4].equals("W")) {
                            longDecimal = "-" + longDecimal
                        }

                        hm.put("latitude",latDecimal)



                        println(latDecimal + " " + longDecimal)

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