import 'package:baby_tracker/date_time_adapter.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:share/share.dart';

class MealSummary extends StatefulWidget {
  const MealSummary({Key? key, required this.events}) : super(key: key);

  final List<Event> events;

  @override
  State<MealSummary> createState() => _MealSummaryState();
}

class DishTime{
  String dish;
  String? time;

  DishTime({ required this.dish, required this.time});
}

class _MealSummaryState extends State<MealSummary> {

  String breastfeedTimeStr = '';
  String bottlefeedTimeStr = '';
  String babyMealTimeStr = '';
  String breadfeedLeftDurationStr = '';
  String breastfeedRightDurationStr = '';
  String breastfeedTotalDurationStr = '';
  String amountStr = '';
  String bottlefeedTotalDurationStr = '';
  String date = '';

  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  List<DishTime> eatenDishes = [];

  @override
  void initState() {
    // TODO: implement initState
    int breastfeedTime = 0;
    int bottlefeedTime = 0;
    int babyMealTime = 0;
    int bfLeftDur = 0;
    int bfRightDur = 0;
    int bfTotalDur = 0;
    double amount = 0.0;
    int bTotalDur = 0;

    for(var event in widget.events){
      switch(event.type){
        case 'breastfeed':
          breastfeedTime++;
          bfLeftDur += event.leftDuration;
          bfRightDur += event.rightDuration;
          bfTotalDur += event.totalDuration;
        case 'bottle feed':
          bottlefeedTime++;
          amount += event.milkAmount;
          bTotalDur += event.totalDuration;
        case 'babymeals':
          babyMealTime++;
          DishTime dish = DishTime(dish: event.dish!, time: event.time!);
          eatenDishes.add(dish);
      }
      date = event.date!;
    }

    breastfeedTimeStr = breastfeedTime.toString();
    bottlefeedTimeStr = bottlefeedTime.toString();
    babyMealTimeStr = babyMealTime.toString();
    breadfeedLeftDurationStr = dateTimeAdapter.formatDuration(bfLeftDur, true);
    breastfeedRightDurationStr = dateTimeAdapter.formatDuration(bfRightDur, true);
    breastfeedTotalDurationStr = dateTimeAdapter.formatDuration(bfTotalDur, true);
    amountStr = '$amount ml';
    bottlefeedTotalDurationStr = dateTimeAdapter.formatDuration(bTotalDur, true);

    super.initState();
  }

