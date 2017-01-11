package com.example.lenin.calculator;

import android.util.Log;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {
    private TextView operationText, resultText;
    private ArrayList<View> allButtons;
    private double resultMemory;


    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate device*************");
        operationText = (TextView)findViewById(R.id.operationLabel);
        resultText = (TextView)findViewById(R.id.resultLabel);
        resultMemory = 0;

        GridLayout pad = (GridLayout)findViewById(R.id.button_container);
        for (int i=0; i<pad.getChildCount(); i++) {
            View v = pad.getChildAt(i);
            if (v instanceof Button) {

            }
        }

        // Get all buttons in GridLayout
        allButtons = ((GridLayout)findViewById(R.id.button_container)).getTouchables();
        while (allButtons.iterator().hasNext()){
            allButtons.iterator().next().toString();
            Log.i(TAG, "******* Iterator " + allButtons.iterator().next().toString());
        }

    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick " + v.getId());
        switch (v.getId()) {
            case R.id.ce:
                clearEntry();
                break;
            case R.id.c:
                clearAll();
                break;
            case R.id.back:
                backCharacter();
                break;
            case R.id.equal:
                equalButton(operationText.getText().toString());
                break;
            default:
                Button  bt = (Button)findViewById(v.getId());
                Log.i(TAG, "button" + bt.toString());
                updateOperationText(bt.getText().toString());
                break;
        }
    }

    private void equalButton(String s) {
        double r  = evalMath(s);
        updateTextView(resultText, Double.toString(r));
        resultMemory = r;
    }

    // CE button action
    private void clearEntry(){
        resetOperationText();
    }

    // C button action
    private void clearAll(){
        resetOperationText();
        resetResultText();
        resetResultMemory();
    }

    private void backCharacter(){
        String boxTex = operationText.getText().toString();
        if (boxTex.length()>0) {
            // delete last written character
            updateTextView(operationText, boxTex.substring(0, boxTex.length()-2));
        }
    }

    // reset result memory
    private void resetResultMemory(){
        resultMemory = 0;
    }

    //update the text of operationTextView
    private void updateOperationText(String txt){
        String boxTex = operationText.getText().toString();
        Log.i(TAG, "Text: " + boxTex);
        /*Ternary operator. e.g.: minVal = (a < b) ? a : b; */
        updateTextView(operationText, (boxTex == "0") ? txt: boxTex + txt);;
    }

    //reset to default text of operationTextView
    private void resetOperationText(){
        updateTextView(operationText, "0");
    }

    //reset to default text of resultTextView
    private void resetResultText(){
        updateTextView(resultText, "");
    }

    //update the tex of an  TextView
    private void updateTextView(TextView object, String txt) {
        Log.i(TAG, "Set text to: "+ object.toString());
        object.setText(txt);
        Log.i(TAG, "New Text: " + object.getText().toString());
    }

    public static double evalMath(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
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
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}
