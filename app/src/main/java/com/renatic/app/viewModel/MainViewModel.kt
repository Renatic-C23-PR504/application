package com.renatic.app.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatic.app.api.ApiConfig
import com.renatic.app.response.PatientItem
import com.renatic.app.response.PatientRequest
import com.renatic.app.response.PatientResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(context: Context): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _patientList = MutableLiveData<List<PatientItem>?>()
    val patientList: LiveData<List<PatientItem>?> = _patientList

    fun getListOfPatient(context: Context) {
        _isLoading.value = true
        val token = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE).getString("token", "")
        runBlocking {
            launch {
                val response = ApiConfig.getApiService(token.toString()).getAllPatient()
                val listOfPatients = response.data
                if (listOfPatients != null) {
                    _isLoading.value = false
                    _patientList.value = listOfPatients
                }
            }
        }
    }

    fun searchPatient(context: Context, bpjs: String) {
        _isLoading.value = true
        val token = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE).getString("token","")
        val request = PatientRequest(bpjs)
        val call = ApiConfig.getApiService(token.toString()).getPatient(request)
        call.enqueue(object: Callback<PatientResponse> {
            override fun onResponse(
                call: Call<PatientResponse>,
                response: Response<PatientResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error.toBooleanStrict()) {
                        val listOfPatients = responseBody.data
                        _patientList.value = listOfPatients
                        Log.d(TAG, "onResponse: Pasien berhasil ditemukan")
                    } else {
                        Log.d(TAG, "onResponse: Pasien tidak ditemukan")
                    }
                } else {
                    Log.d(TAG, "onResponse: Pasien tidak ditemukan")
                }
            }

            override fun onFailure(call: Call<PatientResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: Pasien tidak ditemukan")
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}