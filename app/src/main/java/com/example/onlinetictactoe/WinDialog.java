package com.example.onlinetictactoe;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class WinDialog extends Dialog {

    private String message;
    private final MainActivity mainActivity;
    public WinDialog(@NonNull Context context, String s) {
        super(context);
        this.message = message;
        this.mainActivity =((MainActivity) context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_dialog_layout);

        final TextView messageTV = findViewById(R.id.messageTV);
        final Button startBtn = findViewById(R.id.startNewBtn);

        messageTV.setText(message);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getContext().startActivity(new Intent(getContext(), PlayerName.class));
                mainActivity.finish();
            }
        });
    }
}
