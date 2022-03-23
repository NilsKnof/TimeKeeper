package com.timekeeper;

import java.awt.*;

public class Tray {
    private static final Tray instance = new Tray();
    private final TrayIcon trayIcon;
    private final MenuItem switchStatus, exit;
    private final Image offIcon, onIcon;

    private Tray(){
        SystemTray systemTray = SystemTray.getSystemTray();
        offIcon = Toolkit.getDefaultToolkit().getImage(TimeKeeper.class.getResource("redIcon.png"));
        onIcon = Toolkit.getDefaultToolkit().getImage(TimeKeeper.class.getResource("greenIcon.png"));

        trayIcon = new TrayIcon(offIcon);
        PopupMenu popupMenu = new PopupMenu();
        switchStatus = new MenuItem("Start");
        exit = new MenuItem("Close");
        popupMenu.add(switchStatus);
        popupMenu.add(exit);
        trayIcon.setPopupMenu(popupMenu);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public MenuItem getSwitchStatus() {
        return switchStatus;
    }

    public MenuItem getExit() {
        return exit;
    }

    public static Tray getInstance(){
        return instance;
    }

    public Image getOffIcon() {
        return offIcon;
    }

    public Image getOnIcon() {
        return onIcon;
    }
}
