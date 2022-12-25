import java.util.Queue;
import java.util.*;

public class Compiler {

    private Queue<Integer> left;
    private Queue<Integer> right;
    private Queue<String> stringPool;
    private HashMap<String, String> names;
    private Display window = null;
    private String[] codes;
    private String[] cInst;
    public static boolean vsync = false, fullscreen=false, customLoop = false;

    private int ip;
    private boolean printingStacks = false, printingInstructions = false, printingLabels = false, printingStringPool = false;
    private boolean colorizeStack = false, colorizeInstructions = false, colorizeLabels = false;
    private boolean debugOption = false;
    private boolean stringFormatter = false, preWord = true, showFpsUps = false;

    public Compiler(String fileName) {
        codes = Util.readFile(fileName);
        init();
        startCompile();
    }

    public void init(){
        left = new LinkedList<>();
        right = new LinkedList<>();
        stringPool = new LinkedList<>();
        names = new HashMap<>();
    }

    private void startCompile(){
        compileOptions();
        compileLabel();
        compile();
    }

    private void compile(){
        while (ip < codes.length) {
            cInst = codes[ip].split(" ");
            String token = cInst[0];
            if (printingInstructions) printInstructions(token);
            compileTokens(token);
            if (debugOption) {
                if (printingStacks) printStacks();
                if (printingLabels) printLabels();
                if (printingStringPool) printStringPool();
            }
        }
    }

    private void compileOptions() {
        String options = codes[0];
        if (!(options.charAt(0) == '_')) {
            return;
        }

        debugOption = true;
        String[] modifiedOptions = new String[codes.length - 1];
        System.arraycopy(codes, 1, modifiedOptions, 0, codes.length - 1);
        codes = modifiedOptions;
        for (int i = 0; i < options.length(); i++) {
            switch (options.charAt(i)) {
                case 'S':
                    colorizeStack = true;
                case 's':
                    printingStacks = true;
                    break;
                case 'I':
                    colorizeInstructions = true;
                case 'i':
                    printingInstructions = true;
                    break;
                case 'L':
                    colorizeLabels = true;
                case 'l':
                    printingLabels = true;
                    break;
                case 'p':
                    printingStringPool = true;
                    break;
                case 'f':
                    stringFormatter = true;
                    break;
                case 'F':
                    showFpsUps = true;
                    break;
                case 'c':
                    preWord = false;
                    break;
                case 'V':
                    vsync = true;
                    break;
                case 'U':
                    fullscreen=true;
                    break;
                case 'C':
                    customLoop=true;
                    break;
                default:
            }
        }

    }

    private void compileLabel() {
        for (int i = 0; i < codes.length; i++) {
            char token = codes[i].charAt(0);
            if (token == '{') {
                String label = codes[i].substring(1);
                names.put(label, i + "");
            }
        }
    }

