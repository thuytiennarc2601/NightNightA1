package au.edu.utas.username.nightnight

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeAdapter
import au.edu.utas.username.nightnight.classes.Feed
import au.edu.utas.username.nightnight.databinding.ActivityAddBreastFeedingBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddBreastFeeding : AppCompatActivity() {

    private lateinit var ui: ActivityAddBreastFeedingBinding
    lateinit var timeAdapter: TimeAdapter
    lateinit var dataStorer: DataStorer
    private val timer = Timer()
    private val timer2 = Timer()
    private val breastfeed = Feed()
    private var lastEvent = Feed()
    private var editedBreastFeed: Feed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddBreastFeedingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        var dateFromID = ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val reverseDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        timeAdapter = TimeAdapter(applicationContext)
        dataStorer = DataStorer(applicationContext)

        //get firebase
        val db = Firebase.firestore
        val breastfeeds = db.collection("feed")

        val bundleData = intent.extras
        //the activity can be twisted between 'adding' and 'editing' mode
        val isAdding = bundleData?.getString("currentAction") == "addBreast" && bundleData.getString("feedID") == "newFeed"
        val isEditing = bundleData?.getString("currentAction") == "editBreast" && bundleData.getString("feedID") != "newFeed"

        //when back button is pressed
        ui.bfBackButton.setOnClickListener{
            if(isAdding) {
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
            else if(isEditing || dataStorer.currentActivity() == "none")
            {
                clearData()
                dataStorer.setEventStartTime("00:00")
                dataStorer.setBreastFeedStartSide("none")
                dataStorer.setBreastFeedEndSide("none")
                dataStorer.setEventDescription("No description")
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
            }
            finish()
        }

        if(isAdding)
        {
            //set the current time
            ui.brfTimePicker.text = hourFormat.format(Date())
            dataStorer.setEventStartTime(hourFormat.format(Date()))
            dataStorer.setEventID("${dateFormat.format(Date())} ${ui.brfTimePicker.text}")
        }
        else if(isEditing)
        {
            breastfeeds.document("${bundleData?.getString("feedID")}")
                .get()
                .addOnCompleteListener { result ->
                    if(result.isSuccessful)
                    {
                        if(result.result.exists())
                        {
                            //get the record's data for further update
                            val document = result.result
                            editedBreastFeed = document.toObject()
                            editedBreastFeed?.id = document.id
                            ui.brfTimePicker.text = editedBreastFeed?.startTime
                            ui.brfLeftTimer.setText(timeAdapter.timeStringFromLong(editedBreastFeed?.leftDuration, false))
                            ui.brfRightTimer.setText(timeAdapter.timeStringFromLong(editedBreastFeed?.rightDuration, false))
                            ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(editedBreastFeed?.totalDuration, false)
                            ui.brfNoteTxt.setText(editedBreastFeed?.note)
                            dateFromID = editedBreastFeed?.id.toString().substring(0,10)
                            dataStorer.setEventID(editedBreastFeed?.id)
                            dataStorer.setEventStartTime(editedBreastFeed?.startTime)
                            dataStorer.setBreastFeedStartSide(editedBreastFeed?.startSide)
                            dataStorer.setBreastFeedEndSide(editedBreastFeed?.endSide)
                        }
                    }
                }
        }

        //displays the most recent breastfeeding event's details
        breastfeeds
            .orderBy("dateID", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                var get = false
                for (document in result)
                {
                    lastEvent = document.toObject()
                    if(lastEvent.type == "breastfeed" && !get) {
                        ui.brfLastStartTime.text = lastEvent.dateID
                        ui.brfLastFeedingSide.text = lastEvent.startSide
                        ui.brfEndFeedingSide.text = lastEvent.endSide
                        val leftDuration =
                            timeAdapter.timeStringFromLong(lastEvent.leftDuration, true)
                        val rightDuration =
                            timeAdapter.timeStringFromLong(lastEvent.rightDuration, true)
                        val totalDuration =
                            timeAdapter.timeStringFromLong(lastEvent.totalDuration, true)
                        val lastTime = "$totalDuration (L: $leftDuration, R: $rightDuration)"
                        ui.brfLastDuration.text = lastTime
                        get = true
                    }
                }
            }

        //start choosing a start time for the event
        ui.startTimePicker.setOnClickListener {
            Dialogs().openTimePicker(this, ui.brfTimePicker)
        }

        //when user completes choosing the start time
        ui.brfTimePicker.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            //store that start time (to share preference and the object)
            override fun afterTextChanged(time: Editable?) {
                dataStorer.setEventStartTime(time.toString())
                if(isAdding)
                {
                    dataStorer.setEventID("${dateFormat.format(Date())} ${time.toString()}")
                }
            }
        })

        //when the left timer is pressed
        ui.timerLeft.setOnClickListener {
            //store the start side
            if (dataStorer.breastFeedStartSide() == "none")
            {
                dataStorer.setBreastFeedStartSide("left side")
            }

            //choose the end side
            dataStorer.setBreastFeedEndSide("left side")
            dataStorer.setCurrentActivity("feeding")

            startStopTimerCounting()
        }

        //when the right timer is pressed
        ui.timerRight.setOnClickListener {
            if (dataStorer.breastFeedStartSide() == "none")
            {
                dataStorer.setBreastFeedStartSide("right side")
            }
            dataStorer.setBreastFeedEndSide("right side")
            dataStorer.setCurrentActivity("feeding")

            startStopSecondTimerCounting()
        }

        ui.brfLeftTimer.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                dataStorer.setBreastFeedEndSide("left side")
                if(!timeAdapter.timerCounting()) {
                    timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(p0.toString())))
                    timeAdapter.setStopTime(Date())
                    timeAdapter.setLeftDuration(timeAdapter.millisecondsFromString(p0.toString()))
                    dataStorer.setCurrentActivity("feeding")
                    ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(
                        timeAdapter.leftDuration() + timeAdapter.rightDuration(),
                        false)
                }
                if (dataStorer.breastFeedStartSide() == "none")
                {
                    dataStorer.setBreastFeedStartSide("left side")
                }
            }
        })

        ui.brfRightTimer.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                dataStorer.setBreastFeedEndSide("right side")
                if(!timeAdapter.secondTimerCounting()) {
                    timeAdapter.setSecondStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(p0.toString())))
                    timeAdapter.setSecondStopTime(Date())
                    timeAdapter.setRightDuration(timeAdapter.millisecondsFromString(p0.toString()))
                    dataStorer.setCurrentActivity("feeding")
                    ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(
                        timeAdapter.leftDuration() + timeAdapter.rightDuration(),
                        false)
                }
                if (dataStorer.breastFeedStartSide() == "none")
                {
                    dataStorer.setBreastFeedStartSide("right side")
                }
            }
        })

        //when the user chooses to reset the time
        ui.brfRestartButton.setOnClickListener {
            resetTimer()
        }

        //when the Note text is changed
        ui.brfNoteTxt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            //store it to the share preference
            override fun onTextChanged(description: CharSequence?, p1: Int, p2: Int, p3: Int) {
                dataStorer.setEventDescription(description.toString())
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        //when the user choose to clear all data
        ui.brfClearButton.setOnClickListener{
            if(isAdding) {
                clearData()
                dataStorer.setEventStartTime("00:00")
                dataStorer.setBreastFeedStartSide("none")
                dataStorer.setBreastFeedEndSide("none")
                dataStorer.setEventDescription("No description")
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
            }
            else if(isEditing){
                clearData()
                dataStorer.setEventStartTime("00:00")
                dataStorer.setBreastFeedStartSide("none")
                dataStorer.setBreastFeedEndSide("none")
                dataStorer.setEventDescription("No description")
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
                val builder = AlertDialog.Builder(this)
                val displayer = LayoutInflater.from(this).inflate(R.layout.confirm_delete, null)
                builder.setView(displayer)
                builder.setCancelable(true)
                val dialog = builder.create()
                dialog.show()
                val yesButton = displayer.findViewById<Button>(R.id.yesButton)
                val noButton = displayer.findViewById<Button>(R.id.noButton)
                yesButton.setOnClickListener {
                    db.collection("feed").document("${bundleData?.getString("feedID")}")
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

        ui.brfSaveButton.setOnClickListener{
            //save record to Firebase when both timers are stopped
                breastfeed.id = dataStorer.eventID()
                breastfeed.dateID = breastfeed.id
                breastfeed.startTime = ui.brfTimePicker.text.toString()
                breastfeed.startSide = dataStorer.breastFeedStartSide()
                breastfeed.endSide = dataStorer.breastFeedEndSide()
                breastfeed.leftDuration = timeAdapter.leftDuration()
                breastfeed.rightDuration = timeAdapter.rightDuration()
                breastfeed.totalDuration = breastfeed.leftDuration + breastfeed.rightDuration
                breastfeed.note = ui.brfNoteTxt.text.toString()
                breastfeed.type = "breastfeed"
                breastfeed.date = reverseDateFormat.format(Date())
                if(isEditing){
                    breastfeed.date = editedBreastFeed?.date
                    breastfeed.id = editedBreastFeed?.id
                    breastfeed.dateID = "$dateFromID ${breastfeed.startTime}"
                }

                breastfeeds.document("${breastfeed.id}")
                    .get()
                    .addOnCompleteListener { result ->
                        if(result.isSuccessful)
                        {
                            if(!result.result.exists() || (result.result.exists() && isEditing))
                            {
                                val loadingDialog = Dialogs().showDialog(this, R.layout.loading, false)
                                loadingDialog.show()
                                breastfeeds.document("${breastfeed.id}")
                                .set(breastfeed)
                                .addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    clearData()
                                    dataStorer.setEventStartTime("00:00")
                                    dataStorer.setBreastFeedStartSide("none")
                                    dataStorer.setBreastFeedEndSide("none")
                                    dataStorer.setEventDescription("No description")
                                    dataStorer.setCurrentActivity("none")
                                    dataStorer.setEventID("defaultID")
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
                                builder.setView(dialogError)
                                builder.setCancelable(true)
                                val dialog = builder.create()
                                message.setText(R.string.meal_exist)
                                dialog.show()
                            }
                        }
                    }
        }

        //displays inputs that the user currently choose and resume timers when the activity restarts
        ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(timeAdapter.leftDuration() + timeAdapter.rightDuration(), false)
        if(dataStorer.eventStartTime() != "00:00" || dataStorer.eventStartTime() != "")
        {
            ui.brfTimePicker.text = dataStorer.eventStartTime()
        }
        if(dataStorer.eventDescription() != "No description") ui.brfNoteTxt.setText(dataStorer.eventDescription())
        if(timeAdapter.timerCounting())
        {
            startTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
        }
        else
        {
            stopTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
            if(timeAdapter.startTime() != null && timeAdapter.stopTime() != null)
            {
                val time = Date().time - timeAdapter.calcRestartTime(timeAdapter.startTime()!!.time, timeAdapter.stopTime()!!.time).time
                runOnUiThread { ui.brfLeftTimer.setText(timeAdapter.timeStringFromLong(time, false))}
            }
        }

        if(timeAdapter.secondTimerCounting())
        {
            startTimer("right", ui.brfRightPlayIcon, ui.timerRight)
        }
        else
        {
            stopTimer("right", ui.brfRightPlayIcon, ui.timerRight)
            if(timeAdapter.secondStartTime() != null && timeAdapter.secondStopTime() != null)
            {
                val time = Date().time - timeAdapter.calcRestartTime(timeAdapter.secondStartTime()!!.time, timeAdapter.secondStopTime()!!.time).time
                runOnUiThread {ui.brfRightTimer.setText(timeAdapter.timeStringFromLong(time, false))}
            }
        }

        timer.scheduleAtFixedRate(TimeTask(), 0, 500)
        timer2.scheduleAtFixedRate(TimeTask2(), 0, 500)
    }

    private inner class TimeTask: TimerTask()
    {
        override fun run()
        {
            if(timeAdapter.timerCounting())
            {
                val time = Date().time - timeAdapter.startTime()!!.time
                runOnUiThread { ui.brfLeftTimer.setText(timeAdapter.timeStringFromLong(time, false)) }
            }
        }
    }

    private inner class TimeTask2: TimerTask()
    {
        override fun run()
        {
            if(timeAdapter.secondTimerCounting())
            {
                val time = Date().time - timeAdapter.secondStartTime()!!.time
                runOnUiThread { ui.brfRightTimer.setText(timeAdapter.timeStringFromLong(time, false)) }
            }
        }
    }

    private fun clearData()
    {
        ui.brfTimePicker.setText(R.string.time_stamp)
        ui.brfNoteTxt.setText(R.string.note_content)
        breastfeed.clearData()
        resetTimer()
    }

    private fun resetTimer() {
        timeAdapter.setStartTime(null)
        timeAdapter.setStopTime(null)
        timeAdapter.setSecondStartTime(null)
        timeAdapter.setSecondStopTime(null)
        timeAdapter.setLeftDuration(0)
        timeAdapter.setRightDuration(0)
        stopTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
        stopTimer("right", ui.brfRightPlayIcon, ui.timerRight)
        runOnUiThread {
            ui.brfLeftTimer.setText(timeAdapter.timeStringFromLong(0, false))
            ui.brfRightTimer.setText(timeAdapter.timeStringFromLong(0, false))
            ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(0, false)
        }
    }

    private fun stopTimer(side: String, icon: ImageView, timer: MaterialToolbar)
    {
        if(side == "left") timeAdapter.setTimerCounting(false)
        if(side == "right") timeAdapter.setSecondTimerCounting(false)
        runOnUiThread {
            icon.setImageResource(R.drawable.play)
            timer.setBackgroundResource(R.drawable.rectangle_light_grey_border)
        }
    }

    private fun startTimer(side: String, icon: ImageView, timer: MaterialToolbar)
    {
        if(side == "left") timeAdapter.setTimerCounting(true)
        if(side == "right") timeAdapter.setSecondTimerCounting(true)
        runOnUiThread {
            icon.setImageResource(R.drawable.pause)
            timer.setBackgroundResource(R.drawable.rectangle_grey_border)
        }
    }

    private fun startStopTimerCounting()
    {
        if(timeAdapter.timerCounting())
        {
            timeAdapter.setStopTime(Date())
            stopTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
            timeAdapter.setLeftDuration(timeAdapter.millisecondsFromString(ui.brfLeftTimer.text.toString()))
            runOnUiThread { ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(timeAdapter.leftDuration() + timeAdapter.rightDuration(), false) }
        }
        else
        {
            if(timeAdapter.stopTime() != null)
            {
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.brfLeftTimer.text.toString())))
                timeAdapter.setStopTime(null)

            }
            else
            {
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.brfLeftTimer.text.toString())))
            }
            if(timeAdapter.secondTimerCounting())
            {
                timeAdapter.setSecondStopTime(Date())
                stopTimer("right", ui.brfRightPlayIcon, ui.timerRight)
                timeAdapter.setRightDuration(timeAdapter.millisecondsFromString(ui.brfRightTimer.text.toString()))
                runOnUiThread { ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(timeAdapter.leftDuration() + timeAdapter.rightDuration(), false) }
            }
            startTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
        }
    }

    private fun startStopSecondTimerCounting() {
        if (timeAdapter.secondTimerCounting()) {
            timeAdapter.setSecondStopTime(Date())
            stopTimer("right", ui.brfRightPlayIcon, ui.timerRight)
            timeAdapter.setRightDuration(timeAdapter.millisecondsFromString(ui.brfRightTimer.text.toString()))
            runOnUiThread {
                ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(
                    timeAdapter.leftDuration() + timeAdapter.rightDuration(),
                    false
                )
            }
        } else {
            if (timeAdapter.secondStopTime() != null) {
                timeAdapter.setSecondStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.brfRightTimer.text.toString())))
                timeAdapter.setSecondStopTime(null)
            } else {
                timeAdapter.setSecondStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.brfRightTimer.text.toString())))
            }
            if (timeAdapter.timerCounting()) {
                timeAdapter.setStopTime(Date())
                stopTimer("left", ui.brfLeftPlayIcon, ui.timerLeft)
                timeAdapter.setLeftDuration(timeAdapter.millisecondsFromString(ui.brfLeftTimer.text.toString()))
                runOnUiThread {
                    ui.brfTotalDurationValue.text = timeAdapter.timeStringFromLong(
                        timeAdapter.leftDuration() + timeAdapter.rightDuration(),
                        false
                    )
                }
            }
            startTimer("right", ui.brfRightPlayIcon, ui.timerRight)
        }
    }
}