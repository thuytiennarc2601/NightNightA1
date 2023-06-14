package au.edu.utas.username.nightnight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.viewpager.widget.ViewPager
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.adapter.ImageAdapter
import au.edu.utas.username.nightnight.adapter.TimeAdapter
import au.edu.utas.username.nightnight.adapter.ViewPagerAdapter
import au.edu.utas.username.nightnight.classes.Baby
import au.edu.utas.username.nightnight.databinding.ActivityMainBinding
import au.edu.utas.username.nightnight.fragments.Feeding
import au.edu.utas.username.nightnight.fragments.Sleep
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

//the total categories
const val TOTAL_ICON = 4

class MainActivity : AppCompatActivity(), Feeding.MyInterface, Sleep.MyInterface{

    private lateinit var ui: ActivityMainBinding
    lateinit var timeAdapter: TimeAdapter
    private lateinit var dataStorer: DataStorer
    private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == android.app.Activity.RESULT_OK)
        {
            val bundle = it.data?.extras
            val activity = bundle?.getString("currentActivity")
            val timerCounting = bundle!!.getBoolean("timerCounting", false)
            val secondTimerCounting = bundle.getBoolean("secondTimerCounting", false)
            val startTime = bundle.getString("startTime")
            val stopTime = bundle.getString("stopTime")
            val secondStartTime = bundle.getString("secondStartTime")
            val secondStopTime = bundle.getString("secondStopTime")
            setDataResult(activity, timerCounting, secondTimerCounting, startTime, stopTime, secondStartTime, secondStopTime)
        }
    }
    private var timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ui binding
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        timeAdapter = TimeAdapter(applicationContext)
        dataStorer = DataStorer(applicationContext)

        //get baby details from the database
        var baby = Baby()
        val db = Firebase.firestore
        val babyDetails = db.collection("baby")
        babyDetails
            .get()
            .addOnSuccessListener { result ->
                for(document in result)
                {
                    baby = document.toObject()
                    baby.id = document.id
                }

                ui.babyNameLabel.text = baby.babyName
                ui.babyDOBLabel.text = (baby.dateOfBirth)
                val imageAdapter = ImageAdapter()
                val imageReference = imageAdapter.downloadImage("${baby.image}")
                val oneMEGABYTE: Long = 1024 * 1024 * 16
                imageReference.getBytes(oneMEGABYTE).addOnSuccessListener { bytes ->
                    ui.babyPhotoImg.setImageBitmap(imageAdapter.byteArrayToBitmap(bytes))
                }

                //the current tab of the view pager (optionsContainer)
                var selectedTab = 0

                //the last tab chosen
                var lastTab = 0

                //list of category titles
                val titleList = mutableListOf<String>()
                titleList.add(0, ui.feedingIcon.contentDescription.toString())
                titleList.add(1, ui.nappyIcon.contentDescription.toString())
                titleList.add(2, ui.sleepIcon.contentDescription.toString())
                titleList.add(3, ui.activityIcon.contentDescription.toString())

                //list of icon
                val iconList = mutableListOf<ImageView>()
                iconList.add(0, ui.feedingIcon)
                iconList.add(1, ui.nappyIcon)
                iconList.add(2, ui.sleepIcon)
                iconList.add(3, ui.activityIcon)

                val viewPagerAdapter = ViewPagerAdapter(this, supportFragmentManager, TOTAL_ICON)
                ui.optionsContainer.adapter = viewPagerAdapter

                iconList[selectedTab].setBackgroundResource(R.drawable.rectangle_white_border)
                iconList[selectedTab].setPadding(10)

                //events happening when users click on an icon
                ui.feedingIcon.setOnClickListener{
                    if (selectedTab != 0)
                    {
                        selectedTab = 0
                        ui.optionsContainer.setCurrentItem(selectedTab,true)
                    }
                }

                ui.nappyIcon.setOnClickListener{
                    if (selectedTab != 1)
                    {
                        selectedTab = 1
                        ui.optionsContainer.setCurrentItem(selectedTab,true)
                    }
                }

                ui.sleepIcon.setOnClickListener{
                    if (selectedTab != 2)
                    {
                        selectedTab = 2
                        ui.optionsContainer.setCurrentItem(selectedTab, true)
                    }
                }

                ui.activityIcon.setOnClickListener{
                    if (selectedTab != 3)
                    {
                        selectedTab = 3
                        ui.optionsContainer.setCurrentItem(selectedTab, true)
                    }
                }

                //manage icons when users scroll tabs
                ui.optionsContainer.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {}
                    //set title for each tab
                    override fun onPageSelected(position: Int) {
                        //set the new title and appearance when an icon/tab is selected
                        selectedTab = position
                        ui.title.text = titleList[position]
                        iconList[position].setBackgroundResource(R.drawable.rectangle_white_border)
                        iconList[position].setPadding(10)

                        //undo the UI settings of the last selected icon
                        iconList[lastTab].setBackgroundColor(getColor(R.color.blue_primary))
                        iconList[lastTab].setPadding(0)
                        lastTab = position
                    }

                    override fun onPageScrollStateChanged(state: Int) {}
                })

                //navigate to the edit baby info view
                ui.babyInfo.setOnClickListener {
                    val editBabyInfo = Intent(this, BabyProfile::class.java)
                    startActivity(editBabyInfo)
                }

                ui.buttonEditDuration.setOnClickListener {
                    if(dataStorer.currentActivity() == "feeding")
                    {
                        val intent = Intent(this, AddBreastFeeding::class.java)
                        val bundle = Bundle()
                        bundle.putString("currentAction", "addBreast")
                        bundle.putString("feedID", "newFeed")
                        intent.putExtras(bundle)
                        launcher.launch(intent)
                    }

                    if(dataStorer.currentActivity() == "sleeping")
                    {
                        val intent = Intent(this, AddSleep::class.java)
                        val bundle = Bundle()
                        bundle.putString("currentAction", "addSleep")
                        bundle.putString("sleepID", "newSleep")
                        intent.putExtras(bundle)
                        launcher.launch(intent)
                    }

                    if(dataStorer.currentActivity() == "bottle")
                    {
                        val intent = Intent(this, AddBottleFeeding::class.java)
                        val bundle = Bundle()
                        bundle.putString("currentAction", "addBottle")
                        bundle.putString("feedID", "newFeed")
                        intent.putExtras(bundle)
                        launcher.launch(intent)
                    }
                }

                setTimer(dataStorer.currentActivity())
            }
    }

    override fun onResume() {
        super.onResume()
        setTimer(dataStorer.currentActivity())
    }

    override fun onValuePassed(
        activity: String?,
        timerCounting: Boolean,
        secondTimerCounting: Boolean,
        startTime: String?,
        stopTime: String?,
        secondStartTime: String?,
        secondStopTime: String?
    ) {
        setDataResult(activity, timerCounting, secondTimerCounting, startTime, stopTime, secondStartTime, secondStopTime)
    }

    private fun setDataResult(activity: String?, timerCounting: Boolean, secondTimerCounting: Boolean,
                              startTime: String?, stopTime: String?, secondStartTime: String?, secondStopTime: String?)
    {
        dataStorer.setCurrentActivity(activity)
        timeAdapter.setTimerCounting(timerCounting)
        timeAdapter.setSecondTimerCounting(secondTimerCounting)
        if(startTime != null) {timeAdapter.setStartTime(dateFormat.parse(startTime))}
        //Log.d(FIREBASE_TAG, "TEST: $startTime")
        if(stopTime != null) { timeAdapter.setStopTime(dateFormat.parse(stopTime)) }
        //Log.d(FIREBASE_TAG, "STOP")
        if(secondStartTime != null) { timeAdapter.setSecondStartTime(dateFormat.parse(secondStartTime)) }
        if(secondStopTime != null) { timeAdapter.setSecondStopTime((dateFormat.parse(secondStopTime))) }
    }

    private inner class TimeTask: TimerTask()
    {
        override fun run()
        {
            if(timeAdapter.timerCounting())
            {
                val time = Date().time - timeAdapter.startTime()!!.time
                runOnUiThread { ui.generalTimer.text = timeAdapter.timeStringFromLong(time, false) }
            }

            else if(timeAdapter.secondTimerCounting())
            {
                val time = Date().time - timeAdapter.secondStartTime()!!.time
                runOnUiThread { ui.generalTimer.text = timeAdapter.timeStringFromLong(time, false) }
            }
        }
    }

    private fun setTimer(activity: String?)
    {
        if(activity != "none")
        {
            when(activity)
            {
                "feeding" -> {runOnUiThread {
                    ui.actionLabel.setText(R.string.eating)
                }}
                "sleeping" -> { runOnUiThread {
                    ui.actionLabel.setText(R.string.sleeping)
                }}
                "bottle" -> { runOnUiThread {
                    ui.actionLabel.setText(R.string.bottleFeeding)
                }}
            }

            runOnUiThread {
                ui.actionLabel.setTextColor(getColor(R.color.blue_secondary))
                ui.buttonEditDuration.setBackgroundColor(getColor(R.color.yellow_primary))
            }

            if(timeAdapter.timerCounting() || timeAdapter.secondTimerCounting())
            {
                timer.scheduleAtFixedRate(TimeTask(), 0, 500)
            }
            else if(!timeAdapter.timerCounting() || !timeAdapter.secondTimerCounting())
            {
                if(timeAdapter.secondStartTime() != null && timeAdapter.secondStopTime() != null)
                {
                    val time = Date().time - timeAdapter.calcRestartTime(timeAdapter.secondStartTime()!!.time, timeAdapter.secondStopTime()!!.time).time
                    runOnUiThread {ui.generalTimer.text = timeAdapter.timeStringFromLong(time, false)}
                }

                if(timeAdapter.startTime() != null && timeAdapter.stopTime() != null)
                {
                    val time = Date().time - timeAdapter.calcRestartTime(timeAdapter.startTime()!!.time, timeAdapter.stopTime()!!.time).time
                    runOnUiThread { ui.generalTimer.text = timeAdapter.timeStringFromLong(time, false)}
                }
            }
        }
        else
        {
            runOnUiThread {
                ui.actionLabel.setText(R.string.free)
                ui.actionLabel.setTextColor(getColor(R.color.grey_secondary))
                ui.buttonEditDuration.setBackgroundColor(getColor(R.color.grey_primary))
                ui.generalTimer.text = timeAdapter.timeStringFromLong(0, false)
            }
        }
    }
}