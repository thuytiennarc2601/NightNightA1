import 'package:baby_tracker/event.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:baby_tracker/pages/add_edit_recipe.dart';
import 'package:provider/provider.dart';

class RecipeView extends StatefulWidget {
  const RecipeView({Key? key, required this.isViewing}) : super(key: key);

  final bool isViewing;

  @override
  State<RecipeView> createState() => _RecipeViewState();
}

class _RecipeViewState extends State<RecipeView> {

  TextEditingController searchController = TextEditingController();
  Recipe recipe = Recipe(title: 'No title',description: 'No description');
  List<Recipe> filteredRecipes = [];

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
  }

  void filterSearchResults(String query, List<Recipe> recipes) {
    if (query.isNotEmpty) {
      List<Recipe> tempList = [];

      recipes.forEach((item) {
        if (item.title.toLowerCase().contains(query.toLowerCase())) {
          tempList.add(item);
        }
      });

      setState(() {
        filteredRecipes.clear();
        filteredRecipes.addAll(tempList);
      });
      return;
    } else {
      setState(() {
        filteredRecipes.clear();
      });
    }
  }


  @override
  Widget build(BuildContext context) {
    return Consumer<EventModel>(
      builder: (context, eventModel, _) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('♡  N I G H T   N I G H T   ♡'),
            centerTitle: true,
          ),
          body: Container(
            height: MediaQuery
                .of(context)
                .size
                .height,
            width: MediaQuery
                .of(context)
                .size
                .width,
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
            child: SingleChildScrollView(
              child: Column(
                children: [
                  Container(
                    margin: const EdgeInsets.symmetric(vertical: 20),
                    alignment: Alignment.center,
                    height: 50,
                    width: MediaQuery
                        .of(context)
                        .size
                        .width,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.7),
                    ),
                    child: const Text(
                      'Baby meal recipes',
                      style: TextStyle(fontSize: 20,
                          color: Colors.blueGrey,
                          fontWeight: FontWeight.w700),
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.symmetric(
                        vertical: 0, horizontal: 15),
                    height: 515,
                    width: MediaQuery
                        .of(context)
                        .size
                        .width,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.7),
                    ),
                    child: Column(
                      children: [
                        Container(
                          margin: const EdgeInsets.symmetric(vertical: 10),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              SearchBar(
                                constraints: const BoxConstraints(
                                    maxWidth: 300),
                                controller: searchController,
                                leading: const Icon(Icons.search_outlined),
                                onChanged: (controller){
                                  if (controller.isNotEmpty) {
                                    List<Recipe> tempList = [];

                                    eventModel.recipes.forEach((item) {
                                      if (item.title.toLowerCase().contains(controller.toLowerCase())) {
                                        tempList.add(item);
                                      }
                                    });

                                    setState(() {
                                      filteredRecipes.clear();
                                      filteredRecipes.addAll(tempList);
                                    });
                                    return;
                                  } else {
                                    setState(() {
                                      filteredRecipes.clear();
                                    });
                                  }
                                },
                              ),
                              IconButton.outlined(onPressed: () {
                                Navigator.push(context,
                                    MaterialPageRoute(builder: (context) {
                                      return AddEditRecipe(
                                          isEditing: false, recipe: recipe);
                                    }));
                              }, icon: const Icon(Icons.add), tooltip: 'Add',)
                            ],
                          ),
                        ),
                        Container(
                            height: 415,
                            padding: const EdgeInsets.only(top: 10),
                            child: eventModel.loading
                                ? const Center(child: CircularProgressIndicator(),)
                                : eventModel.recipes.isEmpty
                                ? const Center(
                                child: Text('No recipe found. Add new.'))
                                : ListView.builder(
                                itemBuilder: (_, index) {
                                  var recipe = filteredRecipes.isNotEmpty ? filteredRecipes[index] : eventModel.recipes[index];
                                  return ListTile(
                                    contentPadding: const EdgeInsets.symmetric(
                                        vertical: 5, horizontal: 15),
                                    title: Text(recipe.title,
                                        style: const TextStyle(
                                            fontSize: 18,
                                            fontWeight: FontWeight.w700,
                                            color: Colors.blueGrey)),
                                    leading: SizedBox(
                                      height: 48,
                                      width: 48,
                                      child: Image.asset(
                                          'lib/assets/images/recipe-book.png'),
                                    ),
                                    //added this line, this should be familiar from last week:
                                    onTap: () {
                                      if(widget.isViewing) {
                                        Navigator.push(context,
                                            MaterialPageRoute(
                                                builder: (context) {
                                                  return AddEditRecipe(
                                                      isEditing: true,
                                                      recipe: recipe);
                                                }));
                                      }
                                      else{
                                        Navigator.pop(context, recipe.title);
                                      }
                                      },
                                  );
                                },
                                itemCount: filteredRecipes.isNotEmpty ? filteredRecipes.length : eventModel.recipes.length
                            )
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      }
    );
  }
}
