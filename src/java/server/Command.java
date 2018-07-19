package server;

import java.util.HashMap;
import java.util.Map;

public final class Command {

    public static final String DELIMITER = " ";

    public enum Input {
        REGISTRATION("reg"),
        LOGIN       ("log"),
        TIMEZONE    ("zone"),
        NEW_MESSAGE ("m"),
        NEW_CONTACT ("add"),
        EXIT        ("exit");

        private final String keyword;
        private static final Map<String, Input> INPUTS;

        Input(String keyword) {
            this.keyword = keyword;
        }

        static {
            INPUTS = new HashMap<>();
            for (Input command : Input.values()) {
                INPUTS.put(command.keyword, command);
            }
        }

        public static Input get(String keyword) {
            return INPUTS.get(keyword);
        }
    }

    public enum Output {
        ALLOWED_LOGIN       ("log"),
        DENIED_REGISTRATION ("!reg"),
        DENIED_LOGIN        ("!log"),
        MESSAGE             ("m"),
        FOUND_USER          ("add"),
        NOT_FOUND_USER      ("!add");

        private final String keyword;

        Output(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public String toString() {
            return this.keyword;
        }
    }
}