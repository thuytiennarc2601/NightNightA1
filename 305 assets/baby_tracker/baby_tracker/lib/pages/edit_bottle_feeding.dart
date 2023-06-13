import 'package:baby_tracker/date_time_adapter.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:baby_tracker/pages/edit_breast_feeding.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'dart:async';
import 'package:baby_tracker/color_collections.dart';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';

class EditBottlefeeding extends StatefulWidget{
  const EditBottlefeeding({Key? key, required this.event, required this.type}) : super(key: key);

  final Event event;
  final String type;

  @override
  State<EditBottlefeeding> createState() => _EditBottlefeedingState();
}

class _EditBottlefeedingState extends State<EditBottlefeeding> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  Duration duration = const Duration();
  Timer? timer;
  var timerIsRunning = false;
  TextEditingController noteController = TextEditingController();
  TextEditingController amountController = TextEditingController();
  bool timeChanged = false;
  bool dateChanged = false;

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
    //stop the timer if it is running
    if (timerIsRunning) {
      setState(() {
        timerIsRunning = false;
      });
      timer?.cancel();
    }
    //if not start timer
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
  void initState() {
    // TODO: implement initState
    duration = Duration(seconds: widget.event.totalDuration);
    noteController.text = widget.event.note!;
    amountController.text = widget.event.milkAmount.toString();
    super.initState();
  }

  bool isDouble(String value) {
    final doubleValue = double.tryParse(value);
    return doubleValue != null;
  }

  @override
  Widget build(BuildContext context) {
    //get values for the timer label
    String timeString = dateTimeAdapter.formatDuration(duration.inSeconds, false);
    String usedDate = widget.event.date!;

    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const PageTitle(title: 'BOTTLE FEEDING EVENT DETAIL'),
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
                      dateChanged ? dateFormat.format(dateTime) : widget.event.date!,
                      style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                    ),
                  ),
                  MaterialButton( //TIME BUTTON
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
            const ActionLabel(label: 'Enter milk amount(ml):'),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: TextField(
                controller: amountController,
                decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: '180.0'
                ),
              ),
            ),
            const ActionLabel(label: 'Start recording the duration:'),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 15),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  Container( //TIMER LABEL
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
                      timeString,
                      style: const TextStyle(fontSize: 20, color: Colors.white),
                    ),
                  ),
                  ElevatedButton( //START/STOP BUTTON
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
                        onPressed: () {
                          widget.event.type = 'bottle feed';
                          widget.event.date = dateChanged ? dateFormat.format(dateTime) : widget.event.date;
                          widget.event.time = timeChanged
                              ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute))
                              : widget.event.time;
                          widget.event.dateTime = '${widget.event.date} ${widget.event.time}';
                          widget.event.totalDuration = duration.inSeconds;
                          widget.event.note = noteController.text;
                          if(isDouble(amountController.text)){
                            widget.event.milkAmount = double.tryParse(amountController.text)!;
                            try {
                              Provider.of<EventModel>(context, listen: false).addEventToDB(widget.event, widget.event.id, usedDate, widget.type);
                            } on PlatformException catch (e)
                            {
                              String error= 'Cannot update the event: $e';
                              showDialog(context: context, builder: (BuildContext context){
                                return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                              });
                            }
                            Navigator.pop(context, true);
                          }
                          else{
                            showDialog(context: context, builder: (BuildContext context){
                              return const SuccessAlertBox(title: 'Failed', content: 'Invalid amount', actionContent: 'OK');
                            });
                          }
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
                      child: MaterialButton( //CLEAR BUTTON
                        onPressed: (){
                          showDialog(context: context, builder: (BuildContext context){
                            return DeleteAlertBox(title: 'Delete an event',
                                content: 'Are you sure to delete this event?',
                                actionConfirm: 'Delete',
                                actionCancel: 'Cancel',
                                id: widget.event.id!,
                                updateList: true,
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