package au.edu.utas.username.nightnight.adapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import au.edu.utas.username.nightnight.R
import java.util.*

class Dialogs {

    fun showDialog(context: Context, source: Int, cancelable: Boolean): Dialog
    {
        val builder = AlertDialog.Builder(context)
        val customDialog = LayoutInflater.from(context).inflate(source, null)
        builder.setView(customDialog)
        builder.setCancelable(cancelable)
        return builder.create()
    }

    fun openTimePicker(context: Context, textView: TextView)
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timeChoose = TimePickerDialog(context, R.style.DatePicker, { _, mHour, mMinute ->
            textView.setText(String.format("%02d:%02d", mHour, mMinute))
        }, hour, minute, true)

        timeChoose.show()
        timeChoose.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(context, R.color.blue_primary))
        timeChoose.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(context, R.color.blue_primary))
    }

    fun openDatePicker(context: Context, textView: TextView)
    {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(context, R.style.DatePicker, { _, mYear, mMonth, mDay ->
            textView.setText(String.format("%02d/%02d/%04d", mDay, mMonth + 1, mYear))
        }, year, month + 1, day)

        dialog.show()
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(context, R.color.blue_primary))
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(context, R.color.blue_primary))
    }
}