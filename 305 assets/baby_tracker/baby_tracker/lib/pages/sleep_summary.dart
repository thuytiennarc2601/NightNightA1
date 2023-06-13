import 'package:baby_tracker/date_time_adapter.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:share/share.dart';

class SleepSummary extends StatefulWidget {
  const SleepSummary({Key? key, required this.events}) : super(key: key);

  final List<Event> events;

  @override
  State<SleepSummary> createState() => _SleepSummaryState();
}

class _SleepSummaryState extends State<SleepSummary> {

  String sleepTimeStr = '';
  String sleepDurStr = '';
  String date = '';

  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();

  @override
  void initState() {
    // TODO: implement initState
    int sleepTime = 0;
    int sleepDur = 0;

    for(var event in widget.events){
      if(event.type == 'sleep'){
        sleepTime++;
        sleepDur += event.totalDuration;
      }
      date = event.date!;
    }

    sleepTimeStr = sleepTime.toString();
    sleepDurStr = dateTimeAdapter.formatDuration(sleepDur, true);

    super.initState();
  }

  String getHistory(){
    String text = '';
    text = 'History of sleeps on $date:\n'
        'Total sleeping times: $sleepTimeStr\n'
        'Total duration: $sleepDurStr\n\n';

    int i = 0;
    for(var event in widget.events){
      i++;
      text = '$text$i: ${event.date} ${event.time}\nDuration: ${dateTimeAdapter.formatDuration(event.totalDuration, true)}\nNote: ${event.note}\n\n';
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
            children: [
              Container(
                margin: const EdgeInsets.symmetric(vertical: 10),
                alignment: Alignment.center,
                height: 50,
                width: MediaQuery.of(context).size.width,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.7),
                ),
                child: Text(
                  'History of sleeps ($date)',
                  style: const TextStyle(fontSize: 20, color: Colors.blueGrey, fontWeight: FontWeight.w700),
                ),
              ),
              Container(
                margin: const EdgeInsets.symmetric(vertical: 10),
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
                      child: Image.asset('lib/assets/images/sleeping.png'),
                    ),
                    Container(
                      margin: const EdgeInsets.symmetric(horizontal: 10),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'SLEEP',
                            style: TextStyle(fontSize: 17, color: Colors.deepOrangeAccent),
                          ),
                          Text(
                            'Total sleeping times: $sleepTimeStr',
                          ),
                          Text(
                            'Total duration: $sleepDurStr',
                          ),

                        ],
                      ),
                    ),
                  ],
                ),
              ),
              ElevatedButton.icon(
                onPressed: (){
                  Share.share(getHistory());
                },
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