  String getHistory(){
    String text = '';
    text = 'History of meals on $date:\n'
        'BREASTFEEDING\n'
        'Total breastfeedings: $breastfeedTimeStr\n'
        'Total left duration: $breadfeedLeftDurationStr\n'
        'Total right duration: $breastfeedRightDurationStr\n'
        'Total duration: $breastfeedTotalDurationStr\n\n'
        'BOTTLE FEEDING\n'
        'Total bottle feedings: $bottlefeedTimeStr\n'
        'Total amount: $amountStr\n'
        'Total duration: $bottlefeedTotalDurationStr\n\n'
        'BABY MEALS\n'
        'Total baby meals: $babyMealTimeStr\n\n';

    int i = 0;
    for(var event in widget.events){
      i++;
      switch (event.type) {
        case 'breastfeed':
          text = '$text$i/ Breastfeeding ${event.date} ${event.time}\n'
              'Start side: ${event.startSide}, end side: ${event.endSide}\n'
              'Left duration: ${dateTimeAdapter.formatDuration(
              event.leftDuration, true)}\n'
              'Right duration: ${dateTimeAdapter.formatDuration(
              event.rightDuration, true)}\n'
              'Total duration: ${dateTimeAdapter.formatDuration(
              event.totalDuration, true)}\n'
              'Note: ${event.note}\n\n';

        case 'bottle feed':
          text = '$text$i/ Bottle feeding: ${event.date} ${event.time} ${event.type}\n'
              'Amount: ${event.milkAmount}\n'
              'Duration: ${dateTimeAdapter.formatDuration(
              event.totalDuration, true)}\n'
              'Note: ${event.note}\n\n';

        case 'babymeals':
          text = '$text$i/ Baby meal: ${event.date} ${event.time} ${event.type}\n'
              'Dish: ${event.dish}\n'
              'Note: ${event.note}\n\n';
      }
    }
    return text;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: Container(
        width: MediaQuery.of(context).size.width,
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
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            Container(
              alignment: Alignment.center,
              height: 50,
              width: MediaQuery.of(context).size.width,
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.7),
              ),
              child: const Text(
                'History of meals',
                style: TextStyle(fontSize: 20, color: Colors.blueGrey, fontWeight: FontWeight.w700),
              ),
            ),
            Container(
              height: 150,
              width: MediaQuery.of(context).size.width - 30,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                color: Colors.white.withOpacity(0.7),
              ),
              child: Row(
                children: [
                  Container(
                    margin: const EdgeInsets.only(left: 10),
                    height: 48,
                    width: 48,
                    child: Image.asset('lib/assets/images/breastfeeding.png'),
                  ),
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 10),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'BREASTFEEDING',
                          style: TextStyle(fontSize: 17, color: Colors.deepOrangeAccent),
                        ),
                        Text(
                          'Total feeding times: $breastfeedTimeStr',
                        ),
                        Text(
                          'Total left duration: $breadfeedLeftDurationStr',
                        ),
                        Text(
                          'Total right duration: $breastfeedRightDurationStr',
                        ),
                        Text(
                          'Total duration: $breastfeedTotalDurationStr',
                        )
                      ],
                    ),
                  ),
                ],
              ),
            ),
            Container(
              height: 150,
              width: MediaQuery.of(context).size.width - 30,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                color: Colors.white.withOpacity(0.7),
              ),
              child: Row(
                children: [
                  Container(
                    margin: const EdgeInsets.only(left: 10),
                    height: 48,
                    width: 48,
                    child: Image.asset('lib/assets/images/milk.png'),
                  ),
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 10),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'BOTTLE FEEDING',
                          style: TextStyle(fontSize: 17, color: Colors.deepOrangeAccent),
                        ),
                        Text(
                          'Total feeding times: $bottlefeedTimeStr',
                        ),
                        Text(
                          'Total amount: $amountStr',
                        ),
                        Text(
                          'Total duration: $bottlefeedTotalDurationStr',
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            Container(
              height: 150,
              width: MediaQuery.of(context).size.width - 30,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                color: Colors.white.withOpacity(0.7),
              ),
              child: Row(
                children: [
                  Container(
                    margin: const EdgeInsets.only(left: 10),
                    height: 48,
                    width: 48,
                    child: Image.asset('lib/assets/images/baby-food.png'),
                  ),
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 10),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'BABY MEALS',
                          style: TextStyle(fontSize: 17, color: Colors.deepOrangeAccent),
                        ),
                        Text(
                          'Total feeding times: $babyMealTimeStr',
                        ),
                        ElevatedButton.icon(
                          onPressed: (){
                            showDialog(context: context, builder: (BuildContext context){ return DishBox(dishes: eatenDishes, date: date,);});
                          },
                          style: const ButtonStyle(backgroundColor: PrimaryYellow()),
                          icon: const Icon(Icons.arrow_circle_right_outlined),
                          label: const Text('See what the baby ate'),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            ElevatedButton.icon(
              onPressed: (){Share.share(getHistory());},
              icon: const Icon(Icons.ios_share_outlined),
              label: const Text('Share history'),
            )
          ],
        ),
      )
    );
  }
}

class PrimaryYellow extends MaterialStateColor {
  const PrimaryYellow() : super(_defaultColor);

  static const int _defaultColor = 0xFFE0A98E;
  static const int _pressedColor = 0xFFE0A98E;

  @override
  Color resolve(Set<MaterialState> states) {
    if (states.contains(MaterialState.pressed)) {
      return const Color(_pressedColor);
    }
    return const Color(_defaultColor);
  }
}

class DishBox extends StatelessWidget {
  const DishBox({
    super.key,
    required this.dishes,
    required this.date,
  });

  final List<DishTime> dishes;
  final String date;

  String getContent(){
    String content = '';
    int i=0;
    for(var item in dishes){
      i++;
      content = '$content$i/ ${item.dish} is served at ${item.time}\n';
    }
    return content;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Served dishes on $date'),
      content: Text(getContent()),
      actions: <Widget>[
        MaterialButton(
          color: Colors.blueGrey.shade200,
          onPressed: () {
            // Perform the desired action
            Navigator.of(context).pop();
          },
          child: const Text('OK'),
        ),
      ],
    );
  }
}
