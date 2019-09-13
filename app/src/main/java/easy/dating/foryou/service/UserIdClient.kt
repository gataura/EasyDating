package easy.dating.foryou.service


import retrofit2.Call
import retrofit2.http.*



interface UserIdClient {

    @GET("guid")
    fun generateId(
    ): Call<String>

}