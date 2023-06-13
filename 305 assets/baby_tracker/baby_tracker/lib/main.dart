import 'package:baby_tracker/pages/meal_page.dart';
import 'package:baby_tracker/pages/nappy_page.dart';
import 'package:baby_tracker/pages/sleep_page.dart';
import 'package:baby_tracker/pages/home_page.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:intl/intl.dart';
import 'firebase_options.dart';
import 'package:provider/provider.dart';
import 'package:baby_tracker/event.dart';

Future main() async {
  WidgetsFlutterBinding.ensureInitialized();

  var app = await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  print("\n\nConnected to Firebase App ${app.options.projectId}\n\n");

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    DateTime dateTime = DateTime.now();
    DateFormat dateFormat = DateFormat("yyyy-MM-dd");
    return ChangeNotifierProvider(
      create: (context) => EventModel(dateFormat.format(dateTime), 'all'),
      child: MaterialApp(

       home: HomePage(),
        theme: ThemeData(
          primarySwatch: Colors.blueGrey,
          tabBarTheme: const TabBarTheme(labelColor: Colors.blueGrey),
        ),
      ),
    );
  }
}

class HomePage extends StatelessWidget{
  const HomePage({Key? key}) : super(key: key);
  @override
  Widget build(BuildContext context) {
    return Consumer<EventModel>(
      builder: (context, eventModel, _)
      {
        return DefaultTabController(
          length: 4,
          child: Scaffold(
            appBar: AppBar(
              title: const Text('♡  N I G H T   N I G H T   ♡'),
              centerTitle: true,
            ),
            body: const Column(
              children: [
                TabBar(
                  tabs:[
                    Tab(
                      text: 'HOME',
                    ),
                    Tab(
                      text: 'MEALS',
                    ),
                    Tab(
                      text: "NAPPIES",
                    ),
                    Tab(
                        text: "SLEEPS"
                    ),
                  ],
                ),

                Expanded(
                  child: TabBarView(children: [
                    Home(),
                    MealPage(),
                    AddNappy(),
                    AddSleep(),
                  ]),
                )
              ],
            ),
          ),
        );
      }
    );
  }
}
