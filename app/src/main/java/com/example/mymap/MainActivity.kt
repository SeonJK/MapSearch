package com.example.mymap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymap.adapter.LocationSearchAdapter
import com.example.mymap.api.POIService
import com.example.mymap.databinding.ActivityMainBinding
import com.example.mymap.model.Place
import com.example.mymap.model.SearchResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    val TAG: String = "로그"

    companion object {
        private const val BASE_URL = "https://dapi.kakao.com"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var service: POIService
    private lateinit var adapter: LocationSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initRetrofit()

        searchLocation()

//        getAppKeyHash()
    }

    private fun initRecyclerView() {
        adapter = LocationSearchAdapter { bindMapActivity(it) }
        val layoutManager = LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(POIService::class.java)
    }

    private fun searchLocation() {
        binding.searchButton.setOnClickListener {
            // Retrofit 통신
            service.searchKeyword(key = getString(R.string.rest_api_key), query = binding.searchEditText.text.toString())
                .enqueue(object : Callback<SearchResult> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<SearchResult>,
                        response: Response<SearchResult>,
                    ) {
                        if (response.isSuccessful.not()) {
                            // 응답코드가 있는 실패에 대한 예외처리
                            Log.d(TAG, "onResponse() called :: NOT SUCCESS!!")
                            return
                        }

                        // 검색결과 텍스트 설정
                        binding.resultCountTextView.visibility = View.VISIBLE
                        val totalCount = response.body()?.meta?.totalCount
                        binding.resultCountTextView.text =
                            "총 ${totalCount}개의 검색 결과가 있습니다."

                        adapter.submitList(response.body()?.place.orEmpty())


                    }

                    // 인터넷 장애로 인한 통신실패일 경우
                    override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                        Log.d(TAG, "apiCallback - onFailure() called :: ${t.message.toString()}")
                    }

                })
        }
    }

    private fun bindMapActivity(place: Place) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("place", place)
        startActivity(intent)
    }

//    private fun getAppKeyHash() {
//        try {
//            val info =
//                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                var md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val something = String(Base64.encode(md.digest(), 0))
//                Log.e("Hash key", something)
//            }
//        } catch (e: Exception) {
//
//            Log.e("name not found", e.toString())
//        }
//    }
}
