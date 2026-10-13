package com.example.wordyl;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HelloController {

    @FXML
    private Label infor;

    @FXML
    private GridPane wordGrid;

    @FXML
    private VBox keyboardContainer;

    private final Label[][] labels = new Label[6][5];

    private final Map<Character, Button> keyboardButtons = new HashMap<>();
    private final Map<Character, Integer> keyboardStatus = new HashMap<>();

    private Set<String> allowedWords = new HashSet<>();
    private Set<String> allowedSecrets = new HashSet<>();
    private String secretWord;

    private int currentRow = 0;
    private int currentCol = 0;
    private int wygrane = 0;
    private int przegrane = 0;

    @FXML
    public void initialize() {
        for (Node node : wordGrid.getChildren()) {
            if (node instanceof Label) {
                Integer rowIndex = GridPane.getRowIndex(node);
                Integer colIndex = GridPane.getColumnIndex(node);

                int row = (rowIndex == null) ? 0 : rowIndex;
                int col = (colIndex == null) ? 0 : colIndex;

                labels[row][col] = (Label) node;
            }
        }

        for (Node rowNode : keyboardContainer.getChildren()) {
            if (rowNode instanceof HBox) {
                for (Node btnNode : ((HBox) rowNode).getChildren()) {
                    if (btnNode instanceof Button) {
                        Button btn = (Button) btnNode;
                        String text = btn.getText();
                        if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
                            keyboardButtons.put(text.charAt(0), btn);
                        }
                    }
                }
            }
        }
        allowedWords = loadWordsFromFile("valid-wordle-words.txt");
        allowedSecrets = loadWordsFromFile("wordle-answers-alphabetical.txt");

        // 4. Losujemy sekretne słowo
        if (!allowedSecrets.isEmpty()) {
            List<String> wordList = new ArrayList<>(allowedSecrets);
            Random random = new Random();
            secretWord = wordList.get(random.nextInt(wordList.size()));
            System.out.println("Wylosowano sekretne słowo (do testów): " + secretWord);
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (labels[i][j] != null) {
                    labels[i][j].setStyle("-fx-border-color: #3a3a3c; -fx-border-width: 2px; -fx-background-color: #121213; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px;");
                }
            }
        }
    }

    private Set<String> loadWordsFromFile(String fileName) {
        Set<String> wordSet = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(fileName), StandardCharsets.UTF_8))) {

            if (reader == null) {
                System.out.println("Plik nie został znaleziony" + fileName);
                return wordSet;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String cleanWord = line.trim().toUpperCase();
                if (cleanWord.length() == 5) {
                    wordSet.add(cleanWord);
                }
            }
        } catch (Exception e) {
            System.out.println("Błąd podczas wczytywania pliku: " + fileName);
            e.printStackTrace();
        }

        System.out.println("Wczytano pomyślnie słów: " + wordSet.size() + " z pliku: " + fileName);
        return wordSet;
    }

    public void initKeyboardListener(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code.isLetterKey() && code.name().length() == 1) {
            handleLetterInput(code.name());
        } else if (code == KeyCode.BACK_SPACE) {
            handleBackspace();
        } else if (code == KeyCode.ENTER) {
            handleEnter();
        }
    }

    private void handleLetterInput(String letter) {
        if (currentCol < 5) {
            labels[currentRow][currentCol].setText(letter);
            currentCol++;
        }
    }

    private void handleBackspace() {
        if (currentCol > 0) {
            currentCol--;
            labels[currentRow][currentCol].setText("");
        }
    }

    private void handleEnter() {
        if (currentCol == 5) {
            String word = "";
            for (int i = 0; i < 5; i++) {
                word += labels[currentRow][i].getText();
            }

            if (!allowedWords.contains(word)) {
                infor.setText("Nie ma takiego słowa w słowniku!");
                infor.setTextFill(Color.RED);
                return;
            }

            ifCorrect(word);
            infor.setTextFill(Color.GREEN);
            infor.setText("Zatwierdzono słowo w wierszu: " + word);

            if (word.equals(secretWord)) {
                wygrane++;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Wygrales");
                alert.setHeaderText("Oczekiwane slowo to: " + secretWord);
                alert.setContentText("mega git mordeczko Przegranych w sesji: " + przegrane + ", wygranych w sesji: " + wygrane);
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    restart();
                }
                return;
            }

            currentRow++;
            currentCol = 0;

            if (currentRow == 6 && !word.equals(secretWord)) {
                przegrane++;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Przegrales");
                alert.setHeaderText("Oczekiwane slowo to: " + secretWord);
                alert.setContentText("mega lipa Przegranych w sesji: " + przegrane + ", wygranych w sesji: " + wygrane);
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    restart();
                }
            }
        } else {
            infor.setText("Za mało liter!");
            infor.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleScreenLetter(ActionEvent event) {
        Button btn = (Button) event.getSource();
        handleLetterInput(btn.getText());
    }

    @FXML
    private void handleScreenBackspace(ActionEvent event) {
        handleBackspace();
    }

    @FXML
    private void handleScreenEnter(ActionEvent event) {
        handleEnter();
    }

    private void ifCorrect(String guess) {
        char[] secretArr = secretWord.toCharArray();
        char[] guessArr = guess.toCharArray();
        for (int i = 0; i < 5; i++) {
            char guessedChar = guessArr[i];
            if (guessedChar == secretArr[i]) {
                labels[currentRow][i].setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 24px;");
                updateKeyboardKey(guessedChar, 3); // 3 = zielony (najwyższy priorytet)
            } else {
                boolean found = false;
                for (int j = 0; j < 5; j++) {
                    if (guessedChar == secretArr[j]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    labels[currentRow][i].setStyle("-fx-background-color: yellow; -fx-text-fill: white; -fx-font-size: 24px;");
                    updateKeyboardKey(guessedChar, 2); // 2 = żółty
                } else {
                    labels[currentRow][i].setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-size: 24px;");
                    updateKeyboardKey(guessedChar, 1); // 1 = szary
                }
            }
        }
    }

    // Pomocnicza metoda aktualizująca klawiaturę z uwzględnieniem priorytetów
    private void updateKeyboardKey(char letter, int newPriority) {
        int currentPriority = keyboardStatus.getOrDefault(letter, 0);

        if (newPriority > currentPriority) {
            keyboardStatus.put(letter, newPriority);
            Button btn = keyboardButtons.get(letter);
            if (btn != null) {
                if (newPriority == 3) {
                    btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                } else if (newPriority == 2) {
                    btn.setStyle("-fx-background-color: yellow; -fx-text-fill: white;");
                } else if (newPriority == 1) {
                    btn.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");
                }
            }
        }
    }

    private void restart() {
        currentRow = 0;
        currentCol = 0;

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                labels[i][j].setText("");
                labels[i][j].setStyle("-fx-border-color: #3a3a3c; -fx-border-width: 2px; -fx-background-color: #121213; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            }
        }

        for (Button btn : keyboardButtons.values()) {
            btn.setStyle("");
        }
        keyboardStatus.clear();

        infor.setText("");
        if (!allowedSecrets.isEmpty()) {
            List<String> wordList = new ArrayList<>(allowedSecrets);
            Random random = new Random();
            secretWord = wordList.get(random.nextInt(wordList.size()));
            System.out.println("Wylosowano sekretne słowo (do testów): " + secretWord);
        }
    }
}