    private void compileTokens(String token) {
        switch (token) {
            case "[":
            case "[.":
            case "].":
            case "]": {
                pushToStack();
                ip++;
                break;
            }
            case "'": {
                pushToStringStack();
                ip++;
                break;
            }
            case "\\/":
                popLeftToRight();
                ip++;
                break;
            case "/\\":
                popRightToLeft();
                ip++;
                break;
            case "()":
                peekRightToLeft();
                ip++;
                break;
            case ")(":
                peekLeftToRight();
                ip++;
                break;
            case "~l":
            case "~r":
            case "~": {
                printToOutput();
                ip++;
                break;
            }
            case "!": {
                ip = doCondition() ? Integer.parseInt(names.get(cInst[4])) : ip + 1;
                break;
            }
            case "+":
            case "concat":
            case "-":
            case "*":
            case ":":
            case "%": {
                doArithmetic();
                ip++;
                break;
            }
            case ";": {
                deleteAlias();
                ip++;
                break;
            }
            case "sin":
            case "cos":
            case "tan":
            case "atan":
            case "atan2":
            case "pow":
            case "sqrt":
                doTrigonometric();
                ip++;
                break;
            case "^":
            case ">":
            case "<":
            case "|":
            case "&": {
                bitOperation();
                ip++;
                break;
            }
            case "WC":
            case "WBC":
            case "WR":
            case "WU":
            case "WSA2S":
            case "WNL":
            case "WFPS":
            case "GCS":
            case "SCS":
                doGraphics();
                ip++;
                break;
            case "#WRECT":
            case "#WRECTC":
                addShape();
                ip++;
                break;
            case "#":
                addAlias();
                ip++;
                break;
            case "$":
                try {
                    Thread.sleep(Integer.parseInt(getValue(cInst[1])));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ip++;
                break;
            case "BD":
                convertBinaryToDecimal();
                ip++;
                break;
            case "DB":
                convertDecimalToBinary();
                ip++;
                break;
            case "rand":
                addRandom();
                ip++;
                break;
            default: {
                if (token.startsWith("}")) {
                    gotoLabel();
                    break;
                } else if (token.startsWith("{")) {
                    ip++;
                    break;
                }
                doException("keyword is not implemented: " + token);
            }
        }
    }



    private void printStacks() {
        String msg = "";
        if (colorizeStack) {
            msg += ColorConst.CYAN + "LEFT: " + left.toString();
            msg += ColorConst.RESET_COLOR;
            msg += ColorConst.PURPLE + " RIGHT: " + right.toString();
        } else {
            msg += "LEFT: " + left.toString() + " RIGHT: " + right.toString();
        }

        System.out.println(msg + "\n");
    }

    private void printInstructions(String token) {
        String msg = ColorConst.GREEN + ip + ": " + ColorConst.RESET_COLOR;
        if (colorizeInstructions) {
            msg += ColorConst.YELLOW;
            msg += "instruction: ";
            msg += codes[ip];
            msg += ColorConst.RESET_COLOR;
            msg += ColorConst.RED;
            msg += " token: ";
            msg += token + "\n";
        } else if (printingInstructions) {
            msg += "instruction: " + codes[ip] + " token: " + token + "\n";
        }
        System.out.println(msg + ColorConst.RESET_COLOR);
    }

    private void printLabels() {
        System.out.println("Labels: " + (colorizeLabels ? ColorConst.CYAN : "") + names.toString() + (colorizeLabels ? ColorConst.RESET_COLOR : "") + "\n");
    }

    private void printStringPool() {
        printToConsole("String pool: " + (printingStringPool ? ColorConst.GREEN : "") + stringPool.toString() + ColorConst.RESET_COLOR + "\n");
    }

    private boolean doCondition() {
        int a = Integer.parseInt(getValue(cInst[2]));
        int b = Integer.parseInt(getValue(cInst[3]));
        boolean cond;
        switch (cInst[1]) {
            case "<": {
                cond = a < b;
                break;
            }
            case ">": {
                cond = a > b;
                break;
            }
            case "=": {
                cond = a == b;
                break;
            }
            case "x": {
                cond = a != b;
                break;
            }
            default:
                throw new ArithmeticException("Error with condition checking");
        }
        return cond;
    }

    private void gotoLabel() {
        ip = Integer.parseInt(names.get(cInst[0].substring(1))) + 1;
    }

    private void printToOutput() {
        try {
            switch (cInst[0]) {
                case "~l":
                    printToConsole(ColorConst.BLUE + left.toString() + ColorConst.RESET_COLOR + "\n");
                    break;
                case "~r":
                    printToConsole(ColorConst.BLUE + right.toString() + ColorConst.RESET_COLOR + "\n");
                    break;
                case "~":
                    printToConsole(ColorConst.BLUE + getValue(cInst[1]) + ColorConst.RESET_COLOR + "\n");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            doException("Maybe your syntax is wrong at printing instruction");
        }
    }

    private void pushToStack() {
        switch (cInst[0]) {
            case "[":
                left.add(Integer.parseInt(getValue(cInst[1])));
                break;
            case "]":
                right.add(Integer.parseInt(getValue(cInst[1])));
                break;
            case "].":
                for (int i = 1; i < cInst.length; i++) {
                    right.add(Integer.parseInt(getValue(cInst[i])));
                }
                break;
            case "[.":
                for (int i = 1; i < cInst.length; i++) {
                    left.add(Integer.parseInt(getValue(cInst[i])));
                }
                break;
            default:
                doException("Something is wrong at push string to the stack, maybe wrong syntax, or typo");
        }
    }

    private void pushToStringStack() {
        stringPool.add(cInst[1]);
    }

    private void addAlias() {
        String value;
        String token = cInst[1].charAt(0) + "";
        try {
            switch (token) {
                case "/":
                case "\\":
                case ")":
                case "(": {
                    value = pop(token.charAt(0)) + "";
                    break;
                }
                case "@": {
                    value = getValue(cInst[1]) + "";
                    break;
                }
                case ",":
                case ".": {
                    value = stringPop(token);
                    break;
                }
                default:
                    value = cInst[1];
            }
            names.put(cInst[2], value);
        } catch (ClassCastException e) {
            doException("Your value maybe not that what you need when you try to add to an alias");
        } catch (Exception e) {
            doException("Something wrong at adding alias");
        }

    }

    private void deleteAlias() {
        try {
            names.remove(cInst[1]);
        } catch (Exception e) {
            doException("You are trying to delete an alias that does not exist");
        }
    }

    private void createWindow() {
        int width = Integer.parseInt(getValue(cInst[1]));
        int height = Integer.parseInt(getValue(cInst[2]));
        String title = getValue(cInst[3]);
        if (window == null) {
            window = new Display(width, height, title);
            new Thread(window).start();
        }
        window.setShowFpsUps(showFpsUps);
    }

    private void doGraphics() {

        switch (cInst[0]) {
            case "WC":
                createWindow();
                break;
            case "WR":
                window.render();
                break;
            case "WU":
                window.update();
                break;
            case "WNL":
                window.noLoop();
                while (true) {
                    try {
                        Thread.sleep(1000000000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            case "WFPS":
                window.setFps(Integer.parseInt(getValue(cInst[1])));
                break;
            case "GCS":
                int color = window.getColor(Integer.parseInt(getValue(cInst[1])), Integer.parseInt(getValue(cInst[2])));
                pushIntoToken(color+"", cInst[3]);
                break;
            case "SCS":
                window.setColor(Integer.parseInt(getValue(cInst[1])), Integer.parseInt(getValue(cInst[2])), Integer.parseInt(getValue(cInst[3])));
                break;
            default:
                System.out.println(cInst[0]);
                doException("Unknown command in doGraphics");
        }
    }

    private void popLeftToRight() {
        right.addAll(left);
        left.clear();
    }

    private void popRightToLeft() {
        left.addAll(right);
        right.clear();
    }

    private void peekRightToLeft() {
        left.addAll(right);
    }

    private void peekLeftToRight() {
        right.addAll(left);
    }

    private void addShape() {
        boolean colored = false;
        switch (cInst[0]) {
            case "#WRECT":
                break;
            case "#WRECTC":
                colored = true;
                break;
            default:
                doException("Unknown shape, maybe wrong syntax");
        }
        if (window == null) {
            doException("The display isn't initialized");
        }
        window.addRect(Integer.parseInt(getValue(cInst[1])), Integer.parseInt(getValue(cInst[2])), Integer.parseInt(getValue(cInst[3])), Integer.parseInt(getValue(cInst[4])), colored ? Integer.parseInt(getValue(cInst[5])) : 0);
    }

    private void doArithmetic() {
        int a = 0, b = 0;
        String result = "";
        if (cInst[0].equals("concat")) {
            pushIntoToken(getValue(cInst[1]) + "" + getValue(cInst[2]), cInst[3]);
            return;
        }
        try {
            a = Integer.parseInt(getValue(cInst[1]));
            b = Integer.parseInt(getValue(cInst[2]));
        } catch (Exception e) {
            if ("+".equals(cInst[0])) {
                pushIntoToken((getValue(cInst[1]) + getValue(cInst[2])), cInst[3]);
                return;
            }
        }
        switch (cInst[0].charAt(0)) {
            case '+':
                result = (a + b) + "";
                break;
            case '-':
                result = (a - b) + "";
                break;
            case '*':
                result = (a * b) + "";
                break;
            case ':':
                result = (a / b) + "";
                break;
            case '%':
                result = (a % b) + "";
                break;
            default:
                doException("Error when we do arithmetic calculating");
        }
        pushIntoToken(result, cInst[3]);
    }

    private void addRandom(){
        Random r = new Random();
        pushIntoToken(r.nextBoolean()?1+"":0+"", cInst[1]);
    }

    private void doTrigonometric() {
        int value = Integer.MIN_VALUE;
        boolean two = false;
        switch (cInst[0]) {
            case "sin":
                value = (int) Math.sin(Double.parseDouble(getValue(cInst[1])));
                break;
            case "cos":
                value = (int) Math.cos(Double.parseDouble(getValue(cInst[1])));
                break;
            case "tan":
                value = (int) Math.tan(Double.parseDouble(getValue(cInst[1])));
                break;
            case "atan":
                value = (int) Math.atan(Double.parseDouble(getValue(cInst[1])));
                break;
            case "atan2":
                value = (int) Math.atan2(Double.parseDouble(getValue(cInst[1])), Double.parseDouble(getValue(cInst[2])));
                two = true;
                break;
            case "pow":
                value = (int) Math.pow(Double.parseDouble(getValue(cInst[1])), Double.parseDouble(getValue(cInst[2])));
                two = true;
                break;
            case "sqrt":
                value = (int) Math.sqrt(Double.parseDouble(getValue(cInst[1])));
                break;
            default:
                doException("Wrong syntax at doing trigonometric");
        }
        if (two) {
            pushIntoToken(value + "", cInst[3]);
        } else {
            pushIntoToken(value + "", cInst[2]);
        }
    }

    private void bitOperation() {
        int a = Integer.parseInt(getValue(cInst[1]));
        int b = Integer.parseInt(getValue(cInst[2]));
        String result = "";
        switch (cInst[0].charAt(0)) {
            case '^': {
                result = (a ^ b) + "";
                break;
            }
            case '>': {
                result = (a >> b) + "";
                break;
            }
            case '<': {
                result = (a << b) + "";
                break;
            }
            case '|': {
                result = (a | b) + "";
                break;
            }
            case '&': {
                result = (a & b) + "";
                break;
            }
            default:
                doException("Something wrong with bit operation, maybe your code is wrong, current token? " + cInst[0].charAt(0));
        }
        pushIntoToken(result, cInst[3]);
    }

    private void convertBinaryToDecimal() {
        String[] a = getValue(cInst[1]).split("");
        int sum = 0;
        for (int i = a.length - 1, j = 0; i >= 0; i--, j++) {
            sum += Integer.parseInt(a[j]) * (1 << i);
        }
        pushIntoToken(sum + "", cInst[2]);
    }

    private void convertDecimalToBinary() {
        StringBuilder result = new StringBuilder();
        for (int i = 31; i >= 0; i--) {
            int k = Integer.parseInt(getValue(cInst[1])) >> i;
            if ((k & 1) > 0)
                result.append("1");
            else
                result.append("0");
        }
        pushIntoToken(result.toString(), cInst[2]);
    }

    private int pop(char token) {
        try {
            switch (token) {
                case '\\':
                    return left.poll();
                case '/':
                    return right.poll();
                case '(':
                    return left.peek();
                case ')':
                    return right.peek();
                default:
                    doException("Error when we pop, or peek from the int stacks, maybe wrong symbol");
            }
        } catch (NullPointerException e) {
            doException("Null pointer exception, because maybe a queue is empty, the error is at getting value from queue");
        }
        return Integer.MIN_VALUE;
    }

    private String getValue(String instruction) {
        switch (instruction.charAt(0)) {
            case ',':
            case '.':
                return stringPop(instruction);
            case '\\':
            case '/':
            case '(':
            case ')':
                return pop(instruction.charAt(0)) + "";
            case '@': {
                switch (instruction.charAt(1)) {
                    case ',':
                    case '.':
                        return stringPop(instruction);
                    case '\\':
                    case '/':
                    case '(':
                    case ')':
                        return pop(instruction.charAt(1)) + "";
                    case '@':
                        return names.get(getValue(instruction.substring(1)));
                    default:
                        return names.get(instruction.substring(1));
                }
            }
            default:
                return instruction;
        }
    }

    private String stringPop(String instruction) {
        switch (instruction.charAt(0)) {
            case ',':
                return stringPool.poll();
            case '.':
                return stringPool.peek();
            default:
                doException("Error when we pop, or peel a string from the string pool, maybe wrong symbol");
        }
        return null;
    }

    private void pushIntoToken(String val, String to) {

        switch (to.charAt(0)) {
            case ']':
                right.add(Integer.parseInt(val));
                break;
            case '[':
                left.add(Integer.parseInt(val));
                break;
            case '\'':
                stringPool.add(val);
                break;
            case '#': {
                names.put(to.substring(1), val);
                break;
            }
            default:
                doException("You are using wrong token for do arithmetic operation");
        }
    }

    private void doException(String msg) {
        try {
            throw new Exception(msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error at " + ip + ". -> " + codes[ip]);
            System.exit(1);
        }
    }

    private void printToConsole(String msg) {
        if (stringFormatter) {
            msg = msg.replaceAll("§", "\n");
        }
        if (preWord) {
            msg = "Output: " + msg;
        }
        System.out.println(msg);
    }
}