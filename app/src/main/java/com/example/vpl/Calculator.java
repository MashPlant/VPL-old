package com.example.vpl;

/**
 * Created by MashPlant on 2016/4/30.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Calculator {
    public static final Double e;
    public static final Double pi;
    private static HashMap<Character, Integer> priority;
    SquareWave squareWave = new SquareWave();
    private double ans;
    private ArrayList exp;
    private Stack<Double> num;
    private Stack<Character> opt;
    private String rawStr;
    private double result;
    private String str;
    private ArrayList<Integer> types;// CHAR=0,NUM=1,PARA=2

    static {
        pi = 3.141592653589793;
        e = 2.718281828459045;
        Calculator.priority = new HashMap<Character, Integer>() {
            {
                this.put('(', 0);
                this.put('+', 1);
                this.put('-', 1);
                this.put('*', 2);
                this.put('/', 2);
                this.put('^', 3);
                this.put('g', 3);
                this.put('p', 3);
                this.put('P', 3);
                this.put('s', 3);
                this.put('S', 3);
                this.put('c', 3);
                this.put('l', 3);
                this.put('L', 3);
                this.put('!', 4);
            }
        };
    }

    public String getExp() {
        return str;
    }

    public Calculator(final String s) {
        this.ans = 0.0;
        String str = s;
        if (s.isEmpty()) {
            str = "0";
        }
        this.str = str;
        this.preTreatment();
        this.num = new Stack<>();
        this.opt = new Stack<>();
        this.exp = new ArrayList();
        this.types = new ArrayList<>();
        this.result = 0.0;
        this.ans = 0.0;
    }

    private void calculate(double parameter) {
        this.num.clear();
        this.opt.clear();
        int numOfPara = 0;
        for (int i = 0; i < this.types.size(); ++i) {
            switch (this.types.get(i)) {
                case 0: {
                    final char charValue = (char) this.exp.get(i - numOfPara);
                    if (charValue != 'g' && charValue != 'S' && charValue != '!' && charValue != 's' && charValue != 'L' && charValue != 'l'
                            && charValue != 'c') {
                        final double doubleValue = this.num.pop();
                        final double doubleValue2 = this.num.pop();
                        switch (charValue) {
                            case 43: {
                                this.num.push(doubleValue2 + doubleValue);
                                break;
                            }
                            case 45: {
                                this.num.push(doubleValue2 - doubleValue);
                                break;
                            }
                            case 42: {
                                this.num.push(doubleValue2 * doubleValue);
                                break;
                            }
                            case 47: {
                                this.num.push(doubleValue2 / doubleValue);
                                break;
                            }
                            case 94:
                            case 112: {
                                this.num.push(Math.pow(doubleValue2, doubleValue));
                                break;
                            }
                            case 80: {
                                this.num.push(Math.pow(doubleValue2, -doubleValue));
                                break;
                            }
                        }
                    } else {
                        final double doubleValue3 = this.num.pop();
                        switch (charValue) {
                            case 'g': {
                                this.num.push(Math.sqrt(doubleValue3));
                                break;
                            }
                            case 'S': {
                                this.num.push(this.squareWave.getValue(doubleValue3));
                                break;
                            }
                            case 33: {
                                this.num.push(this.factor(doubleValue3));
                                break;
                            }
                            case 115: {
                                this.num.push(Math.sin(doubleValue3));
                                break;
                            }
                            case 99: {
                                this.num.push(Math.cos(doubleValue3));
                                break;
                            }
                            case 108: {
                                this.num.push(Math.log10(doubleValue3));
                                break;
                            }
                            case 76: {
                                this.num.push(Math.log(doubleValue3));
                                break;
                            }
                        }
                    }
                }
                break;
                case 1:
                    this.num.push((Double) this.exp.get(i - numOfPara));
                    break;
                default:
                    this.num.push(parameter);
                    numOfPara += 1;
                    break;
            }
        }
        this.result = this.num.pop();
    }

    private double factor(final double n) {
        if (n == 1.0 || n == 0.0) {
            return 1.0;
        }
        return this.factor(n - 1.0) * n;
    }

    private int getPriority(final char c) {
        return Calculator.priority.get(c);
    }

    private boolean isDigit(final char c) {
        for (char c2 = '0'; c2 <= '9'; ++c2) {
            if (c2 == c) {
                return true;
            }
        }
        return false;
    }

    private boolean isLetter(final char c) {
        int c1 = (int) c;
        return (c1 <= 122 && c1 >= 97) || (c1 >= 65 && c1 <= 90);
    }

    private void preTreatment() {
        String substring = "";
        for (int i = 0; i < this.str.length(); ++i) {
            String string = substring;
            if (this.str.charAt(i) == '(') {
                string = substring + ")";
            }
            substring = string;
            if (this.str.charAt(i) == ')') {
                substring = string;
                if (!string.isEmpty()) {
                    substring = string.substring(0, string.length() - 1);
                }
            }
        }
        this.str += substring;
        for (int j = 0; j < this.str.length() - 1; ++j) {
            if (this.isDigit(this.str.charAt(j)) && (this.str.charAt(j + 1) == '√' || this.isLetter(this.str.charAt(j + 1)))) {
                final StringBuffer sb = new StringBuffer(this.str);
                sb.insert(j + 1, "*");
                this.str = sb.toString();
            }
        }
        this.str = this.str.replace("|", "");
        this.str = this.str.replace("E", "10^");
        this.str = this.str.replace("√", "g");
        this.str = this.str.replace("^+", "p");
        this.str = this.str.replace("^-", "P");
        this.str = this.str.replace("pi", Calculator.pi.toString());
        this.str = this.str.replace("e", Calculator.e.toString());
        this.str = this.str.replace("sin", "s");
        this.str = this.str.replace("sW", "S");
        this.str = this.str.replace("cos", "c");
        this.str = this.str.replace("lg", "l");
        this.str = this.str.replace("ln", "L");
        this.rawStr = this.str;
    }

    public static double resultOf(final String s) {
        return new Calculator(s).getResult(0.0);
    }

    private void scan() {
        this.exp.clear();
        this.num.clear();
        this.opt.clear();
        final boolean empty = this.types.isEmpty();
        if (this.str.charAt(0) == '+' || this.str.charAt(0) == '-') {
            this.str = "0" + this.str;
        }
        for (int i = 0; i < this.str.length() - 1; ++i) {
            if (this.str.charAt(i) == '(' && (this.str.charAt(i + 1) == '+' || this.str.charAt(i + 1) == '-')) {
                final StringBuffer sb = new StringBuffer(this.str);
                sb.insert(i + 1, '0');
                this.str = new String(sb);
            }
        }
        int n2 = 0;
        int n3 = 0;
        double n = 10.0;
        int n6;
        for (int j = 0; j < this.str.length(); ++j, n2 = n6) {
            final char char1 = this.str.charAt(j);
            if (!this.isDigit(char1)) {
                if (char1 == 'T') {
                    this.types.add(2);
                    n6 = n2;
                    continue;
                }
                double n4 = n;
                int n5 = n3;
                if ((n6 = n2) != 0) {
                    n4 = n;
                    n5 = n3;
                    n6 = n2;
                    if (this.str.charAt(j) != '.') {
                        n = 10.0;
                        final boolean b = false;
                        final boolean b2 = false;
                        this.exp.add(this.num.pop());
                        n4 = n;
                        n5 = (b ? 1 : 0);
                        n6 = (b2 ? 1 : 0);
                        if (empty) {
                            this.types.add(1);
                            n6 = (b2 ? 1 : 0);
                            n5 = (b ? 1 : 0);
                            n4 = n;
                        }
                    }
                }
                if (this.str.charAt(j) == '.') {
                    n3 = 1;
                    n = n4;
                } else if (this.str.charAt(j) == 'a') {
                    this.num.push(this.ans);
                    n = n4;
                    n3 = n5;
                } else {
                    n6 = 0;
                    if (char1 == ')') {
                        while (this.opt.peek() != '(') {
                            this.exp.add(this.opt.pop());
                            if (empty) {
                                this.types.add(0);
                            }
                        }
                        this.opt.pop();
                        n = n4;
                        n3 = n5;
                    } else if (this.opt.isEmpty() || this.getPriority(char1) > this.getPriority(this.opt.peek())
                            || char1 == '(') {
                        this.opt.push(char1);
                        n = n4;
                        n3 = n5;
                    } else {
                        do {
                            this.exp.add(this.opt.pop());
                            if (empty) {
                                this.types.add(0);
                            }
                        }
                        while (!this.opt.isEmpty() && this.getPriority(char1) <= this.getPriority(this.opt.peek())
                                && this.opt.peek() != '(');
                        this.opt.push(char1);
                        n = n4;
                        n3 = n5;
                    }
                }
            } else {
                if (n3 == 0) {
                    if (n2 != 0) {
                        this.num.push(10.0 * this.num.pop() + this.str.charAt(j) - 48.0);
                    } else {
                        this.num.push((double) (this.str.charAt(j) - '0'));
                    }
                } else {
                    this.num.push((this.str.charAt(j) - '0') / n + this.num.pop());
                    n *= 10.0;
                }
                n6 = 1;
            }
        }
        while (!this.num.isEmpty()) {
            this.exp.add(this.num.pop());
            if (empty) {
                this.types.add(1);
            }
        }
        while (!this.opt.isEmpty()) {
            this.exp.add(this.opt.pop());
            if (empty) {
                this.types.add(0);
            }
        }
    }

    public double getResult(double parameter) {
        if (this.types.isEmpty())
            this.scan();
        this.calculate(parameter);
        return this.ans = this.result;
    }

    public void constructSquareWave(ArrayList<Calculator> calculators, ArrayList<Double> times, double T) {
        this.squareWave.calculators = calculators;
        this.squareWave.times = times;
        this.squareWave.T = T;
    }

    class SquareWave {
        ArrayList<Calculator> calculators;
        ArrayList<Double> times;
        double T = 1;

        public double getValue(double t) {
            double t0 = (t - (int) (t / T) * T) / T;
            for (int i = 0; i < times.size(); i++) {
                if (times.get(i) >= t0) {
                    return calculators.get(i).getResult(t0 * T);
                }
            }
            return calculators.get(calculators.size() - 1).getResult(t0 * T);
        }
    }

}