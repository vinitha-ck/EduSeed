package com.vpk.eduseed

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

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
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_third, null)
        editTextText = view.findViewById(R.id.editTextText)
        editTextSubtext = view.findViewById(R.id.editTextSubtext)

        val text = arguments?.getString("text", "") ?: ""
        val subtext = arguments?.getString("subtext", "") ?: ""

        editTextText.setText(text)
        editTextSubtext.setText(subtext)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)

        val dialog = builder.create()

        view.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.saveButton).setOnClickListener {
            listener?.onTaskSaved(editTextText.text.toString(), editTextSubtext.text.toString())
            dialog.dismiss()
        }

        return dialog
    }

    interface ThirdDialogListener {
        fun onTaskSaved(text: String, subtext: String)
        fun onTaskUpdated(position: Int, text: String, subtext: String)
    }
}
