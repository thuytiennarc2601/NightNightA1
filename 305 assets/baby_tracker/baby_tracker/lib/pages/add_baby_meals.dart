import 'package:baby_tracker/date_time_adapter.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';
import 'package:baby_tracker/pages/recipe_home.dart';

class AddBabyMeals extends StatefulWidget{
  const AddBabyMeals({Key? key}) : super(key: key);

  @override
  State<AddBabyMeals> createState() => _AddBabyMealsState();
}

class _AddBabyMealsState extends State<AddBabyMeals> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  Event event = Event();
  TextEditingController noteController = TextEditingController();

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
    });
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: [
          const PageTitle(title: 'BABY MEALS'),
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
          const ActionLabel(label: 'Choose a dish:'),
          SizedBox(
              width: 370,
              height: 45,
              child:  MaterialButton(//DISH-CHOOSING BUTTON
                onPressed: (){
                  Navigator.push(context,
                      MaterialPageRoute(
                          builder: (context) {
                            return const RecipeView(isViewing: false);
                          })).then((value) {
                    setState(() {
                      event.dish = value;
                    });
                  });
                },
                color: event.dish == null ? Colors.grey.shade400 : Colors.deepOrangeAccent.shade200,
                child: Text(
                  event.dish == null ? 'No selected dish' : event.dish!,
                  style: const TextStyle(color: Colors.white, fontSize: 20),
                ),
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
                        event.type = 'babymeals';
                        event.dish = event.dish == null ? 'No selected dish' : event.dish!;
                        event.date = dateFormat.format(dateTime);
                        event.time = DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute));
                        event.dateTime = '${event.date} ${event.time}';
                        event.id = '${event.dateTime}${event.type}';
                        event.note = noteController.text;
                        try {
                          Provider.of<EventModel>(context, listen: false).addEventToDB(event, event.id, dateFormat.format(DateTime.now()), 'all');
                          showDialog(context: context, builder: (BuildContext context){
                            return const SuccessAlertBox(title: 'Succeeded', content: 'The event is added successfully', actionContent: 'OK');
                          });
                          clearData();
                        } on PlatformException catch (e)
                        {
                          showDialog(context: context, builder: (BuildContext context){
                            return SuccessAlertBox(title: 'Failed', content: 'Cannot add the event; $e', actionContent: 'OK');
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