package com.turnrut.sweep;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Random;

public class Start extends Application {

    private static Random random = new Random();
    private static int rows = 8; // 行数
    private static int columns = 8; // 列数
    private static int sweepCount = 10; // 雷数量
    private int blockSize = 30; // 雷块大小
    private int[][] dataArr;// 数组元素大于等于0表示该位置周围有多少个雷,-1表示雷,-2表示已点开
    private Text[][] textArr;// 界面元素
    private Sweep[] sweepArr;// 所有雷数组
    private StackPane[][] panesArr; // 包裹 Text
    private GridPane gridPane;// 父界面
    // 点开后的背景颜色
    private Background grayBack = new Background(new BackgroundFill(
            new Color(0.753, 0.753, 0.753 , 1),
            null,
            null));
    private boolean gameOver = false; // 游戏是否结束
    private boolean start = false; // 游戏是否开始

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initGame();
        gridPane = new GridPane();
        for(int x = 0; x < rows; x++)
            gridPane.addColumn(x, panesArr[x]);
        gridPane.setOnMouseClicked(event -> gridOnClick(event));
        Scene scene = new Scene(gridPane);
        primaryStage.setTitle("sweep");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void initGame() {
        initTextArr();
    }

    /**
     * 初始化界面元素
     */
    void initTextArr() {
        textArr = new Text[columns][rows];
        panesArr = new StackPane[columns][rows];
        for (int x = 0; x < columns; x++)
            for (int y = 0; y < rows; y++) {
                Text text = new Text();
                textArr[x][y] = text;
                StackPane pane = new StackPane();
                pane.setMinWidth(blockSize);
                pane.setMaxWidth(blockSize);
                pane.setMinHeight(blockSize);
                pane.setMaxHeight(blockSize);
                pane.getChildren().add(text);
                pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                panesArr[x][y] = pane;
            }
    }

    void gridOnClick(MouseEvent event) {
        if(gameOver)
            return;
        if(event.getButton() != MouseButton.PRIMARY)
            return;
        int x = (int) event.getX() / blockSize;
        int y = (int) event.getY() / blockSize;
        if(!start)
            initSweepArr(x, y);
        open(x, y);
    }

    /**
     * 点开块
     * @param x
     * @param y
     */
    void open(int x, int y) {
        if(dataArr[x][y] == -1){
            gameOver();
            return;
        }
        if(dataArr[x][y] == -2)
            return;
        int sweepNum = dataArr[x][y];
        panesArr[x][y].setBackground(grayBack);
        if(sweepNum != 0){
            textArr[x][y].setText(sweepNum + "");
            textArr[x][y].setFill(getColor(sweepNum));
        }
        dataArr[x][y] = -2;
        if(sweepNum == 0){
            if (x > 0) { // 左
                open(x - 1, y);
                if(y > 0) // 左上
                    open(x - 1, y - 1);
                if(y < rows-1) // 左下
                    open(x - 1, y + 1);
            }
            if(x < columns - 1){ //右
                open(x + 1, y);
                if (y > 0) // 右上
                    open(x + 1, y - 1);
                if(y < rows - 1) // 右下
                    open(x + 1, y + 1);
            }
            if(y > 0) // 上
                open(x, y - 1);
            if(y < rows - 1) // 下
                open(x, y + 1);
        }
    }

    /**
     * 点开块后数字颜色
     * @param num
     * @return
     */
    Color getColor(int num) {
        switch (num) {
            case 1:
                return Color.BLUE;
            case 2:
                return new Color(0, 0.5, 0, 1);
            case 3:
                return Color.RED;
            case 4:
                return new Color(0, 0, 0.5, 1);
            case 5:
                return new Color(0.5, 0, 0, 1);
            default:
                return Color.BLACK;
        }
    }

    /**
     * 游戏结束
     */
    void gameOver() {
        gameOver = true;
        for (Sweep sweep : sweepArr){
            int x = sweep.x;
            int y = sweep.y;
            textArr[x][y].setText("X");
            textArr[x][y].setFill(Color.RED);
            panesArr[x][y].setBackground(grayBack);
        }
    }

    /**
     * 初始化雷
     */
    void initSweepArr(int x, int y) {
        Sweep click = new Sweep(x, y);
        sweepArr = new Sweep[sweepCount];
        out:
        for (int i = 0; i < sweepCount; ) {
            Sweep sweep = new Sweep();
            if(sweep.equals(click))
                continue;
            for(int j = 0; j < i; j++)
                if(sweepArr[j].equals(sweep))
                    continue out;
            sweepArr[i++] = sweep;
        }
        initDataArr();
        start = true;
    }

    /**
     * 初始化数据
     */
    void initDataArr() {
        dataArr = new int[columns][rows];
        for(Sweep sweep : sweepArr)
            dataArr[sweep.x][sweep.y] = -1;
        for (Sweep sweep : sweepArr) {
            int x = sweep.x;
            int y = sweep.y;
            if(x > 0){ // 该位置左侧
                if(dataArr[x-1][y] >= 0) // 正左边
                    dataArr[x-1][y]++;
                if(y > 0) // 左上
                    if(dataArr[x-1][y-1] >= 0)
                        dataArr[x-1][y-1]++;
                if(y < rows-1) //左下
                    if(dataArr[x-1][y+1] >= 0)
                        dataArr[x-1][y+1]++;
            }
            if(x < columns-1){ // 右侧
                if(dataArr[x+1][y] >= 0) // 正右
                    dataArr[x+1][y]++;
                if(y > 0) // 右上
                    if(dataArr[x+1][y-1] >= 0)
                        dataArr[x+1][y-1]++;
                if(y < rows-1) //右下
                    if(dataArr[x+1][y+1] >= 0)
                        dataArr[x+1][y+1]++;
            }
            if(y > 0) // 正上
                if(dataArr[x][y-1] >= 0)
                    dataArr[x][y-1]++;
            if(y < rows-1) // 正下
                if(dataArr[x][y+1] >= 0)
                    dataArr[x][y+1]++;
        }
    }

    /**
     * 雷实体类
     */
    static class Sweep {
        int x;// x 坐标
        int y;// y 坐标

        Sweep() {
            x = random.nextInt(rows);
            y = random.nextInt(columns);
        }

        Sweep(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Sweep))
                return false;
            Sweep other = (Sweep)obj;
            if(other.x != x || other.y != y)
                return false;
            return true;
        }
    }
}
