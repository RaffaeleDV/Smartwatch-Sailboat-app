/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.sailboatapp.presentation


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sailboatapp.presentation.network.ServerManager
import com.example.sailboatapp.presentation.network.connectionState
import com.example.sailboatapp.presentation.ui.SailboatApp
import com.example.sailboatapp.presentation.ui.screen.LOG_ENABLED
import com.example.sailboatapp.presentation.ui.screen.LocalViewModel
import com.example.sailboatapp.presentation.ui.screen.RemoteViewModel
import com.example.sailboatapp.presentation.ui.theme.SailboatappTheme


var orange = 0xFFFDE293
var red = 0xFFFF723A

class MainActivity : ComponentActivity() {

    private lateinit var serverManager : ServerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        serverManager = ServerManager(this)
        if(LOG_ENABLED) Log.d("DEBUG","startServerCheck")
        serverManager.startServerCheck()


        //setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            SailboatappTheme{
                SailboatApp(this)

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serverManager.stopServerCheck()
    }
}

