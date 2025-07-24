package com.ajaxjs.business.json.parser;

public interface Token {
    Type type();

    enum Type {
        COMMA, COLON, BRACKET_ON, BRACKET_OFF, BRACE_ON, BRACE_OFF, TEXT, NUMBER, BOOLEAN, NULL
    }

    class Comma implements Token {
        @Override
        public Type type() {
            return Type.COMMA;
        }
    }

    class Colon implements Token {
        @Override
        public Type type() {
            return Type.COLON;
        }
    }

    class BracketOn implements Token {
        @Override
        public Type type() {
            return Type.BRACKET_ON;
        }
    }

    class BracketOff implements Token {
        @Override
        public Type type() {
            return Type.BRACKET_OFF;
        }
    }

    class BraceOn implements Token {
        @Override
        public Type type() {
            return Type.BRACE_ON;
        }
    }

    class BraceOff implements Token {
        @Override
        public Type type() {
            return Type.BRACE_OFF;
        }
    }

    class Text implements Token {
        public String value;

        public Text(String value) {
            this.value = value;
        }

        @Override
        public Type type() {
            return Type.TEXT;
        }
    }

    class Num implements Token {
        public double value;

        public Num(double value) {
            this.value = value;
        }

        @Override
        public Type type() {
            return Type.NUMBER;
        }
    }

    class Bool implements Token {
        public boolean value;

        public Bool(boolean value) {
            this.value = value;
        }

        @Override
        public Type type() {
            return Type.BOOLEAN;
        }
    }

    class Null implements Token {
        @Override
        public Type type() {
            return Type.NULL;
        }
    }
}


