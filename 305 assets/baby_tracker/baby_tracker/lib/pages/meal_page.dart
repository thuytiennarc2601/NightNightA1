import 'package:baby_tracker/pages/add_baby_meals.dart';
import 'package:baby_tracker/pages/add_bottle_feeding.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/event.dart';

class MealPage extends StatefulWidget{
  const MealPage({Key? key}) : super(key: key);

  @override
  State<MealPage> createState() => _MealPageState();
}

class _MealPageState extends State<MealPage> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return const DefaultTabController(
      length: 3,
      child: Scaffold(
        bottomNavigationBar:BottomAppBar(
          color: Colors.white,
          child: TabBar(
            tabs: [
              Tab(
                text: 'Breast feeding',
              ),
              Tab(
                text: 'Bottle feeding',
              ),
              Tab(
                text: "Baby meals",
              ),
            ],
          ),
        ),
        body: TabBarView(children: [
          AddBreastfeeding(),
          AddBottlefeeding(),
          AddBabyMeals(),
        ]),
      ),
    );
  }
}