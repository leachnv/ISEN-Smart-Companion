package fr.isen.chanvillard.isensmartcompanion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object to manage Retrofit instance
object RetrofitInstance {
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/") // Base URL
            .addConverterFactory(GsonConverterFactory.create()) // Gson converter for JSON parsing
            .build()
    }

    // RetrofitService interface instance for making the API calls
    val retrofitService: EventApiService by lazy {
        retrofit.create(EventApiService::class.java)
    }
}

