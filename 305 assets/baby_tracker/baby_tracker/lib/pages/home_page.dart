import 'package:baby_tracker/date_time_adapter.dart';
import 'package:baby_tracker/pages/edit_bottle_feeding.dart';
import 'package:baby_tracker/pages/edit_breast_feeding.dart';
import 'package:baby_tracker/pages/edit_baby_meals.dart';
import 'package:baby_tracker/pages/edit_nappy.dart';
import 'package:baby_tracker/pages/edit_sleep.dart';
import 'package:baby_tracker/pages/meal_summary.dart';
import 'package:baby_tracker/pages/nappy_summary.dart';
import 'package:baby_tracker/pages/recipe_home.dart';
import 'package:baby_tracker/pages/sleep_summary.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:intl/intl.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:baby_tracker/event.dart';
import 'package:provider/provider.dart';

class Home extends StatefulWidget{
  const Home({Key? key}) : super(key: key);

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  String selectedList = 'all';
  DateTime dateTime = DateTime.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  List<Event> events = [];

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Consumer<EventModel>(
      builder: (context, eventModel, _)
      {
        return Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
                colors: [
                  Colors.blueGrey.shade300,
                  CustomColors.primaryYellow,
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter
            ),
          ),
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children:[
                  Container(
                    padding: const EdgeInsets.only(top: 10, bottom: 5),
                    child: SegmentedButton<String>(
                      segments: const <ButtonSegment<String>>[
                        ButtonSegment<String>(
                            value: 'all',
                            label: Text('All'),
                            icon: Icon(FontAwesomeIcons.list)),
                        ButtonSegment<String>(
                            value: 'meal',
                            label: Text('Meals'),
                            icon: Icon(FontAwesomeIcons.utensils)),
                        ButtonSegment<String>(
                            value: 'nappy',
                            label: Text('Nappies'),
                            icon: Icon(FontAwesomeIcons.poo)),
                        ButtonSegment<String>(
                            value: 'sleep',
                            label: Text('Sleeps'),
                            icon: Icon(FontAwesomeIcons.bed)),
                      ],
                      selected: <String>{selectedList},
                      onSelectionChanged: (Set<String> newSelection) async {
                        setState(() {
                          selectedList = newSelection.first;
                        });
                        await eventModel.fetch(dateFormat.format(dateTime), selectedList);
                      },
                    ),
                  ),
                ],
              ),
              Container( //time-choosing title
                padding: const EdgeInsets.only(bottom: 5),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton( //date button
                      onPressed: (){
                        showDatePicker(context: context, initialDate: dateTime, firstDate: DateTime(2000), lastDate: DateTime(2025)).then((value) async {
                          setState(() {
                            dateTime = value!;
                          });
                          await eventModel.fetch(dateFormat.format(dateTime), selectedList);
                        });
                      },
                      style: const ButtonStyle(backgroundColor: MyColor()),
                      child: Text(
                        dateFormat.format(dateTime),
                        style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                      ),
                    ),
                    ElevatedButton( //date button
                      onPressed: selectedList != 'all' ? (){
                        Navigator.push(context, MaterialPageRoute(builder: (context) {
                          switch (selectedList){
                            case 'nappy': return NappySummary(events: eventModel.events);
                            case 'sleep': return SleepSummary(events: eventModel.events);
                            default: return MealSummary(events: eventModel.events);
                          }
                        }));
                      } : null,
                      style: ElevatedButton.styleFrom(backgroundColor: const MyColor(), elevation: selectedList != 'all' ? 1 : 0),
                      child: const Text(
                        'Summary',
                        style: TextStyle(color: Colors.blueGrey, fontSize: 16),
                      ),
                    ),
                    ElevatedButton( //date button
                      onPressed: (){Navigator.push(context, MaterialPageRoute(builder: (context) {return const RecipeView(isViewing: true);}));},
                      style: ElevatedButton.styleFrom(backgroundColor: const MyColor(), elevation: selectedList != 'all' ? 1 : 0),
                      child: const Text(
                        'Recipes',
                        style: TextStyle(color: Colors.blueGrey, fontSize: 16),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                  height: 450,
                  width: 370,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(10),
                    color: Colors.white.withOpacity(0.8),
                  ),
                  padding: const EdgeInsets.only(bottom: 5),
                  child: eventModel.loading ? const Center(child: SizedBox(height: 80, width: 80, child: CircularProgressIndicator(),))
                      : eventModel.events.isEmpty
                      ? const Center(child: Text('No activity found'))
                      : ListView.builder(
                      itemBuilder: (_, index) {
                        var event = eventModel.events[index];
                        return ListTile(
                          contentPadding: const EdgeInsets.symmetric(vertical: 5, horizontal: 15),
                          title:Text(event.time!, style: const TextStyle(fontWeight: FontWeight.w700, color: Colors.blueGrey)),
                          subtitle: Text(displayContent(event.type!, event)),
                          leading: Image.asset(displayImage(event.type!)),
                          //added this line, this should be familiar from last week:
                          onTap: () {
                            Navigator.push(context, MaterialPageRoute(builder: (context) {
                              switch (event.type){
                                case 'breastfeed': return EditBreastfeeding(event: event, type: selectedList);
                                case 'babymeals': return EditBabyMeals(event: event, type: selectedList);
                                case 'nappy': return EditNappy(event: event, type: selectedList);
                                case 'sleep': return EditSleep(event: event, type: selectedList);
                                default: return EditBottlefeeding(event: event, type: selectedList);
                              }
                            }));
                          },
                        );
                      },
                      itemCount: eventModel.events.length
                  )
              ),
            ],
          ),
        );
      }
    );
  }



  String displayImage(String type) {
    String imageName = '';
    switch (type){
      case 'breastfeed': imageName = 'lib/assets/images/breastfeeding.png';
      case 'bottle feed': imageName = 'lib/assets/images/milk.png';
      case 'babymeals': imageName = 'lib/assets/images/baby-food.png';
      case 'nappy': imageName = 'lib/assets/images/nappy.png';
      case 'sleep': imageName = 'lib/assets/images/sleeping.png';
    }
    return imageName;
  }

  String displayContent(String type, Event event){
    String content = '';
    switch (type){
      case 'breastfeed': content = 'Start: ${event.startSide}\nLeft: ${dateTimeAdapter.formatDuration(event.leftDuration, true)} | Right: ${dateTimeAdapter.formatDuration(event.rightDuration, true)}';
      case 'bottle feed': content = 'Amount: ${event.milkAmount.toString()}ml\nDuration: ${dateTimeAdapter.formatDuration(event.totalDuration, true)}';
      case 'babymeals': content = 'Dish: ${event.dish}';
      case 'nappy': content = event.condition!.toUpperCase();
      case 'sleep': content = 'Duration: ${dateTimeAdapter.formatDuration(event.totalDuration, true)}';
    }
    return content;
  }
}

class MyColor extends MaterialStateColor {
  const MyColor() : super(_defaultColor);

  static const int _defaultColor = 0xFFFFFFFF;
  static const int _pressedColor = 0xFFFFFFFF;

  @override
  Color resolve(Set<MaterialState> states) {
    if (states.contains(MaterialState.pressed)) {
      return const Color(_pressedColor);
    }
    return const Color(_defaultColor);
  }
}