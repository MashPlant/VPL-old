package com.example.vpl;

/**
 * Created by MashPlant on 2016/4/16.
 */
public class SettingItem {
    static final int NOTHING = 0;
    static final int CHOOSE = 1;
    static final int INPUT = 2;
    static final int SWINPUT = 3;
    private String name;
    private String hint;
    private int kind;

    public SettingItem(String name, String hint, int arg_kind) {
        this.name = name;
        this.hint = hint;
        kind = arg_kind;
    }

    public String getName() {
        return name;
    }

    public String getHint() {
        return hint;
    }

    public int getKind() {
        return kind;
    }
}
