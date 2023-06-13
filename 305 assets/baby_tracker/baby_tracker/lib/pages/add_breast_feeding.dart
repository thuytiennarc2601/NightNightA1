import 'package:baby_tracker/date_time_adapter.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'dart:async';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';

class AddBreastfeeding extends StatefulWidget{
  const AddBreastfeeding({Key? key}) : super(key: key);

  @override
  State<AddBreastfeeding> createState() => _AddBreastfeedingState();
}

class _AddBreastfeedingState extends State<AddBreastfeeding> {

  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  Duration durationOnLeft = const Duration();
  Duration durationOnRight = const Duration();
  Timer? timerLeft;
  Timer? timerRight;
  var leftTimerIsRunning = false;
  var rightTimerIsRunning = false;
  Event event = Event();
  TextEditingController noteController = TextEditingController();

  void addTime()
  {
    const addSeconds = 1;

    if(leftTimerIsRunning) {
      setState(() {
        final seconds = durationOnLeft.inSeconds + addSeconds;
        durationOnLeft = Duration(seconds: seconds);
      });
    }
    else if(rightTimerIsRunning){
      setState(() {
        final seconds = durationOnRight.inSeconds + addSeconds;
        durationOnRight = Duration(seconds: seconds);
      });
    }
  }

  void startRecordingOnLeft()
  {
    //stop right timer if it is running
    if(rightTimerIsRunning){
      setState(() {
        rightTimerIsRunning = false;
      });
      timerRight?.cancel();
    }

    //runs the left timer
    if (leftTimerIsRunning) {
      setState(() {
        leftTimerIsRunning = false;
      });
      timerLeft?.cancel();
    }
    else {
      timerLeft = Timer.periodic(const Duration(seconds: 1), (_) => addTime());
      setState(() {
        leftTimerIsRunning = true;
      });
    }
    //set start side as left side if this button is pressed first
    if(event.startSide == null){
      setState(() {
        event.startSide = 'Left side';
      });
    }

    //set end side as left side if this button is pressed
    setState(() {
      event.endSide = 'Left side';
    });
  }

  void startRecordingOnRight()
  {
    //stop the left timer if is it running
    if(leftTimerIsRunning){
      setState(() {
        leftTimerIsRunning = false;
      });
      timerLeft?.cancel();
    }
    //runs the right timer
    if (rightTimerIsRunning) {
      setState(() {
        rightTimerIsRunning = false;
      });
      timerRight?.cancel();
    }
    else {
      timerRight = Timer.periodic(const Duration(seconds: 1), (_) => addTime());
      setState(() {
        rightTimerIsRunning = true;
      });
    }

    //set start side as right side if the right timer is pressed first
    if(event.startSide == null){
      setState(() {
        event.startSide = 'Right side';
      });
    }
    //set end side as right side if the right timer is pressed
    setState(() {
      event.endSide = 'Right side';
    });
  }

  //reset timer
  void reset()
  {
    setState(() {
      durationOnLeft = const Duration();
      durationOnRight = const Duration();
      leftTimerIsRunning = false;
      rightTimerIsRunning = false;
    });
    timerLeft?.cancel();
    timerRight?.cancel();
  }

  void _showDatePicker()
  {
    showDatePicker(context: context, initialDate: dateTime, firstDate: DateTime(2000), lastDate: DateTime(2025)).then((value) {
      setState(() {
        dateTime = value!;
      });
    });
  }

  void _showTimePicker()
  {
    showTimePicker(context: context, initialTime: TimeOfDay.now()).then((value) {
      setState(() {
        time = value!;
      });
    });
  }

  //clear all fields
  void clearData()
  {
    setState(() {
      dateTime = DateTime.now();
      time = TimeOfDay.now();
      noteController.text = '';
      event = Event();
      reset();
    });
  }

