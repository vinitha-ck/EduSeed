package com.vpk.eduseed

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class ThirdDialogFragment : DialogFragment() {
    private lateinit var editTextText: EditText
    private lateinit var editTextSubtext: EditText
    private var listener: ThirdDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ThirdDialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ThirdDialogListener")
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.activity_third, null)

        editTextText = view.findViewById(R.id.editTextText)
        editTextSubtext = view.findViewById(R.id.editTextSubtext)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)


        // Cancel Button Click
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Save Button Click
        saveButton.setOnClickListener {
            val updatedText = editTextText.text.toString()
            val updatedSubtext = editTextSubtext.text.toString()
            listener?.onTaskSaved(updatedText, updatedSubtext)
            dismiss()
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    interface ThirdDialogListener {
        fun onTaskSaved(text: String, subtext: String)
    }


}
