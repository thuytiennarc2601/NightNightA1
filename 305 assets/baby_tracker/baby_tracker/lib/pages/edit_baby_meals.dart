import 'package:baby_tracker/date_time_adapter.dart';
import 'package:baby_tracker/pages/recipe_home.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:baby_tracker/pages/edit_breast_feeding.dart';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';

class EditBabyMeals extends StatefulWidget{
  const EditBabyMeals({Key? key, required this.event, required this.type}) : super(key: key);

  final Event event;
  final String type;

  @override
  State<EditBabyMeals> createState() => _EditBabyMealsState();
}

class _EditBabyMealsState extends State<EditBabyMeals> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  TextEditingController noteController = TextEditingController();
  bool dateChanged = false;
  bool timeChanged = false;

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

    noteController.text = widget.event.note!;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    String usedDate = widget.event.date!;
    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const PageTitle(title: 'BABY MEAL DETAILS'),
            Container( //time-choosing title
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
            const ActionLabel(label: 'Choose a dish:'),
            SizedBox(
                width: 370,
                height: 45,
                child: MaterialButton( //DISH-CHOOSING BUTTON
                  onPressed: (){
                    Navigator.push(context,
                        MaterialPageRoute(
                            builder: (context) {
                              return const RecipeView(isViewing: false);
                            })).then((value) {
                      setState(() {
                        widget.event.dish = value;
                      });
                    });
                  },
                  color: Colors.deepOrangeAccent.shade200,
                  child: Text(
                    widget.event.dish!,
                    style: const TextStyle(color: Colors.white, fontSize: 20),
                  ),
                )
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
                          widget.event.type = 'babymeals';
                          widget.event.date = dateChanged ? dateFormat.format(dateTime) : widget.event.date;
                          widget.event.time = timeChanged ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute)) : widget.event.time;
                          widget.event.dateTime = '${widget.event.date} ${widget.event.time}';
                          widget.event.note = noteController.text;
                          try {
                            Provider.of<EventModel>(context, listen: false).addEventToDB(widget.event, widget.event.id, usedDate, widget.type);
                          } on PlatformException catch (e)
                          {
                            showDialog(context: context, builder: (BuildContext context){
                              return SuccessAlertBox(title: 'Failed', content: 'Cannot add the event; $e', actionContent: 'OK');
                            });
                          }
                          Navigator.pop(context);
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