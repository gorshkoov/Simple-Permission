package ru.example.simplepermission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MainFragment: Fragment(R.layout.fragment_main) {

    private val permissionsLauncher by requestPermissionLauncher { status ->
        when(status) {
            PermissionStatus.Granted -> msgView.setText(R.string.msg_granted)
            PermissionStatus.Denied -> msgView.setText(R.string.msg_denied)
            PermissionStatus.ShowRationale -> msgView.setText(R.string.msg_rationale)
        }
    }

    private lateinit var msgView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        msgView = view.findViewById(R.id.msg_text)

        view.findViewById<Button>(R.id.request_button).setOnClickListener {
            permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }

        view.findViewById<Button>(R.id.settings_button).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            requireContext().startActivity(intent)
        }
    }
}