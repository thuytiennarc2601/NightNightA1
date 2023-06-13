import 'package:baby_tracker/date_time_adapter.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:baby_tracker/pages/add_breast_feeding.dart';
import 'package:baby_tracker/color_collections.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import 'package:baby_tracker/event.dart';
import 'package:baby_tracker/image_adapter.dart';
import 'package:provider/provider.dart';

class AddNappy extends StatefulWidget{
  const AddNappy({Key? key}) : super(key: key);

  @override
  State<AddNappy> createState() => _AddNappyState();
}

class _AddNappyState extends State<AddNappy> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  CustomColors customColors = CustomColors();
  Event event = Event();
  TextEditingController noteController = TextEditingController();
  File? imageFile;
  ImagePicker imagePicker = ImagePicker();
  ImageAdapter imageAdapter = ImageAdapter();
  String condition = 'pee';

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
      imageFile = null;
      condition = 'pee';
    });
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: [
          const PageTitle(title: 'CHANGING A NAPPY'),
          Container(
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
          //CHOOSE A NAPPY CONDITION
          const ActionLabel(label: 'Choose a condition:'),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 15),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                MaterialButton( //PEE BUTTON
                  onPressed: (){
                    setState(() {
                      condition = 'pee';
                    });
                  },
                  color: condition == 'pee' ? CustomColors.primaryYellow : Colors.blueGrey.shade100,
                  child: const Text('Pee', style: TextStyle(color: Colors.black87)),
                ),
                MaterialButton( //POOP BUTTON
                  onPressed: (){
                    setState(() {
                      condition = 'poop';
                    });
                  },
                  color: condition == 'poop' ? CustomColors.primaryYellow : Colors.blueGrey.shade100,
                  child: const Text('Poop', style: TextStyle(color: Colors.black87)),
                ),
                MaterialButton( //MIXED BUTTON
                  onPressed: (){
                    setState(() {
                      condition = 'mixed';
                    });
                  },
                  color: condition == 'mixed' ? CustomColors.primaryYellow : Colors.blueGrey.shade100,
                  child: const Text('Mixed', style: TextStyle(color: Colors.black87)),
                ),
              ],
            ),
          ),
          //UPLOAD IMAGE
          const ActionLabel(label: 'Upload a nappy image:'),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              SizedBox( //IMAGE VIEW
                height: 160,
                width: 160,
                child:imageFile != null
                    ? Image.file(
                    imageFile!, fit: BoxFit.cover)
                    : Image.asset(
                    'lib/assets/images/nappyex.png',
                    fit: BoxFit.cover),
              ),
              Column(
                children: [
                  MaterialButton( //IMAGE-UPLOADING BUTTON
                    onPressed: () async {
                      try{
                        final image = await imagePicker.pickImage(source: ImageSource.gallery);
                        if(image == null) return;
                        final imagePath = File(image.path);
                        setState(() {
                          imageFile = imagePath;
                        });
                      } on PlatformException catch (e){
                        String error = 'Cannot pick the image: $e';
                        showDialog(context: context, builder: (BuildContext context){
                          return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                        });
                      }
                    },
                    color: CustomColors.primaryYellow,
                    child: const Text('Upload', style: TextStyle(color: Colors.black87)),
                  ),
                  MaterialButton( //VIEW BUTTON
                    onPressed: (){
                      showDialog(context: context, builder: (BuildContext context){
                        return ImageBox(imageFile: imageFile,);
                      });
                    },
                    color: Colors.blueGrey.shade200,
                    child: const Text('View', style: TextStyle(color: Colors.black87)),
                  ),
                  MaterialButton( //IMAGE-REMOVING BUTTON
                    onPressed: (){
                      setState(() {
                        imageFile = null;
                      });
                    },
                    color: Colors.red.shade300,
                    child: const Text('Remove', style: TextStyle(color: Colors.black87)),
                  ),
                ],
              ),
            ],
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
                      onPressed: (){
                        event.type = 'nappy';
                        event.date = dateFormat.format(dateTime);
                        event.time = DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute));
                        event.dateTime = '${event.date} ${event.time}';
                        event.id = '${event.dateTime}${event.type}';
                        event.note = noteController.text;
                        event.condition = condition;
                        String imagePath = '${event.date} ${event.time}.jpg';

                        try {
                          //upload the image
                          try{
                            if(imageFile == null) {
                              event.image = 'nappyex.png';
                            }
                            else{
                              imageAdapter.uploadImage(imagePath, imageFile);
                              event.image = imagePath;
                            }
                          } on FirebaseException catch (e){
                            String error = 'Cannot upload the image: $e';
                            showDialog(context: context, builder: (BuildContext context){
                              return SuccessAlertBox(title: 'failed', content: error, actionContent: 'OK');
                            });
                          }
                          //when the image is uploaded, starts adding the event
                          Provider.of<EventModel>(context, listen: false).addEventToDB(event, event.id, dateFormat.format(DateTime.now()), 'all');
                          showDialog(context: context, builder: (BuildContext context){
                            return const SuccessAlertBox(title: 'Succeeded', content: 'The event is added successfully', actionContent: 'OK');
                          });
                          clearData();
                        } on FirebaseException catch (e)
                        {
                          String error = 'Cannot add the event: $e';
                          showDialog(context: context, builder: (BuildContext context){
                            return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
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
                    child: MaterialButton( //date button
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

class ImageBox extends StatelessWidget {
  const ImageBox({
    super.key, this.imageFile,
  });

  final File? imageFile;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Displaying nappy image'),
      content: SizedBox(
        height: 480,
        width: 480,
        child:imageFile != null
            ? Image.file(
            imageFile!, fit: BoxFit.cover)
            : Image.asset('lib/assets/images/nappyex.png', fit: BoxFit.cover),
      ),
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

