import 'package:flutter/material.dart';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:share/share.dart';

class NappySummary extends StatefulWidget {
  const NappySummary({Key? key, required this.events}) : super(key: key);

  final List<Event> events;

  @override
  State<NappySummary> createState() => _NappySummaryState();
}

class _NappySummaryState extends State<NappySummary> {

  String nappyStr = '';
  String wetStr = '';
  String dryStr = '';
  String mixedStr = '';
  String date = '';


  @override
  void initState() {
    // TODO: implement initState
    int nappy = 0;
    int wet = 0;
    int dry = 0;
    int mixed = 0;

    for(var event in widget.events){
      if(event.type == 'nappy'){
        nappy++;
        switch (event.condition){
          case 'pee': wet++;
          case 'poop': dry++;
          case 'mixed': mixed++;
        }
      }
      date = event.date!;
    }

    nappyStr = nappy.toString();
    wetStr = wet.toString();
    dryStr = dry.toString();
    mixedStr = mixed.toString();

    super.initState();
  }

  String getHistory(){
    String text = '';
    text = 'History of nappies on $date:\n'
        'Dirty nappies: $nappyStr\n'
        'Total wet nappies: $wetStr\n'
        'Total dry nappies: $dryStr\n'
        'Total mixed nappies: $mixedStr\n\n';

    int i = 0;
    for(var event in widget.events){
      i++;
      text = '$text$i: ${event.date} ${event.time}\nCondition: ${event.condition}\nNote: ${event.note}\n\n';
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
                margin :const EdgeInsets.symmetric(vertical: 10),
                alignment: Alignment.center,
                height: 50,
                width: MediaQuery.of(context).size.width,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.7),
                ),
                child: Text(
                  'History of nappies ($date)',
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
                      child: Image.asset('lib/assets/images/nappy.png'),
                    ),
                    Container(
                      margin: const EdgeInsets.symmetric(horizontal: 10),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'NAPPIES',
                            style: TextStyle(fontSize: 17, color: Colors.deepOrangeAccent),
                          ),
                          Text(
                            'Dirty nappies: $nappyStr',
                          ),
                          Text(
                            'Total wet nappies: $wetStr',
                          ),
                          Text(
                            'Total dry nappies: $dryStr',
                          ),
                          Text(
                            'Total mixed nappies: $mixedStr',
                          )
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
