import 'package:baby_tracker/date_time_adapter.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'dart:async';
import 'package:baby_tracker/color_collections.dart';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';

class AddSleep extends StatefulWidget{
  const AddSleep({Key? key}) : super(key: key);

  @override
  State<AddSleep> createState() => _AddSleepState();
}

class _AddSleepState extends State<AddSleep> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  Duration duration = const Duration();
  Timer? timer;
  var timerIsRunning = false;
  Event event = Event();
  TextEditingController noteController = TextEditingController();

  void addTime()
  {
    const addSeconds = 1;
    setState(() {
      final seconds = duration.inSeconds + addSeconds;
      duration = Duration(seconds: seconds);
    });
  }

  void startRecording()
  {
    //stop the timer if is running
    if (timerIsRunning) {
      setState(() {
        timerIsRunning = false;
      });
      timer?.cancel();
    }
    //start it if it is not
    else {
      timer = Timer.periodic(const Duration(seconds: 1), (_) => addTime());
      setState(() {
        timerIsRunning = true;
      });
    }
  }

  //reset the timer
  void reset()
  {
    setState(() {
      duration = const Duration();
      timerIsRunning = false;
    });
    timer?.cancel();
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
    final minutes = twoDigits(duration.inMinutes.remainder(60));
    final seconds = twoDigits(duration.inSeconds.remainder(60));
    final hours = twoDigits(duration.inHours);

    return SingleChildScrollView(
      child: Column(
        children: [
          const PageTitle(title: 'SLEEPING'),
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
          const ActionLabel(label: 'Start recording the duration:'),
          Container( //total duration
            padding: const EdgeInsets.symmetric(horizontal: 15),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Container( //TIME LABEL
                  width: 210,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                      border: Border.all(
                          color: timerIsRunning ? CustomColors.primaryYellow : Colors.grey.shade300,
                          width: 2.0
                      ),
                      color: timerIsRunning ? CustomColors.primaryYellow : Colors.grey.shade300,
                      borderRadius: BorderRadius.circular(8)
                  ),
                  child: Text(
                    '$hours:$minutes:$seconds',
                    style: const TextStyle(fontSize: 20, color: Colors.white),
                  ),
                ),
                ElevatedButton( //START-RECORDING BUTTON
                  onPressed: startRecording,
                  child: timerIsRunning ? const Icon(Icons.pause) : const Icon(Icons.play_arrow),
                ),
                ElevatedButton( //RESTART BUTTON
                  onPressed: reset,
                  child: const Icon(Icons.restart_alt),
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
                    child: MaterialButton( //SAVE BUTTON
                      onPressed: (){
                        event.type = 'sleep';
                        event.date = dateFormat.format(dateTime);
                        event.time = DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute));
                        event.dateTime = '${event.date} ${event.time}';
                        event.id = '${event.dateTime}${event.type}';
                        event.totalDuration = duration.inSeconds;
                        event.note = noteController.text;
                        try {
                          Provider.of<EventModel>(context, listen:false).addEventToDB(event, event.id, dateFormat.format(DateTime.now()), 'all');
                          showDialog(context: context, builder: (BuildContext context){
                            return const SuccessAlertBox(title: 'Succeeded', content: 'The event is added successfully', actionContent: 'OK');
                          });
                          clearData();
                          Provider.of<EventModel>(context, listen:false).update();
                        } on PlatformException catch (e)
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
                    child: MaterialButton( //CLEAR BUTTON
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

