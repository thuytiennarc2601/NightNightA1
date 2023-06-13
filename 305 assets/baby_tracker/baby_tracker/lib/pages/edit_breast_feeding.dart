import 'package:baby_tracker/date_time_adapter.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'dart:async';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:provider/provider.dart';

class EditBreastfeeding extends StatefulWidget{
  const EditBreastfeeding({Key? key, required this.event, required this.type }) : super(key: key);

  final Event event;
  final String type;

  @override
  State<EditBreastfeeding> createState() => _EditBreastfeedingState();
}

class _EditBreastfeedingState extends State<EditBreastfeeding> {

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

  bool timeChanged = false;
  bool dateChanged = false;
  bool updateList = true;

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
    if(widget.event.startSide == null){
      setState(() {
        widget.event.startSide = 'Left side';
      });
    }

    //set end side as left side if this button is pressed
    setState(() {
      widget.event.endSide = 'Left side';
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
    if(widget.event.startSide == null){
      setState(() {
        widget.event.startSide = 'Right side';
      });
    }
    //set end side as right side if the right timer is pressed
    setState(() {
      widget.event.endSide = 'Right side';
    });
  }

  //reset timers
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
        dateChanged = true;
      });
    });
  }

  void _showTimePicker()
  {
    showTimePicker(context: context, initialTime: TimeOfDay.now()).then((value) {
      setState(() {
        time = value!;
        timeChanged = true;
      });
    });

  }

  @override
  void initState(){
    noteController.text = widget.event.note!;
    durationOnLeft = Duration(seconds: widget.event.leftDuration);
    durationOnRight = Duration(seconds: widget.event.rightDuration);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    String usedDate = widget.event.date!;

    //get time strings for timer labels
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    final leftTimeString = dateTimeAdapter.formatDuration(durationOnLeft.inSeconds, false); //left timer
    final rightTimeString = dateTimeAdapter.formatDuration(durationOnRight.inSeconds, false); //right timer
    //total duration
    final totalSeconds = twoDigits((durationOnLeft.inSeconds + durationOnRight.inSeconds).remainder(60));
    final totalMinutes = twoDigits(((durationOnLeft.inSeconds + durationOnRight.inSeconds) ~/60).toInt());
    final totalHours = twoDigits(((durationOnLeft.inMinutes + durationOnRight.inMinutes) ~/60).toInt());

    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const PageTitle(title: 'BREASTFEEDING EVENT DETAILS'),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 15),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  const Text(
                    'Choose a start time: ',
                    style: TextStyle(color: Colors.black, fontSize: 16),
                  ),
                  MaterialButton( //DATE-CHOOSING BUTTON
                    onPressed: _showDatePicker,
                    color: Colors.white70,
                    child: Text(
                      dateChanged ? dateFormat.format(dateTime) : widget.event.date!,
                      style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                    ),
                  ),
                  MaterialButton( //TIME-CHOOSING BUTTON
                    onPressed: _showTimePicker,
                    color: Colors.white70,
                    child: Text(
                      timeChanged ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute)) : widget.event.time!,
                      style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                    ),
                  ),
                ],
              ),
            ),
            const ActionLabel(label: 'Press on a side to start recording'),
            Container( //start/stop buttons (left and right sides)
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
                      leftTimeString,
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
                      rightTimeString,
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
                  Container( //TOTAL DURATION (TIMER LABEL)
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
                  IconButton.filled( //time button
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
                      child: MaterialButton( //UPDATE BUTTON
                        onPressed: () { //get event data and update the database
                          widget.event.type = 'breastfeed';
                          widget.event.date = dateChanged ? dateFormat.format(dateTime) : widget.event.date;
                          widget.event.time = timeChanged
                              ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute))
                              : widget.event.time;
                          widget.event.dateTime = '${widget.event.date} ${widget.event.time}';
                          widget.event.leftDuration = durationOnLeft.inSeconds;
                          widget.event.rightDuration = durationOnRight.inSeconds;
                          widget.event.totalDuration = widget.event.leftDuration + widget.event.rightDuration;
                          widget.event.note = noteController.text;

                          try {
                            Provider.of<EventModel>(context, listen:false).addEventToDB(widget.event, widget.event.id, usedDate, widget.type);
                          } on FirebaseException catch (e)
                          {
                            String error = 'Cannot update the event: $e';
                            showDialog(context: context, builder: (BuildContext context){
                              return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                            });
                          }
                          Navigator.pop(context, updateList);
                        },
                        color: Colors.blueGrey.shade300,
                        child: const Text(
                          'Update record',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                        ),

                      )
                  ),
                  SizedBox(
                      width: 140,
                      height: 45,
                      child: MaterialButton( //DELETE BUTTON
                        onPressed: (){
                          showDialog(context: context, builder: (BuildContext context){
                            return DeleteAlertBox(title: 'Delete an event',
                                content: 'Are you sure to delete this event?',
                                actionConfirm: 'Delete',
                                actionCancel: 'Cancel',
                                id: widget.event.id!,
                                updateList: updateList,
                                usedDate: usedDate,
                                type: widget.type
                            );
                          });
                        },
                        color: Colors.red.shade300,
                        child: const Text(
                          'Delete',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                        ),

                      )
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class DeleteAlertBox extends StatelessWidget {
  const DeleteAlertBox({
    super.key, required this.title,
    required this.content,
    required this.actionConfirm,
    required this.actionCancel,
    required this.id,
    required this.updateList,
    required this.usedDate,
    required this.type
  });

  final String title;
  final String content;
  final String actionConfirm;
  final String actionCancel;
  final String id;
  final bool updateList;
  final String usedDate;
  final String type;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(title),
      content: Text(content),
      actions: <Widget>[
        MaterialButton(
          color: Colors.blueGrey.shade200,
          onPressed: () {
            // Perform the desired action
            Navigator.of(context).pop();
          },
          child: Text(actionCancel),
        ),
        MaterialButton(
          color: Colors.red,
          onPressed: (){
            Provider.of<EventModel>(context, listen:false).delete(id, usedDate, type);
            Navigator.of(context).pop();
            Navigator.pop(context, updateList);
            },
          child: Text(actionConfirm),
        ),
      ],
    );
  }
}