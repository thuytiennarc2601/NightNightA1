import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:provider/provider.dart';

class AddEditRecipe extends StatefulWidget {
  const AddEditRecipe({Key? key, required this.isEditing, required this.recipe}) : super(key: key);

  final bool isEditing;
  final Recipe recipe;

  @override
  State<AddEditRecipe> createState() => _AddEditRecipeState();
}

class _AddEditRecipeState extends State<AddEditRecipe> {

  TextEditingController titleText = TextEditingController();
  TextEditingController descriptionText = TextEditingController();

  @override
  void initState() {
    // TODO: implement initState
    if(widget.isEditing){
      titleText.text = widget.recipe.title;
      descriptionText.text = widget.recipe.description;
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    String oldName = widget.recipe.title;
    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            Container(
              alignment: Alignment.center,
              margin: const EdgeInsets.all(10),
              child: Text(
                widget.isEditing ? 'RECIPE DETAILS' : 'ADDING A RECIPE',
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: Colors.blueGrey),
              ),
            ),
            const ActionLabel(label: 'Enter a recipe name:'),
            Container( //TITLE BOX
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: TextField(
                controller: titleText,
                decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: 'e.g., mushroom soup...'
                ),
              ),
            ),
            const ActionLabel(label: 'Enter the recipe:'),
            Container( //DESCRIPTION BOX
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: TextField(
                maxLines: 15,
                textInputAction: TextInputAction.newline,
                controller: descriptionText,
                decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: 'e.g., mushroom soup...'
                ),
              ),
            ),
            Container(
              margin: const EdgeInsets.symmetric(vertical: 15),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  MaterialButton(onPressed: (){
                    if(titleText.text != '') {widget.recipe.title = titleText.text;}
                    if(descriptionText.text != '') {widget.recipe.description = descriptionText.text;}
                    try{
                      Provider.of<EventModel>(context, listen: false).addRecipe(widget.recipe, widget.recipe.title, oldName);
                    } on FirebaseException catch(e){
                      String error = 'Cannot update the recipe: $e';
                      showDialog(context: context, builder: (BuildContext context){
                        return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                      });
                    }
                    Navigator.pop(context);
                  }, color: Colors.blueGrey.shade300, child: const Text('Save recipe')),
                  MaterialButton(onPressed: (){
                    if(widget.isEditing){
                        showDialog(context: context, builder: (BuildContext context){
                          return AlertDialog(
                            title: Text('Delete ${widget.recipe.title}'),
                            content: Text('Are you sure to delete ${widget.recipe.title}'),
                            actions: <Widget>[
                              MaterialButton(
                                color: Colors.blueGrey.shade200,
                                onPressed: () {
                                  // Perform the desired action
                                  Navigator.of(context).pop();
                                },
                                child: const Text('Cancel'),
                              ),
                              MaterialButton(
                                color: Colors.red,
                                onPressed: (){
                                  Provider.of<EventModel>(context, listen:false).deleteRecipe(oldName);
                                  Navigator.of(context).pop();
                                  Navigator.pop(context);
                                },
                                child: const Text('Delete'),
                              ),
                            ],
                          );
                        });
                    }
                    else {
                      setState(() {
                        titleText.text = '';
                        descriptionText.text = '';
                      });
                    }
                  }, color: Colors.red, child: Text(widget.isEditing ? 'Delete' : 'Clear all'),)
                ],
              ),
            )

          ],
        ),
      ),
    );
  }
}
