package com.example.sailboatapp.presentation.network

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectionStateViewModel : ViewModel() {



    private val _connectionState = MutableLiveData<ConnectionState>().apply {
        value = ConnectionState.Loading
    }

    val connectionState: MutableLiveData<ConnectionState>
        get() = _connectionState

    fun setConnectionState(state : ConnectionState) {
        _connectionState.value = state
    }

}