package com.example.sailboatapp.presentation.network

import androidx.activity.viewModels
import com.example.sailboatapp.presentation.ui.screen.LocalViewModel
import com.example.sailboatapp.presentation.ui.screen.RemoteViewModel



object InstantiateViewModel {
    private var localViewModel: LocalViewModel? = null
    private var remoteViewModel: RemoteViewModel? = null

    fun instantiateLocalViewModel(): LocalViewModel {
        if(localViewModel == null){
                localViewModel = LocalViewModel()
        }
        return localViewModel as LocalViewModel
    }

    fun instantiateRemoteViewModel(): RemoteViewModel {
        if(remoteViewModel == null){
            remoteViewModel = RemoteViewModel()
        }
        return remoteViewModel as RemoteViewModel

    }




}