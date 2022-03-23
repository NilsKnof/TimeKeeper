package com.timekeeper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TKController {
    private final Tray tray = Tray.getInstance();
    private final JSONParser jsonParser;
    private boolean status;
    private JSONObject currentTime;

    @FXML
    private Button btnStartStop;
    @FXML
    private VBox failed;
    @FXML
    private HBox hbDatePicker;
    @FXML
    private TextField stopTime;
    @FXML
    private DatePicker stopDate;
    @FXML
    private DatePicker dpStart, dpStop;

    public TKController() {
        tray.getSwitchStatus().addActionListener(e -> onStartStopButtonClick());
        tray.getExit().addActionListener(e -> {
            if (status) onStartStopButtonClick();
            System.exit(0);
        });
        tray.getTrayIcon().addActionListener(e -> {if(status) calculateOngoingDuration();});
        jsonParser = new JSONParser();
        status = false;
        checkIfSystemCrash();
    }

    @FXML
    protected void onWebButtonClick() {
        if (!hbDatePicker.isVisible() && !status) {
            hbDatePicker.setVisible(true);
            return;
        }
//        Desktop desktop = Desktop.getDesktop();
//        try {
//            Desktop.getDesktop().browse(new URI("file:///index.html"));
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
        hbDatePicker.setVisible(false);
    }

    @FXML
    protected void onStartStopButtonClick() {

        if (hbDatePicker.isVisible()) {
            btnStartStop.setText("");
            calculateTimeFrame();
            dpStop.setValue(null);
            dpStart.setValue(null);
            hbDatePicker.setVisible(false);
        }
        else if (!failed.isVisible()) {
            if (!status) {
                startTime();
                btnStartStop.setStyle("-fx-background-color: green; -fx-background-radius: 100em");
                btnStartStop.setText("");
                tray.getTrayIcon().setImage(tray.getOnIcon());
                tray.getSwitchStatus().setLabel("Stop");
            } else {
                stopTime();
                btnStartStop.setStyle("-fx-background-color: red; -fx-background-radius: 100em");
                tray.getTrayIcon().setImage(tray.getOffIcon());
                tray.getSwitchStatus().setLabel("Start");
            }
            status = !status;
        } else {
            if (stopDate.getValue() != null && checkTime(stopTime.getText())) {
                replaceStopTime();
                failed.setVisible(false);
            }
        }
    }

    private void calculateTimeFrame(){
        LocalDate stop, start;
        int hours = 0, minutes = 0;
        start = (dpStart.getValue() == null) ? null : dpStart.getValue();
        stop = (dpStop.getValue() == null) ? LocalDate.now() : dpStop.getValue();
        if (start == null || start.isAfter(stop)) return;
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            for (Object data : jsonArray) {
                JSONObject cache = (JSONObject) data;
                LocalDate cacheStart = LocalDate.parse(cache.get("startDate").toString());
                LocalDate cacheStop = LocalDate.parse(cache.get("stopDate").toString());
                if ((cacheStart.equals(start) || cacheStart.isAfter(start)) && (stop.isAfter(cacheStop) || stop.equals(cacheStop))) {
                    String[] time = cache.get("duration").toString().split(":");
                    System.out.println( time[0]+"h"+time[1]+"m");
                    hours += Integer.parseInt(time[0]);
                    minutes += Integer.parseInt(time[1]);
                }
            }
            hours += minutes / 60;
            minutes %= 60;
            btnStartStop.setText(hours + "h" + minutes + "m");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void startTime() {
        try {
            currentTime = new JSONObject();
            currentTime.put("startDate", LocalDate.now().toString());
            currentTime.put("stopDate", "");
            if (LocalTime.now().getSecond() >= 30)
                currentTime.put("startTime", LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
            else
                currentTime.put("startTime", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            currentTime.put("stopTime", "");
            currentTime.put("duration", "");

            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            jsonArray.add(currentTime);
            FileWriter fileWriter = new FileWriter("database.json");
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void stopTime() {
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            currentTime.replace("stopDate", LocalDate.now().toString());
            if (LocalTime.now().getSecond() >= 30)
                currentTime.replace("stopTime", LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));
            else
                currentTime.replace("stopTime", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            updateStopTime(jsonArray);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceStopTime() {
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            currentTime = (JSONObject)jsonArray.get(jsonArray.size()-1);
            currentTime.replace("stopDate", stopDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            currentTime.replace("stopTime", stopTime.getText());
            updateStopTime(jsonArray);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateStopTime(JSONArray jsonArray) {
        try {
            Duration duration = calculateDuration();
            btnStartStop.setText(duration.toHours() + "h" + duration.toMinutesPart() + "m");
            currentTime.replace("duration", duration.toHours() + ":" + duration.toMinutesPart());
            jsonArray.set(jsonArray.size()-1, currentTime);
            FileWriter fileWriter = new FileWriter("database.json");
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIfSystemCrash() {
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            if  (jsonArray.size() == 0) return;
            JSONObject lastEntry = (JSONObject) jsonArray.get(jsonArray.size()-1);
            if (lastEntry.get("stopTime").equals("")) Platform.runLater(() -> failed.setVisible(true));
        } catch (IOException | ParseException e) {
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter("database.json");
                fileWriter.write("[]");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Duration calculateDuration() {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(currentTime.get("startDate").toString()), LocalTime.parse(currentTime.get("startTime").toString()));
        LocalDateTime stop = LocalDateTime.of(LocalDate.parse(currentTime.get("stopDate").toString()), LocalTime.parse(currentTime.get("stopTime").toString()));
        return Duration.between(start, stop);
    }

    private boolean checkTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalTime.parse(time, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void calculateOngoingDuration() {
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader("database.json"));
            JSONObject currentTime = (JSONObject) jsonArray.get(jsonArray.size()-1);
            LocalDateTime start = LocalDateTime.of(LocalDate.parse(currentTime.get("startDate").toString()), LocalTime.parse(currentTime.get("startTime").toString()));
            Duration duration = Duration.between(start, LocalDateTime.now());
            btnStartStop.setText(duration.toHours() + "h" + duration.toMinutesPart() + "m");
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }
    }
}