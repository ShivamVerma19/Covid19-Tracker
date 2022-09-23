package com.example.covid19_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var covidAdapter: CovidAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        covidLv.addHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header , covidLv , false))


        fetchResults()
    }

    private fun fetchResults() {
        GlobalScope.launch() {
            val response  = withContext(Dispatchers.IO){Client.api.execute()}

            if(response.isSuccessful){
                val data = Gson().fromJson(response.body?.string() , Response::class.java)
                launch(Dispatchers.Main){
                    bindCombinedData(data.statewise[0])
                    setListView(data.statewise.subList(1,data.statewise.size))
                }

            }

        }
    }

    private fun setListView(subList: List<StatewiseItem>) {
        covidAdapter = CovidAdapter(subList)
        covidLv.adapter = covidAdapter
    }

    private fun bindCombinedData(obj : StatewiseItem) {
           val lastUpdatedTime = obj.lastupdatedtime
           val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
           lastUpdatedTv.text = "Last Updated \n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"


           confirmedTv.text = obj.confirmed
           activeTv.text = obj.active
           recoveredTv.text = obj.recovered
           deceasedTv.text = obj.deaths
    }


    fun getTimeAgo(past : Date) : String{

        val now = Date()

        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

        return when{
            seconds < 60 ->{
                "Few seconds ago"
            }

            minutes < 60 ->{
                "${minutes} minutes ago"
            }

            hours < 24 ->{
                "${hours} hours ago"
            }

            else ->{
                SimpleDateFormat("dd/MM/yy hh:mm a").format(past).toString()
            }
        }
    }
}
