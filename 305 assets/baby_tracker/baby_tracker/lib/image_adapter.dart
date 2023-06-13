import 'package:firebase_storage/firebase_storage.dart';
import 'dart:io';
import 'package:image/image.dart' as Img;

class ImageAdapter{
  final storageRef = FirebaseStorage.instance.ref();

  Future uploadImage(String imagePath, File? imageFile) async{
    if(imageFile != null) {
      // Read the image file
      final imageBytes = await imageFile.readAsBytes();

      // Decode the image using the image package
      final image = Img.decodeImage(imageBytes);

      // Resize the image
      final resizedImage = Img.copyResize(image!, width: 480, height: 480);

      // Convert the resized image back to bytes
      final resizedBytes = Img.encodeJpg(resizedImage);

      // Create a reference to 'images/xxx.jpg'
      final imageRef = storageRef.child(imagePath);
      if(resizedBytes != null)
      {
        await imageRef.putData(resizedBytes);
      }
      //print('Im still working bitch, just because your code is so stupid :)');
    }
    else{
      print('No image available');
    }
  }

  String? downloadImage;

  Future getData(String imagePath) async{
    try{
      await downloadImageURL(imagePath);
      return downloadImage;
    } catch (e){
      return null;
    }
  }

  Future<void> downloadImageURL(String imagePath) async{
    final imageRef = storageRef.child(imagePath);
    downloadImage = await imageRef.getDownloadURL();
  }

  Future deleteImage(String imagePath) async{
    final imageRef = storageRef.child(imagePath);
    imageRef.delete();
  }
}