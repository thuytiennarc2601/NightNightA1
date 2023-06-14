package au.edu.utas.username.nightnight

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeAdapter
import au.edu.utas.username.nightnight.classes.Feed
import au.edu.utas.username.nightnight.databinding.ActivityAddBottleFeedingBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddBottleFeeding : AppCompatActivity() {

    private lateinit var ui: ActivityAddBottleFeedingBinding
    private val bottlefeed = Feed()
    private var editedFeed: Feed? = null
    private var lastEvent = Feed()
    lateinit var timeAdapter: TimeAdapter
    lateinit var dataStorer: DataStorer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddBottleFeedingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //get database
        val db = Firebase.firestore
        val bottlefeeds = db.collection("feed")

        //get bundle from the previous screen
        val bundleData = intent.extras

        //the activity can be twisted between 'adding' and 'editing' mode
        val isEditing = (bundleData?.getString("currentAction") == "editBottle") && (bundleData.getString("feedID") != "newFeed")
        val isAdding = (bundleData?.getString("currentAction") == "addBottle") && (bundleData.getString("feedID") == "newFeed")

        val timer = Timer()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val reverseDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        timeAdapter = TimeAdapter(applicationContext)
        dataStorer = DataStorer(applicationContext)


        ui.bBackButton.setOnClickListener {
            if(isAdding)
            {
                //send data back to the previous activity for running the general timer
                val bundle = Bundle()
                val activityIntent = Intent()
                bundle.putString("currentActivity", dataStorer.currentActivity())
                bundle.putBoolean("timerCounting", timeAdapter.timerCounting())
                bundle.putBoolean("secondTimerCounting", timeAdapter.secondTimerCounting())
                if (timeAdapter.startTime() != null) {
                    bundle.putString("startTime", timeAdapter.startTime().toString())
                }
                if (timeAdapter.secondStartTime() != null) {
                    bundle.putString("secondStartTime", timeAdapter.secondStartTime().toString())
                }
                if (timeAdapter.stopTime() != null) {
                    bundle.putString("stopTime", timeAdapter.stopTime().toString())
                }
                if (timeAdapter.secondStopTime() != null) {
                    bundle.putString("secondStopTime", timeAdapter.secondStopTime().toString())
                }
                setResult(RESULT_OK, activityIntent.putExtras(bundle))
            }
            if(isEditing || dataStorer.currentActivity() == "none")
            {
                //user must complete and save the new record before going back to the list
                clearData()
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
                dataStorer.setEventStartTime("00:00")
                dataStorer.setEventDescription("No description")
            }
            finish()
        }

        if(isAdding)
        {
            //set current time
            ui.bTimePicker.text = hourFormat.format(Date())
            dataStorer.setEventStartTime(hourFormat.format(Date()))
            dataStorer.setEventID("${dateFormat.format(Date())} ${ui.bTimePicker.text}")
        }
        else if(isEditing)
        {
            //retrieve the record's data for further update
            bottlefeeds.document("${bundleData?.getString("feedID")}")
                .get()
                .addOnCompleteListener { result ->
                    if(result.isSuccessful)
                    {
                        if(result.result.exists())
                        {
                            val document = result.result
                            editedFeed = document.toObject()
                            editedFeed?.id = document.id
                            ui.bTimePicker.text = editedFeed?.startTime
                            ui.bAmount.setText(editedFeed?.amount.toString())
                            ui.bNoteTxt.setText(editedFeed?.note)
                            ui.bTimer.setText(timeAdapter.timeStringFromLong(editedFeed?.totalDuration, false))
                            dataStorer.setEventID(editedFeed?.id)
                            dataStorer.setEventID(editedFeed?.id)
                            dataStorer.setEventStartTime(editedFeed?.startTime)
                        }
                    }
                }
        }

        //display the most recent bottlefeeding event
        bottlefeeds.orderBy("dateID", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                var get = false
                for (document in result)
                {
                    lastEvent = document.toObject()
                    if(lastEvent.type == "bottlefeed" && !get)
                    {
                        ui.bLastStartTime.text = lastEvent.dateID
                        ui.bLastAmount.text = getString(R.string.milk_amount, lastEvent.amount)
                        ui.bLastDuration.text = timeAdapter.timeStringFromLong(lastEvent.totalDuration, true)
                        get = true
                    }
                }
            }


        ui.bstartTimePicker.setOnClickListener {
            Dialogs().openTimePicker(this, ui.bTimePicker)
        }

        //when the start time is changed
        ui.bTimePicker.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(time: Editable?) {
                dataStorer.setEventStartTime(time.toString())
                if(isAdding) {
                    dataStorer.setEventID("${dateFormat.format(Date())} ${time.toString()}")
                }
            }
        })

        ui.bTimer.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if(!timeAdapter.timerCounting()) {
                    timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.bTimer.text.toString())))
                    timeAdapter.setStopTime(Date())
                }
            }

        })

        ui.bBottleTimer.setOnClickListener {
            startStopTimerCounting()
            dataStorer.setCurrentActivity("bottle")
        }

        ui.bRestartButton.setOnClickListener {
            resetTimer()
        }

        ui.bNoteTxt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(description: CharSequence?, p1: Int, p2: Int, p3: Int) {
                dataStorer.setEventDescription(description.toString())
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        ui.bClearButton.setOnClickListener{
            if(isAdding) {
                clearData()
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
                dataStorer.setEventStartTime("00:00")
                dataStorer.setEventDescription("No description")
            }
            if(isEditing)
            {
                clearData()
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
                dataStorer.setEventStartTime("00:00")
                dataStorer.setEventDescription("No description")
                val builder = AlertDialog.Builder(this)
                val displayer = LayoutInflater.from(this).inflate(R.layout.confirm_delete, null)
                builder.setView(displayer)
                builder.setCancelable(true)
                val dialog = builder.create()
                dialog.show()
                val yesButton = displayer.findViewById<Button>(R.id.yesButton)
                val noButton = displayer.findViewById<Button>(R.id.noButton)
                yesButton.setOnClickListener {
                    bottlefeeds.document("${bundleData?.getString("feedID")}")
                        .delete()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            finish()
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                            val builder2 = AlertDialog.Builder(this)
                            val displayer2 = LayoutInflater.from(this).inflate(R.layout.dialog_error, null)
                            val message = displayer2.findViewById<TextView>(R.id.errorText)
                            message.setText(R.string.cannot_delete)
                            builder2.setView(displayer2)
                            builder2.setCancelable(true)
                            builder2.create().show()
                        }
                }
                noButton.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        ui.bSaveButton.setOnClickListener{
            bottlefeed.startTime = ui.bTimePicker.text.toString()
            bottlefeed.totalDuration = timeAdapter.millisecondsFromString(ui.bTimer.text.toString())
            bottlefeed.id = "${dateFormat.format(Date())} ${ui.bTimePicker.text}"
            bottlefeed.date = reverseDateFormat.format(Date())
            bottlefeed.dateID = bottlefeed.id
            if(isEditing)
            {
                bottlefeed.id = editedFeed?.id
                bottlefeed.date = editedFeed?.date
                bottlefeed.dateID = editedFeed?.id?.substring(0, 11) + "${ui.bTimePicker.text}"
            }

            if(isNumber(ui.bAmount.text.toString()))
            {
                bottlefeed.amount = ui.bAmount.text.toString().toFloat()
            }
            else
            {
                bottlefeed.amount = 0.0f
            }
            bottlefeed.note = ui.bNoteTxt.text.toString()
            bottlefeed.type = "bottlefeed"

            bottlefeeds.document("${bottlefeed.id}")
                .get()
                .addOnCompleteListener { result ->
                    if(result.isSuccessful)
                    {
                        if(!result.result.exists() || (result.result.exists() && isEditing)) {
                            val loadingDialog = Dialogs().showDialog(this, R.layout.loading, false)
                            loadingDialog.show()
                            bottlefeeds.document("${bottlefeed.id}")
                                .set(bottlefeed)
                                .addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    clearData()
                                    dataStorer.setCurrentActivity("none")
                                    dataStorer.setEventID("defaultID")
                                    dataStorer.setEventStartTime("00:00")
                                    dataStorer.setEventDescription("No description")
                                    if(isAdding) {
                                        val intent = Intent(this, ViewLists::class.java)
                                        intent.putExtra("currentList", "feeding")
                                        startActivity(intent)
                                    }
                                    finish()
                                }
                                .addOnFailureListener {
                                    loadingDialog.dismiss()
                                    Dialogs().showDialog(this, R.layout.dialog_error, true).show()
                                }
                        }
                        else
                        {
                            val builder = AlertDialog.Builder(this)
                            val dialogError = LayoutInflater.from(this).inflate(R.layout.dialog_error, null)
                            val message = dialogError.findViewById<TextView>(R.id.errorText)
                            message.setText(R.string.meal_exist)
                            builder.setView(dialogError)
                            builder.setCancelable(true)
                            builder.create().show()
                        }
                    }
                }

        }
        //display inputs that the user already entered (when the activity restarts)
        if(dataStorer.eventStartTime() != "00:00" || dataStorer.eventStartTime() != "")
        {
            ui.bTimePicker.text = dataStorer.eventStartTime()
        }
        if(dataStorer.eventDescription() != "No description") ui.bNoteTxt.setText(dataStorer.eventDescription())
        if(timeAdapter.timerCounting())
        {
            startTimer()
        }
        else
        {
            stopTimer()
            if(timeAdapter.startTime() != null && timeAdapter.stopTime() != null)
            {
                val time = Date().time - timeAdapter.calcRestartTime(timeAdapter.startTime()!!.time, timeAdapter.stopTime()!!.time).time
                runOnUiThread { ui.bTimer.setText(timeAdapter.timeStringFromLong(time, false))}
            }
        }

        timer.scheduleAtFixedRate(TimeTask(), 0, 500)
    }

    private inner class TimeTask: TimerTask()
    {
        override fun run()
        {
            if(timeAdapter.timerCounting())
            {
                val time = (Date().time - timeAdapter.startTime()!!.time)
                runOnUiThread { ui.bTimer.setText(timeAdapter.timeStringFromLong(time, false)) }
            }
        }
    }


    private fun stopTimer() {
        timeAdapter.setTimerCounting(false)
        runOnUiThread {
            ui.bPlayIcon.setImageResource(R.drawable.play)
            ui.bBottleTimer.setBackgroundResource(R.drawable.rectangle_light_grey_border)
        }
    }

    private fun startTimer() {
        timeAdapter.setTimerCounting(true)
        runOnUiThread {
            ui.bPlayIcon.setImageResource(R.drawable.pause)
            ui.bBottleTimer.setBackgroundResource(R.drawable.rectangle_grey_border)
        }
    }

    private fun startStopTimerCounting() {
        //if timer is running
        if(timeAdapter.timerCounting())
        {
            timeAdapter.setStopTime(Date())
            stopTimer()
        }
        else
        {
            //if timer is paused
            if(timeAdapter.stopTime() != null)
            {
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.bTimer.text.toString())))
                timeAdapter.setStopTime(null)
            }
            //if timer is not running
            else
            {
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.bTimer.text.toString())))

            }
            startTimer()
        }
    }

    private fun resetTimer() {
        timeAdapter.setStartTime(null)
        timeAdapter.setStopTime(null)
        stopTimer()
        runOnUiThread {
            ui.bTimer.setText(timeAdapter.timeStringFromLong(0, false))
        }
    }
    private fun isNumber(string: String): Boolean {
        return when(string.toFloatOrNull())
        {
            null -> false
            else -> true
        }
    }

    private fun clearData()
    {
        ui.bTimePicker.setText(R.string.time_stamp)
        ui.bAmount.setText(R.string.amount_example)
        ui.bNoteTxt.setText(R.string.note_content)
        ui.bTimer.setText(R.string.timer_example)
        resetTimer()
        bottlefeed.clearData()
    }
}