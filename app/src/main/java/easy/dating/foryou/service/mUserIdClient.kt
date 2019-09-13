package easy.dating.foryou.service

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class mUserIdClient {
    private val builder = Retrofit
        .Builder()
        .baseUrl("https://www.uuidgenerator.net/api/")
        .addConverterFactory(ScalarsConverterFactory.create())

    private val retrofit: Retrofit by lazy {
        builder.build()
    }

    private val client: UserIdClient by lazy {
        retrofit.create(UserIdClient::class.java)
    }

    fun build() = client
}