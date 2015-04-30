package edu.cmu.andrew.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Aditi on 4/23/2015.
 */
public class PopupManager {

   // @Override
    public void onCreate(Bundle savedInstanceState) {
      //  super.onCreate(savedInstanceState);
      //  setContentView(R.layout.main);
        //init();
        //popupInit();
    }

   /* public void init() {
       // popupButton = (Button) findViewById(R.id.popupbutton);
        popupText = new TextView(this);
        insidePopupButton = new Button(this);
        layoutOfPopup = new LinearLayout(this);
        insidePopupButton.setText("OK");
        popupText.setText("This is Popup Window.press OK to dismiss         it.");
        popupText.setPadding(0, 0, 0, 20);
        layoutOfPopup.setOrientation(1);
        layoutOfPopup.addView(popupText);
        layoutOfPopup.addView(insidePopupButton);
    }

    public void popupInit() {
        popupButton.setOnClickListener(this);
        insidePopupButton.setOnClickListener(this);
        popupMessage = new PopupWindow(layoutOfPopup, LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        popupMessage.setContentView(layoutOfPopup);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.popupbutton) {
            popupMessage.showAsDropDown(popupButton, 0, 0);
        }

        else {
            popupMessage.dismiss();
        }
    }
}
*/
}
