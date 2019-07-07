package com.rameshpenta.callRecorder;

/**
 * Created by Sujatha on 12-07-2015.
 */
public class TitleBarMenuItem {

    private String titleName;
    private  int   title_icon;


    TitleBarMenuItem(String titleName, int title_icon)
    {

        this.titleName=titleName;
        this.title_icon=title_icon;

    }

    public String getTitle()
    {

        return titleName;
    }
    public int getTitle_icon()
    {

        return title_icon;
    }
}