  @override
  Widget build(BuildContext context) {
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    //get values for the left timer
    final minutesLeft = twoDigits(durationOnLeft.inMinutes.remainder(60));
    final secondsLeft = twoDigits(durationOnLeft.inSeconds.remainder(60));
    final hoursLeft = twoDigits(durationOnLeft.inHours);
    //get values for the right timer
    final minutesRight = twoDigits(durationOnRight.inMinutes.remainder(60));
    final secondsRight = twoDigits(durationOnRight.inSeconds.remainder(60));
    final hoursRight = twoDigits(durationOnRight.inHours);
    //get values for total duration
    final totalSeconds = twoDigits((durationOnLeft.inSeconds + durationOnRight.inSeconds).remainder(60));
    final totalMinutes = twoDigits(((durationOnLeft.inSeconds + durationOnRight.inSeconds) ~/60).toInt());
    final totalHours = twoDigits(((durationOnLeft.inMinutes + durationOnRight.inMinutes) ~/60).toInt());

    return SingleChildScrollView(
      child: Column(
        children: [
          const PageTitle(title: 'BREASTFEEDING'),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 15),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                const Text(
                  'Choose a start time: ',
                  style: TextStyle(color: Colors.black, fontSize: 16),
                ),
                MaterialButton( //DATE BUTTON
                  onPressed: _showDatePicker,
                  color: Colors.white70,
                  child: Text(
                    dateFormat.format(dateTime),
                    style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                  ),
                ),
                MaterialButton( //TIME BUTTON
                  onPressed: _showTimePicker,
                  color: Colors.white70,
                  child: Text(
                    DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute)),
                    style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                  ),
                ),
              ],
            ),
          ),
          const ActionLabel(label: 'Press on a side to start recording'),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 15),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                SizedBox(
                  width: 183,
                  child: ElevatedButton.icon( //LEFT START/STOP BUTTON
                    onPressed: startRecordingOnLeft,
                    style: const ButtonStyle(alignment: Alignment.center),
                    icon: Icon(leftTimerIsRunning ? Icons.pause : Icons.play_arrow),
                    label: const Text(
                      'Left side',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    ),
                  )
                ),
                SizedBox(
                  width: 183,
                    child: ElevatedButton.icon( //LEFT START/STOP BUTTON
                      onPressed: startRecordingOnRight,
                      style: const ButtonStyle(alignment: Alignment.center),
                      icon: Icon(rightTimerIsRunning ? Icons.pause : Icons.play_arrow),
                      label: const Text(
                        'Right side',
                        style: TextStyle(color: Colors.white, fontSize: 16),
                      ),
                    )
                ),
              ],
            ),
          ),
          Container( //TIME DISPLAYED ON BOTH SIDES
            padding: const EdgeInsets.symmetric(horizontal: 15),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Container( //LEFT TIMER LABEL
                  width: 183,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                      border: Border.all(
                          color: leftTimerIsRunning ? Colors.blueGrey : Colors.blueGrey.shade200,
                          width: 2.0
                      ),
                      borderRadius: BorderRadius.circular(8)
                  ),
                  child: Text(
                    '$hoursLeft:$minutesLeft:$secondsLeft',
                    style: const TextStyle(fontSize: 20),
                  ),
                ),
                Container( //RIGHT TIMER LABEL
                  width: 183,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                      border: Border.all(
                        color: rightTimerIsRunning ? Colors.blueGrey : Colors.blueGrey.shade200,
                        width: 2.0
                      ),
                      borderRadius: BorderRadius.circular(8)
                  ),
                  child: Text(
                    '$hoursRight:$minutesRight:$secondsRight',
                    style: const TextStyle(fontSize: 20),
                  ),
                ),
              ],
            ),
          ),
          Container( //TOTAL DURATION
            padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 10),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                const Text(
                  'Total duration:',
                  style: TextStyle(color: Colors.black, fontSize: 16),
                ),
                Container(
                  width: 210,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                      border: Border.all(
                        color: Colors.grey.shade400,
                        width: 2.0
                      ),
                      color: Colors.grey.shade400,
                      borderRadius: BorderRadius.circular(8)
                  ),
                  child: Text(
                    '$totalHours:$totalMinutes:$totalSeconds',
                    style: const TextStyle(fontSize: 20, color: Colors.white),
                  ),
                ),
                IconButton.filled( //TIMER RESTART BUTTON
                  onPressed: reset,
                  color: Colors.blueGrey,
                  icon: const Icon(Icons.restart_alt)
                ),
              ],
            ),
          ),
          const ActionLabel(label: 'Note:'),
          Container( //NOTE BOX
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: TextField(
              controller: noteController,
              maxLines: null,
              textInputAction: TextInputAction.newline,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                hintText: 'Describe the event...'
              ),
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 20),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                SizedBox(
                    width: 170,
                    height: 45,
                    child: MaterialButton( //SAVE RECORD
                      onPressed: () {
                        event.type = 'breastfeed';
                        event.date = dateFormat.format(dateTime);
                        event.time = DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute));
                        event.dateTime = '${event.date} ${event.time}';
                        event.id = '${event.dateTime}${event.type}';
                        event.leftDuration = durationOnLeft.inSeconds;
                        event.rightDuration = durationOnRight.inSeconds;
                        event.totalDuration = event.leftDuration + event.rightDuration;
                        event.note = noteController.text;
                        try {
                          Provider.of<EventModel>(context, listen:false).addEventToDB(event, event.id, dateFormat.format(DateTime.now()), 'all');
                          showDialog(context: context, builder: (BuildContext context){
                            return const SuccessAlertBox(title: 'Succeeded', content: 'The event is added successfully', actionContent: 'OK');
                          });
                          clearData();
                        } on FirebaseException catch (e)
                        {
                          String error = 'Cannot add the event: $e';
                          showDialog(context: context, builder: (BuildContext context){
                            return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                          });
                        }
                      },
                      color: Colors.blueGrey.shade300,
                      child: const Text(
                        'Save record',
                        style: TextStyle(color: Colors.white, fontSize: 16),
                      ),

                    )
                ),
                SizedBox(
                  width: 140,
                  height: 45,
                  child: MaterialButton( //date button
                    onPressed: clearData,
                    color: Colors.red.shade300,
                    child: const Text(
                      'Clear all',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    ),

                  )
                )
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class SuccessAlertBox extends StatelessWidget {
  const SuccessAlertBox({
    super.key, required this.title, required this.content, required this.actionContent,
  });

  final String title;
  final String content;
  final String actionContent;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(title),
      content: Text(content),
      actions: <Widget>[
        TextButton(
          child: Text(actionContent),
          onPressed: () {
            // Perform the desired action
            Navigator.of(context).pop();
          },
        ),
      ],
    );
  }
}

class ActionLabel extends StatelessWidget {
  const ActionLabel({
    super.key, required this.label,
  });

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container( //recording label
      alignment: Alignment.centerLeft,
      padding: const EdgeInsets.symmetric(horizontal: 21, vertical: 10),
      child: Text(
        label,
        style: const TextStyle(
          color: Colors.black,
          fontSize: 16,
        ),
      ),
    );
  }
}

class PageTitle extends StatelessWidget {
  const PageTitle({
    super.key, required this.title,
  });

  final String title;

  @override
  Widget build(BuildContext context) {
    return Container( //page title
      alignment: Alignment.center,
      padding: const EdgeInsets.all(10),
      child: Text(
        title,
        style: const TextStyle(
          color: Colors.grey,
          fontSize: 20,
        ),
      ),
    );
  }
}