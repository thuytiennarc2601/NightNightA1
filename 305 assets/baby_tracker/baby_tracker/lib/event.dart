import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

class Event
{
  String? id;
  String? date;
  String? time;
  String? note;
  String? type;
  String? dateTime;

  //feed, sleep
  int totalDuration = 0;
  //breastfeeding
  int leftDuration = 0;
  int rightDuration = 0;
  String? startSide;
  String? endSide;
  //bottle feed
  double milkAmount = 0.0;
  //baby meals
  String? dish;

  //nappy
  String? condition;
  String? image;

  Event({
    this.id,
    this.dateTime,
    this.date,
    this.time,
    this.note,
    this.type,
    this.totalDuration = 0,
    this.leftDuration = 0,
    this.rightDuration = 0,
    this.startSide,
    this.endSide,
    this.milkAmount = 0.0,
    this.dish,
    this.condition,
    this.image
  });

  Event.fromJson(Map<String, dynamic> json):
        id = json['id'],
        dateTime = json['dateTime'],
        date = json['date'],
        time = json['time'],
        type = json['type'],
        totalDuration = json['totalDuration'] ?? 0,
        leftDuration = json['leftDuration'] ?? 0,
        rightDuration = json['rightDuration'] ?? 0,
        startSide = json['startSide'],
        endSide = json['endSide'],
        milkAmount = json['milkAmount'] ?? 0.0,
        dish = json['dish'],
        condition = json['condition'],
        image = json['image'],
        note = json['note'];

  Map<String, dynamic> toJson() =>
      {
        'id': id,
        'dateTime': dateTime,
        'date': date,
        'time': time,
        'note' : note,
        'type': type,
        'leftDuration' : leftDuration,
        'rightDuration' : rightDuration,
        'totalDuration' : totalDuration,
        'startSide' : startSide,
        'endSide' : endSide,
        'milkAmount' : milkAmount,
        'dish' : dish,
        'condition' : condition,
        'image' : image,
      };
}

class Recipe{
  String title = 'No title';
  String description = 'Recipe content';

  Recipe({required this.title, required this.description});

  Recipe.fromJson(Map<String, dynamic> json):
        title = json['title'],
        description = json['description'];

  Map<String, dynamic> toJson() =>
      {
        'title': title,
        'description': description,
      };
}

class EventModel extends ChangeNotifier
{
  //get target collection reference in db
  CollectionReference eventsCollection = FirebaseFirestore.instance.collection('events');
  CollectionReference recipesCollection = FirebaseFirestore.instance.collection('recipes');

  //loading indicator
  bool loading = false;

  final List<Event> events = [];
  final List<Recipe> recipes = [];

  var recipe = Recipe(title: '', description: '');

  EventModel(String date, String type)
  {
    fetch(date, type);
    fetchRecipe();
  }

  Future addRecipe(Recipe recipe, String id, String oldID) async
  {
    loading = true;

    update();

    await recipesCollection.doc(oldID).delete();

    await recipesCollection.doc(id).set(recipe.toJson());

    await fetchRecipe();

    loading = false;

    update();
  }

  Future fetchRecipe() async
  {
    //clear any existing data we have gotten previously, to avoid duplicate data
    recipes.clear();

    //indicate that we are loading
    loading = true;
    notifyListeners(); //tell children to redraw, and they will see that the loading indicator is on

    //get all movies
    var querySnapshot = await recipesCollection.orderBy('title').get();
    //iterate over the movies and add them to the list
    for (var doc in querySnapshot.docs) {
      //note not using the add(Movie item) function, because we don't want to add them to the db
      var recipe = Recipe.fromJson(doc.data()! as Map<String, dynamic>);
      recipes.add(recipe);
    }

    //put this line in to artificially increase the load time, so we can see the loading indicator (when we add it in a few steps time)
    //comment this out when the delay becomes annoying
    //await Future.delayed(const Duration(seconds: 2));
    //we're done, no longer loading
    loading = false;

    print(recipes.length);

    update();
  }

  Future getRecipe(String id) async{
    loading = true;

    update();

    var doc = await recipesCollection.doc(id).get();

    recipe = Recipe.fromJson(doc.data()! as Map<String, dynamic>);

    loading = false;

    update();
  }

  Future deleteRecipe(String id) async{

    loading = true;

    update();

    await recipesCollection.doc(id).delete();

    await fetchRecipe();

    loading = false;

    update();

  }

  Future addEventToDB(Event event, String? id, String date, String type) async
  {
    loading = true;

    update();

    await eventsCollection.doc(id!).set(event.toJson());

    await fetch(date, type);

    loading = false;

    update();
  }

  Future delete(String id, String date, String type) async
  {
    loading = true;

    update();

    await eventsCollection.doc(id).delete();

    await fetch(date, type);

    loading = false;

    update();
  }

  Future documentExist(String? id) async
  {
    final docRef = eventsCollection.doc(id!);

    final snapShot = await docRef.get();

    return snapShot.exists;
  }

  Future fetch(String date, String type) async
  {
    //clear any existing data we have gotten previously, to avoid duplicate data
    events.clear();

    //indicate that we are loading
    loading = true;
    notifyListeners(); //tell children to redraw, and they will see that the loading indicator is on

    //get all movies
    var querySnapshot = await eventsCollection.orderBy("dateTime", descending: true).get();
    //iterate over the movies and add them to the list
    for (var doc in querySnapshot.docs) {
      //note not using the add(Movie item) function, because we don't want to add them to the db
      var event = Event.fromJson(doc.data()! as Map<String, dynamic>);
      if (event.date == date) {
        if (type != 'all') {
          if (type == 'meal') {
            if (event.type == 'breastfeed' || event.type == 'bottle feed' ||
                event.type == 'babymeals') {
              events.add(event);
            }
          }
          else if (event.type == type) {
            events.add(event);
          }
        }
        else {
          events.add(event);
        }
      }
    }

    //put this line in to artificially increase the load time, so we can see the loading indicator (when we add it in a few steps time)
    //comment this out when the delay becomes annoying
    //await Future.delayed(const Duration(seconds: 2));
    //we're done, no longer loading
    loading = false;

    update();
  }

  void update() { notifyListeners(); }

}