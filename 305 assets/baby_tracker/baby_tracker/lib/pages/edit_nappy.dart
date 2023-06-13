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
import 'package:baby_tracker/pages/edit_breast_feeding.dart ';

class EditNappy extends StatefulWidget{
  const EditNappy({Key? key, required this.event, required this.type}) : super(key: key);

  final Event event;
  final String type;

  @override
  State<EditNappy> createState() => _EditNappyState();
}

class _EditNappyState extends State<EditNappy> {
  DateTime dateTime = DateTime.now();
  TimeOfDay time = TimeOfDay.now();
  DateFormat dateFormat = DateFormat("yyyy-MM-dd");
  DateTimeAdapter dateTimeAdapter = DateTimeAdapter();
  CustomColors customColors = CustomColors();
  TextEditingController noteController = TextEditingController();
  File? imageFile;
  ImagePicker imagePicker = ImagePicker();
  ImageAdapter imageAdapter = ImageAdapter();
  String condition = 'pee';
  bool timeChanged = false;
  bool dateChanged = false;
  bool imageRemoved = false;

  void _showDatePicker()
  {
    showDatePicker(context: context, initialDate: dateTime, firstDate: DateTime(2000), lastDate: DateTime(2025)).then((value) {
      setState(() {
        dateTime = value!;
        dateChanged = true;
      });
    });
  }

  void _showTimePicker()
  {
    showTimePicker(context: context, initialTime: TimeOfDay.now()).then((value) {
      setState(() {
        time = value!;
        timeChanged = true;
      });
    });
  }

  @override
  void initState() {
    // TODO: implement initState
    noteController.text = widget.event.note!;
    condition = widget.event.condition!;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    String usedDate = widget.event.date!;
    return Scaffold(
      appBar: AppBar(
        title: const Text('♡  N I G H T   N I G H T   ♡'),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const PageTitle(title: 'NAPPY DETAILS'),
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
                      dateChanged ? dateFormat.format(dateTime) : widget.event.date!,
                      style: const TextStyle(color: Colors.blueGrey, fontSize: 16),
                    ),
                  ),
                  MaterialButton( //TIME BUTTON
                    onPressed: _showTimePicker,
                    color: Colors.white70,
                    child: Text(
                      timeChanged ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute)) : widget.event.time!,
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
                  child: imageFile != null
                      ? Image.file(
                        imageFile!, fit: BoxFit.cover)
                      : !imageRemoved
                      ? FutureBuilder(
                      future: imageAdapter.getData(widget.event.image!),
                      builder:  (context, snapshot) {
                        if (snapshot.connectionState == ConnectionState.done && snapshot.hasData) {
                          return Image.network(snapshot.data!.toString(), fit: BoxFit.cover);
                        }
                        if (snapshot.hasError) {
                          return Text('${snapshot.error}');
                        }
                        return const SizedBox(height: 20, width: 20, child: CircularProgressIndicator());
                      })
                      : Image.asset('lib/assets/images/nappyex.png', fit: BoxFit.cover)
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
                            imageRemoved = false;
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
                          return ImageBox(imagePath: widget.event.image!, imageRemoved: imageRemoved, imageFile: imageFile,);
                        });
                      },
                      color: Colors.blueGrey.shade200,
                      child: const Text('View', style: TextStyle(color: Colors.black87)),
                    ),
                    MaterialButton( //IMAGE-REMOVING BUTTON
                      onPressed: (){
                        setState(() {
                          imageFile = null;
                          imageRemoved = true;
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
                          widget.event.type = 'nappy';
                          widget.event.date = dateChanged ? dateFormat.format(dateTime) : widget.event.date;
                          widget.event.time = timeChanged
                              ? DateFormat.Hm().format(DateTime(1, 1, 1, time.hour, time.minute))
                              : widget.event.time;
                          widget.event.dateTime = '${widget.event.date} ${widget.event.time}';
                          widget.event.note = noteController.text;
                          widget.event.condition = condition;
                          String imagePath = '${widget.event.dateTime!}.jpg';
                          try {
                            //upload the image
                            try{
                              if(imageFile != null) {
                                imageAdapter.uploadImage(imagePath, imageFile);
                                widget.event.image = imagePath;
                              }
                              else{
                                if(imageRemoved && widget.event.image != 'nappyex.png'){
                                  imageAdapter.deleteImage(widget.event.image!);
                                  widget.event.image = 'nappyex.png';
                                }
                              }
                            } on FirebaseException catch (e){
                              String error = 'Cannot upload the image: $e';
                              showDialog(context: context, builder: (BuildContext context){
                                return SuccessAlertBox(title: 'failed', content: error, actionContent: 'OK');
                              });
                            }
                            //when the image is uploaded, starts adding the event
                            Provider.of<EventModel>(context, listen: false).addEventToDB(widget.event, widget.event.id, usedDate, widget.type);
                          } on FirebaseException catch (e)
                          {
                            String error = 'Cannot update the event: $e';
                            showDialog(context: context, builder: (BuildContext context){
                              return SuccessAlertBox(title: 'Failed', content: error, actionContent: 'OK');
                            });
                          }
                          Navigator.pop(context, true);
                        },
                        color: Colors.blueGrey.shade300,
                        child: const Text(
                          'Update record',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                        ),

                      )
                  ),
                  SizedBox(
                      width: 140,
                      height: 45,
                      child: MaterialButton( //date button
                        onPressed: (){
                          showDialog(context: context, builder: (BuildContext context){
                            return DeleteAlertBox(title: 'Delete an event',
                                content: 'Are you sure to delete this event?',
                                actionConfirm: 'Delete',
                                actionCancel: 'Cancel',
                                id: widget.event.id!,
                                updateList: true,
                                usedDate: usedDate,
                                type: widget.type
                            );
                          });
                        },
                        color: Colors.red.shade300,
                        child: const Text(
                          'Delete',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                        ),

                      )
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class ImageBox extends StatelessWidget {
  const ImageBox({
    super.key, required this.imagePath, this.imageFile, required this.imageRemoved
  });

  final String imagePath;
  final File? imageFile;
  final bool imageRemoved;

  @override
  Widget build(BuildContext context) {
    ImageAdapter imageAdapter = ImageAdapter();
    return AlertDialog(
      title: const Text('Displaying nappy image'),
      content: SizedBox(
        height: 480,
        width: 480,
        child:imageFile != null
          ? Image.file(
          imageFile!, fit: BoxFit.cover)
          : !imageRemoved
          ? FutureBuilder(
          future: imageAdapter.getData(imagePath),
          builder:  (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.done && snapshot.hasData) {
              return Image.network(snapshot.data!.toString(), fit: BoxFit.cover);
            }
            if (snapshot.hasError) {
              return Text('${snapshot.error}');
            }
            return const SizedBox(height: 20, width: 20, child: CircularProgressIndicator());
          })
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

