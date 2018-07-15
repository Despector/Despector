package org.spongepowered.despector.util.viewer;

import javax.swing.JTextArea;

public class TabData {

    public final TabType type;
    public String data = "";
    public JTextArea left = new JTextArea();
    public JTextArea right = new JTextArea();

    public TabData(TabType type) {
        this.type = type;
    }

    void update(String dat) {
        this.data = dat;
        this.left.setText(dat);
        this.right.setText(dat);
    }
}
