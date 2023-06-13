import 'dart:ffi';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class DateTimeAdapter{
  DateFormat dateFormat = DateFormat("dd/MM/yyyy");
  DateFormat timeFormat = DateFormat("HH:mm");

  String showDate(DateTime date)
  {
    String dateString = dateFormat.format(date);
    return dateString;
  }

  String showTime(DateTime time)
  {
    String timeString = timeFormat.format(time);
    return timeString;
  }

  int fromTimeStringToSeconds(String timeString)
  {
    List<String> components = timeString.split(':');

    int hours = int.parse(components[0]);
    int minutes = int.parse(components[1]);
    int seconds = int.parse(components[2]);

    int timeInSeconds = (hours * 3600) + (minutes * 60) + seconds;
    return timeInSeconds;
  }

  String formatDuration(int seconds, bool decorated) {
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    final secondString = twoDigits(seconds.remainder(60));
    final minuteString = twoDigits(seconds.remainder(3600) ~/ 60);
    final hourString = twoDigits(seconds ~/ 3600);
    String timeString = '';
    if(decorated){
      timeString = '${hourString}h${minuteString}m${secondString}s';
    }
    else{
      timeString = '$hourString:$minuteString:$secondString';
    }
    return timeString;
  }
}