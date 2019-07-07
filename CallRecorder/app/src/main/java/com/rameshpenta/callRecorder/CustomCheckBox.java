package com.rameshpenta.callRecorder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by Sujatha on 16-11-2015.
 */
public class CustomCheckBox extends CheckBox {
    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        setChecked(false);

    }
}
