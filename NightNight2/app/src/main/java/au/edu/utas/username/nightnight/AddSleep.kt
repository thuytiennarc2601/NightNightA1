package au.edu.utas.username.nightnight

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeAdapter
import au.edu.utas.username.nightnight.classes.Sleep
import au.edu.utas.username.nightnight.databinding.ActivityAddSleepBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddSleep : AppCompatActivity() {

    private lateinit var ui: ActivityAddSleepBinding
    lateinit var timeAdapter: TimeAdapter
    lateinit var dataStorer: DataStorer

    private val timer = Timer()
    private val sleep = Sleep()
    private var editedSleep: Sleep? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddSleepBinding.inflate(layoutInflater)
        setContentView(ui.root)

        var dateFromID = ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val reverseDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        //get firebase and corresponding collection
        val db = Firebase.firestore
        val sleeps = db.collection("sleep")

        //get bundle from the first screens
        val bundleData = intent.extras

        //the activity can be twisted between 'adding' and 'editing' mode
        val isAdding = bundleData?.getString("currentAction") == "addSleep" && bundleData.getString("sleepID") == "newSleep"
        val isEditing = bundleData?.getString("currentAction") == "editSleep" && bundleData.getString("sleepID") != "newSleep"

        timeAdapter = TimeAdapter(applicationContext)
        dataStorer = DataStorer(applicationContext)

        ui.sBackButton.setOnClickListener {
            if(isAdding) {
                //send data back to MainActivity and Sleep Fragment
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
                dataStorer.setEventDescription("No description")
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
            }
            finish()
        }

        //if a new sleep period is adding, set the date is at the current time
        if(isAdding)
        {
            ui.sTimePickerStart.text = timeFormat.format(Date())
            dataStorer.setEventStartTime(timeFormat.format(Date()))
            dataStorer.setEventID("${dateFormat.format(Date())} ${ui.sTimePickerStart.text}")
        }
        else if(isEditing) //set the record on the UI
        {
            Log.d(FIREBASE_TAG, "${bundleData?.getString("sleepID")}")
            sleeps.document("${bundleData?.getString("sleepID")}")
                .get()
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        Log.d(FIREBASE_TAG, "Yeah1: ${bundleData?.getString("sleepID")}")
                        if (result.result.exists()) {
                            Log.d(FIREBASE_TAG, "Yeah2: ${bundleData?.getString("sleepID")}")
                            val document = result.result
                            editedSleep = document.toObject()
                            editedSleep?.id = document.id
                            Log.d(FIREBASE_TAG, "TEsting: ${editedSleep?.id}")
                            ui.sTimePickerStart.text = editedSleep?.startTime.toString()
                            ui.sTimer.setText(
                                timeAdapter.timeStringFromLong(
                                    editedSleep?.duration,
                                    false
                                )
                            )
                            ui.sNoteTxt.setText(editedSleep?.note)
                            dataStorer.setEventID(editedSleep?.id)
                            dateFromID = editedSleep?.id.toString().substring(0,10)
                            dataStorer.setEventID(editedSleep?.id)
                            dataStorer.setEventStartTime(editedSleep?.startTime)
                        }
                    }
                }
        }

        ui.sStartTimePicker.setOnClickListener {
            Dialogs().openTimePicker(this, ui.sTimePickerStart)
        }

        //when the start time is changed
        ui.sTimePickerStart.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(time: Editable?) {
                dataStorer.setEventStartTime(time.toString())
                if(isAdding) {
                    dataStorer.setEventID("${dateFormat.format(Date())} ${time.toString()}")
                }
            }
        })

        ui.sNoteTxt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(description: CharSequence?, p1: Int, p2: Int, p3: Int) {
                dataStorer.setEventDescription(description.toString())
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        ui.sSleepTimer.setOnClickListener {
            startStopTimerCounting()
            dataStorer.setCurrentActivity("sleeping")
        }

        ui.sTimer.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if(!timeAdapter.timerCounting()) {
                    timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.sTimer.text.toString())))
                    timeAdapter.setStopTime(Date())
                }
            }
        })

        ui.sRestartButton.setOnClickListener {
            resetTimer()
        }

        ui.sClearButton.setOnClickListener {
            if(isAdding) {
                clearData()
                dataStorer.setEventStartTime("00:00")
                dataStorer.setEventDescription("No description")
                dataStorer.setCurrentActivity("none")
                dataStorer.setEventID("defaultID")
            }
            else if(isEditing){
                clearData()
                dataStorer.setEventStartTime("00:00")
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
                    db.collection("sleep").document("${bundleData?.getString("sleepID")}")
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

        ui.sSaveButton.setOnClickListener {
            //add data to Firebase if the timer is stopped
                sleep.startTime = ui.sTimePickerStart.text.toString()
                sleep.note = ui.sNoteTxt.text.toString()
                sleep.id = dataStorer.eventID()
                sleep.dateID = sleep.id
                sleep.date = reverseDateFormat.format(Date())
                sleep.duration = timeAdapter.millisecondsFromString(ui.sTimer.text.toString())
                if(isEditing){
                    sleep.id = editedSleep?.id
                    sleep.date = editedSleep?.date
                    sleep.dateID = "$dateFromID ${sleep.startTime}"
                }
                val loadingDialog = Dialogs().showDialog(this, R.layout.loading, false)
                loadingDialog.show()
                sleeps.document("${sleep.id}")
                    .set(sleep)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        clearData()
                        dataStorer.setEventStartTime("00:00")
                        dataStorer.setEventDescription("No description")
                        dataStorer.setCurrentActivity("none")
                        dataStorer.setEventID("defaultID")
                        if(isAdding) {
                            val intent = Intent(this, ViewLists::class.java)
                            intent.putExtra("currentList", "sleep")
                            startActivity(intent)
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        loadingDialog.dismiss()
                        Dialogs().showDialog(this, R.layout.dialog_error, true).show()
                    }

        }

        //display inputs that the user already entered (when the activity restarts)
        if(dataStorer.eventStartTime() != "00:00" || dataStorer.eventStartTime() != "")
        {
            ui.sTimePickerStart.text = dataStorer.eventStartTime()
        }
        if(dataStorer.eventDescription() != "No description") ui.sNoteTxt.setText(dataStorer.eventDescription())
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
                runOnUiThread { ui.sTimer.setText(timeAdapter.timeStringFromLong(time, false))}
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
                runOnUiThread { ui.sTimer.setText(timeAdapter.timeStringFromLong(time, false)) }
            }
        }
    }

    private fun startTimer() {
        timeAdapter.setTimerCounting(true)
        runOnUiThread {
            ui.sPlayIcon.setImageResource(R.drawable.pause)
            ui.sSleepTimer.setBackgroundResource(R.drawable.rectangle_grey_border)
        }
    }

    private fun stopTimer() {
        timeAdapter.setTimerCounting(false)
        runOnUiThread {
            ui.sPlayIcon.setImageResource(R.drawable.play)
            ui.sSleepTimer.setBackgroundResource(R.drawable.rectangle_light_grey_border)
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
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.sTimer.text.toString())))
                timeAdapter.setStopTime(null)
            }
            //if timer is not running
            else
            {
                timeAdapter.setStartTime(Date(System.currentTimeMillis() - timeAdapter.millisecondsFromString(ui.sTimer.text.toString())))

            }
            startTimer()
        }
    }

    private fun clearData()
    {
        sleep.clearData()
        ui.sTimePickerStart.setText(R.string.time_stamp)
        ui.sNoteTxt.setText(R.string.note_content)
        resetTimer()
    }

    private fun resetTimer() {
        timeAdapter.setStartTime(null)
        timeAdapter.setStopTime(null)
        stopTimer()
        runOnUiThread {
            ui.sTimer.setText(timeAdapter.timeStringFromLong(0, false))
        }
    }
}