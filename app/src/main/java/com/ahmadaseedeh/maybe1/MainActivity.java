package com.example.myapp1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView inputDisplay;
    private TextView resultDisplay;
    private String currentExpression = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // الحاوية الرئيسية للتطبيق
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#121212"));
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // شاشة عرض العملية الحسابية (الإدخال)
        inputDisplay = new TextView(this);
        inputDisplay.setTextSize(26);
        inputDisplay.setTextColor(Color.parseColor("#90A4AE"));
        inputDisplay.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        inputDisplay.setPadding(32, 48, 32, 8);

        // شاشة عرض النتيجة
        resultDisplay = new TextView(this);
        resultDisplay.setTextSize(46);
        resultDisplay.setTextColor(Color.WHITE);
        resultDisplay.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        resultDisplay.setPadding(32, 8, 32, 32);

        mainLayout.addView(inputDisplay);
        mainLayout.addView(resultDisplay);

        // شبكة الأزرار (GridLayout) مضبوطة بالكامل لتتوزع بشكل متساوٍ على الشاشة
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount(5);
        gridLayout.setPadding(16, 16, 16, 16);

        // خصائص توزع الأزرار بانتظام
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        );
        gridLayout.setLayoutParams(gridParams);

        String[] buttons = {
                "C", "(", ")", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", ".", "DEL", "="
        };

        for (String label : buttons) {
            Button button = new Button(this);
            button.setText(label);
            button.setTextSize(22);
            button.setIncludeFontPadding(false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f, 1f);
            params.setMargins(10, 10, 10, 10);
            button.setLayoutParams(params);

            // تخصيص ألوان الأزرار لتكون مرتبة وعصرية
            if (label.equals("=")) {
                button.setBackgroundColor(Color.parseColor("#00E676"));
                button.setTextColor(Color.BLACK);
            } else if (label.equals("C") || label.equals("DEL")) {
                button.setBackgroundColor(Color.parseColor("#D50000"));
                button.setTextColor(Color.WHITE);
            } else if (label.equals("/") || label.equals("*") || label.equals("-") || label.equals("+")) {
                button.setBackgroundColor(Color.parseColor("#FF9100"));
                button.setTextColor(Color.WHITE);
            } else {
                button.setBackgroundColor(Color.parseColor("#212121"));
                button.setTextColor(Color.WHITE);
            }

            button.setOnClickListener(this);
            gridLayout.addView(button);
        }

        mainLayout.addView(gridLayout);
        setContentView(mainLayout);
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        String buttonText = b.getText().toString();

        switch (buttonText) {
            case "C":
                currentExpression = "";
                resultDisplay.setText("");
                break;
            case "DEL":
                if (currentExpression.length() > 0) {
                    currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
                }
                break;
            case "=":
                try {
                    double result = evaluate(currentExpression);
                    if (result == (long) result) {
                        resultDisplay.setText(String.format("%d", (long) result));
                    } else {
                        resultDisplay.setText(String.format("%s", result));
                    }
                } catch (Exception e) {
                    resultDisplay.setText("خطأ");
                }
                break;
            default:
                currentExpression += buttonText;
                break;
        }

        inputDisplay.setText(currentExpression);
    }

    // محرك الحساب الذاتي الداخلي
    private double evaluate(String expression) {
        return new Object() {
            private int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
